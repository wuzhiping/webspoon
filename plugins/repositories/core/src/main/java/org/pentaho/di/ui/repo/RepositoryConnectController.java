/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2017 by Hitachi Vantara : http://www.pentaho.com
 *
 *******************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 ******************************************************************************/

package org.pentaho.di.ui.repo;

import com.google.common.annotations.VisibleForTesting;
import org.apache.commons.lang.ClassUtils;
import org.eclipse.rap.rwt.SingletonUtil;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.logging.KettleLogStore;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.core.plugins.PluginInterface;
import org.pentaho.di.core.plugins.PluginRegistry;
import org.pentaho.di.core.plugins.RepositoryPluginType;
import org.pentaho.di.core.util.ExecutorUtil;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.repository.AbstractRepository;
import org.pentaho.di.repository.ReconnectableRepository;
import org.pentaho.di.repository.RepositoriesMeta;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.repository.RepositoryMeta;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.ui.core.PropsUI;
import org.pentaho.di.ui.repo.model.RepositoryModel;
import org.pentaho.di.ui.repo.model.UserRepositoriesMeta;
import org.pentaho.di.ui.spoon.Spoon;

import com.google.common.annotations.VisibleForTesting;

import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.function.Supplier;

/**
 * Created by bmorrise on 4/18/16.
 */
public class RepositoryConnectController {

  public static final String DISPLAY_NAME = "displayName";
  public static final String DESCRIPTION = "description";
  public static final String IS_DEFAULT = "isDefault";
  public static final String URL = "url";
  public static final String DATABASE_CONNECTION = "databaseConnection";
  public static final String SHOW_HIDDEN_FOLDERS = "showHiddenFolders";
  public static final String LOCATION = "location";
  public static final String DO_NOT_MODIFY = "doNotModify";

  public static final String DEFAULT_URL = "defaultUrl";
  public static final String ERROR_MESSAGE = "errorMessage";
  public static final String SUCCESS = "success";
  public static final String ERROR_401 = "401";

  private static Class<?> PKG = RepositoryConnectController.class;
  private static LogChannelInterface log =
    KettleLogStore.getLogChannelInterfaceFactory().create( RepositoryConnectController.class );

  private PluginRegistry pluginRegistry;
  private Supplier<Spoon> spoonSupplier;
  private List<RepositoryContollerListener> listeners = new ArrayList<>();
  private boolean relogin = false;

  public RepositoryConnectController( PluginRegistry pluginRegistry, Supplier<Spoon> spoonSupplier ) {
    this.pluginRegistry = pluginRegistry;
    this.spoonSupplier = spoonSupplier;
  }

  public RepositoryConnectController() {
    this( PluginRegistry.getInstance(), Spoon::getInstance );
  }

  @SuppressWarnings( "unchecked" )
  public String getPlugins() {
    List<PluginInterface> plugins = pluginRegistry.getPlugins( RepositoryPluginType.class );
    JSONArray list = new JSONArray();
    for ( PluginInterface pluginInterface : plugins ) {
      if ( !pluginInterface.getIds()[0].equals( "PentahoEnterpriseRepository" ) ) {
        JSONObject repoJSON = new JSONObject();
        repoJSON.put( "id", pluginInterface.getIds()[ 0 ] );
        repoJSON.put( "name", pluginInterface.getName() );
        repoJSON.put( "description", pluginInterface.getDescription() );
        list.add( repoJSON );
      }
    }
    return list.toString();
  }

