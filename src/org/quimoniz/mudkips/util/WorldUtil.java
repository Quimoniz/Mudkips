package org.quimoniz.mudkips.util;

import java.io.File;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.Server;
import org.bukkit.craftbukkit.CraftServer;
import org.quimoniz.mudkips.Mudkips;

public class WorldUtil {
  private Server server;
  private Mudkips mudkips;
  public final static java.util.HashSet<Integer> notObtainables= new java.util.HashSet<Integer>(java.util.Arrays.asList(0,2,8,7,9,10,11,16,21,26,30,32,34,36,43,51,52,55,56,59,60,62,63,64,68,71,73,74,75,78,79,83,90,92,93,94,95,97,99,100,104,105,110,119,120));
  public WorldUtil(Server server, Mudkips mudkips) {
    this.server = server;
    this.mudkips = mudkips;
  }
  public boolean worldLoadable(String worldName) {
    net.minecraft.server.MinecraftServer mcServer = ((org.bukkit.craftbukkit.CraftServer)server).getServer();
    File serverFolder = mcServer.a(".");
    if((new File(serverFolder, "server.properties")).exists() && (new File(serverFolder, "plugins")).exists()) {
      File worldFolder = new File(serverFolder, worldName);
      if(worldFolder.exists() && worldFolder.isDirectory()) {
        File levelDat = new File(worldFolder, "level.dat");
        if(levelDat.exists() && levelDat.isFile())
          return true;
      }
    }
    return false;
  }
  public World loadWorld(String worldName) {
    if(worldLoadable(worldName)) {
      return server.createWorld(new WorldCreator(worldName));
    } else {
      return null;
    }
  }
  public World attainWorld(String worldName) {
    if(worldName == null) {
      return null;
    }
    World returnWorld = server.getWorld(worldName);
    if(returnWorld == null) {
      returnWorld = loadWorld(worldName);
    }
    return returnWorld;
  }
  public static double calcDistance(Location locA, Location locB, boolean checkHeight) {
    double distance = 0;
    double deltaX = locA.getBlockX()-locB.getBlockX(), deltaY = locA.getBlockY()-locB.getBlockY(), deltaZ = locA.getBlockZ()-locB.getBlockZ();
    if(checkHeight) // Euclidean distance
      distance = Math.sqrt(Math.pow(deltaX,2) + Math.pow(deltaY,2) + Math.pow(deltaZ,2));
    else // Pythagorean equation
        distance = Math.sqrt(deltaX*deltaX + deltaZ*deltaZ);
    return distance;
  }
  public boolean isCertainBlock(Location loc, int offsetX, int offsetY, int offsetZ, org.bukkit.Material mat) {
    return isCertainBlock(new Location(loc.getWorld(), loc.getBlockX() + offsetX, loc.getBlockY() + offsetY, loc.getBlockZ() + offsetZ), mat);
  }
  public boolean isCertainBlock(Location loc, org.bukkit.Material mat) {
    if(loc.getWorld().isChunkLoaded(loc.getBlockX(), loc.getBlockZ())) {
      if(loc.getWorld().getBlockAt(loc).getType().equals(mat)) {
        return true;
      } else {
        return false;
      }
    } else {
      loc.getWorld().loadChunk(loc.getBlockX(), loc.getBlockZ());
      @SuppressWarnings("unused") org.bukkit.Chunk chunk = loc.getWorld().getChunkAt(loc);
      if(loc.getWorld().getBlockAt(loc).getType().equals(mat)) {
        return true;
      } else {
        return false;
      }
    }
  }
  public Location getDefaultSpawn() {
    World defaultWorld = getDefaultWorld();
    if(defaultWorld != null) {
      return defaultWorld.getSpawnLocation();
    }
    return null;
  }
  public World getDefaultWorld() {
    String worldName = mudkips.serverUtil.getProperty("level-name");
    if(worldName != null) {
      return server.getWorld(worldName);
    } else {
      return null;
    }
  }
  //TODO: Make it static
  public Location findSpaceForPlayer(Location loc) {
    Location tempLoc = new Location(loc.getWorld(), loc.getX() + 1, loc.getY(), loc.getZ());
    if(isCertainBlock(tempLoc, Material.AIR) && isCertainBlock(tempLoc, 0, 1, 0, Material.AIR)) {
      return tempLoc;
    }
    tempLoc = new Location(loc.getWorld(), loc.getX(), loc.getY(), loc.getZ() + 1);
    if(isCertainBlock(tempLoc, Material.AIR) && isCertainBlock(tempLoc, 0, 1, 0, Material.AIR)) {
      return tempLoc;
    }
    tempLoc = new Location(loc.getWorld(), loc.getX() - 1, loc.getY(), loc.getZ());
    if(isCertainBlock(tempLoc, Material.AIR) && isCertainBlock(tempLoc, 0, 1, 0, Material.AIR)) {
      return tempLoc;
    }
    tempLoc = new Location(loc.getWorld(), loc.getX(), loc.getY(), loc.getZ() - 1);
    if(isCertainBlock(tempLoc, Material.AIR) && isCertainBlock(tempLoc, 0, 1, 0, Material.AIR)) {
      return tempLoc;
    }
    return loc;
  }
}
