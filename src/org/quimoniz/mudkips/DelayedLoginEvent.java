package org.quimoniz.mudkips;

import org.bukkit.event.player.PlayerJoinEvent;

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