  public boolean createRepository( String id, Map<String, Object> items ) {
    try {
      RepositoryMeta repositoryMeta = pluginRegistry.loadClass( RepositoryPluginType.class, id, RepositoryMeta.class );
      RepositoriesMeta repositoriesMeta = getRepositoriesMeta();
      repositoryMeta.populate( items, repositoriesMeta );

      if ( repositoryMeta.getName() != null ) {
        Repository repository =
          pluginRegistry.loadClass( RepositoryPluginType.class, repositoryMeta.getId(), Repository.class );
        repository.init( repositoryMeta );

        if ( getCurrentRepository() != null ) {
          if ( isCompatibleRepositoryEdit( repositoryMeta ) ) {
            setConnectedRepository( repositoryMeta );
          }
          repositoriesMeta.removeRepository( repositoriesMeta.indexOfRepository( getCurrentRepository() ) );
        }
        repositoriesMeta.addRepository( repositoryMeta );
        repositoriesMeta.writeData();
        setCurrentRepository( repositoryMeta );
        if ( !testRepository( repository ) ) {
          return false;
        }
        ( (AbstractRepository) repository ).create();
      }
    } catch ( KettleException ke ) {
      log.logError( "Unable to load repository type", ke );
      return false;
    }
    return true;
  }

  private boolean isCompatibleRepositoryEdit( RepositoryMeta repositoryMeta ) {
    RepositoryMeta connectedRepository = getConnectedRepository();
    RepositoryMeta currentRepository = getCurrentRepository();
    if ( getRepositoriesMeta().indexOfRepository( currentRepository ) >= 0
        && connectedRepository != null
        && repositoryEquals( connectedRepository, currentRepository ) ) {
      // only name / description / default changed ?
      RepositoryMeta clone = repositoryMeta.clone();
      clone.setName( connectedRepository.getName() );
      clone.setDescription( connectedRepository.getDescription() );
      clone.setDefault( connectedRepository.isDefault() );
      return repositoryEquals( connectedRepository, clone );
    }
    return false;
  }

  private boolean repositoryEquals( RepositoryMeta repo1, RepositoryMeta repo2 ) {
    return repo1.toJSONObject().equals( repo2.toJSONObject() );
  }

  @SuppressWarnings( "unchecked" )
  public String getRepositories() {
    RepositoriesMeta repositoriesMeta = getRepositoriesMeta();
    JSONArray list = new JSONArray();
    if ( repositoriesMeta != null ) {
      for ( int i = 0; i < repositoriesMeta.nrRepositories(); i++ ) {
        list.add( repositoriesMeta.getRepository( i ).toJSONObject() );
      }
    }
    return list.toString();
  }

  public String getRepository( String name ) {
    RepositoriesMeta repositoriesMeta = getRepositoriesMeta();
    RepositoryMeta repositoryMeta = repositoriesMeta.findRepository( name );
    if ( repositoryMeta != null ) {
      setCurrentRepository( repositoryMeta );
      return repositoryMeta.toJSONObject().toString();
    }
    return "";
  }

  public DatabaseMeta getDatabase( String name ) {
    RepositoriesMeta repositoriesMeta = getRepositoriesMeta();
    return repositoriesMeta.searchDatabase( name );
  }

  public void removeDatabase( String name ) {
    RepositoriesMeta repositoriesMeta = getRepositoriesMeta();
    int index = repositoriesMeta.indexOfDatabase( repositoriesMeta.searchDatabase( name ) );
    if ( index != -1 ) {
      repositoriesMeta.removeDatabase( index );
    }
    save();
  }

  @SuppressWarnings( "unchecked" )
  public String getDatabases() {
    RepositoriesMeta repositoriesMeta = getRepositoriesMeta();
    JSONArray list = new JSONArray();
    for ( int i = 0; i < repositoriesMeta.nrDatabases(); i++ ) {
      JSONObject databaseJSON = new JSONObject();
      databaseJSON.put( "name", repositoriesMeta.getDatabase( i ).getName() );
      list.add( databaseJSON );
    }
    return list.toString();
  }

  public void connectToRepository() throws KettleException {
    connectToRepository( getCurrentRepository() );
  }

  public String connectToRepository( String username, String password ) {
    return connectToRepository( getCurrentRepository(), username, password );
  }

