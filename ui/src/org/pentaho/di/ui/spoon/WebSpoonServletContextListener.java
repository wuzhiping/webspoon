/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2016 by Pentaho : http://www.pentaho.com
 * Copyright (C) 2016 by Hitachi America, Ltd., R&D : http://www.hitachi-america.us/rd/
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

package org.pentaho.di.ui.spoon;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import javax.servlet.ServletContextEvent;

import org.eclipse.rap.rwt.engine.RWTServletContextListener;
import org.pentaho.di.core.KettleClientEnvironment;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.exception.KettleException;

public class WebSpoonServletContextListener extends RWTServletContextListener {
  public void contextInitialized( ServletContextEvent event ) {
    System.setProperty( "KETTLE_CONTEXT_PATH", event.getServletContext().getContextPath() );
    /*
     *  The following lines are from Spoon.main
     *  because they are application-wide context.
     */
    ExecutorService executor = Executors.newCachedThreadPool();
    Future<KettleException> pluginRegistryFuture = executor.submit( new Callable<KettleException>() {

      @Override
      public KettleException call() throws Exception {
        Spoon.registerUIPluginObjectTypes();

        KettleClientEnvironment.getInstance().setClient( KettleClientEnvironment.ClientType.SPOON );
        try {
          KettleEnvironment.init( false );
        } catch ( KettleException e ) {
          return e;
        }

        return null;
      }
    } );
    KettleException registryException;
    try {
      registryException = pluginRegistryFuture.get();
      if ( registryException != null ) {
        throw registryException;
      }
      Spoon.initLogging( Spoon.getCommandLineArgs( new ArrayList<String>( Arrays.asList( "" ) ) ) );
    } catch ( Throwable t ) {
      // avoid calls to Messages i18n method getString() in this block
      // We do this to (hopefully) also catch Out of Memory Exceptions
      //
      t.printStackTrace();
    }
    super.contextInitialized( event );
  }
  public void contextDestroyed( ServletContextEvent event ) {
    super.contextDestroyed( event );
    // Kill all remaining things in this VM!
    System.exit( 0 );
  }
}
