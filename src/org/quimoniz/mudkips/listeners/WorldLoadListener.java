package org.quimoniz.mudkips.listeners;

import org.quimoniz.mudkips.Mudkips;
import org.bukkit.event.Listener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.world.WorldLoadEvent;

public class WorldLoadListener implements Listener {
  private Mudkips mudkips;
  public WorldLoadListener(Mudkips mudkips) {
    this.mudkips = mudkips;
  }
  @EventHandler public void onWorldLoad(WorldLoadEvent event) {
    mudkips.configManager.worldLoaded(event.getWorld());
  }
}
