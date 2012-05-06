package org.quimoniz.mudkips.player;

import java.util.Arrays;
import java.util.Map.Entry;
import org.quimoniz.mudkips.util.ArrayMap;

public class PlayerStats {
  public int timesDied;
  public ArrayMap<String, Integer> mobsKilled;
  public PlayerStats() {
    mobsKilled = new ArrayMap(10, false);
    //just a sample...

    
  }
  private void addMob(String name, int count) {
    mobsKilled.put(name, new Integer(count));
  }
}
