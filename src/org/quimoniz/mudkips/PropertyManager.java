package org.quimoniz.mudkips;

import org.bukkit.Server;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.Properties;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.logging.Logger;

public class PropertyManager {
  private Properties myProps;
  private Server serverHandle;
  private HashMap<String, String> usedProperties = new HashMap<String, String>();
  private String pathToProps;
  private Logger logHandle;
  public PropertyManager(String pathToProps, Server serverHandle) {
	this.serverHandle = serverHandle;
	this.pathToProps = pathToProps;
	logHandle = this.serverHandle.getLogger();
    try {
  	  myProps = new Properties();
   	  myProps.load(new FileInputStream(pathToProps));
    } catch(IOException exc) {
   	  serverHandle.getLogger().log(Level.SEVERE, "Mudkips couldn't load Properties file to be located at \"" + pathToProps + "\".");
    }
  }
  public boolean getBooleanProperty(String key, boolean defaultValue) {
    String val = getProperty(key, defaultValue?"true":"false").toLowerCase();
    if(val.equals("on") || val.equals("true") || val.equals("1") || val.equals("active") || val.equals("activated") || val.equals("yes") || val.equals("y")) {
      return true;
    } else if(val.equals("off") || val.equals("false") || val.equals("0") || val.equals("unactive") || val.equals("inactive") || val.equals("deactivated") || val.equals("no") || val.equals("n")) {
      return false;
	} else {
	  logHandle.log(Level.WARNING,"Couldn't parse property \""+key+"\" in " + pathToProps + " to boolean.");
	  return defaultValue;
	}
  }
  public byte getByteProperty(String key, byte defaultValue) {
    byte parsedByte = defaultValue;
    try {
      parsedByte = Byte.parseByte(getProperty(key, ""+defaultValue));
    } catch(NumberFormatException exc) {
  	  logHandle.log(Level.WARNING,"Couldn't parse property \""+key+"\" in " + pathToProps + " to byte.");
    }
    return parsedByte;
  }
  public short getShortProperty(String key, short defaultValue) {
    short parsedShort = defaultValue;
    try {
      parsedShort = Short.parseShort(getProperty(key, ""+defaultValue));
    } catch(NumberFormatException exc) {
  	  logHandle.log(Level.WARNING,"Couldn't parse property \""+key+"\" in " + pathToProps + " to short.");
    }
    return parsedShort;
  }
  public int getIntProperty(String key, int defaultValue) {
    int parsedInt = defaultValue;
    try {
      parsedInt = Integer.parseInt(getProperty(key, ""+defaultValue));
    } catch(NumberFormatException exc) {
  	  logHandle.log(Level.WARNING,"Couldn't parse property \""+key+"\" in " + pathToProps + " to int.");
    }
    return parsedInt;
  }
  public long getLongProperty(String key, long defaultValue) {
    long parsedLong = defaultValue;
    try {
      parsedLong = Long.parseLong(getProperty(key, ""+defaultValue));
    } catch(NumberFormatException exc) {
  	  logHandle.log(Level.WARNING,"Couldn't parse property \""+key+"\" in " + pathToProps + " to long.");
    }
    return parsedLong;
  }
  public float getFloatProperty(String key, float defaultValue) {
    float parsedFloat = defaultValue;
    try {
      parsedFloat = Float.parseFloat(getProperty(key, ""+defaultValue));
    } catch(NumberFormatException exc) {
  	  logHandle.log(Level.WARNING,"Couldn't parse property \""+key+"\" in " + pathToProps + " to float.");
    }
    return parsedFloat;
  }
  public double getDoubleProperty(String key, double defaultValue) {
    double parsedDouble = defaultValue;
    try {
      parsedDouble = Double.parseDouble(getProperty(key, ""+defaultValue));
    } catch(NumberFormatException exc) {
  	  logHandle.log(Level.WARNING,"Couldn't parse property \""+key+"\" in " + pathToProps + " to double.");
    }
    return parsedDouble;
  }
  public String getStringProperty(String key, String defaultValue) {
	  return getProperty(key, defaultValue);
  }
  public String getProperty(String key, String defaultValue) {
	String readValue = myProps.getProperty(key,defaultValue);
    usedProperties.put(key, readValue);
    return readValue;
  }
  public void save() {
    for(Entry<String,String> e : usedProperties.entrySet()) {
      myProps.setProperty(e.getKey(), e.getValue());
    }
	FileOutputStream outStream = null;
	try {
      outStream = new FileOutputStream(pathToProps,false);
	} catch(IOException exc) {
	  logHandle.log(Level.WARNING,"Failed to create temporary Properties file for saving Properties (" + pathToProps + "): " + exc.getMessage());
	  return;
	}
	if(outStream != null) {
	  try {
	    myProps.store(outStream, "");
	  } catch(IOException exc) {
	    logHandle.log(Level.WARNING,"Failed to write properties to Properties File (" + pathToProps + "): " + exc.getMessage());
	    return;
	  }
	}
	try {
	  outStream.close();
	} catch(IOException exc) {
      logHandle.log(Level.WARNING,"Failed to close FileOutputStream (" + pathToProps + "): " + exc.getMessage());
	  return;
	}
  }
}