  public void connectToRepository( RepositoryMeta repositoryMeta ) throws KettleException {
    connectToRepository( repositoryMeta, null, null );
  }

  public String connectToRepository( RepositoryMeta repositoryMeta, String username, String password ) {
    JSONObject jsonObject = new JSONObject();
    try {
      final Repository repository = loadRepositoryObject( repositoryMeta.getId() );
      repository.init( repositoryMeta );
      repositoryConnect( repository, username, password );
      if ( username != null ) {
        getPropsUI().setLastRepositoryLogin( username );
      }
      Spoon spoon = spoonSupplier.get();
      Runnable execute = () -> {
        if ( spoon.getRepository() != null ) {
          spoon.closeRepository();
        } else {
          spoon.closeAllJobsAndTransformations( true );
        }
        spoon.setRepository( repository );
        setConnectedRepository( repositoryMeta );
        fireListeners();
      };
      if ( spoon.getShell() != null ) {
        spoon.getShell().getDisplay().asyncExec( execute );
      } else {
        execute.run();
      }
      jsonObject.put( SUCCESS, true );
    } catch ( KettleException ke ) {
      if ( ke.getMessage().contains( ERROR_401 ) ) {
        jsonObject.put( ERROR_MESSAGE, BaseMessages.getString( PKG, "RepositoryConnection.Error.InvalidCredentials" ) );
      } else {
        jsonObject.put( ERROR_MESSAGE, BaseMessages.getString( PKG, "RepositoryConnection.Error.InvalidServer" ) );
      }
      jsonObject.put( SUCCESS, false );
      log.logError( "Unable to connect to repository", ke );
    }
    return jsonObject.toString();
  }

  private Repository loadRepositoryObject( String id ) throws KettleException {
    Repository repository =
      pluginRegistry.loadClass( RepositoryPluginType.class, id, Repository.class );
    if ( repository instanceof ReconnectableRepository ) {
      repository = wrapWithRepositoryTimeoutHandler( (ReconnectableRepository) repository );
    }

    return repository;
  }

  public String reconnectToRepository( String username, String password ) {
    Repository currentRepositoryInstance = getConnectedRepositoryInstance();
    return reconnectToRepository( getCurrentRepository(), (ReconnectableRepository) currentRepositoryInstance, username, password );
  }

  private String reconnectToRepository( RepositoryMeta repositoryMeta, ReconnectableRepository repository,
                                      String username, String password ) {
    JSONObject jsonObject = new JSONObject();
    try {
      if ( username != null ) {
        getPropsUI().setLastRepositoryLogin( username );
      }
      if ( repository.isConnected() ) {
        repository.disconnect();
      }
      repository.init( repositoryMeta );
      repositoryConnect( repository, username, password );
      jsonObject.put( "success", true );
    } catch ( KettleException ke ) {
      if ( ke.getMessage().contains( ERROR_401 ) ) {
        jsonObject.put( ERROR_MESSAGE, BaseMessages.getString( PKG, "RepositoryConnection.Error.InvalidCredentials" ) );
      } else {
        jsonObject.put( ERROR_MESSAGE, BaseMessages.getString( PKG, "RepositoryConnection.Error.InvalidServer" ) );
      }
      jsonObject.put( "success", false );
      log.logError( "Unable to connect to repository", ke );
    }
    return jsonObject.toString();
  }

  private void repositoryConnect( Repository repository, String username, String password ) throws KettleException {
    ExecutorService executorService = ExecutorUtil.getExecutor();
    Future<KettleException> future = executorService.submit( () -> {
      ClassLoader currentClassLoader = Thread.currentThread().getContextClassLoader();
      try {
        Thread.currentThread().setContextClassLoader( Trans.class.getClassLoader() );
        repository.connect( username, password );
      } catch ( KettleException e ) {
        return e;
      } finally {
        Thread.currentThread().setContextClassLoader( currentClassLoader );
      }
      return null;
    } );

    try {
      KettleException exception = future.get();
      if ( exception != null ) {
        throw exception;
      }
    } catch ( InterruptedException | ExecutionException e ) {
      throw new KettleException();
    }
  }

