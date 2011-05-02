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
    		long dayNumber = (currentWorld.getFullTime()/24000L);
    		//No Leap year! :]
    		long year = (long) (dayNumber/365);
    		int dayOfYear = (int) (dayNumber - (year*365));
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
    		String yearString = "";
    		if(dayOfMonth == 1)
    		  yearString += "first";
    		 else if(dayOfMonth == 2)
    		  yearString += "second";
    		 else if(dayOfMonth == 3)
    	      yearString += "third";
    		 else if(dayOfMonth > 3)
    		  yearString += dayOfMonth + "th";
    		yearString += " ";
    		yearString += monthNames[month];
    		yearString += " ";
    		if(year >= 1) {
    		  if(year == 1)
    			yearString += "of the second year";
    		  else if(year == 2)
    		    yearString += "of the third year";
    		  else
        	    yearString += year;
    		} else {
    		  yearString += "of the first year";
    		}
    	    for(Player currentPlayer : currentWorld.getPlayers()) {
    		  currentPlayer.sendMessage(ChatColor.LIGHT_PURPLE + "Welcome to " + yearString); 
    	    }
    	  }
    	  worldTimeList.put(currentWorld.getName(), new Long(newTime));
    	} else {
    	  worldTimeList.put(currentWorld.getName(), currentWorld.getTime());
    	}
      }
	}

}
