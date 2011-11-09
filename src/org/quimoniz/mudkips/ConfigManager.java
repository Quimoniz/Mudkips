package org.quimoniz.mudkips;

import org.bukkit.configuration.file.YamlConfiguration;
import java.io.File;
import java.io.IOException;
import org.bukkit.Server;
import java.util.logging.Level;
import java.io.InputStream;
import java.io.FileOutputStream;

public class ConfigManager {
  private File pluginFolder;
  private YamlConfiguration globalYamlConfig;
  private YamlConfiguration coreYamlConfig;
  private Server server;
  public ConfigManager(File pluginFolder, Server serverObj) {
    this.pluginFolder = pluginFolder;
    this.server = serverObj;
    File globalConfigFile = new File(pluginFolder, "globalConfig.yml");
    File coreConfigFile = new File(pluginFolder, "core.yml");
    boolean globalConfigFileExists = globalConfigFile != null && globalConfigFile.exists() && globalConfigFile.isFile();
    boolean coreConfigFileExists = coreConfigFile != null && coreConfigFile.exists() && coreConfigFile.isFile();
    globalYamlConfig = YamlConfiguration.loadConfiguration(globalConfigFile);
    globalYamlConfig.options().copyDefaults(true);
    globalYamlConfig.setDefaults(YamlConfiguration.loadConfiguration(getStream("configurations/globalConfig.yml")));
    coreYamlConfig = YamlConfiguration.loadConfiguration(coreConfigFile);
    coreYamlConfig.options().copyDefaults(true);
    coreYamlConfig.setDefaults(YamlConfiguration.loadConfiguration(getStream("configurations/core.yml")));
    if(!globalConfigFileExists) {
      flushStreamIntoFile(getStream("configurations/globalConfig.yml"), globalConfigFile);
    }
    if(!coreConfigFileExists) {
      flushStreamIntoFile(getStream("configurations/core.yml"), coreConfigFile);
    }
    
    //DEBUG:
    for(String curKey : globalYamlConfig.getKeys(true)) {
      Object valObj = globalYamlConfig.get(curKey);
      System.out.println("Key: " + curKey + " (" + valObj.getClass().getName() + ")");
      if(valObj instanceof java.util.List) {
        java.util.List objList = (java.util.List)valObj;
        for(Object curObj : objList) {
          System.out.println("  " + curObj + " ("+curObj.getClass().getName()+")");
        }
      } else {
        System.out.println("  value:" + valObj + (valObj!=null?(" ("+valObj.getClass().getName()+")"):""));
      }
    }
    System.out.println("Key \"general.portals-file\":"+globalYamlConfig.get("general.portals-file"));
  }
  private InputStream getStream(String filePath) {
    return getClass().getClassLoader().getResourceAsStream(filePath);
  }
  private void flushStreamIntoFile(InputStream input, File destinationFile) {
    boolean reachedEnd = false;
    final int BUF_SIZE = 1400;
    byte[] byteBuf = new byte[BUF_SIZE];
    int bytesRead = 0;
    FileOutputStream output = null;
    try {
      output = new FileOutputStream(destinationFile);
    } catch(IOException exc) {
      server.getLogger().log(Level.SEVERE, "Could not save " + destinationFile, exc);
      return ;
    }
    while(!reachedEnd) {
      bytesRead = 0;
      try {
        bytesRead = input.read(byteBuf, 0, BUF_SIZE);
      } catch(IOException exc) { }
      if(bytesRead == -1) {
        reachedEnd = true;
      } else if(bytesRead > 0) {
        try {
          output.write(byteBuf, 0, bytesRead);
        } catch(IOException exc) {
          server.getLogger().log(Level.SEVERE, "Could not save " + destinationFile, exc);
          return;
        }
      }
    }
    try {
      input.close();
      output.close();
    } catch(IOException ex) { }
    
  }
}
