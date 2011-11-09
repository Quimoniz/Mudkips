package org.quimoniz.mudkips;

import java.util.HashMap;
import java.util.Iterator;
import java.util.logging.Level;

import org.bukkit.ChatColor;
import org.bukkit.Server;
import org.bukkit.entity.Player;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.TimeUnit;
import java.io.Closeable;

public class MudkipsPlayerProvider implements Runnable,Closeable {
  private HashMap<String, MudkipsPlayer> mapPlayers = new HashMap<String, MudkipsPlayer>();
  private DelayQueue<PlayerTask> playerTaskQueue = new DelayQueue<PlayerTask>();
  private Object playerTaskQueueChanged = new Object();
  private Server server;
  private Mudkips myPlugin;
  private Boolean running = false;
  private Thread threadObject = null;
  public MudkipsPlayerProvider(Server myServer, Mudkips main) {
    server = myServer;
    myPlugin = main;
  }

  public void playerJoin(Player p) {
    String playerName = p.getName();
    if(!mapPlayers.containsKey(playerName)) {
      mapPlayers.put(playerName, generatePlayer(p));
    } else {
      if(lookup(playerName).isClosed()) {
        mapPlayers.remove(playerName);
        mapPlayers.put(playerName, generatePlayer(p));
      } else {
        lookup(playerName).reconnect();
      }
    }
  }
  private MudkipsPlayer generatePlayer(Player p) {
//    logDebug("Generating Player \""+p.getName()+"\"");
    return new MudkipsPlayer(p, myPlugin, server);
  }
  public void playerQuit(String playerName) {
    MudkipsPlayer mPlayer = lookup(playerName);
    if(mPlayer != null) {
      final long DELAY_NANO = 30000000000l;
      addUniqueTask(new PlayerTask(mPlayer, PlayerTask.Type.REMOVAL, DELAY_NANO));
    }
  }
  private void addUniqueTask(PlayerTask task) {
    synchronized(playerTaskQueueChanged) {
      if(playerTaskQueue.contains(task)) {
        playerTaskQueue.remove(task);
//        logDebug("Removed PlayerTask.Type."+task.getType()+" from playerTaskQueue");
      }
      playerTaskQueue.offer(task);
//      logDebug(stackTrace() + "| Added PlayerTask.Type."+task.getType()+" to playerTaskQueue");
      start();
    }
  }
  public void playerJailed(String playerName, long jailDuration) {
    MudkipsPlayer mPlayer = get(playerName);
    if(mPlayer != null) {
      long jailDurationNanoseconds = jailDuration * 1000000L;
      addUniqueTask(new PlayerTask(mPlayer, PlayerTask.Type.UNJAIL, jailDurationNanoseconds));
    }
  }
  public MudkipsPlayer lookup(String playerName) {
    return mapPlayers.get(playerName);
  }
  public MudkipsPlayer get(String playerName) {
    MudkipsPlayer mPlayer = mapPlayers.get(playerName);
    if(mPlayer!= null && !mPlayer.isOnline()) {
      playerQuit(playerName);
    } else if(mPlayer == null || mPlayer.isClosed()) {
      Player pObj = server.getPlayer(playerName);
      if(pObj != null && pObj.isOnline()) {
        playerJoin(pObj);
        return mapPlayers.get(playerName);
      }
    }
    return mPlayer;
  }
  public MudkipsPlayer get(Player player) {
    return get(player.getName());
  }
  public boolean has(String playerName) {
    return mapPlayers.containsKey(playerName);
  }
  public MudkipsPlayer getByAlterName(String playerName) {
    String searchName = playerName.toLowerCase();
    Iterator<MudkipsPlayer> playerIterator = mapPlayers.values().iterator();
    int i = 0;
    while(playerIterator.hasNext()) {
      MudkipsPlayer mPlayer = playerIterator.next();
      if(!mPlayer.isOnline()) {
        playerQuit(mPlayer.getName());
        continue;
      }
      String alterName = mPlayer.getAlterName();
      if(alterName != null) {
        alterName = alterName.toLowerCase();
        if(alterName.indexOf(searchName)==0) {
          return mPlayer;
        }
      }
    }
    return null;
  }
  public boolean has(Player player) {
    return has(player.getName());
  }
  //Suggestion by winterschwert: Make it so, that abbreviation is only allowed for the beginning of the name, so that 'schwert' wouldnt trigger on player 'winterschwert', but 'winter' would
  public Player matchPlayer(String playerName) {
    String playerNameLC = playerName.toLowerCase();
    java.util.List<Player> playerList = server.matchPlayer(playerName);
    //TODO: add algorithm, move match Player to MudkipsPlayerProvider 
    if(playerList.size() > 0) {
      for(Player pMatch : playerList) {
        String matchLC = pMatch.getName().toLowerCase(); 
        if(matchLC.equals(playerNameLC) || (matchLC.indexOf(playerNameLC) == 0 && ((matchLC.length()*0.15) < playerName.length()) && (playerName.length() >= 2))) {
          return pMatch;
        }
      }
    }
    if(this.has(playerName)) {
      return this.get(playerName).getPlayer();
    } else {
      MudkipsPlayer mPlayer = this.getByAlterName(playerName);
      if(mPlayer != null) {
        return mPlayer.getPlayer();
      } else {
        return null;
      }
    }
  }
  public void start() {
    synchronized(running) {
      if(threadObject == null || running==false) {
        threadObject = new Thread(this);
        running = true;
//        logDebug("starting MukdipsPlayerProvider");
        threadObject.start();
      }
    }
  }
  public boolean isRunning() {
    boolean amIRunning;
    synchronized(running) {
      amIRunning = running;
    }
    return amIRunning;
  }
  @Override public void run() {
    while(true) {
      if(playerTaskQueue.size() > 0) {
        PlayerTask task = null;
        try {
//          logDebug("before \"playerTaskQueue.take()\"");
          task = playerTaskQueue.take();
        } catch(InterruptedException exc) {
          synchronized(running) {
            running = false;
            threadObject = null;
          }
          break;
        }
//        logDebug("after \"playerTaskQueue.take()\"");
        if(task != null) {
          MudkipsPlayer mPlayer = task.getPlayer();
          try {
            if(mPlayer != null) {
              switch(task.getType()) {
                case REMOVAL:
//                  logDebug("Processing PlayerTask.Type.REMOVAL");
                  if(!mPlayer.isOnline()) {
                    mPlayer.close();
                  } else {
//                    logDebug("Cant close mPlayer, player is online!");
                  }
                  if(mPlayer.isClosed()) {
                    mapPlayers.remove(task.getName());
//                    logDebug("Removing mPlayer.");
                  } else {
//                    logDebug("Cant remove mPlayer, not closed!");
                  }
                  break;
                case UNJAIL:
//                  logDebug("Processing PlayerTask.Type.UNJAIL");
                  if(mPlayer.isOnline())
                    mPlayer.unjail();
                  break;
              }
            } else {
//              logDebug("mPlayer is null!");
            }
          } catch(Exception exc) {
            this.myPlugin.errorHandler.logException(exc);
          }
        }
      }
      synchronized(running) {
        if(playerTaskQueue.size() < 1 || running == false) {
          running = false;
          threadObject = null;
//          logDebug("stopping MukdipsPlayerProvider");
          break;
        }
      }
      
      int sleepDurationMillis = 100;
      if(playerTaskQueue.size() > 50)
        sleepDurationMillis = 40;
      try {
        threadObject.sleep(sleepDurationMillis);
      } catch(InterruptedException exc) {
        synchronized(running) {
          running = false;
          threadObject = null;
        }
        break;
      }
    }
  }
  public String playerListing() {
    Player[] players = server.getOnlinePlayers();
    final String listPrefix = ChatColor.YELLOW + "Players online: " + ChatColor.WHITE; 
    StringBuilder playerList = null;
    int length = players.length*8 + listPrefix.length() + 7;
    try {
      playerList = new StringBuilder(length);
    } catch(OutOfMemoryError error) {
      server.getLogger().log(Level.SEVERE, "Failed to allocate " + length + " Bytes for PlayerListing: OutOfMemoryError");
      System.gc();
      playerList = new StringBuilder();
      //return null;
    }
    playerList.append(listPrefix);
    Iterator<MudkipsPlayer> playerIterator = mapPlayers.values().iterator();
    int i = 0;
    while(playerIterator.hasNext()) {
      MudkipsPlayer mPlayer = playerIterator.next();
      if(!mPlayer.isOnline()) {
        playerQuit(mPlayer.getName());
        continue;
      }
      if(i > 0)
        playerList.append(", ");
      if(mPlayer.isAfk())
        playerList.append(ChatColor.GRAY + mPlayer.displayName() + ChatColor.WHITE);
      else 
        playerList.append(mPlayer.displayName());
      i ++;
    }
    playerList.append(" (" + i + "/" + server.getMaxPlayers() + ")");
    return playerList.toString();
  }
  @Override public void close() {
    synchronized(running) {
      running = false;
      threadObject = null;
    }
    Iterator<MudkipsPlayer> playerIterator = mapPlayers.values().iterator();
    while(playerIterator.hasNext()) {
      playerIterator.next().close();
    }
    mapPlayers = null;
    playerTaskQueue = null;
    server = null;
    myPlugin = null;
  }
  public void fix(String playerName) {
    MudkipsPlayer mPlayer = this.mapPlayers.get(playerName);
    this.mapPlayers.remove(playerName);
    if(mPlayer != null) {
      mPlayer.printDebug();
      mPlayer.close();
    }
    Player p = this.server.getPlayer(playerName);
    MudkipsPlayer newMudkips = generatePlayer(p);
    this.mapPlayers.put(playerName, newMudkips);
  }
}
