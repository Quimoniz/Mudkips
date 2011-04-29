package org.quimoniz.mudkips;

import org.bukkit.event.player.PlayerEvent;
import org.bukkit.event.player.PlayerListener;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class MPlayerListener extends PlayerListener {
  private Mudkips pluginMain;
  
  MPlayerListener(Mudkips pluginMain) {
	this.pluginMain = pluginMain;
  }
  @Override public void onPlayerLogin(PlayerLoginEvent e) {
	pluginMain.playerJoin(e.getPlayer());
  }
  @Override public void onPlayerChat(PlayerChatEvent e) {
	pluginMain.playerChat(e);
  }
  @Override public void onPlayerQuit(PlayerQuitEvent e) {
	pluginMain.playerQuit(e.getPlayer());
  }
}
