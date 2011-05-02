package org.quimoniz.mudkips;

import org.bukkit.entity.Player;
import org.bukkit.Server;

public class MudkipsPlayer {
  private String name = null;
  private Player pObject;
  private Server serverObject;
  private String alias;
  private boolean afk;
  private String afkMessage;
  private long lastAfkToggle = 0;
  private long initRealTime = 0;
  private long initTime = 0;
  private final String DEFAULT_AFK_MSG = "is afk";
  private Mudkips mudkipsMain;
  private String chatPartner = null;
  
  public MudkipsPlayer(Player p, Mudkips mudkipsMain, Server s) {
	name = p.getName();
	pObject = p;
	serverObject = s;
	this.mudkipsMain = mudkipsMain;
	initRealTime = System.currentTimeMillis();
	initTime = p.getWorld().getFullTime();
	buildAlias();
  }
  public Player getPlayer() {
	Player pReturn = null;
	pReturn =  serverObject.getPlayer(name);
	if(pReturn == null)
	  pReturn = pObject;
	return pReturn;
  }
  public String getAlias() {
	return alias;
  }
  public void buildAlias() {
	char c=0,lastC=0;
	int posSplit = -1;
	for(int i = 0; i < name.length(); i++) {
	  c = name.charAt(i);
	  if(i > 0)
		if((c >= 65 && c <= 90) && (lastC >= 97 && lastC <= 122)) { //switch from lowercase to uppercase
		  posSplit = i;
		  break;
		} else if((c >= 48 && c <= 57) && (lastC >=48 && lastC<=57)) {//two numbers in a row
		  posSplit = i - 1;
		  break;
		}
	  lastC = c;
	}
	if(posSplit < 2)
	  alias = name;
	else
	  alias = name.substring(0,posSplit);
  }
 public long toggleAfk() {
   return toggleAfk(null);
 }
 public long toggleAfk(String message) {
   long currentTime = System.currentTimeMillis();
   long lastAfkAction = lastAfkToggle;
   if(afk) {
	 afk = false;
	 afkMessage = null;
     lastAfkToggle = currentTime;
   } else {
	 afk = true;
	 if(message != null)
	   afkMessage = message;
	 else
	   afkMessage = DEFAULT_AFK_MSG;
	 lastAfkToggle = currentTime;
   }
   return currentTime - lastAfkAction;
 }
 public void setPrivateChatPartner(String chatPartner) {
   this.chatPartner = chatPartner;
 }
 public String getPrivateChatPartner() {
   return chatPartner;
 }
 public long getInitTime() {
   return initTime;
 }
 public long getInitRealTime() {
   return initRealTime;
 }
 public boolean isAfk() {
   return afk;
 }
 public String afkMessage() {//maybe implement set Method for changing afk message while being afk?
   return afkMessage;
 }
 public String getName() {
   return name;
 }
 public String toString() {
   return name;
 }
 public boolean inPrivateChat() {
   if(chatPartner == null)
	 return false;
   else
	 return true;
 }
}
