package org.quimoniz.mudkips;

import org.bukkit.ChatColor;

public class DefaultProperties {
  public static void writeDefaultProperties(PropertyManager props) {
    props.setIfNotSetProperty("motd", "Welcome %s");
    props.setIfNotSetProperty("afk-return-message-duration", "" + (1000 * 60 * 1));
    props.setIfNotSetProperty("afk-return-message", ChatColor.GRAY + "is back");
    props.setIfNotSetProperty("help", ChatColor.WHITE + "Mudkips Plugin Commands:\n"
    		                                                 + ChatColor.BLACK + "+ " + ChatColor.YELLOW + "/afk <message>\n  " + ChatColor.AQUA + "Toggles afk status\n"
    		                                                 + ChatColor.BLACK + "+ " + ChatColor.YELLOW + "/playerlist\n  " + ChatColor.AQUA + "Prints a list of players being online\n"
    		                                                 + ChatColor.BLACK + "+ " + ChatColor.YELLOW + "/motd\n  " + ChatColor.AQUA + "Prints out the Welcome Message\n"
    		                                                 + ChatColor.BLACK + "+ " + ChatColor.YELLOW + "/help\n  " + ChatColor.AQUA + "Prints out this help message\n"
                                                             );
    props.setIfNotSetProperty("player-afk","You went afk, with message: %m");
    props.setIfNotSetProperty("player-back","Welcome back.");
      //Uh, oh, someone with %a in his Name could fiddle in a message duplication
      //TODO: Write a method to escape %s %a %m stuff with 1 call
      //done ^_^
    props.setIfNotSetProperty("afk-message", ChatColor.YELLOW + "%s is %m (afk)");
    props.setIfNotSetProperty("private-chat-join", ChatColor.YELLOW +  "Notice: You are in private chat with \"" + ChatColor.WHITE + "%s" + ChatColor.YELLOW + "\"");
    props.setIfNotSetProperty("private-chat-leave", ChatColor.YELLOW + "Notice: You left private chat with \"" + ChatColor.WHITE + "%s" + ChatColor.YELLOW + "\"");
    props.setIfNotSetProperty("chat-message", ChatColor.WHITE + "> %s says \"" + "%m" + "\"" );
    props.setIfNotSetProperty("action-propagation-distance", "70");
    props.setIfNotSetProperty("chat-propagation-distance", "300");
    props.setIfNotSetProperty("whisper-propagation-distance", "10");
    props.setIfNotSetProperty("whisper-message", ChatColor.AQUA + "> %s whispers \"" + ChatColor.WHITE + "%m" + ChatColor.AQUA + "\"");
    props.setIfNotSetProperty("action-no-receiver", ChatColor.YELLOW + "Notice: No one was able to observe what you did.");
    props.setIfNotSetProperty("whisper-no-receiver", ChatColor.YELLOW + "Notice: No one took notice of your susurrus.");
    props.setIfNotSetProperty("distance-check-height", "true");
    props.setIfNotSetProperty("chat-no-receiver",ChatColor.YELLOW + "Notice: No one heard you.");
    props.setIfNotSetProperty("action-message", "* %s %m");
    props.setIfNotSetProperty("shout-message", ChatColor.WHITE + "> %s shouts \"" + "%m" + "\"" );
    props.setIfNotSetProperty("shout-no-receiver", ChatColor.YELLOW + "Notice: Even 'though you are shouting as loud as you are able to, no one hears you."); 
    props.setIfNotSetProperty("shout-propagation-distance", "0");
    props.setIfNotSetProperty("chat-server-name", "SERVER");
    
    props.setIfNotSetProperty("message-sender", ChatColor.GRAY + "[%s->%r]: %m");
    props.setIfNotSetProperty("message-receiver", ChatColor.GRAY + "[%s]: %m");

    props.setIfNotSetProperty("join-message", ChatColor.YELLOW + "%m (\"%s\")joined.");
    props.setIfNotSetProperty("quit-message", ChatColor.YELLOW + "%m (\"%s\")left.");
    props.setIfNotSetProperty("join-quit-propagation-distance", "0");
    props.setIfNotSetProperty("death-propagation-distance", "300");
    
    props.setIfNotSetProperty("chat-log-whisper", "true");
    props.setIfNotSetProperty("chat-log-say", "true");
    props.setIfNotSetProperty("chat-log-rpg", "true");
    props.setIfNotSetProperty("chat-log-shout", "true");
    props.setIfNotSetProperty("chat-log-announce", "true");
    props.setIfNotSetProperty("chat-log-private", "true");
    props.setIfNotSetProperty("chat-log-console", "true");
    
    props.setIfNotSetProperty("daily-date", "true");
    
    props.setIfNotSetProperty("teleport-bounding-rect", "-1000,-1000,2000,2000");
    props.setIfNotSetProperty("enable-warps", "true");
    props.setIfNotSetProperty("warps-file", "warps.prop");
    props.setIfNotSetProperty("enable-portals", "true");
    props.setIfNotSetProperty("portals-file", "portals.csv");
    props.setIfNotSetProperty("travel-with-equipment", "false");
    
    props.setIfNotSetProperty("use-permissions", "true");
    
    props.setIfNotSetProperty("protect-obsidian", "true");
    
    props.setIfNotSetProperty("pvp-enabled", "true");
    props.setIfNotSetProperty("pvp-cooldown", "300");
    
    props.setIfNotSetProperty("kick-cooldown", "60");
    props.setIfNotSetProperty("ban-cooldown", "300");
    props.setIfNotSetProperty("enable-bans", "true");
    props.setIfNotSetProperty("bans-file", "bans.properties");

    props.setIfNotSetProperty("enable-jail", "true");
    props.setIfNotSetProperty("protect-jail-break", "true");
    props.setIfNotSetProperty("protect-jail-place", "true");
    props.setIfNotSetProperty("protect-jail-pickup", "true");
    props.setIfNotSetProperty("protect-jail-interact", "23,25,54,58,61,62,64,71,96");
    
    // Default Burnables: 4   17   18   31   35   46   47   53   85   106
    props.setIfNotSetProperty("block-fire-placement", "");
    props.setIfNotSetProperty("block-burn", "0,1,2,3,4,6,7,8,9,10,11,12,13,14,15,16,19,20,21,22,23,24,25,26,27,28,29,30,32,33,34,36,37,38,39,40,41,42,43,44,45,48,49,50,51,52,54,55,56,57,58,59,60,61,62,63,64,65,66,67,68,69,70,71,72,73,74,75,76,77,78,79,80,81,82,83,84,86,87,88,89,90,91,92,93,94,95,96,97,98,99,100,101,102,103,104,105,107,108,109,110,111,112,113,114,115,116,117,118,119,120,121,122,123,124,125,126,127,128,129,130,131,132,133,134,135,136,137,138,139,140,141,142,143,144,145,146,147,148,149,150,151,152,153,154,155,156,157,158,159,160,161,162,163,164,165,166,167,168,169,170,171,172,173,174,175,176,177,178,179,180,181,182,183,184,185,186,187,188,189,190,191,192,193,194,195,196,197,198,199,200,201,202,203,204,205,206,207,208,209,210,211,212,213,214,215,216,217,218,219,220,221,222,223,224,225,226,227,228,229,230,231,232,233,234,235,236,237,238,239,240,241,242,243,244,245,246,247,248,249,250,251,252,253,254,255");
    
    props.setIfNotSetProperty("tall-grass-makes-grass-block","true");
    
    props.setIfNotSetProperty("set-defaults", "false");
  }

}
