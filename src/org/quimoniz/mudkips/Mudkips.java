package org.quimoniz.mudkips;

import org.bukkit.event.Event;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.event.Event.Type;
import org.bukkit.event.Event.Priority;
import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.command.CommandSender;
import org.bukkit.command.Command;
import org.bukkit.entity.Player;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;

import java.util.Properties;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.File;
import java.util.HashMap;
import java.util.Set;
import java.util.Iterator;
import java.lang.ref.SoftReference;

public class Mudkips extends JavaPlugin {
//    private Properties myProps = null;
    private PropertyManager myProps;
    private HashMap<String, MudkipsPlayer> mapPlayers = new HashMap<String, MudkipsPlayer>();
    //private Set<String> aliasSet
    private SoftReference<Set<AliasEntry<String,String>>> cachedAliasSet;
    //Every one of these semi-constants may be changed during runtime without causing any error nor exception nor buggy behavior
	private int AFK_RETURN_MSG_DURATION;
	private String AFK_RETURN_MSG;
	private String PLAYER_AFK_NOTIFICATION;
	private String PLAYER_BACK_NOTIFICATION;
    private String WELCOME_MESSAGE = null;
    private String HELP_MSG = null;
    private String AFK_MESSAGE;
    private String PRIVATE_CHAT_JOIN;
    private String PRIVATE_CHAT_LEAVE;
    private String CHAT_MSG;
    private String ACTION_MSG;
    private int ACTION_PROPAGATION_DISTANCE;
    private int CHAT_PROPAGATION_DISTANCE;
    private String CHAT_NO_RECEIVER;
    private int WHISPER_PROPAGATION_DISTANCE;
    private String WHISPER_MSG;
    private String ACTION_NO_RECEIVER;
    private boolean DISTANCE_CHECK_HEIGHT;
    private String WHISPER_NO_RECEIVER;
    private String SHOUT_MSG;
    private String SHOUT_NO_RECEIVER;
    private int SHOUT_PROPAGATION_DISTANCE;
    private String pathToProps = "mudkips.properties";
    @Override
	public void onDisable() {
      saveProperties();
	}
	@Override
	public void onEnable() {
	  File pluginFolder = this.getFile().getParentFile();
	  pathToProps = pluginFolder.getPath() + "/" + "mudkips.properties";
      myProps = new PropertyManager(pathToProps, this.getServer());
      WELCOME_MESSAGE = myProps.getProperty("motd", "Welcome %s");
      AFK_RETURN_MSG_DURATION = loadIntFromProperties("afk-return-message-duration",1000 * 60 * 1);
      AFK_RETURN_MSG = myProps.getProperty("afk-return-message", "is back");
      HELP_MSG = myProps.getProperty("help", ChatColor.WHITE + "Mudkips Plugin Commands:\n"
    		                                                 + ChatColor.BLACK + "+ " + ChatColor.YELLOW + "/afk <message>\n  " + ChatColor.AQUA + "Toggles afk status\n"
    		                                                 + ChatColor.BLACK + "+ " + ChatColor.YELLOW + "/playerlist\n  " + ChatColor.AQUA + "Prints a list of players being online\n"
    		                                                 + ChatColor.BLACK + "+ " + ChatColor.YELLOW + "/motd\n  " + ChatColor.AQUA + "Prints out the Welcome Message\n"
    		                                                 + ChatColor.BLACK + "+ " + ChatColor.YELLOW + "/help\n  " + ChatColor.AQUA + "Prints out this help message\n"
                                                             );
      PLAYER_AFK_NOTIFICATION = myProps.getProperty("player-afk","You went afk, with message: ");
      PLAYER_BACK_NOTIFICATION = myProps.getProperty("player-back","Welcome back.");
      //Uh, oh, someone with %a in his Name could fiddle in a message duplication
      //TODO: Write a method to escape %s %a %m stuff with 1 call
      AFK_MESSAGE = myProps.getProperty("afk-message", ChatColor.YELLOW + "%s is %a (afk)");
      PRIVATE_CHAT_JOIN = myProps.getProperty("private-chat-join", ChatColor.YELLOW +  "Notice: You are in private chat with \"" + ChatColor.WHITE + "%s" + ChatColor.YELLOW + "\"");
      PRIVATE_CHAT_LEAVE = myProps.getProperty("private-chat-leave", ChatColor.YELLOW + "Notice: You left private chat with \"" + ChatColor.WHITE + "%s" + ChatColor.YELLOW + "\"");
      CHAT_MSG = myProps.getProperty("chat-message", ChatColor.WHITE + "> %s says \"" + "%m" + "\"" );
      ACTION_PROPAGATION_DISTANCE = loadIntFromProperties("action-propagation-distance",70);
      CHAT_PROPAGATION_DISTANCE = loadIntFromProperties("chat-propagation-distance",300);
      WHISPER_PROPAGATION_DISTANCE = loadIntFromProperties("whisper-propagation-distance",10);
      WHISPER_MSG = myProps.getProperty("whisper-message", ChatColor.AQUA + "> %s whispers \"" + ChatColor.WHITE + "%m" + ChatColor.AQUA + "\"");
      ACTION_NO_RECEIVER = myProps.getProperty("action-no-receiver", ChatColor.YELLOW + "Notice: No one was able to observe what you did.");
      WHISPER_NO_RECEIVER = myProps.getProperty("whisper-no-receiver", ChatColor.YELLOW + "Notice: No one took notice of your susurrus.");
      DISTANCE_CHECK_HEIGHT = loadBoolFromProperties("distance-check-height",true);
      CHAT_NO_RECEIVER = myProps.getProperty("chat-no-receiver",ChatColor.YELLOW + "Notice: No one heard you.");
      ACTION_MSG = myProps.getProperty("action-message","* %s %m");
      SHOUT_MSG = myProps.getProperty("shout-message", ChatColor.WHITE + "> %s shouts \"" + "%m" + "\"" );
      SHOUT_NO_RECEIVER = myProps.getProperty("shout-no-receiver", ChatColor.YELLOW + "Notice: Even 'though you are shouting as loud as you are able to, no one hears you."); 
      SHOUT_PROPAGATION_DISTANCE = loadIntFromProperties("shout-propagation-distance", 0);
      //Register Player events
      PluginManager pm = this.getServer().getPluginManager();
      MPlayerListener playerListener = new MPlayerListener(this);
      pm.registerEvent(Event.Type.PLAYER_CHAT, playerListener, Event.Priority.Normal, this);
      pm.registerEvent(Event.Type.PLAYER_LOGIN, playerListener, Event.Priority.Normal, this);
      pm.registerEvent(Event.Type.PLAYER_QUIT, playerListener, Event.Priority.Normal, this);
      
      //parameters: first the Plugin.
      //            second the task
      //            third the delay until the task is run first
      //            fourth the delay between each following invocation
      this.getServer().getScheduler().scheduleSyncRepeatingTask(this, new TimeCheckerTask(this), 20, 100);
      
      saveProperties();
	}
	public int loadIntFromProperties(String propertyName, int defaultValue) {
	  int parsedInt = defaultValue;
	  try {
	    parsedInt = Integer.parseInt(myProps.getProperty(propertyName, ""+defaultValue));
	  } catch(NumberFormatException exc) {
	  	getServer().getLogger().log(Level.WARNING,"Couldn't parse property \""+propertyName+"\" in " + pathToProps + " to int.");
	  }
	  return parsedInt;
	}
	public boolean loadBoolFromProperties(String propertyName, boolean defaultValue) {
      String val = myProps.getProperty(propertyName, defaultValue?"true":"false").toLowerCase();
      if(val.equals("on") || val.equals("true") || val.equals("1") || val.equals("active") || val.equals("activated") || val.equals("yes") || val.equals("y")) {
        return true;
      } else if(val.equals("off") || val.equals("false") || val.equals("0") || val.equals("unactive") || val.equals("inactive") || val.equals("deactivated") || val.equals("no") || val.equals("n")) {
    	return false;
	  } else {
		getServer().getLogger().log(Level.WARNING,"Couldn't parse property \""+propertyName+"\" in " + pathToProps + " to boolean.");
		return defaultValue;
	  }
	}
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
      //firstof casting sender to a Player ...
      Player pSender = null;
      if(sender instanceof Player)
    	pSender = (Player) sender;
      //... and universalizing the command
      String rawCommand = command.getName().toLowerCase();
      if(rawCommand.charAt(0) == '/') rawCommand = rawCommand.substring(1, rawCommand.length());
      //Player listing, ignores any arguments
      if(rawCommand.indexOf("who") == 0 || rawCommand.indexOf("online") == 0 || rawCommand.indexOf("playerlist") == 0) {
        String playerListing = playerListing();
    	sender.sendMessage(playerListing);
      } else if(rawCommand.indexOf("afk") == 0) {
    	if(pSender != null)
    	  if(mapPlayers.containsKey(pSender.getName())) {
    		MudkipsPlayer mPlayer = mapPlayers.get(pSender.getName());
    		if(!mPlayer.isAfk()) {
    		  String afkMessage= concatenate(args, " ", 0);
    		  mPlayer.toggleAfk(afkMessage);
      		  sender.sendMessage(ChatColor.GRAY + PLAYER_AFK_NOTIFICATION + afkMessage);
    		} else {
    		  long afkTime = mPlayer.toggleAfk();
    		  if(afkTime > AFK_RETURN_MSG_DURATION) {
    			getServer().broadcastMessage(mPlayer.getName() + AFK_RETURN_MSG);
    		  } else {
    			  sender.sendMessage(ChatColor.GRAY + PLAYER_BACK_NOTIFICATION);
    		  }
    		}
    	  }
      } //The help command prints out HELP_MSG to the player
        else if(rawCommand.indexOf("help") == 0) {
          if(pSender != null) {
            sender.sendMessage(HELP_MSG);
          }
      }
        else if(rawCommand.indexOf("motd") == 0) {
          sendMotd((Player)sender);
      }
        else if(rawCommand.indexOf("me") == 0) {
          if(pSender != null) {
            if(args.length > 0)
              rpChat(pSender, concatenate(args, " ", 0));
            else
              pSender.sendMessage(ChatColor.RED + "You did not specify an RP action.");
          }
      }
        else if(rawCommand.indexOf("msg") == 0) {
          if(pSender != null) {
            if(args.length >= 2)
              privateChat(pSender.getName(), args[0], concatenate(args, " ", 1));
            else
       	      pSender.sendMessage(ChatColor.RED + "Not enough parameters, receiver and message required.");
          }
        	
      } else if(rawCommand.indexOf("pmsg") == 0) {
    	 if(pSender != null) {
   	       MudkipsPlayer mP = mapPlayers.get(pSender.getName());
    	   if(args.length > 0) {
    	     if(mP != null) {
    	       Player pReceiver = matchPlayer(args[0]);
    	       if(pReceiver != null) {
    	         mP.setPrivateChatPartner(pReceiver.getName());
    	         pSender.sendMessage(PRIVATE_CHAT_JOIN.replaceAll("%s", pReceiver.getName()));
    	         if(args.length >= 2)
    	           privateChat(pSender, pReceiver, concatenate(args, " ", 1));
    	       } else {
    	         pSender.sendMessage(ChatColor.RED + "Coudn't determine receiver \"" + ChatColor.WHITE + args[0] + ChatColor.RED + "\"");
    	       }
    	     } else {
    		   pSender.sendMessage(ChatColor.RED + "Unable to locate you in the playerlist. " + ChatColor.GRAY + "(maybe rejoining in will fix it)");
    		   if(args.length >= 2) {
    	         Player pReceiver = matchPlayer(args[0]);
    	         if(pReceiver != null) {
    	           privateChat(pSender, pReceiver, concatenate(args, " ", 1));
    	         } else {
    	           pSender.sendMessage(ChatColor.RED + "Coudn't determine receiver \"" + ChatColor.WHITE + args[0] + ChatColor.RED + "\"");
    	         }
    		   }
    	     }
    	   } else {
    	     if(mP != null) {
    		   if(mP.inPrivateChat()) {
    			 String chatPartner = mP.getPrivateChatPartner();
    			 if(chatPartner != null)
      	           pSender.sendMessage(PRIVATE_CHAT_LEAVE.replaceAll("%s", chatPartner));
    		     mP.setPrivateChatPartner(null);
    		   } else
    		     pSender.sendMessage(ChatColor.RED + "Can't do anything without playername nor message");
    	     } else
    		     pSender.sendMessage(ChatColor.RED + "Unable to locate you in the playerlist. " + ChatColor.GRAY + "(maybe rejoining in will fix it)");
    	   }
    	 }
      } else if(rawCommand.indexOf("weather") == 0) {
    	 if(sender.isOp()) { //pSender == null || pSender.isOp()
    	   //Just inform about the weather
           if(args.length == 0) {
        	 //It's a player
        	 if(pSender != null) {
        	   org.bukkit.World worldPlayerIsIn = pSender.getWorld();
        	   if(worldPlayerIsIn.hasStorm()) {
        		 pSender.sendMessage("It's a " + (worldPlayerIsIn.isThundering()?" harsh Thunder- ":"") + " Storm, which will probably last " + ((int)(worldPlayerIsIn.getWeatherDuration()/24000.00*100))/100.00 + " days");
        	   } else {
        		 pSender.sendMessage("It's shun-shine!!!!");
        	   }
        	 } else { //If invoked from Console. Console wants to know the weather for every world
        	   java.util.List <org.bukkit.World> worlds = this.getServer().getWorlds();
        	   String [] output = null;
        	   if(worlds.size() > 1) {
        	     output = new String[worlds.size()];
        	     int i = 0;
        	     for(org.bukkit.World currentWorld : worlds) {
        	       output [i] = currentWorld.getName() + ": ";
        	       if(currentWorld.hasStorm()) {
        	         output [i] += " Stormy for " + currentWorld.getWeatherDuration() + " Ticks";
        	         if(currentWorld.isThundering())
        	           output[i] += ", also Thundering for " + currentWorld.getThunderDuration() + " more Ticks";
        	       } else
        		     output[i] += " shun-shine!!!!";
        	     }
        	   } else {
        	     output = new String[1];
        	     org.bukkit.World theWorld=worlds.get(0);
      	         output [0] = theWorld.getName() + ": ";
      	         if(theWorld.hasStorm()) {
      	           output [0] += " Stormy for " + theWorld.getWeatherDuration() + " Ticks";
      	           if(theWorld.isThundering())
      	             output[0] += ", also Thundering for " + theWorld.getThunderDuration() + " more Ticks";
      	         } else
      		       output[0] += " shun-shine!!!!";
        	   }
        	   sender.sendMessage(this.concatenate(output, "\n", 0));
        	 }
           } else if(args.length >= 1) {//Changing the weather  
        	 
        	  
        	 //How about making param an array? so we can do like '/weather On thunder 2000' OR '/weather 2000 thunder Off'
        	 String param = null;
        	 String paramThunder = null;
        	 org.bukkit.World worldToChangeWeatherIn = null;
        	 //Separating the world Argument from the ON/OFF/Ticks Parameter
        	 if(args.length >= 2) {
        	   param = args[1].toLowerCase();
        	   if(! param.equalsIgnoreCase("thunder")) {
        	     if(args[0].equalsIgnoreCase("thunder")) {
              	   if(pSender != null) {
              	     worldToChangeWeatherIn = pSender.getWorld();
              	   } else {
              		 java.util.List <org.bukkit.World> listOfWorlds = this.getServer().getWorlds();
              		 if(listOfWorlds.size() == 1)
              	       worldToChangeWeatherIn = listOfWorlds.get(0);
              	   }
              	   if(worldToChangeWeatherIn != null)
              	     setThunder(worldToChangeWeatherIn, args[1], sender);
              	   else
              		 sender.sendMessage(ChatColor.RED + "Could not associate a World!"); 
        	     } else {
        	       worldToChangeWeatherIn = this.getServer().getWorld(args[0]);
        	     }
        	   } else {
          		 java.util.List <org.bukkit.World> listOfWorlds = this.getServer().getWorlds();
        		 if(listOfWorlds.size() == 1)
        	       worldToChangeWeatherIn = listOfWorlds.get(0);
        	   }
        	   if(param.equalsIgnoreCase("thunder")) {
        	     paramThunder = param = args[2].toLowerCase();
        	     param = args[0].toLowerCase();
        	   } else if(args.length >= 4) {
        		 if(args[2].equalsIgnoreCase("thunder"))
        		   paramThunder = args[3].toLowerCase();
        	   }
        	 } else {
        	   if(pSender != null) {
        	     worldToChangeWeatherIn = pSender.getWorld();
        	   } else {
        		 java.util.List <org.bukkit.World> listOfWorlds = this.getServer().getWorlds();
        		 if(listOfWorlds.size() == 1)
        	       worldToChangeWeatherIn = listOfWorlds.get(0);
        	   }
        	   param = args[0].toLowerCase();
        	 }
        	 if(worldToChangeWeatherIn != null) {
        	   setWeather(worldToChangeWeatherIn, param, paramThunder, sender);

        	 } else {
        	   sender.sendMessage(ChatColor.RED + "Could not associate a World!"); 
        	 }
            }
    	 } else {
    	   sender.sendMessage(ChatColor.YELLOW + "Notice: You are not authorized.");
    	 }
      }
       else if(rawCommand.equalsIgnoreCase("w")) {
          if(pSender != null) {
            if(args.length > 0)
              whisperChat(pSender, concatenate(args, " ", 0));
             else
              pSender.sendMessage(ChatColor.RED + "You did not whisper anything.");
           } else {
        	 sender.sendMessage("It seems, that Gods are incapable of whispering.");
           }
        }
       else if(rawCommand.indexOf("shout") == 0 || rawCommand.equalsIgnoreCase("s")) {
           if(pSender != null) {
             if(args.length > 0)
               shoutChat(pSender, concatenate(args, " ", 0));
              else
               pSender.sendMessage(ChatColor.RED + "You did not shout anything.");
            } else {
             //Maybe instead we could make a lightning strike right before the eyes of everyone, and announce the message
             //even better, why not using Server.makeSound ( "weather.thunder") somethin?
         	 sender.sendMessage("Gods dont need to shout.");
            }
        }
       else if(rawCommand.equals("info")) {
    	 String playerName = null;
    	 if(args.length < 1) {
    	   if(pSender != null)
    		 playerName = pSender.getName();
    	   else {
    		 sender.sendMessage("Cant display any information, parameter required.");
    		 return false;
    	   }
    	 } else {
    	   Player p = matchPlayer(args[0]);
    	   if(p != null)
    	     playerName = p.getName();
    	 }
    	 MudkipsPlayer mPlayer = mapPlayers.get(playerName);
    	 if(mPlayer != null) {
    	   StringBuilder buf = new StringBuilder();
    	   //mPlayer.afkMessage()
    	   buf.append(mPlayer.getName() + ": \n");
    	   if(mPlayer.isAfk())
    	     buf.append(this.afkNotification(mPlayer) + "\n" + ChatColor.WHITE);
    	   if(pSender != null && !pSender.getName().equals(mPlayer.getName())) {
    	     if(calcDistance(pSender.getLocation(), mPlayer.getPlayer().getLocation(), true) < CHAT_PROPAGATION_DISTANCE)
    		   buf.append("Is somewhere around you\n");
    	     else
    		   buf.append("Is far away\n");
    	   }
    	   double duration = (mPlayer.getPlayer().getWorld().getFullTime()-mPlayer.getInitTime())/24000.00;
    	   duration *= 100;
    	   duration = (int) duration;
    	   duration /= 100;
    	   if(duration < 0.02)
    	     buf.append("Just logged in");
    	   else if(duration >= 0.45 && duration <= 0.55)
    	     buf.append("Logged in half a day ago.\n");
    	   else
    	     buf.append("Logged in " + duration + " days ago.\n");
    	   sender.sendMessage(buf.toString());
    	 }
       }
       else if(rawCommand.equals("say")) {
    	 if(pSender != null)
    	   sayChat(pSender, concatenate(args, " ", 0));
       }
      return true;
    }
  public void saveProperties() {
	//ToDO: Fill this here out!
	myProps.save();
  }
  public void playerJoin(Player p) {
	this.getServer().getScheduler().scheduleAsyncDelayedTask(this, new DelayedLoginEvent(this, p), 1);
//	aliasMap.put(mP.getAlias(), p.getName());
  }
  public void delayedLoginHandle(Player p) {
	  MudkipsPlayer mPlayer = new MudkipsPlayer(p, this, getServer());
	  mapPlayers.put(p.getName(), mPlayer);
	  sendMotd(p);
  }
  public void playerQuit(Player p) {
	String playerName = p.getName();
	MudkipsPlayer mP= mapPlayers.remove(playerName);
//	if(mP != null)
//	  aliasMap.remove(mP.getAlias());
  }
  public void playerChat(PlayerChatEvent e) {
	Player p = e.getPlayer();
	String msg = e.getMessage();
	try {
	  Set<AliasEntry<String,String>> aliasSet = null;
	  if(cachedAliasSet != null)
		  aliasSet = cachedAliasSet.get();
	  if(aliasSet == null || mapPlayers.size() != aliasSet.size()) {
		  //Creating a new HashSet with Name -> Alias entries (stored in Class AliasEntry<String,String>)
		  java.util.HashSet<AliasEntry<String, String>> newSet = new java.util.HashSet<AliasEntry<String, String>>(mapPlayers.size());
		  Iterator<MudkipsPlayer> iter = mapPlayers.values().iterator();
		  while(iter.hasNext()) {
			  MudkipsPlayer mPlayer = iter.next();
			  AliasEntry<String,String> entry = new AliasEntry<String,String>(mPlayer.getName(),mPlayer.getAlias());
			  if(newSet.contains(entry))
				    entry = new AliasEntry<String,String>(mPlayer.getName(),mPlayer.getName());
			  if(newSet.contains(entry))
				    getServer().getLogger().log(Level.INFO, "Failed to register an Alias for user \"" + mPlayer.getName() + "\". Entry is in HashSet already.");
			  else
				   newSet.add(entry);
			  }
		  aliasSet = newSet;
		  cachedAliasSet = new SoftReference<Set<AliasEntry<String,String>>>(aliasSet);
		  }
	
	  Iterator<AliasEntry<String,String>> iter = aliasSet.iterator();
	  String[] regexStart = new String[]{"^","( |,|:)?.*"};
	  String[] regexEnd = new String[]{".*( |,|:)?","$"};
	  //Checking which player has been mentioned by some chatter, at the begin or end of a message
	  while(iter.hasNext()) {
		  AliasEntry<String, String> alias=iter.next();
		  String msgLowerCase = msg.toLowerCase();
		  if(msgLowerCase.matches(regexStart[0] + alias.getKey().toLowerCase() + regexStart[1]) || msgLowerCase.matches(regexStart[0] + alias.getValue().toLowerCase() + regexStart[1])
				  || msgLowerCase.matches(regexEnd[0] + alias.getKey().toLowerCase() + regexEnd[1]) || msgLowerCase.matches(regexEnd[0] + alias.getValue().toLowerCase()+ regexEnd[1])) {
			  MudkipsPlayer mP = mapPlayers.get(alias.getKey());
			  if(mP != null)
				  playerMentioned(mP, p);  
			  }
		
		  }
	} catch(Exception exc) {
		//No exception handling here
		getServer().getLogger().log(Level.INFO, "Exception during checking of aliases: " + exc.toString());
	}
   MudkipsPlayer mP = mapPlayers.get(p.getName());
   if( mP != null)
	 if(mP.inPrivateChat()) {
	   String chatPartnerName = mP.getPrivateChatPartner();
	   Player chatPartner = this.getServer().getPlayer(chatPartnerName);
	   if(chatPartner != null) {
	     privateChat(p, chatPartner, msg);
	     e.setCancelled(true);
	   } else {
		 mP.setPrivateChatPartner(null);
	   }
	 }
  if(!e.isCancelled()) {
	sayChat(p, e.getMessage());
    e.setCancelled(true);
  }
  }
  public String concatenate(String[] concat, String delim, int offset) {
	StringBuilder concatenateBuf = new StringBuilder(concat.length*4);
	int i = 0;
	for(int j = offset; j < concat.length; j++) {
      if(i > 0)
        concatenateBuf.append(delim + concat[j]);
      else concatenateBuf.append(concat[j]);
	  i ++;
	}
	return concatenateBuf.toString();
  }
  public double calcDistance(Location locA, Location locB, boolean checkHeight) {
	double distance = 0;
	double deltaX = locA.getBlockX()-locB.getBlockX(), deltaY = locA.getBlockY()-locB.getBlockY(), deltaZ = locA.getBlockZ()-locB.getBlockZ();
	if(checkHeight) // Euclidean distance
	  distance = Math.sqrt(Math.pow(deltaX,2) + Math.pow(deltaY,2) + Math.pow(deltaZ,2));
	else // Pythagorean theorem
		distance = Math.sqrt(deltaX*deltaX + deltaZ*deltaZ);
	return distance;
  }
  public int sendMessageAround(String message, Location loc, double distance) {
	int i = 0;
    if(distance <= 0) {
	  for(Player pReceiver : loc.getWorld().getPlayers()) {
	    pReceiver.sendMessage(message);
		i ++;
	  }  
	} else {
	  for(Player pReceiver : loc.getWorld().getPlayers()) {
	    if(calcDistance(loc, pReceiver.getLocation(), DISTANCE_CHECK_HEIGHT) < distance) {
	  	  pReceiver.sendMessage(message);
	      i ++;
	    }
	  }
	}
    return i;
  }
  public String playerListing() {
  	Player[] players = this.getServer().getOnlinePlayers();
	final String listPrefix = ChatColor.YELLOW + "Players online: "; 
	StringBuilder playerList = null;
	int length = players.length*8 + listPrefix.length() + 7;
	try {
      playerList = new StringBuilder(length);
	} catch(OutOfMemoryError error) {
      this.getServer().getLogger().log(Level.SEVERE, "Failed to allocate " + length + " Bytes for PlayerListing: OutOfMemoryError");
	  System.gc();
	  playerList = new StringBuilder();
      return null;
	}
	playerList.append(listPrefix);
	Iterator<MudkipsPlayer> playerIterator = mapPlayers.values().iterator();
	int i = 0;
	while(playerIterator.hasNext()) {
	  MudkipsPlayer mPlayer = playerIterator.next();
	  if(i > 0)
		playerList.append(", ");
	  if(mPlayer.isAfk())
	    playerList.append(ChatColor.GRAY + mPlayer.getName() + ChatColor.WHITE);
	  else 
		playerList.append(mPlayer.getName());
	  i ++;
	}
	playerList.append(" (" + mapPlayers.size() + "/" + getServer().getMaxPlayers() + ")");
	return playerList.toString();
  }
  public void playerMentioned(MudkipsPlayer mP, Player byPlayer) {
	if(mP.isAfk()) {
	  byPlayer.sendMessage(afkNotification(mP));
	}
  }
  public String afkNotification(MudkipsPlayer playerAfk) {
//	if(playerAfk.isAfk())
	 return AFK_MESSAGE.replaceAll("%s", playerAfk.getName()).replaceAll("%a", playerAfk.afkMessage());
	  
  }
  public MudkipsPlayer getMudkipsPlayer(String playerName) {
	return mapPlayers.get(playerName);
  }
  public void sendMotd(Player p) {
	try {
	  MudkipsPlayer mPlayer = getMudkipsPlayer(p.getName());
	  if(mPlayer == null)
	    p.sendMessage(WELCOME_MESSAGE.replaceAll("%s", p.getDisplayName()));
	  else
	    p.sendMessage(WELCOME_MESSAGE.replaceAll("%s", mPlayer.getAlias()));
	} catch(NullPointerException exc) {
	  this.getServer().getLogger().log(Level.WARNING, "NullPointerException while sending MOTD!");
	}
  }
  public void sayChat(Player p, String msg) {
//    if(sendMessageAround(CHAT_MSG.replaceAll("%s",p.getDisplayName()).replaceAll("%m",msg), p.getLocation(), CHAT_PROPAGATION_DISTANCE) <= 1)
//	  p.sendMessage(this.CHAT_NO_RECEIVER.replaceAll("%s",p.getDisplayName()));
    if(sendMessageAround(replaceAll(CHAT_MSG, new char[]{'s','m'}, new String[] {p.getDisplayName(), msg}), p.getLocation(), CHAT_PROPAGATION_DISTANCE) <= 1)
	  p.sendMessage(replaceAll(CHAT_NO_RECEIVER, new char[] {'s'}, new String[] {p.getDisplayName()}));
  }
  public void rpChat(Player p, String action) {
	MudkipsPlayer mP = mapPlayers.get(p.getName());
	String msg = null;
	if(mP != null && p.getDisplayName().equals(p.getName()))
	  msg = ACTION_MSG.replaceAll("%s",mP.getAlias()).replaceAll("%m",action);
	else
	  msg = ACTION_MSG.replaceAll("%s",p.getDisplayName()).replaceAll("%m",action);
	if(sendMessageAround(msg, p.getLocation(), ACTION_PROPAGATION_DISTANCE) <= 1)
	  p.sendMessage(ACTION_NO_RECEIVER.replaceAll("%s", mP.getAlias()));
  }
  //Copy and Pasted the Code from rpChat, bad habbit :-(
  public void whisperChat(Player sender, String message) {
    MudkipsPlayer mPlayer = mapPlayers.get(sender.getName());
	String msg = null;
	if(mPlayer != null && sender.getDisplayName().equals(sender.getName()))
	  msg = WHISPER_MSG.replaceAll("%s", mPlayer.getAlias()).replaceAll("%m", message);
	else
	  msg = WHISPER_MSG.replaceAll("%s", sender.getDisplayName()).replaceAll("%m", message);

	if(sendMessageAround(msg, sender.getLocation(), WHISPER_PROPAGATION_DISTANCE) <= 1)
	  sender.sendMessage(WHISPER_NO_RECEIVER.replaceAll("%s", mPlayer.getAlias()));
  }
  public void shoutChat(Player sender, String message) {
	MudkipsPlayer mPlayer = mapPlayers.get(sender.getName());
	String msg = null;
	if(mPlayer != null && sender.getDisplayName().equals(sender.getName()))
	  msg = SHOUT_MSG.replaceAll("%s", mPlayer.getAlias()).replaceAll("%m", message);
	else
	  msg = SHOUT_MSG.replaceAll("%s", sender.getDisplayName()).replaceAll("%m", message);
	
	if(sendMessageAround(msg, sender.getLocation(), SHOUT_PROPAGATION_DISTANCE) <= 1)
      sender.sendMessage(SHOUT_NO_RECEIVER.replaceAll("%s", mPlayer.getAlias()));
	}
  public void privateChat(Player sender, Player receiver, String message) {
	MudkipsPlayer mPlayerReceiver = mapPlayers.get(receiver.getName());
	if(mPlayerReceiver != null) {
	  if(mPlayerReceiver.isAfk())
		  sender.sendMessage(afkNotification(mPlayerReceiver));
	}
	boolean makeInvisible = message.startsWith("-i");  
	if(!makeInvisible)
	  sender.sendMessage(ChatColor.GRAY + "[" + sender.getDisplayName() + "->" + receiver.getDisplayName() + "]: " + message);
    receiver.sendMessage(ChatColor.GRAY + "[" + sender.getDisplayName() + "]: " + (makeInvisible?message.substring(2):message));
  }
  public void privateChat(String sender, String receiver, String message) {
	Player pSender = null, pReceiver = null;
	pSender = this.getServer().getPlayer(sender);
	pReceiver = matchPlayer(receiver);
	if(pReceiver != null) {
	  privateChat(pSender, pReceiver, message);
	}
  }
  public Player matchPlayer(String playerName) {
	java.util.List<Player> playerList = this.getServer().matchPlayer(playerName);
	if(playerList.size() > 0) {
	  if(playerList.size() > 1) {
		  for(Player pMatch : playerList)
			if(pMatch.getName().indexOf(playerName) >= 0)
			  return pMatch;
		  return playerList.get(0);
	  } else
	      return playerList.get(0);
  } else
	  if(mapPlayers.containsKey(playerName))
		return mapPlayers.get(playerName).getPlayer();
	  else
        return null;
  }
  public boolean setWeather(World worldToChangeWeatherIn, String param, String paramThunder, CommandSender sender) {
	Player pSender = null;
	if(sender instanceof Player)
	  pSender = (Player) sender;
	 //Activate Weather
    if(param.equals("on") || param.equals("true") || param.equals("active") || param.equals("activated") || param.equals("yes") || param.equals("y")) {
	  if(!worldToChangeWeatherIn.hasStorm()) {
	    sender.sendMessage(ChatColor.YELLOW + "Notice: It's storming now.");
   	    this.getServer().getLogger().log(Level.INFO, "Storm activated" + (pSender!=null?(" by " + pSender.getName()):" from Console"));
     	worldToChangeWeatherIn.setStorm(true);
	  }
   	  if(paramThunder != null) {
   		 setThunder(worldToChangeWeatherIn, param, sender);
   	   }
     return true;
      //Deactivate Weather
   } else if(param.equals("off") || param.equals("false") || param.equals("0") || param.equals("unactive") || param.equals("deactivated") || param.equals("no") || param.equals("n")) {
	 if(worldToChangeWeatherIn.hasStorm()) {
       sender.sendMessage(ChatColor.YELLOW + "Notice: It stopped storming now.");
       this.getServer().getLogger().log(Level.INFO, "Storm deactivated" + (pSender!=null?(" by " + pSender.getName()):" from Console"));
       worldToChangeWeatherIn.setStorm(false);
	 }
   	  if(paramThunder != null) {
        setThunder(worldToChangeWeatherIn, param, sender);
      }
      return true;
      //Specifically set the weather duration 
   } else {
   	 int stormTicks = 0;
   	 try {
   	   stormTicks = Integer.parseInt(param);
   	 } catch(NumberFormatException exc) {
   		 sender.sendMessage("Failed to parse the parameter Ticks");
   	     return false; 
   	 }
	 if(stormTicks > 0) {
	   worldToChangeWeatherIn.setStorm(true);
	   worldToChangeWeatherIn.setWeatherDuration(stormTicks);
	   this.getServer().getLogger().log(Level.INFO, "Storm set to " + stormTicks + " Ticks" + (pSender!=null?(" by " + pSender.getName()):" from Console"));
	 } else {
	   if(worldToChangeWeatherIn.hasStorm()) {
	     worldToChangeWeatherIn.setStorm(false);
	   	 sender.sendMessage(ChatColor.YELLOW + "Notice: It stopped storming now.");  
	   }
	 }
  	if(paramThunder != null) {
      setThunder(worldToChangeWeatherIn, param, sender);
    }
    return true;
   }
  }
  public void setThunder(World worldToSetThunderIn, String param, CommandSender sender) {
	Player pSender = null;
	if(sender instanceof Player)
	  pSender = (Player) sender;
	if(param.equals("on") || param.equals("true") || param.equals("active") || param.equals("activated") || param.equals("yes") || param.equals("y")) {
	  if(!worldToSetThunderIn.hasStorm()) {
	    sender.sendMessage(ChatColor.YELLOW + "Notice: It's thundering now.");
	    this.getServer().getLogger().log(Level.INFO, "Thunder activated" + (pSender!=null?(" by " + pSender.getName()):" from Console"));
		worldToSetThunderIn.setThundering(true);
	  }
	} else if(param.equals("off") || param.equals("false") || param.equals("0") || param.equals("unactive") || param.equals("deactivated") || param.equals("no") || param.equals("n")) {
      if(worldToSetThunderIn.isThundering()) {
		sender.sendMessage(ChatColor.YELLOW + "Notice: It stopped Thundering now.");
		this.getServer().getLogger().log(Level.INFO, "Thundering deactivated" + (pSender!=null?(" by " + pSender.getName()):" from Console"));
		worldToSetThunderIn.setThundering(false);
      }
   } else {
	 int thunderTicks = 0;
   	 try {
   	   thunderTicks = Integer.parseInt(param);
   	 } catch(NumberFormatException exc) {
   		 sender.sendMessage("Failed to parse the parameter Ticks");
   	     return; 
   	 }
	 if(thunderTicks > 0) {
	   worldToSetThunderIn.setThundering(true);
	   worldToSetThunderIn.setThunderDuration(thunderTicks);
	   this.getServer().getLogger().log(Level.INFO, "Thunder set to " + thunderTicks + " Ticks" + (pSender!=null?(" by " + pSender.getName()):" from Console"));
	 } else {
	   if(worldToSetThunderIn.hasStorm()) {
	     worldToSetThunderIn.setStorm(false);
	   	 sender.sendMessage(ChatColor.YELLOW + "Notice: It stopped thundering now.");  
	   }
	  }
    }
  }
  /*
   * @param: origin the string wherein to replace, replacer where to replace %<char> tags, replacement the stuff that will replace it
   */
  public String replaceAll(String origin, char [] replacer, String [] replacements) {
	StringBuilder buf = new StringBuilder();
	//modReplace: %modulus + char
	boolean modReplace = false;
	for(int i = 0; i < origin.length(); i++) {
	  char c = origin.charAt(i);
	  if(modReplace) {
		int j = 0;
		for(; j < replacer.length; j++)
		  if(replacer[j] == c)
		    break;
		if(j == replacer.length && j < replacements.length) {
		  buf.append("%"+c);
		} else {
		  buf.append(replacements[j]);
		}
		modReplace = false;
	  } else if(c == '%') {
		modReplace = true;
	  } else {
	    buf.append(c);
	  }
	}
   return buf.toString();
  }
}

