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
    private Properties myProps = null;
    private HashMap<String, MudkipsPlayer> mapPlayers = new HashMap<String, MudkipsPlayer>();
    //private Set<String> aliasSet
    private SoftReference<Set<AliasEntry<String,String>>> cachedAliasSet;
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
    private int ACTION_PROPAGATION_DISTANCE;
	private String pathToProps = "mudkips.properties";
    @Override
	public void onDisable() {
		// TODO Auto-generated method stub
      saveProperties();
	}
	@Override
	public void onEnable() {
		// TODO Auto-generated method stub
	  File pluginFolder = this.getFile().getParentFile();
	  pathToProps = pluginFolder.getPath() + "/" + "mudkips.properties";
      try {
    	  myProps = new Properties();
    	  myProps.load(new FileInputStream(pathToProps));
      } catch(IOException exc) {
    	  this.getServer().getLogger().log(Level.SEVERE, "Mudkips couldn't load Properties file to be located at \"" + pathToProps + "\".");
      }
      WELCOME_MESSAGE = myProps.getProperty("motd", "Welcome %s");
      AFK_RETURN_MSG_DURATION = 1000 * 60 * 2;
      try {
        AFK_RETURN_MSG_DURATION = Integer.parseInt(myProps.getProperty("afk-return-message-duration", "" + (1000 * 60 *2)));
      } catch(NumberFormatException exc) {
    	getServer().getLogger().log(Level.WARNING,"Couldn't parse property \"afk-return-message-duration\" in " + pathToProps + " to int.");
      }
      AFK_RETURN_MSG = myProps.getProperty("afk-return-msg", "is back");
      HELP_MSG = myProps.getProperty("help", ChatColor.WHITE + "Mudkips Plugin Commands:\n"
    		                                                 + ChatColor.BLACK + "+ " + ChatColor.YELLOW + "/afk <message>\n  " + ChatColor.AQUA + "Toggles afk status\n"
    		                                                 + ChatColor.BLACK + "+ " + ChatColor.YELLOW + "/playerlist\n  " + ChatColor.AQUA + "Prints a list of players being online\n"
    		                                                 + ChatColor.BLACK + "+ " + ChatColor.YELLOW + "/motd\n  " + ChatColor.AQUA + "Prints out the Welcome Message\n"
    		                                                 + ChatColor.BLACK + "+ " + ChatColor.YELLOW + "/help\n  " + ChatColor.AQUA + "Prints out this help message\n"
                                                             );
      PLAYER_AFK_NOTIFICATION = myProps.getProperty("player-afk","You went afk, with message: ");
      PLAYER_BACK_NOTIFICATION = myProps.getProperty("player-back","Welcome back.");
//      AFK_CONCAT = myProps.getProperty("afk-concat","is");
      AFK_MESSAGE = myProps.getProperty("afk-message", ChatColor.YELLOW + "%s is %a (afk)");
      PRIVATE_CHAT_JOIN = myProps.getProperty("private-chat-join", ChatColor.YELLOW +  "Notice: You are in private chat with \"" + ChatColor.WHITE + "%s" + ChatColor.YELLOW + "\"");
      PRIVATE_CHAT_LEAVE = myProps.getProperty("private-chat-leave", ChatColor.YELLOW + "Notice: You left private chat with \"" + ChatColor.WHITE + "%s" + ChatColor.YELLOW + "\"");
      CHAT_MSG = myProps.getProperty("chat-message", ChatColor.WHITE + "> %s says \"" + "%m" + "\"" );
      ACTION_PROPAGATION_DISTANCE = 0;
      try {
    	  ACTION_PROPAGATION_DISTANCE = Integer.parseInt(myProps.getProperty("action-propagation-distance", "70"));
      } catch(NumberFormatException exc) {
    	getServer().getLogger().log(Level.WARNING,"Couldn't parse property \"action-propagation-distance\" in " + pathToProps + " to int.");
      }
      //Register Player events
      PluginManager pm = this.getServer().getPluginManager();
      MPlayerListener playerListener = new MPlayerListener(this);
      pm.registerEvent(Event.Type.PLAYER_CHAT, playerListener, Event.Priority.Normal, this);
      pm.registerEvent(Event.Type.PLAYER_LOGIN, playerListener, Event.Priority.Normal, this);
      pm.registerEvent(Event.Type.PLAYER_QUIT, playerListener, Event.Priority.Normal, this);
      
      saveProperties();
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
              pSender.sendMessage(ChatColor.RED + "You did not specifie an RP action.");
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
      }  
      return true;
    }
  public void saveProperties() {
	//ToDO: Fill this here out!
	myProps.setProperty("motd", WELCOME_MESSAGE);
	myProps.setProperty("afk-return-message-duration", AFK_RETURN_MSG_DURATION + "");
	myProps.setProperty("afk-return-msg", AFK_RETURN_MSG);
	myProps.setProperty("help", HELP_MSG);
	myProps.setProperty("player-afk", PLAYER_AFK_NOTIFICATION);
	myProps.setProperty("player-back", PLAYER_BACK_NOTIFICATION);
	myProps.setProperty("afk-message", AFK_MESSAGE);
	myProps.setProperty("private-chat-join", PRIVATE_CHAT_JOIN);
	myProps.setProperty("private-chat-leave", PRIVATE_CHAT_LEAVE);
	myProps.setProperty("chat-message", CHAT_MSG);
    myProps.setProperty("action-propagation-distance", ""+ACTION_PROPAGATION_DISTANCE);
	FileOutputStream outStream = null;
	try {
      outStream = new FileOutputStream(pathToProps,false);
	} catch(IOException exc) {
	  getServer().getLogger().log(Level.WARNING,"Failed to create temporary Properties file for saving Properties (" + pathToProps + "): " + exc.getMessage());
	  return;
	}
	if(outStream != null) {
	  try {
	    myProps.store(outStream, "");
	  } catch(IOException exc) {
	    getServer().getLogger().log(Level.WARNING,"Failed to write properties to temporary Properties File (" + pathToProps + "): " + exc.getMessage());
	    return;
	  }
	}
	try {
	  outStream.close();
	} catch(IOException exc) {
      getServer().getLogger().log(Level.WARNING,"Failed to close FileOutputStream (" + pathToProps + "): " + exc.getMessage());
	  return;
	}
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
	//Checking which player have been mentioned by some chatter, at the begin or end of a message
	while(iter.hasNext()) {
	  AliasEntry<String, String> alias=iter.next();
      msg = msg.toLowerCase();
	  if(msg.matches(regexStart[0] + alias.getKey().toLowerCase() + regexStart[1]) || msg.matches(regexStart[0] + alias.getValue().toLowerCase() + regexStart[1])
		|| msg.matches(regexEnd[0] + alias.getKey().toLowerCase() + regexEnd[1]) || msg.matches(regexEnd[0] + alias.getValue().toLowerCase()+ regexEnd[1])) {
	    MudkipsPlayer mP = mapPlayers.get(alias.getKey());
		if(mP != null)
		  playerMentioned(mP, p);  
	  }
		
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
	this.getServer().broadcastMessage(CHAT_MSG.replaceAll("%s",p.getDisplayName()).replaceAll("%m",e.getMessage()));
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
      afkNotification(mP, byPlayer);
	  
	}
  }
  public void afkNotification(MudkipsPlayer playerAfk, Player receiver) {
//	if(playerAfk.isAfk())
	  receiver.sendMessage(AFK_MESSAGE.replaceAll("%s", playerAfk.getName()).replaceAll("%a", playerAfk.afkMessage()));
	  
  }
  public MudkipsPlayer getMudkipsPlayer(String playerName) {
	return mapPlayers.get(playerName);
  }
  public void sendMotd(Player p) {
	MudkipsPlayer mPlayer = getMudkipsPlayer(p.getName());
	if(mPlayer == null)
	  p.sendMessage(WELCOME_MESSAGE.replaceAll("%s", p.getDisplayName()));
	else
	  p.sendMessage(WELCOME_MESSAGE.replaceAll("%s", mPlayer.getAlias()));
  }
  public void rpChat(Player p, String action) {
	MudkipsPlayer mP = mapPlayers.get(p.getName());
	String msg = null;
	if(mP != null && p.getDisplayName().equals(p.getName()))
	  msg = "* " + mP.getAlias() + " " + action;
	else
	  msg = "* " + p.getDisplayName() + " " + action;
	int[] locActor = {p.getLocation().getBlockX(),p.getLocation().getBlockY(),p.getLocation().getBlockZ()};
	for(Player pReceiver : this.getServer().getOnlinePlayers()) {
      int[] locReceiver = {pReceiver.getLocation().getBlockX(),pReceiver.getLocation().getBlockY(),pReceiver.getLocation().getBlockZ()};
      int distance = (int) Math.sqrt(Math.pow(Math.sqrt(Math.pow(locActor[0]-locReceiver[0],2) + Math.pow(locActor[2]-locReceiver[2],2)),2) + Math.pow(locActor[1]-locReceiver[1],2));
      if(distance < ACTION_PROPAGATION_DISTANCE)
    	pReceiver.sendMessage(msg);
	}
//	this.getServer().broadcastMessage(msg);
  }
  public void privateChat(Player sender, Player receiver, String message) {
	MudkipsPlayer mPlayerReceiver = mapPlayers.get(receiver.getName());
	if(mPlayerReceiver != null) {
	  if(mPlayerReceiver.isAfk())
	    afkNotification(mPlayerReceiver, sender);
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
}

