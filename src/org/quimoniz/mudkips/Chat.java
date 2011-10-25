package org.quimoniz.mudkips;

import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Closeable;
import java.util.logging.Logger;
import org.quimoniz.mudkips.util.*;

public class Chat implements Closeable {
  public enum Type { UNSPECIFIED, MESSAGE, PERSIST, MESSAGE_PERSIST, RPG, WHISPER, SAY, SHOUT, ANNOUNCE }
  private Server serverReference;
  private PropertyManager mudkipsProps;
  private Logger logReference;
  private Mudkips pluginMain;
  private FileOutputStream chatLogFile;
  private boolean isClosed = false;
  public Chat(Server serverReference, PropertyManager props, Logger logger, FileOutputStream chatLogFile, Mudkips pluginMain) {
	serverReference = serverReference;
	mudkipsProps = props;
	logReference = logger;
	this.chatLogFile = chatLogFile;
	this.pluginMain = pluginMain;
  }
  private void checkServer(CommandSender reference) {
   if(serverReference == null) {
     serverReference = reference.getServer();
     if(serverReference == null) {
       logReference.log(java.util.logging.Level.SEVERE, "Mudkips Chat can't associate Server Instance!");
       throw new NullPointerException("Mudkips Chat can't associate Server Instance!");
     }
   }
  }
  public boolean consoleChat(CommandSender console, Type chatType, String[] message) {
	checkServer(console);
	if(chatType == Type.SHOUT || chatType == Type.ANNOUNCE || chatType == Type.SAY) {
      StringBuilder bufAnnounce = new StringBuilder();
      if(message.length > 0)
    	bufAnnounce.append(message[0]);
      for(int i = 1; i < message.length; i++) {
    	bufAnnounce.append(" ");  
        bufAnnounce.append(message[i]);
      }
      bufAnnounce.insert(0, ChatColor.LIGHT_PURPLE + "[" + mudkipsProps.getProperty("chat-server-name") + "] ");
      String msg = bufAnnounce.toString();
      for(Player p : serverReference.getOnlinePlayers()) {
    	playLightningSound(p);
        p.sendMessage(msg);
      }
      if(mudkipsProps.getBooleanProperty("chat-log-console"))
        logChat(msg , null);
      return true;
	} else if(chatType == Type.MESSAGE) {
      MudkipsPlayer mudkipsReceiver = pluginMain.getMudkipsPlayer(pluginMain.matchPlayer(message[0]).getName());
      if(mudkipsReceiver != null) {
    	String msg = StringUtil.replaceAll(mudkipsProps.getProperty("message-receiver"), new char[] {'s', 'r', 'm'}, new String[] {mudkipsProps.getProperty("chat-server-name"), mudkipsReceiver.displayName(), StringUtil.concatenate(message, " ", 0)});
    	mudkipsReceiver.sendMessage(msg);
        if(mudkipsProps.getBooleanProperty("chat-log-console"))
        logChat( msg, null);
        return true;
      } else {
    	Player receiver = pluginMain.matchPlayer(message[0]);
    	if(receiver != null) {
    	  String msg = StringUtil.replaceAll(mudkipsProps.getProperty("message-receiver"), new char[] {'s', 'r', 'm'}, new String[] {mudkipsProps.getProperty("chat-server-name"), receiver.getName(), StringUtil.concatenate(message, " ", 0)});
    	  receiver.sendMessage(msg);
    	  logChat( msg, null);
          return true;
    	} else {
    	  String msg = ChatColor.LIGHT_PURPLE + "[" + mudkipsProps.getProperty("chat-server-name") + " -> " + message[0] + "]: " + StringUtil.concatenate(message, " ", 0);
    	  serverReference.broadcastMessage(msg);
    	  logChat(msg, null);
          return true;
    	}
      }
	} else if(chatType == Type.RPG) {
	  String msg = ChatColor.LIGHT_PURPLE + StringUtil.replaceAll(mudkipsProps.getProperty("action-message"), new char[] {'s', 'm'}, new String[] {mudkipsProps.getProperty("chat-server-name"), StringUtil.concatenate(message, " ", 0)});
      serverReference.broadcastMessage(msg);
      logChat(msg, null);
      return true;
	} else if(chatType == Type.WHISPER) {
	  String msg = ChatColor.LIGHT_PURPLE + StringUtil.replaceAll(mudkipsProps.getProperty("whisper-message"), new char[] {'s', 'm'}, new String[] {mudkipsProps.getProperty("chat-server-name"), StringUtil.concatenate(message, " ", 0)});
	  serverReference.broadcastMessage(msg);
	  logChat(msg, null);
      return true;
	}  else {
	  return false;
	}
  }
  public boolean playerChat(Player sender, Type chatType, String[] message) {
	checkServer(sender);
	MudkipsPlayer mPlayer = pluginMain.getMudkipsPlayer(sender.getName());
    if(chatType == Type.UNSPECIFIED) {
      if(mPlayer.inPrivateChat()) {
        MudkipsPlayer mudkipsReceiver = pluginMain.getMudkipsPlayer(mPlayer.getPrivateChatPartner());
        if(mudkipsReceiver != null) {
          if(mudkipsReceiver.isAfk())
            mPlayer.sendMessage(afkNotification(mudkipsReceiver));
          String messageToSend = StringUtil.concatenate(message, " ", 0);
          boolean makeInvisible = messageToSend.startsWith("-i");
          String msgSender = StringUtil.replaceAll(mudkipsProps.getProperty("message-sender"), new char[] {'s', 'r', 'm'}, new String[] {mPlayer.displayName(), mudkipsReceiver.displayName(), messageToSend});
          if(!makeInvisible) {
            mPlayer.sendMessage(msgSender);
          }
          if(mudkipsProps.getBooleanProperty("chat-log-private"))
            logChat("[" + mPlayer.getName() + "]" + msgSender, sender.getLocation());
          mudkipsReceiver.sendMessage(StringUtil.replaceAll(mudkipsProps.getProperty("message-receiver"), new char[] {'s', 'r', 'm'}, new String[] {mPlayer.displayName(), mudkipsReceiver.displayName(), makeInvisible ? messageToSend.substring(2) : messageToSend}));
        }
      } else {
        chatType = Type.SAY;
      }
    }
    if(chatType == Type.MESSAGE) {
      //ToDo: Handle a possible NullPointerException in this following line
      Player pReceiver = pluginMain.matchPlayer(message[0]);
      if(pReceiver != null) {
        MudkipsPlayer mudkipsReceiver = pluginMain.getMudkipsPlayer(pluginMain.matchPlayer(message[0]).getName());
        if(mudkipsReceiver != null) {
          if(mudkipsReceiver.isAfk())
            mPlayer.sendMessage(afkNotification(mudkipsReceiver));
          String messageToSend = StringUtil.concatenate(message, " ", 1);
          boolean makeInvisible = messageToSend.startsWith("-i");
          String msgSender = StringUtil.replaceAll(mudkipsProps.getProperty("message-sender"), new char[] {'s', 'r', 'm'}, new String[] {mPlayer.displayName(), mudkipsReceiver.displayName(), messageToSend});
          if(!makeInvisible)
            mPlayer.sendMessage(msgSender);
          if(mudkipsProps.getBooleanProperty("chat-log-private"))
            logChat("[" + mPlayer.getName() + "]" + msgSender, sender.getLocation());
          mudkipsReceiver.sendMessage(StringUtil.replaceAll(mudkipsProps.getProperty("message-receiver"), new char[] {'s', 'r', 'm'}, new String[] {mPlayer.displayName(), mudkipsReceiver.displayName(), makeInvisible ? messageToSend.substring(2) : messageToSend}));
        }
      } else {
        if(message[0].equalsIgnoreCase(this.mudkipsProps.getProperty("chat-server-name"))) {
          System.out.println(StringUtil.replaceAll("private message by %s:%m", new char[] {'s','m'}, new String[] {mPlayer.getName(), StringUtil.concatenate(message, " ", 1)}));
        } else {
          mPlayer.sendMessage(ChatColor.YELLOW + "Could not associate a receiver.");
        }
      }
    } else if(chatType == Type.MESSAGE_PERSIST) {
        Player pReceiver = pluginMain.matchPlayer(message[0]);
        if(pReceiver != null) {
          MudkipsPlayer mudkipsReceiver = pluginMain.getMudkipsPlayer(pReceiver.getName());
          mPlayer.setPrivateChatPartner(mudkipsReceiver.getName());
          mPlayer.sendMessage(StringUtil.replaceAll(mudkipsProps.getProperty("private-chat-join"), new char[] {'s'}, new String[] {mudkipsReceiver.getName()}));
          if(message.length > 1) {
            if(mudkipsReceiver.isAfk())
                mPlayer.sendMessage(afkNotification(mudkipsReceiver));
            String messageToSend = StringUtil.concatenate(message, " ", 1);
            boolean makeInvisible = messageToSend.startsWith("-i");
            String msgSender = StringUtil.replaceAll(mudkipsProps.getProperty("message-sender"), new char[] {'s', 'r', 'm'}, new String[] {mPlayer.displayName(), mudkipsReceiver.displayName(), messageToSend});
            if(!makeInvisible)
              mPlayer.sendMessage(msgSender);
            if(mudkipsProps.getBooleanProperty("chat-log-private"))
              logChat("[" + mPlayer.getName() + "]" + msgSender, sender.getLocation());
            mudkipsReceiver.sendMessage(StringUtil.replaceAll(mudkipsProps.getProperty("message-receiver"), new char[] {'s', 'r', 'm'}, new String[] {mPlayer.displayName(), mudkipsReceiver.displayName(), makeInvisible ? messageToSend.substring(2) : messageToSend}));
          }
        } else {
          mPlayer.sendMessage(ChatColor.YELLOW + "Could not associate a receiver.");
        }
        //pSender.sendMessage(replaceAll(PRIVATE_CHAT_JOIN,'s', pReceiver.getName()));
    } else if(chatType == Type.PERSIST) {
        if(mPlayer.inPrivateChat()) {
          MudkipsPlayer mudkipsReceiver = pluginMain.getMudkipsPlayer(mPlayer.getPrivateChatPartner());
          if(mudkipsReceiver == null) {
            mPlayer.sendMessage(ChatColor.YELLOW + "Could not associate a receiver.");
            mPlayer.sendMessage(StringUtil.replaceAll(mudkipsProps.getProperty("private-chat-leave"),'s', "unknown"));
            return true;
          }
          if(mudkipsReceiver.isAfk())
              mPlayer.sendMessage(afkNotification(mudkipsReceiver));
          String messageToSend = StringUtil.concatenate(message, " ", 0);
          boolean makeInvisible = messageToSend.startsWith("-i");
          String msgSender = StringUtil.replaceAll(mudkipsProps.getProperty("message-sender"), new char[] {'s', 'r', 'm'}, new String[] {mPlayer.displayName(), mudkipsReceiver.displayName(), messageToSend});
          if(!makeInvisible)
            mPlayer.sendMessage(msgSender);
          if(mudkipsProps.getBooleanProperty("chat-log-private"))
            logChat("[" + mPlayer.getName() + "]" + msgSender, sender.getLocation());
          mudkipsReceiver.sendMessage(StringUtil.replaceAll(mudkipsProps.getProperty("message-receiver"), new char[] {'s', 'r', 'm'}, new String[] {mPlayer.displayName(), mudkipsReceiver.displayName(), makeInvisible ? messageToSend.substring(2) : messageToSend}));
    	}      
    } else if(chatType == Type.RPG) {
      String msg = StringUtil.replaceAll(mudkipsProps.getProperty("action-message"), new char[] {'s', 'm'}, new String[] {mPlayer.displayName(), StringUtil.concatenate(message, " ", 0)}); 
      if(sendMessageAround(msg, mPlayer.getLocation(), mudkipsProps.getIntProperty("action-propagation-distance")) <= 1)
        mPlayer.sendMessage(StringUtil.replaceAll(mudkipsProps.getProperty("action-no-receiver"), new char[] {'s', 'm'}, new String[] {mPlayer.displayName(), StringUtil.concatenate(message, " ", 0)}));
      if(mudkipsProps.getBooleanProperty("chat-log-rpg"))
        logChat("[" + mPlayer.getName() + "]" + msg, sender.getLocation());
    } else if(chatType == Type.WHISPER) {
      String msg = StringUtil.replaceAll(mudkipsProps.getProperty("whisper-message"), new char[] {'s', 'm'}, new String[] {mPlayer.displayName(), StringUtil.concatenate(message, " ", 0)});
      if(sendMessageAround(msg, mPlayer.getLocation(), mudkipsProps.getIntProperty("whisper-propagation-distance")) <= 1)
        mPlayer.sendMessage(StringUtil.replaceAll(mudkipsProps.getProperty("whisper-no-receiver"), new char[] {'s', 'm'}, new String[] {mPlayer.displayName(), StringUtil.concatenate(message, " ", 0)}));
      if(mudkipsProps.getBooleanProperty("chat-log-whisper"))
        logChat("[" + mPlayer.getName() + "]" + msg, sender.getLocation());
    } else if(chatType == Type.SAY) {
      String msg = StringUtil.replaceAll(mudkipsProps.getProperty("chat-message"), new char[] {'s', 'm'}, new String[] {mPlayer.displayName(), StringUtil.concatenate(message, " ", 0)});
      if(sendMessageAround(msg, mPlayer.getLocation(), mudkipsProps.getIntProperty("chat-propagation-distance")) <= 1)
        mPlayer.sendMessage(StringUtil.replaceAll(mudkipsProps.getProperty("chat-no-receiver"), new char[] {'s', 'm'}, new String[] {mPlayer.displayName(), StringUtil.concatenate(message, " ", 0)}));
      if(mudkipsProps.getBooleanProperty("chat-log-say"))
        logChat("[" + mPlayer.getName() + "]" + msg, sender.getLocation());
    } else if(chatType == Type.SHOUT) {
      String msg = StringUtil.replaceAll(mudkipsProps.getProperty("shout-message"), new char[] {'s', 'm'}, new String[] {mPlayer.displayName(), StringUtil.concatenate(message, " ", 0)});
      if(mPlayer.inSlappy())
        mPlayer.slap();
      if(sendMessageAround(msg, mPlayer.getLocation(), mudkipsProps.getIntProperty("shout-propagation-distance")) <= 1)
        mPlayer.sendMessage(StringUtil.replaceAll(mudkipsProps.getProperty("shout-no-receiver"), new char[] {'s', 'm'}, new String[] {mPlayer.displayName(), StringUtil.concatenate(message, " ", 0)}));
      if(mudkipsProps.getBooleanProperty("chat-log-shout"))
        logChat("[" + mPlayer.getName() + "]" + msg, sender.getLocation());
    } else if(chatType == Type.ANNOUNCE) {
      String msg = ChatColor.LIGHT_PURPLE + StringUtil.concatenate(message, " ", 0);
      for(Player p : serverReference.getOnlinePlayers()) {
      	playLightningSound(p);
        p.sendMessage(msg);
      }
      if(mudkipsProps.getBooleanProperty("chat-log-announce"))
        logChat("[" + mPlayer.getName() + "]" + msg, sender.getLocation());
    }
    return true;
  }
  public int notification(String note, Location loc) {
	return sendMessageAround(note, loc, mudkipsProps.getIntProperty("chat-propagation-distance"));  
  }
  public String afkNotification(MudkipsPlayer afkPlayer) {
    return StringUtil.replaceAll(mudkipsProps.getProperty("afk-message"), new char[] {'s', 'm'}, new String[] { afkPlayer.getName(), afkPlayer.afkMessage()});
  }
  public void playLightningSound(Player p) {
	net.minecraft.server.World mcWorld = ((net.minecraft.server.World)((org.bukkit.craftbukkit.CraftWorld)p.getWorld()).getHandle());
	net.minecraft.server.EntityPlayer mcPlayer = ((org.bukkit.craftbukkit.entity.CraftPlayer) p).getHandle();  
//	  mcWorld.makeSound(p.getLocation().getX(), p.getLocation().getY(), p.getLocation().getZ(), "ambient.weather.thunder", 10000.0F, (float)(0.8F + Math.random() * 0.2F));
    mcWorld.makeSound(mcPlayer, "ambient.weather.thunder", 10000.0F, (float)(0.8F + Math.random() * 0.2F));
  }
  public void logChat(String message, Location loc) {
	if(isClosed) return;
	if(loc != null)
	  message = "[" + loc.getBlockX() + "," + loc.getBlockY() + "," + loc.getBlockZ() + "]" + ChatColor.stripColor(message) + ((char)10);
	else
	  message = "[]" + ChatColor.stripColor(message) + ((char)10);
	try {
      chatLogFile.write(message.getBytes());
	} catch(IOException exc) {
	  logReference.log(java.util.logging.Level.SEVERE, "Exception \"" + exc.toString() + "\" during writing into chat.log");
	}
  }
  public void close() {
	boolean isClosed = true;
	try {
	  chatLogFile.close();
	} catch(IOException exc) {
	  pluginMain.errorHandler.logError(exc);
	}
  }
  public int sendMessageAround(String message, Location loc, double distance) throws RuntimeException {
	int i = 0;
	try {
      if(distance <= 0) {
	    for(Player pReceiver : loc.getWorld().getPlayers()) {
	      pReceiver.sendMessage(message);
		  i ++;
	    }  
	  } else {
	    for(Player pReceiver : loc.getWorld().getPlayers()) {
	      if(WorldUtil.calcDistance(loc, pReceiver.getLocation(), mudkipsProps.getBooleanProperty("distance-check-height")) < distance) {
	  	    pReceiver.sendMessage(message);
	        i ++;
	      }
	    }
	  }
	} catch(java.util.ConcurrentModificationException exc) {
	  pluginMain.errorHandler.logError(exc);
	  throw new RuntimeException("Playerlist changed while iterating players in 'sendMessageAround(String, Location, double)', that means chat message couldnt be delivered.");
	}
    return i;
  }
  public static String locTag(Location loc) {
	return "[" + loc.getBlockX() + "," + loc.getBlockY() + "," + loc.getBlockZ() + "]";  
  }
}