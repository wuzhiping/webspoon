package org.pentaho.di.ui.spoon;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.rap.rwt.service.ServiceHandler;

public class DownloadServiceHandler implements ServiceHandler {

  public void service( HttpServletRequest request, HttpServletResponse response )
    throws IOException, ServletException {
    // Which file to download?
    String fileName = request.getParameter( "filename" );
    // Get the file content
    File file = new File( fileName );
    FileInputStream fin = null;
    byte[] download = null;
    try {
      fin = new FileInputStream( file );
      download = new byte[(int) file.length() ];
      fin.read( download );
    } catch ( IOException e ) {
      throw new IOException( e.getMessage() );
    }
    fin.close();
    /*
     *  TODO : if the following line is not executed, the exported xml file remains in the server.
     *  There might be a better way to ensure the file is deleted.
     */
    file.delete();
    // Send the file in the response
    response.setContentType( "application/octet-stream" );
    response.setContentLength( download.length );
    String contentDisposition = "attachment; filename=\"export.xml\"";
    response.setHeader( "Content-Disposition", contentDisposition );
    response.getOutputStream().write( download );
  }
}
