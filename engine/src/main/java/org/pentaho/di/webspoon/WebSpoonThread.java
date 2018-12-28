/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2018 by Hitachi America, Ltd., R&D : http://www.hitachi-america.us/rd/
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

package org.pentaho.di.webspoon;

import org.eclipse.rap.rwt.RWT;
import org.eclipse.rap.rwt.service.UISession;

public class WebSpoonThread extends Thread implements WebSpoonRunnable {

  final private UISession uiSession;

  public WebSpoonThread() {
    this.uiSession = getUISession();
  }

  public static UISession getUISession() {
    UISession uiSession;
    try {
      uiSession = RWT.getUISession();
    } catch ( Exception e ) {
      uiSession = null;
    }
    return uiSession;
  }

  @Override
  public void run() {
    if ( uiSession == null ) {
      runInternal();
    } else {
      uiSession.exec( () -> {
        runInternal();
      });
    }
  }

  @Override
  public void runInternal() {
    // TODO Auto-generated method stub

  }

}
