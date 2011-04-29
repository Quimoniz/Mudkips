package org.quimoniz.mudkips;

import org.bukkit.entity.Player;

public class DelayedLoginEvent implements Runnable {
	private Mudkips mainPlugin;
	private Player p;
    public DelayedLoginEvent(Mudkips mainPlugin, Player p) {
      this.mainPlugin = mainPlugin;
      this.p = p;
    }
	@Override
	public void run() {
		// TODO Auto-generated method stub
	  Player currentPlayer = mainPlugin.getServer().getPlayer(p.getName());
	  if(currentPlayer != null)
		mainPlugin.delayedLoginHandle(currentPlayer);
	  else
        mainPlugin.delayedLoginHandle(p);
	}

}
