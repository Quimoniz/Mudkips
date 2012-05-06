package org.quimoniz.mudkips.listeners;

import org.bukkit.event.player.PlayerBedEnterEvent;
import org.bukkit.event.Listener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.event.player.PlayerPreLoginEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.quimoniz.mudkips.Mudkips;
import org.quimoniz.mudkips.player.MudkipsPlayer;

import java.util.HashSet;

public class MPlayerListener implements Listener {
  private Mudkips pluginMain;
  private boolean blockJailPickup = false;
  private HashSet<Integer> blockJailInteractIds = null;
  private boolean blockAllJailInteract = false;
  public boolean listenPortalEvents = false;
  public boolean listenPreloginEvents = false;
  public MPlayerListener(Mudkips pluginMain) {
	this.pluginMain = pluginMain;
  }// note: Join is after successfull login (LoginEvent)
  @EventHandler public void onPlayerJoin(PlayerJoinEvent e) {
	pluginMain.playerJoin(e);
  }
  @EventHandler public void onPlayerChat(PlayerChatEvent e) {
	pluginMain.playerChat(e);
  }
  @EventHandler public void onPlayerQuit(PlayerQuitEvent e) {
	pluginMain.playerQuit(e);
  }
  @EventHandler public void onPlayerBedEnter(PlayerBedEnterEvent e) {
    pluginMain.setHomeBed(e.getPlayer(),e.getBed().getLocation());
  }
  @EventHandler public void onPlayerPortal(PlayerPortalEvent e) {
    pluginMain.portaling(e);
  }
  @EventHandler public void onPlayerPreLogin(PlayerPreLoginEvent e) {
    pluginMain.playerPreLogin(e);
  }
  @EventHandler public void onPlayerTeleport(PlayerTeleportEvent e) {
    pluginMain.playerTeleport(e);
  }
  @EventHandler public void onPlayerRespawn(PlayerRespawnEvent e) {
    pluginMain.playerRespawn(e);
  }
  @EventHandler public void onPlayerPickupItem(PlayerPickupItemEvent e) {
    if(blockJailPickup) {
      MudkipsPlayer mPlayer = pluginMain.getMudkipsPlayer(e.getPlayer());
      if(mPlayer != null) {
        if(mPlayer.isJailed()) {
          e.setCancelled(true);
        }
      }
    }
  }
  @EventHandler public void onPlayerInteract(PlayerInteractEvent e) {
    if(e.isCancelled() || e.getClickedBlock() == null) return;
    MudkipsPlayer mPlayer;
    if(blockAllJailInteract) {
      mPlayer = pluginMain.getMudkipsPlayer(e.getPlayer());
      if(mPlayer != null) {
        if(mPlayer.isJailed()) {
          e.setCancelled(true);
          return;
        }
      }
    }
    if(blockJailInteractIds != null) {
      mPlayer = pluginMain.getMudkipsPlayer(e.getPlayer());
      if(mPlayer != null) {
        if(mPlayer.isJailed()) {
          if(blockJailInteractIds.contains(new Integer(e.getClickedBlock().getTypeId()))) {
            e.setCancelled(true);
          }
        }
      }
    }
  }
  public void blockJailPickup(boolean watchIt) {
    blockJailPickup = watchIt;
  }
  public void blockAllJailInteract(boolean watchIt) {
    blockAllJailInteract = watchIt;
  }
  public void blockJailInteract(java.util.List<Integer> ids) {
	if(ids == null) {
	  blockJailInteractIds = null;
	} else {
      blockJailInteractIds = new HashSet<Integer>(ids);
	}
  }
}
