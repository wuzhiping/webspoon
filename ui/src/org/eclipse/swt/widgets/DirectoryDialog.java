/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2017 by Pentaho : http://www.pentaho.com
 * Copyright (C) 2017 by Hitachi America, Ltd., R&D : http://www.hitachi-america.us/rd/
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

package org.eclipse.swt.widgets;

import java.io.File;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.eclipse.swt.SWT;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.vfs.KettleVFS;
import org.pentaho.di.core.vfs.KettleVfsDelegatingResolver;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.ui.core.dialog.ErrorDialog;
import org.pentaho.di.ui.spoon.Spoon;
import org.pentaho.vfs.ui.VfsFileChooserDialog;

public class DirectoryDialog {
  private static Class<?> PKG = Spoon.class;

  int style;
  Shell parent;
  String title;
  String message = "";
  String filterPath = "";

  private VfsFileChooserDialog vfsFileChooserDialog;

  public DirectoryDialog( Shell parent ) {
    this.parent = parent;
    FileObject initialFile;
    FileObject rootFile;
    try {
      initialFile = KettleVFS.getFileObject( Spoon.getInstance().getLastFileOpened() );
      rootFile = initialFile.getFileSystem().getRoot();
    } catch ( Exception e ) {
      String message = Const.getStackTracker( e );
      new ErrorDialog( parent, BaseMessages.getString( PKG, "Spoon.Error" ), message, e );

      return;
    }
    vfsFileChooserDialog = new VfsFileChooserDialog( parent, new KettleVfsDelegatingResolver(), rootFile, initialFile );
  }

  public DirectoryDialog( Shell parent, int style ) {
    this( parent );
    this.style = style;
  }

  public String open() {
    String directoryPath = null;
    // Set fileFilter "[0-9]" so that every file is filtered out and only folders can be selected.
    FileObject returnFile =
        vfsFileChooserDialog.open( parent, null, new String[] { "[0-9]" }, new String[] { "All folders" }, VfsFileChooserDialog.VFS_DIALOG_OPEN_DIRECTORY );
    File file = null;
    if ( returnFile != null ) {
      try {
        file = new File( returnFile.getURL().getPath() );
        Spoon.getInstance().setLastFileOpened( file.getPath() );
      } catch ( FileSystemException e ) {
        e.printStackTrace();
      }
      directoryPath = filterPath = file.getPath();
    }
    return directoryPath;
  }

  public String getFilterPath() {
    return filterPath;
  }

  public void setFilterPath( String string ) {
    filterPath = string;
  }

  public String getMessage() {
    return message;
  }

  public void setMessage( String string ) {
    if ( string == null ) {
      SWT.error( SWT.ERROR_NULL_ARGUMENT );
    }
    message = string;
  }

  public String getText() {
    return title;
  }

  public void setText( String string ) {
    if ( string == null ) {
      SWT.error( SWT.ERROR_NULL_ARGUMENT );
    }
    title = string;
  }

  public Shell getParent() {
    return parent;
  }

  public int getStyle() {
    return style;
  }
}
