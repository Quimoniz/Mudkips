package org.quimoniz.mudkips.listeners;

import org.bukkit.event.player.PlayerJoinEvent;
import org.quimoniz.mudkips.Mudkips;

public class DelayedLoginEvent implements Runnable {
	private Mudkips mainPlugin;
	private PlayerJoinEvent event;
    public DelayedLoginEvent(Mudkips mainPlugin, PlayerJoinEvent event) {
      this.mainPlugin = mainPlugin;
      this.event = event;
    }
	@Override
	public void run() {
      mainPlugin.delayedLoginHandle(event);
	}

}
