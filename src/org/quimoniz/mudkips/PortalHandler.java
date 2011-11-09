package org.quimoniz.mudkips;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.event.player.PlayerPortalEvent;
import org.quimoniz.mudkips.player.MudkipsPlayer;

import java.util.Iterator;
import java.util.LinkedList;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.util.logging.Logger;

public class PortalHandler {
  private LinkedList<Portal> portalList = new LinkedList<Portal>();
  private Mudkips mudkips;
  private File dataFile;
  private Logger log; 
  public PortalHandler(File dataFile, Logger log, Mudkips mudkips) {
    this.mudkips = mudkips;
    this.dataFile = dataFile;
    this.log = log;
    if(!dataFile.exists()) {
      try {
        dataFile.createNewFile();
      } catch(IOException exc) {
        mudkips.errorHandler.logError(exc);
        log.severe("Could not create File \"" + dataFile + "\"for saving Portals!");
      }
    } else {
      load();
    }
  }
  public boolean registerPortal(Block targetBlock, Object destination) {
    if(targetBlock.getType() == Material.PORTAL) {
      Location loc = targetBlock.getLocation();
      World world = loc.getWorld();
      BlockFace portalFacing = null;
      if(world.getBlockAt(loc.getBlockX()-1, loc.getBlockY(), loc.getBlockZ()).getType() == Material.PORTAL) {
        portalFacing = BlockFace.NORTH;  
      } else if(world.getBlockAt(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ()-1).getType() == Material.PORTAL) {
        portalFacing = BlockFace.EAST;
      } else if(world.getBlockAt(loc.getBlockX()+1, loc.getBlockY(), loc.getBlockZ()).getType() == Material.PORTAL) {
        portalFacing = BlockFace.SOUTH;
      } else if(world.getBlockAt(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ()+1).getType() == Material.PORTAL) {
        portalFacing = BlockFace.WEST;
      }
      Block lowestBlock = world.getBlockAt(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());

      while(lowestBlock.getType() == Material.PORTAL) {
        loc = lowestBlock.getLocation();
        lowestBlock = world.getBlockAt(loc.getBlockX(), loc.getBlockY() - 1, loc.getBlockZ());
      }
      Block lowestSouthWestBlock = null;
      if(BlockFace.WEST.equals(portalFacing)) {
        lowestSouthWestBlock = world.getBlockAt(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ()+1);
      } else if(BlockFace.SOUTH.equals(portalFacing)) {
        lowestSouthWestBlock = world.getBlockAt(loc.getBlockX()+1, loc.getBlockY(), loc.getBlockZ());
      } else {
        lowestSouthWestBlock = world.getBlockAt(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
      }
      Portal newPortal = null;
      if(destination instanceof Location)
        newPortal = new Portal(lowestSouthWestBlock, BlockFace.NORTH.equals(portalFacing) | BlockFace.SOUTH.equals(portalFacing), (Location) destination, mudkips);
      else if(destination instanceof String) {
        newPortal = new Portal(lowestSouthWestBlock, BlockFace.NORTH.equals(portalFacing) | BlockFace.SOUTH.equals(portalFacing), (String) destination, mudkips);
      }
      if(newPortal != null) {
        removePortal(lowestSouthWestBlock);
        portalList.add(newPortal);
      }
    }
    
    return true;
  }
  public void removePortal(Block targetBlock) {
    Iterator<Portal> portalIterator = portalList.iterator();
    while(portalIterator.hasNext()) {
      if(portalIterator.next().matches(targetBlock.getLocation())) {
        portalIterator.remove();
        break;
      }
    }
  }
  public void onPlayerPortal(PlayerPortalEvent e) {
    boolean travelWithItems = mudkips.myProps.getBooleanProperty("travel-with-equipment");
    boolean hasEquipment = false;
    MudkipsPlayer mPlayer = mudkips.getMudkipsPlayer(e.getPlayer().getName());
    if(!travelWithItems) {
      if(mPlayer.isEquipped() || mPlayer.armorEquipped())
        hasEquipment = true;
    }
    for(Portal curPortal : portalList) {
      if(curPortal.matches(e.getFrom())) {
        if(!travelWithItems) {
          if(hasEquipment) {
            e.setCancelled(true);
            mPlayer.sendMessage(ChatColor.YELLOW + "Notice: No travelling with items!");
            return;
          }
        }
        e.setCancelled(true);
        curPortal.teleport(e.getPlayer());
        break;
      }
    }
  }
  public void save() {
    FileOutputStream output = null;
    try {
      output = new FileOutputStream(dataFile);
    } catch(IOException exc) {
      mudkips.errorHandler.logError(exc);
      log.severe("Mudkips/Portals: Could not initialize OutputStream!");
    }
    if(output == null) {
      return;
    }
    for(Portal curPortal : portalList) {
      try {
        output.write(curPortal.dataLine().getBytes("ISO-8859-1"));
        output.write((byte)'\n');
      } catch(IOException exc) {
        mudkips.errorHandler.logError(exc);
        log.severe("Mudkips/Portals: Could not properly write bytes into file!");
      }
    }
    try {
      output.close();
    } catch(IOException exc) { }
  }
  public void load() {
    BufferedReader reader = null;
    try {
      reader = new BufferedReader(new InputStreamReader(new FileInputStream(dataFile),"ISO-8859-1"));
    } catch(IOException exc) {
      mudkips.errorHandler.logError(exc);
      log.severe("Mudkips/Portals: Could not initialize BufferedReader!");
    }
    String curLine;
    portalList = new LinkedList<Portal>();
    try {
      while((curLine = reader.readLine())!= null) {
        Portal portalToAdd = Portal.parse(curLine, mudkips);
        if(portalToAdd != null)
          portalList.add(portalToAdd);
      }
      reader.close();
    } catch(IOException exc) {
      mudkips.errorHandler.logError(exc);
      log.severe("Mudkips/Portals: Could not properly read Portals!");
    }
  }
}