  private boolean testRepository( Repository repository ) {
    ExecutorService executorService = ExecutorUtil.getExecutor();
    Future<Boolean> future = executorService.submit( () -> {
      ClassLoader currentClassLoader = Thread.currentThread().getContextClassLoader();
      try {
        Thread.currentThread().setContextClassLoader( Trans.class.getClassLoader() );
        return ( (AbstractRepository) repository ).test();
      } finally {
        Thread.currentThread().setContextClassLoader( currentClassLoader );
      }
    } );

    try {
      return future.get();
    } catch ( InterruptedException | ExecutionException e ) {
      return false;
    }
  }

  public boolean deleteRepository( String name ) {
    RepositoriesMeta repositoriesMeta = getRepositoriesMeta();
    RepositoryMeta repositoryMeta = repositoriesMeta.findRepository( name );
    int index = repositoriesMeta.indexOfRepository( repositoryMeta );
    if ( index != -1 ) {
      Spoon spoon = spoonSupplier.get();
      if ( spoon.getRepositoryName() != null && spoon.getRepositoryName().equals( repositoryMeta.getName() ) ) {
        spoon.closeRepository();
        setConnectedRepository( null );
      }
      repositoriesMeta.removeRepository( index );
      save();
    }
    return true;
  }

  public void addDatabase( DatabaseMeta databaseMeta ) {
    RepositoriesMeta repositoriesMeta = getRepositoriesMeta();
    if ( databaseMeta != null ) {
      repositoriesMeta.addDatabase( databaseMeta );
      save();
    }
  }

  public boolean setDefaultRepository( String name ) {
    RepositoriesMeta repositoriesMeta = getRepositoriesMeta();
    RepositoryMeta repositoryMeta = repositoriesMeta.findRepository( name );
    for ( int i = 0; i < repositoriesMeta.nrRepositories(); i++ ) {
      repositoriesMeta.getRepository( i ).setDefault( false );
    }
    if ( repositoryMeta != null ) {
      repositoryMeta.setDefault( true );
    }
    try {
      repositoriesMeta.writeData();
    } catch ( KettleException ke ) {
      log.logError( "Unable to set default repository", ke );
    }
    return true;
  }

  public String getDefaultUrl() {
    ResourceBundle resourceBundle = PropertyResourceBundle.getBundle( PKG.getPackage().getName() + ".plugin" );
    return resourceBundle.getString( DEFAULT_URL );
  }

  public String getCurrentUser() {
    return getPropsUI().getLastRepositoryLogin();
  }

  public void setCurrentRepository( RepositoryMeta repositoryMeta ) {
    UserRepositoriesMeta repos = SingletonUtil.getSessionInstance( UserRepositoriesMeta.class );
    repos.setCurrentRepository( repositoryMeta );
  }

  public RepositoryMeta getCurrentRepository() {
    UserRepositoriesMeta repos = SingletonUtil.getSessionInstance( UserRepositoriesMeta.class );
    return repos.getCurrentRepository();
  }

  public RepositoryMeta getConnectedRepository() {
    UserRepositoriesMeta repos = SingletonUtil.getSessionInstance( UserRepositoriesMeta.class );
    return repos.getConnectedRepository();
  }

  public void setConnectedRepository( RepositoryMeta connectedRepository ) {
    UserRepositoriesMeta repos = SingletonUtil.getSessionInstance( UserRepositoriesMeta.class );
    repos.setConnectedRepository( connectedRepository );
  }

