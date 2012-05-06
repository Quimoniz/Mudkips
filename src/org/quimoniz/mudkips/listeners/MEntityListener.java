package org.quimoniz.mudkips.listeners;

import org.bukkit.event.Listener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.Event;
import org.bukkit.entity.Player;
import org.bukkit.entity.Entity;
import org.bukkit.ChatColor;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.quimoniz.mudkips.Mudkips;

public class MEntityListener implements Listener {
  private Mudkips mudkips;
  public MEntityListener(Mudkips mudkips) {
    this.mudkips = mudkips;
  }
  @EventHandler public void onEntityDamage(EntityDamageEvent event) {
    if(event.getEntity() instanceof Player && event instanceof EntityDamageByEntityEvent) {
      Entity source = ((EntityDamageByEntityEvent) event).getDamager();
      if(source instanceof Player) {
        if(!mudkips.playerFight((Player) source, (Player) event.getEntity(), event.getDamage()))
          event.setCancelled(true);
      }
    }
  }
  @EventHandler public void onEntityDeath(EntityDeathEvent event) {
    if(event.getEntity() instanceof Player && event instanceof PlayerDeathEvent) {
      mudkips.playerDied((PlayerDeathEvent) event);
    }
  }
}
