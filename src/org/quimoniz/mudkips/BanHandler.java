package org.quimoniz.mudkips;

import java.io.File;
import java.io.IOException;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.logging.Logger;
import java.util.Properties;

public class BanHandler {
  private File propsFile = null;
  private Properties propsData = null;
  private Logger log;
  private Mudkips mudkips;
  private final String COMMENT = "Temp Ban File";
  public BanHandler(File banFile, Logger log, Mudkips mudkips) throws IOException {
    propsFile = banFile;
    this.mudkips = mudkips;
    load();
  }
  private void load() throws IOException {
    propsData = new Properties();
    if(propsFile.exists() && propsFile.isFile()) {
      boolean couldLoad = false;
      FileInputStream stream = null;
      try {
        stream = new FileInputStream(propsFile);
      } catch(IOException exc) { mudkips.errorHandler.logError(exc); }
      if(stream != null) {
        try {
          propsData.load(stream);
          couldLoad = true;
        } catch(IOException exc) { mudkips.errorHandler.logError(exc); }
      }
      if(!couldLoad) {
        throw new IOException("Could not load ban properties file.");
      }
    } else {
      if(propsFile.exists() && !propsFile.isFile()) {
        throw new IOException("The Ban file is not a file!");
      }
    }
  }
  public void save() {
    FileOutputStream stream = null;
    try {
      stream = new FileOutputStream(propsFile);
    } catch(IOException exc) { }
    if(stream != null) {
      try {
        this.propsData.store(stream, COMMENT);
      } catch(IOException exc) { mudkips.errorHandler.logError(exc); }
    }
  }
  public void banPlayer(String playerToBan, long banDuration, String reason) {
    this.propsData.setProperty(playerToBan, (System.currentTimeMillis()+banDuration) + "," + reason);
  }
  public String getBanned(String playerName) {
    String value = this.propsData.getProperty(playerName);
    if(value != null) {
      Long bannedFor = null;
      String reason = null;
      int commaPos = value.indexOf(',');
      if(commaPos > 0) {
        try {
          bannedFor = Long.parseLong(value.substring(0,commaPos)); 
        } catch(NumberFormatException exc) { }
        if((commaPos + 1) < value.length())
          reason = value.substring(commaPos + 1);
      }
      if(bannedFor == null || reason == null) {
        this.propsData.remove(playerName);
        return null;
      } else {
        if(bannedFor < System.currentTimeMillis()) {
          this.propsData.remove(playerName);
          return null;
        } else {
          return reason;
        }
      }
    } else {
      return null;
    }
  }
}
