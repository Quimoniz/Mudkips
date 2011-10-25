package org.quimoniz.mudkips;

import java.util.List;
import java.util.Iterator;
import org.bukkit.World;
import org.bukkit.entity.Player;
import java.util.HashMap;
import org.bukkit.ChatColor;

public class TimeCheckerTask implements Runnable {
    private Mudkips pluginMain;
    private HashMap<String, Long> worldTimeList = null;
    private HashMap<String, String> lastDate = new HashMap<String, String>(); 
    public TimeCheckerTask(Mudkips main) {
      pluginMain = main;
    }
	@Override
	public void run() {
	  List<World> worlds = pluginMain.getServer().getWorlds();
      Iterator<World> worldIterator = worlds.iterator();
      if(worldTimeList == null)
    	worldTimeList = new HashMap<String, Long>();
      while(worldIterator.hasNext()) {
    	World currentWorld = worldIterator.next();
    	if(worldTimeList.containsKey(currentWorld.getName())) {
    	  Long oldTime = worldTimeList.get(currentWorld.getName()).longValue();
    	  long newTime = currentWorld.getTime();
    	  //System.out.println(oldTime.toString() + " | " + newTime + " | FullTime: " + currentWorld.getFullTime());
    	  if((newTime < 200) && (oldTime > newTime)) {
    		//TODO: Make a date function like config storable format, also see php's date function
    		String yearString = generateDate(currentWorld);
    	    for(Player currentPlayer : currentWorld.getPlayers()) {
    		  currentPlayer.sendMessage(ChatColor.LIGHT_PURPLE + "It's " + yearString); 
    	    }
    	  }
    	  worldTimeList.put(currentWorld.getName(), new Long(newTime));
    	} else {
    	  worldTimeList.put(currentWorld.getName(), currentWorld.getTime());
    	}
      }
	}
  public String generateDate(World w) {
    long dayNumber = (w.getFullTime()/24000L);
    //No Leap year! :]
    long year = (long) (dayNumber/365);
    int dayOfYear = (int) (dayNumber - (year*365));
    int weekday = (int)(((year * (365 % 7)) % 7) + dayOfYear) % 7;
    int month = -1;
    int dayOfMonth = -1;
    if(dayOfYear < 31) { month = 0; dayOfMonth = dayOfYear; }
      else if(dayOfYear < (31+28)) { month = 1; dayOfMonth = dayOfYear-31; }
      else if(dayOfYear < (31+28+31)) { month = 2; dayOfMonth = dayOfYear-(31+28); }
      else if(dayOfYear < (31+28+31+30)) { month = 3; dayOfMonth = dayOfYear-(31+28+31); }
      else if(dayOfYear < (31+28+31+30+31)) { month = 4; dayOfMonth = dayOfYear-(31+28+31+30); }
      else if(dayOfYear < (31+28+31+30+31+30)) { month = 5; dayOfMonth = dayOfYear-(31+28+31+30+31); }
      else if(dayOfYear < (31+28+31+30+31+30+31)) { month = 6; dayOfMonth = dayOfYear-(31+28+31+30+31+30); }
      else if(dayOfYear < (31+28+31+30+31+30+31+31)) { month = 7; dayOfMonth = dayOfYear-(31+28+31+30+31+30+31); }
      else if(dayOfYear < (31+28+31+30+31+30+31+31+30)) { month = 8; dayOfMonth = dayOfYear-(31+28+31+30+31+30+31+31); }
      else if(dayOfYear < (31+28+31+30+31+30+31+31+30+31)) { month = 9; dayOfMonth = dayOfYear-(31+28+31+30+31+30+31+31+30); }
      else if(dayOfYear < (31+28+31+30+31+30+31+31+30+31+30)) { month = 10; dayOfMonth = dayOfYear-(31+28+31+30+31+30+31+31+30+31); }
      else if(dayOfYear < (31+28+31+30+31+30+31+31+30+31+30+31)) { month = 11; dayOfMonth = dayOfYear-(31+28+31+30+31+30+31+31+30+31+30); }
       dayOfMonth ++;
    String [] monthNames = new String[] {"January","February","March","April","May","June","July","August","September","October","November","December"};
    String [] weekdayNames = new String[] {"Sunday","Monday","Tuesday","Wednesday","Thursday","Friday","Saturday"};
    StringBuilder yearString = new StringBuilder("");
    yearString.append( weekdayNames[weekday] + " the ");
    if(dayOfMonth == 1) {
      yearString.append( "first");
    }else if(dayOfMonth == 2) {
      yearString.append( "second");
     }else if(dayOfMonth == 3) {
         yearString.append( "third");
    }else if(dayOfMonth > 3) {
      yearString.append( dayOfMonth + "th");
    }
    yearString.append( " ");
    yearString.append( monthNames[month]);
    yearString.append( " ");
    if(year >= 1) {
      if(year == 1) {
        yearString.append( "of the second year AK");
      } else if(year == 2) {
        yearString.append( "of the third year AK");
      } else {
        yearString.append( "in the year ");
        yearString.append("" + year);
        yearString.append(" AK");
      }
    } else {
      yearString.append( "of the first year AK");
    }
    String rawString = yearString.toString();
    lastDate.put(w.getName(), rawString);
    return rawString;
  }
  public String getDate(World w) {
    if(!lastDate.containsKey(w.getName()))
      generateDate(w);
    return lastDate.get(w.getName());
  }
}
