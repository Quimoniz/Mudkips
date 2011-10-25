package org.quimoniz.mudkips;

import org.bukkit.event.entity.EntityListener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.Event;
import org.bukkit.entity.Player;
import org.bukkit.entity.Entity;
import org.bukkit.ChatColor;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;

public class MEntityListener extends EntityListener {
  private Mudkips mudkips;
  public MEntityListener(Mudkips mudkips) {
    this.mudkips = mudkips;
  }
  public void onEntityDamage(EntityDamageEvent event) {
    if(event.getEntity() instanceof Player && event instanceof EntityDamageByEntityEvent) {
      Entity source = ((EntityDamageByEntityEvent) event).getDamager();
      if(source instanceof Player) {
        if(!mudkips.playerFight((Player) source, (Player) event.getEntity(), event.getDamage()))
          event.setCancelled(true);
      }
    }
  }
  public void onEntityDeath(EntityDeathEvent event) {
    if(event.getEntity() instanceof Player && event instanceof PlayerDeathEvent) {
      mudkips.playerDied((PlayerDeathEvent) event);
    }
  }
}
