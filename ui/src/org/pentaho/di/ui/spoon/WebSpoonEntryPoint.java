/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2016 by Pentaho : http://www.pentaho.com
 * Copyright (C) 2016-2017 by Hitachi America, Ltd., R&D : http://www.hitachi-america.us/rd/
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
import java.util.List;

import org.eclipse.rap.rwt.RWT;
import org.eclipse.rap.rwt.application.AbstractEntryPoint;
import org.eclipse.rap.rwt.client.service.ExitConfirmation;
import org.eclipse.rap.rwt.client.service.StartupParameters;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.MessageBox;
import org.pentaho.di.core.LastUsedFile;
import org.pentaho.di.core.Props;
import org.pentaho.di.core.logging.KettleLogStore;
import org.pentaho.di.ui.core.PropsUI;
import org.springframework.util.CollectionUtils;

public class WebSpoonEntryPoint extends AbstractEntryPoint {

  @Override
  protected void createContents(Composite parent) {
    /*
     *  The following lines were migrated from Spoon.main
     *  because they are session specific.
     */
    PropsUI.init( parent.getDisplay(), Props.TYPE_PROPERTIES_SPOON );
    KettleLogStore
      .init( PropsUI.getInstance().getMaxNrLinesInLog(), PropsUI.getInstance().getMaxLogLineTimeoutMinutes() );

    // Execute Spoon.createContents
    Spoon.getInstance().setCommandLineArgs( Spoon.getCommandLineArgs( new ArrayList<String>( Arrays.asList( "" ) ) ) );
    Spoon.getInstance().setShell( parent.getShell() );
    Spoon.getInstance().createContents( parent );

    /*
     *  The following lines are webSpoon additional functions
     */
    ExitConfirmation serviceConfirm = RWT.getClient().getService( ExitConfirmation.class );
    serviceConfirm.setMessage( "Do you really wanna leave this site?" );

    /* Open a file when appropriate parameters are given */
    StartupParameters serviceParams = RWT.getClient().getService( StartupParameters.class );
    String fileType = serviceParams.getParameter( "fileType" );
    String filename = serviceParams.getParameter( "filename" );
    String directory = serviceParams.getParameter( "directory" );
    Boolean sourceRepository = serviceParams.getParameter( "sourceRepository" ) == null ? null : serviceParams.getParameter( "sourceRepository" ).equalsIgnoreCase( "true" );
    String repositoryName = serviceParams.getParameter( "repositoryName" );
    if ( !CollectionUtils.isEmpty( serviceParams.getParameterNames() ) ) {
      if ( fileType == null | filename == null | directory == null | sourceRepository == null | repositoryName == null ) {
        MessageBox box = new MessageBox( parent.getShell(), SWT.ICON_WARNING | SWT.OK );
        box.setText( "Incorrect parameters" );
        box.setMessage( "Check your URL parameters\n"
          + "Ex1) /?fileType=Trans\n&&filename=Transformation%201\n&&directory=%2Fhome%2Fadmin\n&&sourceRepository=true\n&&repositoryName=pentaho-ee\n"
          + "Ex2) /?fileType=Trans\n&&filename=file%3A%2F%2F%2Fhome%2Fuser%2F%2FTransformation%202.ktr\n&&directory=\n&&sourceRepository=false\n&&repositoryName=" );
        box.open();
      } else {
        List<LastUsedFile> lastUsedFiles = new ArrayList<LastUsedFile>();
        lastUsedFiles.add( new LastUsedFile( fileType, filename, directory, sourceRepository, repositoryName, false, LastUsedFile.OPENED_ITEM_TYPE_MASK_GRAPH ) );
        Spoon.getInstance().props.setLastUsedFiles( lastUsedFiles );
        Spoon.getInstance().lastFileSelect( "0" );
      }
    }
  }

}
