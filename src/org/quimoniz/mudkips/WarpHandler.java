package org.quimoniz.mudkips;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.logging.Logger;

import org.bukkit.World;
import org.bukkit.Server;
import org.bukkit.Location;



public class WarpHandler {
  private File warpsFile;
  private Properties accessHandler;
  private Logger myLogger;
  private Mudkips mudkips;
  private String COMMENT = "Warps file";
  public WarpHandler(File warpsFile, Logger myLogger, Mudkips mudkips) {
	this.warpsFile = warpsFile;
    this.accessHandler = new Properties();
    this.myLogger = myLogger;
    this.mudkips = mudkips;
    try {
      accessHandler.load(new FileInputStream(warpsFile));
    } catch(IOException exc) {
      mudkips.errorHandler.logError(exc);
      myLogger.severe("Couldn't load " + warpsFile.getParentFile().getParent() + "/" + warpsFile.getParent() + "/" + warpsFile.getName());
    }
  }
  public Location getWarp(String warpName) {
	if(warpName.indexOf((char)92) == 0) {
	  World world = null;
	  if(warpName.length() == 1) {
	    world = mudkips.worldUtil.getDefaultWorld();
	  } else {
	    world = mudkips.worldUtil.attainWorld(warpName.substring(1));
	  }
	  if(world == null) {
	    return null;
	  } else {
	    return world.getSpawnLocation();
	  }
	}
	String rawProperty = accessHandler.getProperty(warpName);
	if(rawProperty != null) {
	  String[] propSplit = rawProperty.split(":");
	  if(propSplit.length >= 2) {
		World worldLoc = mudkips.worldUtil.attainWorld(propSplit[0]);
		if(worldLoc == null) {
          myLogger.warning("Could not load world \""+propSplit[0]+"\" no such Folder and level.dat exist!");
		}
	    String[] valSplits = propSplit[1].split(",");
	    boolean parsingSuccessfull = false;
	    int x = 0, y = 0, z = 0;
	    try {
		  x = Integer.parseInt(valSplits[0]);
		  y = Integer.parseInt(valSplits[1]);
		  z = Integer.parseInt(valSplits[2]);
		  parsingSuccessfull = true;
	    } catch(NumberFormatException exc) {
		  parsingSuccessfull = false;
	    }
	    if(parsingSuccessfull) {
		  return new Location(worldLoc, x, y ,z); 
	    } else {
	      myLogger.warning("Could not return a location for " + warpName + " (\"" + rawProperty +"\")");
	    }
	  }
    }
	return null;
  }
  public void setWarp(String warpName, Location warpLoc) {
	accessHandler.setProperty(warpName.replaceAll(" ", "."), warpLoc.getWorld().getName() + ":" + warpLoc.getBlockX() + "," + warpLoc.getBlockY() + "," + warpLoc.getBlockZ());
  }
  public void save() {
	try {
	  accessHandler.store(new FileOutputStream(warpsFile), COMMENT);
	} catch(IOException exc) {
	  mudkips.errorHandler.logError(exc);
	  myLogger.severe("Could NOT save warps to file.");
	}
  }
}
