package org.quimoniz.mudkips;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Closeable;

public class ErrorHandler implements Closeable {
  private File errorLogFile;
  private FileOutputStream outStream;
  public ErrorHandler(File errorLogFile) {
    this.errorLogFile = errorLogFile;
    try {
      outStream = new FileOutputStream(errorLogFile,true);
    } catch(IOException exc) {
      System.out.println("Exception during opening stream to log errors, how ironic..");
      outStream = null;
    }
  }
  public void logError(Exception exceptionObject) {
	logException(exceptionObject);
  }
  public void logException(Exception exceptionObject) {
	StringBuilder bufException = new StringBuilder((new java.util.Date(System.currentTimeMillis())).toString());
	bufException.append(": ");
	bufException.append(exceptionObject.toString());
	bufException.append("\n");
    for(int i = 0; i < exceptionObject.getStackTrace().length; i++) {
      StackTraceElement traceElement = exceptionObject.getStackTrace()[i];
      bufException.append("\t");
      bufException.append(traceElement);
      bufException.append("\n");
    }
    try {
      outStream.write(bufException.toString().getBytes());
    } catch(IOException exc) {
      System.out.println("Exception during logging exception..");
    }
  }
  @Override public void close() {
    try {
      outStream.close();
    } catch(IOException exc) {
      System.out.println("Exception during closing log for exceptions....");
    }
  }
  public static String stackTrace() {
    StringBuilder buf = new StringBuilder();
    int i = 0;
    for(StackTraceElement element : Thread.currentThread().getStackTrace()) {
      if(i>0) buf.append("\n  ");
      buf.append(element.toString());
      i++;
    }
    return buf.toString();
  }
}
