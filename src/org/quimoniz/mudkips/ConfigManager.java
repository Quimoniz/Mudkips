package org.quimoniz.mudkips;

import org.bukkit.configuration.file.YamlConfiguration;
import java.io.File;
import java.io.IOException;
import org.bukkit.Server;
import java.util.logging.Level;
import java.io.InputStream;
import java.io.FileOutputStream;
import org.bukkit.World;
import org.quimoniz.mudkips.util.ArrayMap;
import org.quimoniz.mudkips.util.StringUtil;
import java.util.List;
import java.util.HashSet;

public class ConfigManager {
  private File pluginFolder;
  private YamlConfiguration globalYamlConfig;
  private YamlConfiguration coreYamlConfig;
  private ArrayMap<String, YamlConfiguration> worldConfigMap;
  private Server server;
  private File configFolder;
  private Mudkips mudkips;
  public ConfigManager(File pluginFolder, Mudkips mudkips, Server serverObj) {
	worldConfigMap = new ArrayMap<String, YamlConfiguration>(4,true);
    this.pluginFolder = pluginFolder;
    this.server = serverObj;
    this.mudkips= mudkips;
    this.configFolder = new File(pluginFolder, "config");
    if(!configFolder.exists()) configFolder.mkdir();
    System.out.println("Config Folder:"+configFolder);
    System.out.println("Config Folder's absolute path:"+configFolder.getAbsolutePath());
    File globalConfigFile = new File(configFolder, "globalConfig.yml");
    File coreConfigFile = new File(configFolder, "core.yml");
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
    //TODO: Register WorldLoadListener
    
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
    System.out.println("Iterating through all Values");
    for(java.util.Map.Entry<String, Object> curEntry : globalYamlConfig.getValues(true).entrySet()) {
      System.out.println(curEntry.getKey() + ":" + curEntry.getValue());
    }
    //DEBUG END
    
  }
  /** Loads all the loaded world's configurations
   **
    */
  public void loadWorldConfigs() {
    List<World> worldList = server.getWorlds();
    for(World curWorld : worldList) {
      this.worldLoaded(curWorld);
    }
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
  public void worldLoaded(World worldLoaded) {
    String worldName = worldLoaded.getName();
    if(!worldConfigMap.containsKey(worldName)) {
      String fileName = StringUtil.escapeFileName(worldName);
      File curConfigFile = new File(configFolder, fileName);
      YamlConfiguration curYamlConfig = YamlConfiguration.loadConfiguration(curConfigFile);
      if(!curConfigFile.exists()) {
        try {
          globalYamlConfig.save(curConfigFile);
        } catch(IOException exc) {
          server.getLogger().log(java.util.logging.Level.WARNING, "IOException while saving config file\""+curConfigFile+"\"", exc);
        }
        curYamlConfig.options().copyDefaults(true);
        curYamlConfig.setDefaults(globalYamlConfig);
        mudkips.listenersManager.onConfigLoad(worldLoaded);
      }
      worldConfigMap.put(worldName, curConfigFile);
    }
  }
  public Object getValue(String key, World worldObj) {
    return getValue(key, worldObj.getName());
  }
  public Object getValue(String key, String worldName) {
    YamlConfiguration worldConfig = worldConfigMap.get(worldName);
    if(worldConfig != null) {
      if(worldConfig.contains(key))
        return worldConfig.get(key);
    }
    return this.globalYamlConfig.get(key);
  }
  public Boolean getBooleanValue(String key, World worldObj, boolean defaultForNull) {
    return getBooleanValue(key, worldObj.getName(), defaultForNull);
  }
  public Boolean getBooleanValue(String key, String worldName, boolean defaultForNull) {
    Object val = getValue(key, worldName);
    if(val == null || !(val instanceof Boolean)) {
      return null;
    } else {
      return (Boolean) val;
    } 
  }
}
