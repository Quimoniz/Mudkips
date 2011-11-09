package org.quimoniz.mudkips;

import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.quimoniz.mudkips.player.MudkipsPlayer;
import org.quimoniz.mudkips.util.WorldUtil;
import org.quimoniz.wigglytuff.ReaderStreamParser;
import org.bukkit.Server;

public class Portal {
  private BlockState lowestBlockSouthWest;
  private Location loc;
  private boolean isSouth;
  private Location targetLoc;
  private String targetWarp = null;
  private boolean hasDestinationWarp = false;
  private Mudkips mudkips;
  public Portal(Block lowestBlockSouthWest, boolean otherIsNorth, Location targetLoc, Mudkips mudkips) {
    this.mudkips = mudkips;
    this.lowestBlockSouthWest = lowestBlockSouthWest.getState();
    this.loc = lowestBlockSouthWest.getLocation();
    isSouth = otherIsNorth;
    this.targetLoc = targetLoc;
    hasDestinationWarp = false;
  }
  public Portal(Block lowestBlockSouthWest, boolean otherIsNorth, String targetWarp, Mudkips mudkips) {
    this.mudkips = mudkips;
    this.lowestBlockSouthWest = lowestBlockSouthWest.getState();
    this.loc = lowestBlockSouthWest.getLocation();
    isSouth = otherIsNorth;
    this.targetWarp = targetWarp;
    hasDestinationWarp = true;
  }
  public boolean matches(Location travelLoc) {
    try {
      if(loc.getWorld().getName().equals(travelLoc.getWorld().getName()) && travelLoc.getBlockY() >= loc.getBlockY() && travelLoc.getBlockY() < (loc.getBlockY() + 3)) {
        if(isSouth) {
          if((travelLoc.getBlockX() <= loc.getBlockX() && travelLoc.getBlockX() > (loc.getBlockX()-2))
                  && (travelLoc.getBlockZ() == loc.getBlockZ())) {
            return true;
          }
        } else {
          if((travelLoc.getBlockZ() <= loc.getBlockZ() && travelLoc.getBlockZ() > (loc.getBlockZ()-2))
                  && (travelLoc.getBlockX() == loc.getBlockX())) {
            return true;
          }
        }
      }
    } catch(NullPointerException exc) { }
    return false;
  }
  public void teleport(Player p) {
    MudkipsPlayer mPlayer = mudkips.getMudkipsPlayer(p.getName());
    if(hasDestinationWarp) {
      mPlayer.teleport(mudkips.warpHandler.getWarp(targetWarp));
    } else {
      mPlayer.teleport(targetLoc);
    }
  }
  public String dataLine() {
    StringBuilder buf = new StringBuilder();
    buf.append("\"");
    buf.append(ReaderStreamParser.escapeCharacters(loc.getWorld().getName()));
    buf.append("\",");
    buf.append(loc.getBlockX());
    buf.append(",");
    buf.append(loc.getBlockY());
    buf.append(",");
    buf.append(loc.getBlockZ());
    if(isSouth)
      buf.append(",\"south\",");
    else
      buf.append(",\"west\",");
    if(hasDestinationWarp) {
      buf.append("\"warp\",");
      buf.append("\"");
      buf.append(ReaderStreamParser.escapeCharacters(targetWarp));
      buf.append("\"");
    } else {
      buf.append("\",loc\"");
      buf.append("\"");
      buf.append(ReaderStreamParser.escapeCharacters(targetLoc.getWorld().getName()));
      buf.append("\",");
      buf.append(targetLoc.getBlockX());
      buf.append(",");
      buf.append(targetLoc.getBlockY());
      buf.append(",");
      buf.append(targetLoc.getBlockZ());
    }
    return buf.toString();
  }
  public static Portal parse(String dataLine, Mudkips mudkips) {
    StringBuilder buf = new StringBuilder(16);
    boolean inString = false;
    int elementIndex = 0;
    String name = null;
    int x=0,y=0,z=0;
    boolean isSouth = false;
    Portal result = null;
    Location portalLoc = null;
    boolean destinationExact = false;
    String warpName = null;
    Location destinationLoc = null;
    boolean nextCharIsEscape = false;
    boolean satisfied = false;
    for(char curChar : dataLine.toCharArray()) {
      if(inString) {
        if(nextCharIsEscape) {
          buf.append(ReaderStreamParser.unescapeCharacter(curChar));
          nextCharIsEscape = false;
        } else if(curChar == 92) {// backslash
          nextCharIsEscape = true;
        } else if(curChar == 34) {//quotation mark
          inString = false;
        } else {
          buf.append(curChar);
        }
      } else {
        if(curChar == 34) { //quotation mark
          inString = true;
        } else if(curChar == 44) {
          satisfied = false;
          switch(elementIndex++) {
            case 0:
              name = buf.toString();
              break;
            case 1:
              try {
                x = Integer.parseInt(buf.toString());
              } catch(NumberFormatException exc) {
                
              }
              break;
            case 2:
              try {
                y = Integer.parseInt(buf.toString());
              } catch(NumberFormatException exc) {
                
              }
              break;
            case 3:
              try {
                z = Integer.parseInt(buf.toString());
              } catch(NumberFormatException exc) {
                
              }
              break;
            case 4:
              if("south".equalsIgnoreCase(buf.toString())) {
                isSouth = true;
              }
              org.bukkit.World world = mudkips.worldUtil.attainWorld(name);
              if(world != null) {
                portalLoc = new Location(world,x,y,z);
              }
              break;
            case 5:
              if("warp".equalsIgnoreCase(buf.toString())) {
                destinationExact = false;
              } else {
                destinationExact = true;
              }
              break;
            case 6:
              if(destinationExact) {
                name = buf.toString();
              } else {
                warpName = buf.toString();
                satisfied = true;
              }
              break;
            case 7:
              try {
                x = Integer.parseInt(buf.toString());
              } catch(NumberFormatException exc) {
                
              }
              break;
            case 8:
              try {
                y = Integer.parseInt(buf.toString());
              } catch(NumberFormatException exc) {
                
              }
              break;
            case 9:
              try {
                z = Integer.parseInt(buf.toString());
              } catch(NumberFormatException exc) {
                
              }
              org.bukkit.World destinationWorld = mudkips.worldUtil.attainWorld(name);
              if(destinationWorld != null) {
                destinationLoc = new Location(destinationWorld, x, y, z);
              }
              satisfied = true;
              break;
          }
          if(satisfied) {
            break;
          }
          buf.delete(0, buf.length());
        } else {
          buf.append(curChar);
        }
      }
    }
    if(!satisfied) {
      if(destinationExact) {
        if(destinationLoc == null) {
          org.bukkit.World destinationWorld = mudkips.worldUtil.attainWorld(name);
          if(destinationWorld != null) {
            destinationLoc = new Location(destinationWorld, x, y, z);
          }
        }
      } else {
        if(warpName == null) {
          warpName = buf.toString();
        }
      }
    }
    
    if(portalLoc != null) {
      if(destinationExact)
        return new Portal(portalLoc.getBlock(), isSouth, destinationLoc, mudkips);
      else 
        return new Portal(portalLoc.getBlock(), isSouth, warpName, mudkips);
    } else {
      return null;
    }
  }
  public String toString() {
    return dataLine();
  }
}
