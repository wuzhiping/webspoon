/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2016-2018 by Hitachi America, Ltd., R&D : http://www.hitachi-america.us/rd/
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

import org.apache.commons.lang.StringEscapeUtils;
import org.eclipse.rap.rwt.RWT;
import org.eclipse.rap.rwt.client.service.JavaScriptExecutor;
import org.eclipse.rap.rwt.widgets.WidgetUtil;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Widget;

public class WebSpoonUtils {
  public static void setTestId( Widget widget, String value ) {
    if ( !widget.isDisposed() ) {
      String $el = widget instanceof Text ? "$input" : "$el";
      String id = WidgetUtil.getId( widget );
      value = StringEscapeUtils.escapeJavaScript( value );
      exec( "rap.getObject( '", id, "' ).", $el, ".attr( 'test-id', '", value + "' );" );
    }
  }

  private static void exec( String... strings ) {
    StringBuilder builder = new StringBuilder();
    builder.append( "try{" );
    for ( String str : strings ) {
      builder.append( str );
    }
    builder.append( "}catch(e){}" );
    JavaScriptExecutor executor = RWT.getClient().getService( JavaScriptExecutor.class );
    executor.execute( builder.toString() );
  }
}