  public RepositoryMeta getDefaultRepositoryMeta() {
    RepositoriesMeta repositoriesMeta = getRepositoriesMeta();
    for ( int i = 0; i < repositoriesMeta.nrRepositories(); i++ ) {
      RepositoryMeta repositoryMeta = repositoriesMeta.getRepository( i );
      if ( repositoryMeta.isDefault() ) {
        return repositoryMeta;
      }
    }
    return null;
  }

  public RepositoryMeta getRepositoryMetaByName( String name ) {
    RepositoriesMeta repositoriesMeta = getRepositoriesMeta();
    return repositoriesMeta.findRepository( name );
  }

  public boolean isConnected( String name ) {
    if ( spoonSupplier.get().rep != null ) {
      return spoonSupplier.get().rep.getName().equals( name );
    }
    return false;
  }

  public boolean isConnected() {
    return spoonSupplier.get().rep != null;
  }

  public Repository getConnectedRepositoryInstance() {
    return spoonSupplier.get().rep;
  }

  public void save() {
    RepositoriesMeta repositoriesMeta = getRepositoriesMeta();
    try {
      repositoriesMeta.writeData();
    } catch ( KettleException ke ) {
      log.logError( "Unable to write to repositories", ke );
    }
  }

  @SuppressWarnings( "unchecked" )
  private Repository wrapWithRepositoryTimeoutHandler( ReconnectableRepository repository ) {
    List<Class<?>> repositoryIntrerfaces = ClassUtils.getAllInterfaces( repository.getClass() );
    Class<?>[] repositoryIntrerfacesArray = repositoryIntrerfaces.toArray( new Class<?>[repositoryIntrerfaces.size()] );
    return (Repository) Proxy.newProxyInstance( repository.getClass().getClassLoader(), repositoryIntrerfacesArray,
        new RepositorySessionTimeoutHandler( repository, this ) );
  }

  public PropsUI getPropsUI() {
    return PropsUI.getInstance();
  }

  public void addListener( RepositoryContollerListener listener ) {
    listeners.add( listener );
  }

  public void removeListener( RepositoryContollerListener listener ) {
    listeners.remove( listener );
  }

  public void fireListeners() {
    for ( RepositoryContollerListener listener : listeners ) {
      listener.update();
    }
  }

  public interface RepositoryContollerListener {
    void update();
  }

  public boolean isRelogin() {
    return relogin;
  }

  public void setRelogin( boolean relogin ) {
    this.relogin = relogin;
  }

  public Map<String, Object> modelToMap( RepositoryModel model ) {
    Map<String, Object> properties = new HashMap<>();
    properties.put( DISPLAY_NAME, model.getDisplayName() );
    properties.put( DESCRIPTION, model.getDescription() );
    properties.put( IS_DEFAULT, model.getIsDefault() );
    properties.put( URL, model.getUrl() );
    properties.put( DATABASE_CONNECTION, model.getDatabaseConnection() );
    properties.put( SHOW_HIDDEN_FOLDERS, model.getShowHiddenFolders() );
    properties.put( LOCATION, model.getLocation() );
    properties.put( DO_NOT_MODIFY, model.getDoNotModify() );

    return properties;
  }

  @VisibleForTesting
  boolean isDatabaseWithNameExist( DatabaseMeta databaseMeta, boolean isNew ) {
    RepositoriesMeta repositoriesMeta = getRepositoriesMeta();
    for ( int i = 0; i < repositoriesMeta.nrDatabases(); i++ ) {
      final DatabaseMeta iterDatabase = repositoriesMeta.getDatabase( i );
      if ( iterDatabase.getName().trim().equalsIgnoreCase( databaseMeta.getName().trim() ) ) {
        if ( isNew || databaseMeta != iterDatabase ) { // do not check the same instance
          return true;
        }
      }
    }
    return false;
  }

  @VisibleForTesting
  RepositoriesMeta getRepositoriesMeta() {
    UserRepositoriesMeta repos = SingletonUtil.getSessionInstance( UserRepositoriesMeta.class );
    return repos.getRepositoriesMeta();
  }
}
