package org.quimoniz.mudkips.player;


import org.bukkit.entity.Player;
import org.bukkit.ChatColor;
import org.bukkit.Server;
import org.bukkit.Location;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import org.quimoniz.mudkips.Mudkips;
import org.quimoniz.mudkips.util.StringUtil;
import org.quimoniz.mudkips.util.WorldUtil;
import org.quimoniz.wigglytuff.EntityReadListener;
import org.quimoniz.wigglytuff.Entity;
import org.quimoniz.wigglytuff.ReaderStreamParser;
import org.bukkit.World;
import java.io.Closeable;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.util.Vector;
import org.bukkit.inventory.ItemStack;
import org.bukkit.event.player.PlayerRespawnEvent;

public class MudkipsPlayer implements Closeable{
  private String name = null;
  private Player pObject;
  private Server serverObject;
  private String alias;
  private long wasSmitted = 0;
  private String smitter = null;
  private boolean afk;
  private boolean closed = false;
  private String afkMessage;
  private long lastAfkToggle = 0;
  private long initRealTime = 0;
  private long initTime = 0;
  private final String DEFAULT_AFK_MSG = "is afk";
  private Mudkips mudkipsMain;
  private String chatPartner = null;
  private Location homeBed = null; 
  private String alterName = null;
  private String lastWorld = null;
  private boolean slappyOn = false;
  private boolean pvpEnabled = true;
  private long jailedFor = -1;
  private boolean jailed = false;
  private long lastAttack = 0;
  private long lastKick = 0;
  private long lastBan = 0;
  private long lastDeath = 0;
  private int timesDied = 0;
  private ItemStack[] inventory = null;
  public MudkipsPlayer(Player p, Mudkips mudkipsMain, Server s) {
    name = p.getName();
    pObject = p;
    serverObject = s;
    this.mudkipsMain = mudkipsMain;
    initRealTime = System.currentTimeMillis();
    initTime = p.getWorld().getFullTime();
    pvpEnabled = mudkipsMain.myProps.getBooleanProperty("pvp-enabled", true);
    buildAlias();
    p.setDisplayName(alias);
    try {
      load();
    } catch(Exception exc) {
      mudkipsMain.errorHandler.logException(exc);
      s.getLogger().warning("Mudkips: Could not load player data of Player \""+name+"\"");
    }
  }
  public void sendPlayerListUpdate() {
    pObject.setPlayerListName(displayName());
  }
  public void reconnect() {
    if(jailedFor > 0) {
      jail(jailedFor-System.currentTimeMillis());
    }
  }
  public Player getPlayer() {
	if(pObject != null)
	  return pObject;
	Player pReturn = null;
	pReturn =  serverObject.getPlayer(name);
	if(pReturn == null)
	  pReturn = pObject;
	return pReturn;
  }
  public String getAlias() {
	return alias;
  }
  public void buildAlias() {
	char c=0,lastC=0;
	int posSplit = -1;
	for(int i = 0; i < name.length(); i++) {
	  c = name.charAt(i);
	  if(i > 0)
		if((c >= 65 && c <= 90) && (lastC >= 97 && lastC <= 122)) { //switch from lowercase to uppercase
		  posSplit = i;
		  break;
		} else if((c >= 48 && c <= 57) && (lastC >=48 && lastC<=57)) {//two numbers in a row
		  posSplit = i - 1;
		  break;
		} else if(c == '.' || c == '_') {
		  posSplit = i;
		  break;
		}
	  lastC = c;
	}
	if(posSplit < 2)
	  alias = name;
	else
	  alias = name.substring(0,posSplit);
  }
 public long toggleAfk() {
   return toggleAfk(null);
 }
 public long toggleAfk(String message) {
   long currentTime = System.currentTimeMillis();
   long lastAfkAction = lastAfkToggle;
   if(afk) {
	 afk = false;
	 afkMessage = null;
     lastAfkToggle = currentTime;
   } else {
	 afk = true;
	 if(message != null)
	   afkMessage = message;
	 else
	   afkMessage = DEFAULT_AFK_MSG;
	 lastAfkToggle = currentTime;
   }
   return currentTime - lastAfkAction;
 }
 public void setPrivateChatPartner(String chatPartner) {
   this.chatPartner = chatPartner;
 }
 public String getPrivateChatPartner() {
   return chatPartner;
 }
 public long getInitTime() {
   return initTime;
 }
 public long getInitRealTime() {
   return initRealTime;
 }
 public boolean isAfk() {
   return afk;
 }
 public String afkMessage() {//maybe implement set Method for changing afk message while being afk?
   return afkMessage;
 }
 public String getName() {
   return name;
 }
 public String toString() {
   return "MudkipsPlayer \"" + name + "\"";
 }
 public boolean inPrivateChat() {
   if(chatPartner == null)
	 return false;
   else
	 return true;
 }
 public String displayName() {
   if(alterName != null)
	 return alterName;
   else {
     if(this.pObject.getDisplayName().equals(this.pObject.getName()))
       return alias;
      else
       return pObject.getDisplayName();
   }
 }
 public void setAlterName(String alterName) {
   this.alterName = alterName;
   pObject.setDisplayName(alterName);
   pObject.setPlayerListName(alterName);
 }
 public String getAlterName() {
   return alterName;
 }
 public Location getLocation() {
   return pObject.getLocation();
 }
 public void sendMessage(String message) {
  if(!updateBinding())
    return;
  if(message != null) {
    for(String curLine : message.split("\n"))
      pObject.sendMessage(curLine);
  }
 }
 public void setHomeBed(Location homeBed) {
   this.homeBed = homeBed;
 }
 public Location getHomeBed() {
   return homeBed;
 }
 public boolean isOnline() {
   if(closed) return false;
   Player pLookup = serverObject.getPlayer(name);
   if(pLookup == null || !pLookup.isOnline()) {
     return false;
   } else {
     return true;
   }
 }
 public String dataLine() {
   StringBuilder buf = new StringBuilder(55);
   buf.append("Player {");
   boolean valueBefore = false;
   if(homeBed != null) {
     buf.append("\"HomeBed=");
     buf.append("" + (int)homeBed.getX());
     buf.append(",");
     buf.append("" + (int)homeBed.getY());
     buf.append(",");
     buf.append("" + (int)homeBed.getZ());
     buf.append(",");
     buf.append(ReaderStreamParser.escapeCharacters(homeBed.getWorld().getName()));
     buf.append("\"");
     valueBefore = true;
   }
   if(alterName != null) {
     if(valueBefore)
       buf.append(",");
     buf.append("\"AlterName=");
     buf.append(ReaderStreamParser.escapeCharacters(alterName));
     buf.append("\"");
     valueBefore = true;
   }
   if(slappyOn) {
     if(valueBefore)
       buf.append(",");
     buf.append("\"slappy=true\"");
   }
   if(valueBefore)
     buf.append(",");
   if(lastWorld != null) {
     buf.append("\"lastWorld=");
     buf.append(lastWorld);
     buf.append("\",");
   }
   if(jailed) {
     buf.append("\"jailed=on\",");
   }
   buf.append("\"pvp=");
   buf.append(pvpEnabled?"true":"false");
   buf.append("\",\"lastAttack=");
   buf.append(lastAttack);
   buf.append("\",\"lastKick=");
   buf.append(lastKick);
   buf.append("\",\"lastBan=");
   buf.append(lastBan);
   buf.append("\",");
   buf.append("\"jailedFor=");
   buf.append(jailedFor);
   buf.append("\",");
   buf.append("\"lastDeath=");
   buf.append(lastDeath);
   buf.append("\",");
   buf.append("\"timesDied=");
   buf.append(timesDied);
   buf.append("\"");
   if(inventory != null) {
     buf.append(",\"inventory=");
     for(int i = 0; i < inventory.length; i++) {
       if(inventory[i] != null) {
         buf.append(inventory[i].getTypeId());
         buf.append(":");
         buf.append(inventory[i].getAmount());
         buf.append(":");
         buf.append(Short.toString(inventory[i].getDurability()));
         buf.append(":");
         buf.append(Byte.toString(inventory[i].getData().getData()));
       } else {
         buf.append("null");
       }
       buf.append(",");
     }
     buf.append("\"");
   }
   buf.append("}");
   return buf.toString();
 }
  public void load() {
    File playerFolder = new File(this.mudkipsMain.getDataFolder(), "players");
    if(!playerFolder.exists()) {
      return;
    }
    String escapedFileName = escapeFileName(name) + ".dat";
    File playerData = new File(playerFolder, escapedFileName);
    final MudkipsPlayer selfReference = this;
    if(playerData.exists()) {
      FileInputStream dataStream = null;
      try {
        dataStream = new FileInputStream(playerData);
      } catch(IOException exc) {
        mudkipsMain.errorHandler.logError(exc);
        System.out.println("Failed to open an input stream on file \"" + playerData + "\"");
      }
      if(dataStream != null) {
        ReaderStreamParser parser = new ReaderStreamParser(dataStream);
        parser.addListener("Player", new EntityReadListener() {
          public void entityFinished(Entity e) {
            for(Entity currentEntity : e.getBracerParameters()) {
              int equalsPos = currentEntity.name.indexOf('=');
              if(equalsPos >= 0) {
                String parameterName = currentEntity.name.substring(0, equalsPos);
                String parameterValue = null;
                if((equalsPos + 1) < currentEntity.name.length()) {
                  parameterValue = currentEntity.name.substring(equalsPos + 1);
                } else {
                  continue;
                }
                if(parameterName.equalsIgnoreCase("HomeBed")) {
                  int x = 0,y = 0,z = 0;
                  int commaIndex = 0;
                  boolean parsingLocationSuccessfull = true;
                  StringBuilder curVal = new StringBuilder(10);
                  World bedWorld = null;
                  for(char curChar : parameterValue.toCharArray()) {
                    if(curChar != ',') {
                      curVal.append(curChar);
                    } else {
                      if(commaIndex == 0) {
                        try {
                          x = Integer.parseInt(curVal.toString());
                        } catch(NumberFormatException exc) {
                          parsingLocationSuccessfull = false;
                          break;
                        }
                      } else if(commaIndex == 1) {
                        try {
                          y = Integer.parseInt(curVal.toString());
                        } catch(NumberFormatException exc) {
                          parsingLocationSuccessfull = false;
                          break;
                        }
                      } else if(commaIndex == 2) {
                        try {
                          z = Integer.parseInt(curVal.toString());
                        } catch(NumberFormatException exc) {
                          parsingLocationSuccessfull = false;
                          break;
                        }
                      } else if(commaIndex == 3) {
                        bedWorld = serverObject.getWorld(curVal.toString());
                        if(bedWorld == null) {
                          parsingLocationSuccessfull = false;
                        }
                        break;
                      }
                      commaIndex++;
                      curVal = new StringBuilder(10);
                    }
                  }
                  if(curVal != null && curVal.length() > 0) {
                    bedWorld = serverObject.getWorld(curVal.toString());
                    if(bedWorld == null) {
                      parsingLocationSuccessfull = false;
                    }
                  }
                  if(parsingLocationSuccessfull) {
                    Location locBed = new Location(bedWorld, x, y, z);
                    selfReference.setHomeBed(locBed);
                  }
                } else if(parameterName.equalsIgnoreCase("AlterName")) {
                  selfReference.setAlterName(parameterValue);
                } else if(parameterName.equalsIgnoreCase("slappy")) {
                  selfReference.slappyOn = true;
                } else if(parameterName.equalsIgnoreCase("slappy")) {
                  selfReference.jailed = true;
                } else if(parameterName.equalsIgnoreCase("lastWorld")) {
                  lastWorld = parameterValue;
                } else if(parameterName.equalsIgnoreCase("pvp")) {
                  if(parameterValue.equalsIgnoreCase("true") || parameterValue.equalsIgnoreCase("on") || parameterValue.equalsIgnoreCase("yes") || parameterValue.equalsIgnoreCase("1") || parameterValue.equalsIgnoreCase("enabled")) {
                    pvpEnabled = true;
                  } else {
                    pvpEnabled = false;
                  }
                } else if(parameterName.equalsIgnoreCase("lastAttack")) {
                  Long parsedLastAttack = null;
                  try {
                    parsedLastAttack = Long.parseLong(parameterValue);
                  } catch(NumberFormatException exc) { }
                  if(parsedLastAttack != null)
                    selfReference.lastAttack = parsedLastAttack.longValue();
                } else if(parameterName.equalsIgnoreCase("lastKick")) {
                  Long parsedLastKick = null;
                  try {
                    parsedLastKick = Long.parseLong(parameterValue);
                  } catch(NumberFormatException exc) { }
                  if(parsedLastKick != null)
                    selfReference.lastKick = parsedLastKick.longValue();
                } else if(parameterName.equalsIgnoreCase("lastBan")) {
                  Long parsedLastBan = null;
                  try {
                    parsedLastBan = Long.parseLong(parameterValue);
                  } catch(NumberFormatException exc) { }
                  if(parsedLastBan != null)
                    selfReference.lastBan = parsedLastBan.longValue();
                } else if(parameterName.equalsIgnoreCase("jailed")) {
                  if(parameterValue.equalsIgnoreCase("true") || parameterValue.equalsIgnoreCase("on") || parameterValue.equalsIgnoreCase("yes") || parameterValue.equalsIgnoreCase("1") || parameterValue.equalsIgnoreCase("enabled")) {
                    jailed = true;
                  } else {
                    jailed = false;
                  }
                } else if(parameterName.equalsIgnoreCase("jailedFor")) {
                  Long parsedJailedFor = null;
                  try {
                    parsedJailedFor = Long.parseLong(parameterValue);
                  } catch(NumberFormatException exc) { }
                  if(parsedJailedFor != null)
                    selfReference.jailedFor = parsedJailedFor.longValue();
                } else if(parameterName.equalsIgnoreCase("inventory")) {
                  int commaPos = 0, lastCommaPos = 0;
                  int i = 0;
                  ItemStack[] inventory = new ItemStack[40];
                  while(commaPos < parameterValue.length() && (commaPos = parameterValue.indexOf(',',commaPos)) > 0) {
                    String item = parameterValue.substring(lastCommaPos, commaPos);
                    if("null".equalsIgnoreCase(item)) {
                      inventory[i++] = null;
                    } else {
                      Integer itemId = null, amount = null;
                      Byte dataByte = null;
                      Short durability = null;
                      int colonPos = item.indexOf(':'), lastColonPos = 0;
                      if(colonPos >= 0) {
                        try {
                          itemId = new Integer(item.substring(0, colonPos));
                        } catch(NumberFormatException exc) { }
                        lastColonPos = colonPos;
                        colonPos = item.indexOf(':',lastColonPos + 1);
                        if(colonPos >= 0) {
                          try {
                            amount = new Integer(item.substring(lastColonPos +1, colonPos));
                          } catch(NumberFormatException exc) { }
                          lastColonPos = colonPos;
                          colonPos = item.indexOf(':',lastColonPos + 1);
                          if(colonPos >= 0) {
                            try {
                              durability = new Short(item.substring(lastColonPos + 1, colonPos));
                            } catch(NumberFormatException exc) { }
                            lastColonPos = colonPos;
                            colonPos = item.indexOf(':',lastColonPos + 1);
                            try {
                              dataByte = new Byte(item.substring(lastColonPos + 1));
                            } catch(NumberFormatException exc) { }
                            if(itemId != null) {
                              if(amount != null) {
                                if(dataByte != null) {
                                  if(durability != null) {
                                    inventory[i++] = new ItemStack(itemId, amount, durability, dataByte);
                                  } else {
                                    inventory[i] = new ItemStack(itemId, amount);
                                    inventory[i++].setData(new org.bukkit.material.MaterialData(itemId, dataByte));
                                  }
                                } else {
                                  if(durability != null) {
                                    inventory[i++] = new ItemStack(itemId, amount, durability);
                                  } else {
                                    inventory[i++] = new ItemStack(itemId, amount);
                                  }
                                }
                              } else {
                                inventory[i++] = null;
                              }
                            } else {
                              inventory[i++] = null;
                            }
                          } else {
                            if(itemId != null && amount!= null) {
                              inventory[i++] = new ItemStack(itemId, amount);
                            } else {
                              inventory[i++] = null;
                            }
                          }
                        } else {
                          inventory[i++] = null;
                        }
                      }
                    }
                    lastCommaPos = ++commaPos;
                  }
                  selfReference.inventory = inventory;
                } else if(parameterName.equalsIgnoreCase("lastDeath")) {
                  Long parsedLastDeath = null;
                  try {
                    parsedLastDeath = Long.parseLong(parameterValue);
                  } catch(NumberFormatException exc) { }
                  if(parsedLastDeath != null)
                    selfReference.lastDeath = parsedLastDeath.longValue();
                } else if(parameterName.equalsIgnoreCase("timesDied")) {
                  Integer parsedTimesDied = null;
                  try {
                    parsedTimesDied = Integer.parseInt(parameterValue);
                  } catch(NumberFormatException exc) { }
                  if(parsedTimesDied != null)
                    selfReference.timesDied = parsedTimesDied.intValue();
                }
              }
            }
          }
          }
        );
        parser.addFinalizerMethod(new Runnable() { public void run() {
                    if(selfReference.name != selfReference.displayName()) {
                      selfReference.sendPlayerListUpdate();
                    }
                    Location reconnectLocation = null;
                    if(selfReference.lastWorld != null) {
                      World reconnectWorld = selfReference.mudkipsMain.worldUtil.attainWorld(selfReference.lastWorld);
                      if(reconnectWorld != null) {
                        reconnectLocation = selfReference.pObject.getLocation();
                        reconnectLocation.setWorld(reconnectWorld);
                      }
                    }
                    if(selfReference.jailed) {
                      selfReference.jail(selfReference.jailedFor-System.currentTimeMillis());
                    } else if(reconnectLocation != null){
                      selfReference.teleport(reconnectLocation);
                    }
        }});
        parser.start();
      }
    } else {
      return;
    }
  }
  public static String escapeFileName(String fileName) {
    StringBuilder newFileName = new StringBuilder(fileName.length()*3/2);
    for(int i = 0; i < fileName.length(); i++) {
      char curChar = fileName.charAt(i);
      if(curChar== 45 || curChar==46 || curChar > 47 && curChar < 58 || curChar > 64 && curChar < 91 || curChar == 95 || curChar > 96 || curChar < 123) {
        newFileName.append(curChar);
      } else {
        newFileName.append("%");
        String hexString = Integer.toHexString(curChar);
        switch(hexString.length()) {
          case 0:
            newFileName.append("00");
            break;
          case 1:
            newFileName.append("0");
            break;
        }
        newFileName.append(hexString);
      }
    }
    return newFileName.toString();
  }
  public static String unescapeFileName(String fileName) {
    StringBuilder unescapedFileName = new StringBuilder();
    for(int i=0; i<fileName.length(); i++) {
      char curChar = fileName.charAt(i);
      if(curChar != '%' || (i+2)>=fileName.length()) {
        unescapedFileName.append(curChar);
      } else {
        String hex = fileName.substring(i,i+2);
        int parsedNum = -1;
        try {
          parsedNum = Integer.parseInt(hex,16);
        } catch(NumberFormatException exc) { parsedNum = -1; }
        if(parsedNum >= 0 && parsedNum < 256) {
          unescapedFileName.append((char)parsedNum);
        }
        i+=2;
      }
    }
    return unescapedFileName.toString();
  }
  public void save() {
    if(mudkipsMain == null || name == null) { return; }
    File playerFolder = new File(this.mudkipsMain.getDataFolder(), "players");
    if(!playerFolder.exists()) {
      if(playerFolder.mkdir()) {
        System.out.println("Failed to create directory \"" + playerFolder + "\".");
      }
    }
    String escapedFileName = escapeFileName(name) + ".dat";
    File playerData = new File(playerFolder, escapedFileName);
    FileOutputStream dataStream = null;
    try {
      dataStream = new FileOutputStream(playerData);
    } catch(IOException exc) {
      mudkipsMain.errorHandler.logError(exc);
      System.out.println("Failed to open an output stream on file \"" + playerData + "\"");
    }
    if(dataStream != null) {
      try {
        dataStream.write(this.dataLine().getBytes());
        dataStream.flush();
        dataStream.close();
      } catch(IOException exc) {
        mudkipsMain.errorHandler.logError(exc);
        System.out.println("Failed to write to file \"" + playerData + "\"");
      }
    }
  }
  public void close() {
    closed = true;
    save();
    name = null;
    pObject = null;
    serverObject = null;
    alias = null;
    afkMessage = null;
    mudkipsMain = null;
    chatPartner = null;
    homeBed = null; 
    alterName = null;
  }
  public boolean isClosed() {
    return closed;
  }
  public boolean updateBinding() {
    if(this.pObject == null) {
      this.pObject = serverObject.getPlayer(this.name);
      if(this.pObject == null) {
        this.mudkipsMain.playerProvider.playerQuit(this.name);
        return false;
      }
    }
    if(!this.isOnline()) {
      return false;
    }
    return true;
  }
  public boolean isEquipped() {
    for(org.bukkit.inventory.ItemStack curStack : pObject.getInventory().getContents()) {
      if(curStack != null) { 
        if(curStack.getAmount()> 0) {
          return true;
        }
      }
    }
    return false;
  }
  public boolean armorEquipped() {
    for(org.bukkit.inventory.ItemStack curStack : pObject.getInventory().getArmorContents()) {
      if(curStack != null) { 
        if(curStack.getAmount()> 0) {
          return true;
        }
      }
    }
    return false;
  }
  public void teleport(Location loc) {
    if(loc == null) {
      this.mudkipsMain.getServer().getLogger().warning("Can not teleport to null.");
      return;
    }
    if(loc.getWorld() == null) {
      this.mudkipsMain.getServer().getLogger().warning("Can not teleport to a World which is null.");
      return;
    }
    if(!updateBinding())
      return;
    Chunk chunk = loc.getWorld().getChunkAt(loc);
    World world= loc.getWorld();
    if(world.isChunkLoaded(loc.getBlockX(), loc.getBlockZ())) {
      chunk = world.getChunkAt(loc);
    } else {
      world.loadChunk(loc.getBlockX(), loc.getBlockZ());
      chunk = world.getChunkAt(loc);
    }
    boolean previousWasAir = false,currentIsAir = false;
    int y = loc.getBlockY()-1;
    while(!previousWasAir || !currentIsAir) {
      previousWasAir = currentIsAir;
      if(world.getBlockTypeIdAt(loc.getBlockX(), y, loc.getBlockZ()) == Material.AIR.getId()) {
        currentIsAir = true;
      } else {
        currentIsAir = false;
      }
      y++;
    }
    final Location blockChangeLocation = new Location(loc.getWorld(),(double)loc.getBlockX(),(double)(y-3),(double)loc.getBlockZ());
    final org.bukkit.block.Block blockChanged = blockChangeLocation.getBlock();
    this.pObject.sendBlockChange(blockChangeLocation, Material.BEDROCK, (byte)0);
    CraftPlayer cp = ((CraftPlayer)this.pObject);
    Vector vec = cp.getMomentum();
    vec.setY(0.3);
    cp.setMomentum(vec);
    double x = loc.getX(),z = loc.getZ();
    if((x-Math.floor(x))==0 && (z-Math.floor(z))==0) {
      x+=0.5;
      z+=0.5;
    }
    final Location targetLoc = new Location(loc.getWorld(),x,(double)(y-2),z);
    this.pObject.teleport(targetLoc);
    chunk = null;
    final Player playerReference = this.pObject;
    serverObject.getScheduler().scheduleSyncDelayedTask(mudkipsMain, new Runnable() {public void run() {playerReference.teleport(targetLoc);}}, 4);
    serverObject.getScheduler().scheduleSyncDelayedTask(mudkipsMain, new Runnable() {public void run() {playerReference.teleport(targetLoc);}}, 8);
    serverObject.getScheduler().scheduleSyncDelayedTask(mudkipsMain, new Runnable() {public void run() {playerReference.sendBlockChange(blockChangeLocation, blockChanged.getType(), blockChanged.getData());}}, 100);
    Player reAttainedPlayer = this.serverObject.getPlayer(this.name) ;
    if(reAttainedPlayer != null)
      this.pObject = reAttainedPlayer;
  }
  public void teleportSpawn() {
    if(this.homeBed != null) {
      teleport(mudkipsMain.worldUtil.findSpaceForPlayer(homeBed));
    } else {
      teleport(mudkipsMain.worldUtil.getDefaultSpawn());
    }
  }
  public void renameRequest(String newName) {
    
  }
  public void printDebug() {
    StringBuilder buf = new StringBuilder();
    buf.append("name:");
    buf.append(name);
    buf.append("\n");
    buf.append("pObject:");
    buf.append(pObject);
    buf.append("\n");
    buf.append("serverObject:");
    buf.append(serverObject);
    buf.append("\n");
    buf.append("alias:");
    buf.append(alias);
    buf.append("\n");
    buf.append("afkMessage:");
    buf.append(afkMessage);
    buf.append("\n");
    buf.append("mudkipsMain:");
    buf.append(mudkipsMain);
    buf.append("\n");
    buf.append("chatPartner:");
    buf.append(chatPartner);
    buf.append("\n");
    buf.append("homeBed:");
    buf.append(homeBed);
    buf.append("\n");
    buf.append("alterName:");
    buf.append(alterName);
    buf.append("\n");
    buf.append("slappyOn:");
    buf.append(slappyOn);
    buf.append("\n");
    String fileName = "debug_" + System.currentTimeMillis() + ".dat";
    File fileToWrite = new File(this.mudkipsMain.getDataFolder(), fileName);
    try {
      FileOutputStream stream = new FileOutputStream(fileToWrite);
      stream.write(buf.toString().getBytes());
      stream.close();
    } catch(IOException exc) {
      mudkipsMain.errorHandler.logError(exc);
      System.out.println(buf.toString());
    }
  }
  public boolean slap() {
    if(!updateBinding()) return false;
    this.pObject.setVelocity(this.pObject.getLocation().getDirection().multiply(-0.8));
    final MudkipsPlayer playerReference = this;
    serverObject.getScheduler().scheduleAsyncDelayedTask(mudkipsMain, new Runnable() { public void run() {playerReference.getPlayer().damage(1);}}, 4);
    serverObject.getScheduler().scheduleAsyncDelayedTask(mudkipsMain, new Runnable() { public void run() {playerReference.sendMessage(ChatColor.YELLOW + "You were slapped!"); }}, 12);
    return true;
  }
  public boolean toggleSlappy() {
    if(slappyOn) return slappyOn = false;
    else return slappyOn = true;
  }
  public boolean inSlappy() {
    return slappyOn;
  }
  public boolean isPvpEnabled() {
    return pvpEnabled;
  }
  public boolean playerFight(MudkipsPlayer mVictim, int damage) {
    if(!this.isJailed() && this.isPvpEnabled() && (mVictim.isPvpEnabled() || mVictim.isJailed())) {
      lastAttack = System.currentTimeMillis();
      return true;
    } else {
      if(this.isJailed()) {
        sendMessage(ChatColor.RED + "You can not hit anynone while you are jailed.");
      } else if(!this.isPvpEnabled()) {
        sendMessage(ChatColor.RED + "PvP is disabled for you.");
      } else if(!mVictim.isPvpEnabled()) {
        sendMessage(ChatColor.RED + "\""+mVictim.getName()+"\" does not participate in PvP.");
      }
      return false;
    }
  }
  public void setPvpEnabled(boolean pvpEnabled) {
    this.pvpEnabled = pvpEnabled;
  }
  public boolean fightCooldownElapsed() {
    if((this.lastAttack + (mudkipsMain.myProps.getIntProperty("pvp-cooldown")*1000)) < System.currentTimeMillis()) {
      return true;
    } else {
      return false;
    }
  }
  public boolean kickCooldownElapsed() {
    if((this.lastKick + mudkipsMain.myProps.getIntProperty("kick-cooldown")*1000) < System.currentTimeMillis()) {
      return true;
    } else {
      return false;
    }
  }
  public void kicked() {
    lastKick = System.currentTimeMillis();
  }
  public boolean banCooldownElapsed() {
    if((this.lastBan + mudkipsMain.myProps.getIntProperty("ban-cooldown")*1000) < System.currentTimeMillis()) {
      return true;
    } else {
      return false;
    }
  }
  public boolean mayTeleport(Location fromLoc, Location toLoc) {
    if(isJailed()) {
      Location jailLoc = mudkipsMain.warpHandler.getWarp("jail");
      if(jailLoc != null && WorldUtil.calcDistance(toLoc, jailLoc, true) < 10) {
        return true;
      } else {
        return false;
      }
    }
    return true;
  }
  public boolean isJailed() {
    if(jailedFor < 1) {
//      jailed = false;
      return false;
    }
    if(this.jailedFor < System.currentTimeMillis()) {
//      jailedFor = 0;
//      jailed = false;
      return false;
    } else {
//      jailed = true;
      return true;
    }    
  }
  public void log(String logMessage) {
    this.serverObject.getLogger().info(logMessage);
  }
  private void teleportJail() {
    Location warpLoc = this.mudkipsMain.warpHandler.getWarp("jail");
    if(warpLoc != null) {
      this.teleport(warpLoc);
    } else {
      log("No jail warp existent!");
    }
  }
  public void jail(long duration) {
    if(duration > 0) {
      if(!jailed) {
        teleportJail();
        String durationDesc = StringUtil.describeDuration(duration, "en");
        sendMessage(ChatColor.RED + "You are being jailed for " + durationDesc);
        storeInventory();
        this.jailedFor = System.currentTimeMillis() + duration;
        this.jailed = true;
        mudkipsMain.playerProvider.playerJailed(name, duration);
      } else {
        teleportJail();
        String durationDesc = StringUtil.describeDuration(duration, "en");
        sendMessage(ChatColor.RED + "You are still jailed for " + durationDesc);
        if(inventory == null) {
          storeInventory();
        }
        this.jailedFor = System.currentTimeMillis() + duration;
        this.jailed = true;
        mudkipsMain.playerProvider.playerJailed(name, duration);
      }
    } else {
      unjail();
    }
  }
  public void unjail() {
	//TODO:
	//X Tell Player that he is unjailed
	//X Teleport player back to his /spawn
	//X Give Player his inventory back
    sendMessage(ChatColor.YELLOW + "You are unjailed.");
    System.out.println("unjailing \""+name+"\"");
	teleportSpawn();
	restoreInventory();
	this.jailedFor = 0;
	this.jailed = false;
  }
  public long getJailedForDuration() {
    if(jailedFor < 1) return 0;
    else return jailedFor - System.currentTimeMillis();
  }
  public void storeInventory() {
    ItemStack[] inventory = new ItemStack[40];
    
    ItemStack[] playerInventory = pObject.getInventory().getContents();
    int i = 0;
    for(; (i < 36 && i<playerInventory.length); i++) {
      inventory[i] = playerInventory[i];
    }
    i = 36;
    playerInventory = pObject.getInventory().getArmorContents();
    for(int j = 0; (j < 4 && j < playerInventory.length); j++) {
      inventory[i++] = playerInventory[j];
    }
    this.updateInventory(new ItemStack[36], new ItemStack[4]);
    this.inventory = inventory;
  }
  public void restoreInventory() {
    if(inventory != null && inventory.length >= 40) {
      ItemStack[] contentArr = new ItemStack[36];
      int i = 0;
      for(; i < 36; i++) {
        contentArr[i] = inventory[i];
      }
      i = 36;
      ItemStack[] armorArr = new ItemStack[4];
      for(int j = 0; j < 4; j++) {
        armorArr[j] = inventory[i++];
      }
      this.updateInventory(contentArr, armorArr);
      this.inventory = null;
    } else {
      this.serverObject.getLogger().severe("Could not restore inventory of \""+this.name+"\", it is "+(inventory==null?"null":"too small")+"!");
    }
  }
  public void updateInventory(ItemStack[] contentArr, ItemStack[] armorArr) {
    pObject.getInventory().setContents(contentArr);
    pObject.getInventory().setArmorContents(armorArr);

  }
  public void kill(MudkipsPlayer murderer) {
    if(pObject.getGameMode() != org.bukkit.GameMode.CREATIVE) {
      if(murderer == null || !name.equals(murderer.getName())) {
        wasSmitted = pObject.getWorld().getFullTime();
        if(murderer != null) {
          smitter = murderer.getName();
        }
      }
      //pObject.damage(20, murderer.getPlayer());
      //pObject.damage(20, null);
      net.minecraft.server.EntityPlayer ePlayer = ((CraftPlayer)pObject).getHandle(); 
      ePlayer.damageEntity(net.minecraft.server.DamageSource.GENERIC, 1000);
    } else {
      // Does not work, instead it kills the server!
      //((CraftPlayer)pObject).getHandle().die(net.minecraft.server.DamageSource.playerAttack((net.minecraft.server.EntityHuman)((CraftPlayer)murderer.getPlayer()).getHandle()));
    }
  }
  public void died(org.bukkit.event.entity.PlayerDeathEvent e) {
    lastDeath = System.currentTimeMillis();
    timesDied++;
    if(wasSmitted > 0 && wasSmitted == pObject.getWorld().getFullTime()) {
      e.setDeathMessage(name + " was smitted"+(smitter==null?"":(" by " + smitter))+".");
    }
    wasSmitted = 0;
    smitter = null;
    String deathMessage = ChatColor.DARK_AQUA + "                    " + e.getDeathMessage();
    mudkipsMain.chatObj.sendMessageAround(deathMessage, pObject.getLocation(), mudkipsMain.myProps.getIntProperty("death-propagation-distance"));
  }
  public int getTimesDied() {
    return timesDied;
  }
  public void respawn(PlayerRespawnEvent e) {
    if(isJailed()) {
      Location jailLocation = mudkipsMain.warpHandler.getWarp("jail");
      if(jailLocation != null) {
        e.setRespawnLocation(jailLocation);
        return;
      }
    }
    //TODO: remove this, or replace with a 'e.setRespawnLocation(homeBed!=null?homeBed:worldUtil.getDefaultSpawn())'
//    teleportSpawn();
  }
  public World getWorld() {
    return pObject.getWorld();
  }
}
