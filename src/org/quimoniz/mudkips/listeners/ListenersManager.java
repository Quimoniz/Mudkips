package org.quimoniz.mudkips.listeners;

import org.bukkit.World;
import org.quimoniz.mudkips.Mudkips;
import org.quimoniz.mudkips.util.StringUtil;
import org.bukkit.Server;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.SimplePluginManager;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.Event;
import org.bukkit.Server;
import org.quimoniz.mudkips.ConfigManager;

public class ListenersManager {
  private Mudkips mudkips;
  private Server server;
  private boolean blockBreakRegistered = false,
                  blockPlaceRegistered = false,
                  blockBurnRegistered = false,
                  blockIgniteRegistered = false;
  private boolean entityDeathRegistered = false,
                  entityDamageRegistered = false;
  private boolean playerJoinRegistered = false,
                  playerChatRegistered = false,
                  playerQuitRegistered = false,
                  playerBedEnterRegistered = false,
                  playerPortalRegistered = false,
                  playerPreLoginRegistered = false,
                  playerTeleportRegistered = false,
                  playerRespawnRegistered = false,
                  playerPickupRegistered = false,
                  playerInteractRegistered = false;
  
  public ListenersManager(Mudkips mudkips, Server server) {
    this.mudkips = mudkips;
    this.server = server;
  }
  public void onConfigLoad(World worldLoaded) {
	  //TODO: Modify the copy&pasted code to work here, rewrite the myProps.get... to use configManager.getValue(String: worldName, String: keyName)
      //Register Player events
      try {
        PluginManager pm = server.getPluginManager();
        ConfigManager config = mudkips.configManager;
        String worldName = worldLoaded.getName();
        MPlayerListener playerListener = new MPlayerListener(mudkips);
        pm.registerEvents(playerListener, mudkips);
        //TODO: Register correct event hooks, with Flint&Steel the event is BLOCK_IGNITE !!!
        BlockInteractListener touchListener = new BlockInteractListener(mudkips);
        pm.registerEvents(touchListener, mudkips);

        if(config.getBooleanValue("protect-obsidian",worldName,false)) {
          touchListener.blockObsidianDigging = true;
        }
        if((config.getBooleanValue("enable-jail",worldName,false) && config.getBooleanValue("protect-jail-break",worldName,false))) {
          touchListener.blockJailBreak(worldName, true);
        }
        if(config.getBooleanValue("enable-jail",worldName,false) && config.getBooleanValue("protect-jail-place",worldName,false)) {
          touchListener.blockJailPlace(worldName, true);
          touchListener.blockPlacementWasRegistered = true;
        }
        if(config.getBooleanValue("enable-jail",worldName,false) && config.getBooleanValue("protect-jail-pickup",worldName,false)) {
          playerListener.blockJailPickup(true);
        }
        //TODO: Take care of rewriting the list feature
        if(config.getBooleanValue("enable-jail",worldName,false)) {
          String[] numsBlocked = StringUtil.separate(mudkips.myProps.getProperty("protect-jail-interact"),',');
          if(numsBlocked != null && numsBlocked.length > 0) {
            java.util.ArrayList<Integer> idsBlocked  = new java.util.ArrayList<Integer>(numsBlocked.length);
            boolean allInteractionsBlocked = false;
            for(int i = 0; i < numsBlocked.length; i++) {
              if("*".equals(numsBlocked)) {
                allInteractionsBlocked = true;
                break;
              } else {
                try {
                  idsBlocked.add(new Integer(numsBlocked[i]));
                } catch(NumberFormatException exc) { }
              }
            }
            if(idsBlocked.size() > 0) {
              if(allInteractionsBlocked) {
                playerListener.blockAllJailInteract(true);
              } else if(idsBlocked.size() > 0) {
                  playerListener.blockJailInteract(idsBlocked);
              }
            }
          }
        }
        String[] numsBlocked = StringUtil.separate(mudkips.myProps.getProperty("block-fire-placement"),',');
        if(numsBlocked != null && numsBlocked.length > 0) {
          java.util.ArrayList<Integer> idsBlocked  = new java.util.ArrayList<Integer>(numsBlocked.length);
          boolean allFirePlacementBlocked = false;
          for(int i = 0; i < numsBlocked.length; i++) {
            if("*".equals(numsBlocked)) {
              allFirePlacementBlocked = true;
              break;
            } else {
              try {
                idsBlocked.add(new Integer(numsBlocked[i]));
              } catch(NumberFormatException exc) { }
            }
          }
          if(idsBlocked.size() > 0) {
            if(allFirePlacementBlocked) {
              touchListener.blockAllFirePlace(true);

              touchListener.blockIgniteWasRegistered = true;
            } else {
              if(idsBlocked.size() > 0) {
                //TODO: Per World configurable ...    anyone????
                touchListener.blockFirePlace(idsBlocked);
                touchListener.blockIgniteWasRegistered = true;
              }
            }
          }
        }
        numsBlocked = StringUtil.separate(mudkips.myProps.getProperty("block-burn"),',');
        if(numsBlocked != null && numsBlocked.length > 0) {
          java.util.ArrayList<Integer> idsBlocked  = new java.util.ArrayList<Integer>(numsBlocked.length);
          boolean allBurningBlocked = false;
          for(int i = 0; i < numsBlocked.length; i++) {
            if("*".equals(numsBlocked)) {
              allBurningBlocked = true;
            } else {
              try {
                idsBlocked.add(new Integer(numsBlocked[i]));
              } catch(NumberFormatException exc) { }
            }
          }
          if(idsBlocked.size() > 0) {
            if(allBurningBlocked) {
              touchListener.blockAllBurning(true);
              if(!touchListener.blockIgniteWasRegistered) {
                touchListener.blockIgniteWasRegistered = true;
              }
            } else {
              if(idsBlocked.size() > 0) {
                touchListener.blockBurning(idsBlocked);
                if(touchListener.blockIgniteWasRegistered) {
                  touchListener.blockIgniteWasRegistered = true;
                }
              }
            }
          }
        }
        if(config.getBooleanValue("tall-grass-makes-grass-block",worldName,false)) {
          touchListener.tallGrassMakesGrassBlock(worldName, true);
          if(!touchListener.blockPlacementWasRegistered) {
            touchListener.blockPlacementWasRegistered = true;
          }
        }
        if(config.getBooleanValue("enable-portals",worldName,false)) {
          playerListener.listenPortalEvents = true;
        }
        if(config.getBooleanValue("enable-bans",worldName,false)) {
          playerListener.listenPreloginEvents = true;
        }
        
        MEntityListener entityListener = new MEntityListener(mudkips);
        pm.registerEvents(entityListener, mudkips);
      } catch(Exception exc) {
          System.out.println("Exception during registering listeners");
          mudkips.errorHandler.logException(exc);
      }
  }
}
