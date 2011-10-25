package org.quimoniz.mudkips;

import org.bukkit.event.Event;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.command.CommandSender;
import org.bukkit.command.Command;
import org.bukkit.entity.Player;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.logging.Level;
import java.io.FileOutputStream;
import java.io.FileNotFoundException;
import java.io.File;

import com.nijiko.permissions.PermissionHandler;
import com.nijikokun.bukkit.Permissions.Permissions;
import org.bukkit.plugin.Plugin;

import java.awt.Rectangle;
import org.bukkit.entity.CreatureType;
import org.bukkit.DyeColor;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Sheep;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.Creature;
import org.bukkit.block.Block;
import org.bukkit.event.player.PlayerPortalEvent;
import org.quimoniz.mudkips.util.*;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import org.bukkit.event.player.PlayerPreLoginEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

public class Mudkips extends JavaPlugin {
//    private Properties myProps = null;
    private FileOutputStream logStream;
    private int dateProcesserTask;
    private String pathToProps = "mudkips.properties";
    public PermissionHandler permissionHandler;
    private boolean usingPermissions = false;
    private TimeCheckerTask dateProvider;
    
    /** Components */
    public Chat chatObj;
    public MudkipsPlayerProvider playerProvider = null;
    public WarpHandler warpHandler;
    public ErrorHandler errorHandler;
    public PortalHandler portalHandler;
    public BanHandler banHandler;
    public PropertyManager myProps;
/* DEBUG CODE */
//    public java.util.logging.Logger debugLog = null;
    
    /** Utilities */
    public StringUtil stringUtil;
    public WorldUtil worldUtil;
    public ServerUtil serverUtil;
    
    @Override
    public void onDisable() {
      try {
        if(myProps.getBooleanProperty("daily-date"))
          this.getServer().getScheduler().cancelTask(dateProcesserTask);
      } catch(Exception exc) {
        System.out.println("Exception while de-registering daily date Task.");
      }
      try {
        saveProperties();
      } catch(Exception exc) {
        System.out.println("Exception while saving Properties.");
      }
      if(warpHandler != null) {
        try {
          warpHandler.save();
        } catch(Exception exc) {
            System.out.println("Exception while saving Warps.");
        }
      }
      if(portalHandler != null) {
        try {
          portalHandler.save();
        } catch(Exception exc) {
            System.out.println("Exception while saving Portals.");
        }
      }
      if(banHandler != null) {
        try {
          banHandler.save();
        } catch(Exception exc) {
            System.out.println("Exception while saving Portals.");
        }
      }
      try {
        playerProvider.close();
      } catch(Exception exc) {
        System.out.println("Exception while saving player data.");
      }
      try {
        chatObj.close();
      } catch(NullPointerException exc) {
        System.out.println("Mudkips Chat Module wasnt loaded, unloading Exception");
      }
      try {
        errorHandler.close();
      } catch(NullPointerException exc) {
        System.out.println("Mudkips Error-log Module wasnt loaded, unloading Exception");
      }
      myProps = null;
      playerProvider = null;
      chatObj = null;
      logStream = null;
      dateProcesserTask = -1;
      permissionHandler = null;
      usingPermissions = false;
      warpHandler = null;
      errorHandler = null;
    }
/* DEBUG CODE
  public void logDebug(String line) {
    if(myPlugin.debugLog != null) {
      debugLog.log(java.util.logging.Level.INFO, line);
    }
  }
*/
    @Override
    public void onEnable() {
/* DEBUG CODE
      try {
        debugLog = java.util.logging.Logger.getLogger("Debug Log");
        java.util.logging.FileHandler fileHandler = new java.util.logging.FileHandler(new java.io.File(getDataFolder(),"debugLog.txt").getAbsolutePath(), true);
        debugLog.addHandler(fileHandler);
      } catch(java.io.IOException exc) { System.out.println("Could not set up debug logger!"); }
*/      
      try {
        File pluginFolder = this.getFile().getParentFile();
        pathToProps = pluginFolder.getPath() + "/" + "mudkips.properties";
        //Initialize Properties
        myProps = new PropertyManager(pathToProps, this.getServer());
        //if(myProps.getBooleanProperty("set-defaults", true))
        DefaultProperties.writeDefaultProperties(myProps);
        if(myProps.getBooleanProperty("use-permissions")) {
          setupPermissions();
          usingPermissions = true;
        }
      } catch(Exception exc) {
        // very bad, an exception at this point dimisses all the plugins capabilities
        this.getServer().getLogger().severe("Exception during initializing Properties");
        return;
      }
      try {
        if(!this.getDataFolder().exists()) {
            //checking if it is a folder, doesnt work, when it doesnt exist
            if(!getDataFolder().mkdir())
            getServer().getLogger().log(Level.SEVERE, "Can't create folder \"" + this.getDataFolder().getParent() + "/" + this.getDataFolder().getName());
        }
      } catch(SecurityException exc) {
        System.out.println("Could NOT create data folder.");
      }
      try {
        errorHandler = new ErrorHandler(new File(getDataFolder(), "errors.log"));
      } catch(Exception exc) {
        System.out.println("Could NOT initialize error logger");
      }
      try {
        stringUtil = new StringUtil();
        worldUtil = new WorldUtil(this.getServer(),this);
        serverUtil = new ServerUtil(this.getServer());
      } catch(Exception exc) {
        System.out.println("Exception during initialization of utilities.");
        errorHandler.logException(exc);
      }
      if(myProps.getBooleanProperty("enable-warps")) {
        try {
          warpHandler = new WarpHandler(new File(this.getDataFolder(), myProps.getProperty("warps-file")),this.getServer().getLogger(), this);
        } catch(Exception exc) {
          System.out.println("Exception during loading warps");
          errorHandler.logException(exc);
        }
      }
      if(myProps.getBooleanProperty("enable-portals")) {
        try {
          portalHandler = new PortalHandler(new File(this.getDataFolder(), myProps.getProperty("portals-file")),this.getServer().getLogger(), this);
        } catch(Exception exc) {
          System.out.println("Exception during loading portals");
          errorHandler.logException(exc);
        }
      }
      if(myProps.getBooleanProperty("enable-bans")) {
        try {
          banHandler = new BanHandler(new File(this.getDataFolder(), myProps.getProperty("bans-file")),this.getServer().getLogger(), this);
        } catch(Exception exc) {
          System.out.println("Exception during loading bans");
          errorHandler.logException(exc);
        }
      }
      try {
        File logFile = new File(this.getDataFolder(), "chat.log");
        try {
          logStream = new FileOutputStream(logFile, true);
        } catch (FileNotFoundException exc) {
          this.getServer().getLogger().log(Level.SEVERE, "Can't access \"" + this.getDataFolder().getParent() + "/" + this.getDataFolder().getName() + "/" + logFile.getName() + "\" for logging");
        }
        chatObj = new Chat(this.getServer(), myProps, this.getServer().getLogger(), logStream, this);
      } catch(Exception exc) {
        System.out.println("Exception during intitializing stream for logging chat");
        errorHandler.logException(exc);
      }
      
      //Register Player events
      try {
        PluginManager pm = this.getServer().getPluginManager();
        MPlayerListener playerListener = new MPlayerListener(this);
        pm.registerEvent(Event.Type.PLAYER_CHAT, playerListener, Event.Priority.Normal, this);
        pm.registerEvent(Event.Type.PLAYER_JOIN, playerListener, Event.Priority.Normal, this);
        pm.registerEvent(Event.Type.PLAYER_QUIT, playerListener, Event.Priority.Normal, this);
        pm.registerEvent(Event.Type.PLAYER_BED_ENTER, playerListener, Event.Priority.Low, this);
        BlockInteractListener touchListener = null;
        if(myProps.getBooleanProperty("protect-obsidian") || (myProps.getBooleanProperty("enable-jail") && myProps.getBooleanProperty("protect-jail-break"))) {
          //pm.registerEvent(Event.Type.BLOCK_DAMAGE, new BlockTouchListener(), Event.Priority.Normal, this);
          touchListener = new BlockInteractListener(this);
          if(myProps.getBooleanProperty("enable-jail") && myProps.getBooleanProperty("protect-jail-break")) touchListener.blockJailBreak(true);
          pm.registerEvent(Event.Type.BLOCK_BREAK, touchListener, Event.Priority.Normal, this);
        }
        if(myProps.getBooleanProperty("enable-jail") && myProps.getBooleanProperty("protect-jail-place")) {
          if(touchListener == null) touchListener = new BlockInteractListener(this);
          touchListener.blockJailPlace(true);
          pm.registerEvent(Event.Type.BLOCK_PLACE, touchListener, Event.Priority.Normal, this);
        }
        if(myProps.getBooleanProperty("enable-jail") && myProps.getBooleanProperty("protect-jail-pickup")) {
          playerListener.blockJailPickup(true);
          pm.registerEvent(Event.Type.PLAYER_PICKUP_ITEM, playerListener, Event.Priority.Normal, this);
        }
        if(myProps.getBooleanProperty("enable-jail")) {
          String[] numsBlocked = StringUtil.separate(myProps.getProperty("protect-jail-interact"),',');
          if(numsBlocked != null && numsBlocked.length > 0) {
            java.util.ArrayList<Integer> idsBlocked  = new java.util.ArrayList<Integer>(numsBlocked.length);
            boolean allInteractionsBlocked = false;
            for(int i = 0; i < numsBlocked.length; i++) {
              if("*".equals(numsBlocked)) {
                allInteractionsBlocked = true;
              } else {
                try {
                  idsBlocked.add(new Integer(numsBlocked[i]));
                } catch(NumberFormatException exc) { }
              }
            }
            if(idsBlocked.size() > 0) {
              if(allInteractionsBlocked) {
                playerListener.blockAllJailInteract(true);
                pm.registerEvent(Event.Type.PLAYER_INTERACT, playerListener, Event.Priority.Normal, this);
              } else {
                if(idsBlocked.size() > 0) {
                  playerListener.blockJailInteract(idsBlocked);
                  pm.registerEvent(Event.Type.PLAYER_INTERACT, playerListener, Event.Priority.Normal, this);
                }
              }
            }
          }
        }
        String[] numsBlocked = StringUtil.separate(myProps.getProperty("block-fire-placement"),',');
        if(numsBlocked != null && numsBlocked.length > 0) {
          java.util.ArrayList<Integer> idsBlocked  = new java.util.ArrayList<Integer>(numsBlocked.length);
          boolean allFirePlacementBlocked = false;
          for(int i = 0; i < numsBlocked.length; i++) {
            if("*".equals(numsBlocked)) {
              allFirePlacementBlocked = true;
            } else {
              try {
                idsBlocked.add(new Integer(numsBlocked[i]));
              } catch(NumberFormatException exc) { }
            }
          }
          if(idsBlocked.size() > 0) {
            if(allFirePlacementBlocked) {
              if(touchListener == null) touchListener = new BlockInteractListener(this);
              touchListener.blockAllFirePlace(true);
              //TODO: Register correct event hooks, with Flint&Steel the event is BLOCK_IGNITE !!!
              pm.registerEvent(Event.Type.BLOCK_IGNITE, touchListener, Event.Priority.Normal, this);
              touchListener.blockIgniteWasRegistered = true;

            } else {
              if(idsBlocked.size() > 0) {
                if(touchListener == null) touchListener = new BlockInteractListener(this);
                touchListener.blockFirePlace(idsBlocked);
                pm.registerEvent(Event.Type.BLOCK_IGNITE, touchListener, Event.Priority.Normal, this);
                touchListener.blockIgniteWasRegistered = true;
              }
            }
          }
        }
        numsBlocked = StringUtil.separate(myProps.getProperty("block-burn"),',');
        if(numsBlocked != null && numsBlocked.length > 0) {
          java.util.ArrayList<Integer> idsBlocked  = new java.util.ArrayList<Integer>(numsBlocked.length);
          boolean allBurningBlocked = false;
          for(int i = 0; i < numsBlocked.length; i++) {
            if("*".equals(numsBlocked)) {
              allBurningBlocked = true;
            } else {
              try {
                idsBlocked.add(new Integer(numsBlocked[i]));
              } catch(NumberFormatException exc) { }
            }
          }
          if(idsBlocked.size() > 0) {
            if(allBurningBlocked) {
              if(touchListener == null) touchListener = new BlockInteractListener(this);
              touchListener.blockAllBurning(true);
              pm.registerEvent(Event.Type.BLOCK_BURN, touchListener, Event.Priority.Normal, this);
              if(!touchListener.blockIgniteWasRegistered) {
                pm.registerEvent(Event.Type.BLOCK_IGNITE, touchListener, Event.Priority.Normal, this);
                touchListener.blockIgniteWasRegistered = true;
              }
            } else {
              if(idsBlocked.size() > 0) {
                if(touchListener == null) touchListener = new BlockInteractListener(this);
                touchListener.blockBurning(idsBlocked);
                pm.registerEvent(Event.Type.BLOCK_BURN, touchListener, Event.Priority.Normal, this);
                if(touchListener.blockIgniteWasRegistered) {
                  pm.registerEvent(Event.Type.BLOCK_IGNITE, touchListener, Event.Priority.Normal, this);
                  touchListener.blockIgniteWasRegistered = true;
                }
              }
            }
          }
        }
        if(myProps.getBooleanProperty("enable-portals")) {
          pm.registerEvent(Event.Type.PLAYER_PORTAL, playerListener, Event.Priority.Normal, this);
        }
        if(myProps.getBooleanProperty("enable-bans")) {
          pm.registerEvent(Event.Type.PLAYER_PRELOGIN, playerListener, Event.Priority.Normal, this);
        }
        pm.registerEvent(Event.Type.PLAYER_TELEPORT, playerListener, Event.Priority.Normal, this);
        pm.registerEvent(Event.Type.PLAYER_RESPAWN, playerListener, Event.Priority.Normal, this);
        
        MEntityListener entityListener = new MEntityListener(this);
        pm.registerEvent(Event.Type.ENTITY_DAMAGE, entityListener, Event.Priority.Normal, this);
        pm.registerEvent(Event.Type.ENTITY_DEATH, entityListener, Event.Priority.Normal, this);
      } catch(Exception exc) {
          System.out.println("Exception during registering listeners");
          errorHandler.logException(exc);
      }
      
      //parameters: first the Plugin.
      //            second the task
      //            third the delay until the task is run first
      //            fourth the delay between each following invocation
      if(myProps.getBooleanProperty("daily-date")) {
        try {
          dateProvider = new TimeCheckerTask(this);
          dateProcesserTask = this.getServer().getScheduler().scheduleSyncRepeatingTask(this, dateProvider, 20, 100);
        } catch(Exception exc) {
          System.out.println("Exception during registering date scheduler");
          errorHandler.logException(exc);
        }
      }
      org.bukkit.Server server = getServer();
      if(server == null) throw new RuntimeException("getServer() returns null!");
      try {
        playerProvider = new MudkipsPlayerProvider(server, this);
      } catch(Exception exc) {
        System.out.println("Exception during initializing Mudkips Player Provider");
        errorHandler.logException(exc);
      }
      saveProperties();
    }
    private boolean setupPermissions() {
      if(permissionHandler == null) {
        Plugin permPlugin = this.getServer().getPluginManager().getPlugin("Permissions");
        if(permPlugin != null) {
          this.permissionHandler = ((Permissions)permPlugin).getHandler();
          return true;
        }
        if(permissionHandler == null) {
          this.getServer().getLogger().log(Level.SEVERE, "Mudkips couldn't obtain Permissions Handler!");
          return false;
        } else { // This case can not be reached! However it produces error "This method must return [..]" if omitted
          return true;
        }
      } else return true;
    }
    public boolean hasPermission(CommandSender p, String permNode, boolean defaultValNonOp, boolean defaultValOp) {
      if(p == null) return defaultValNonOp;
      if(!(p instanceof Player) && p.isOp()) {// Console always has permission
        return true;
      } else if(usingPermissions) {
        if(permissionHandler == null) {
          setupPermissions();
        }
        if(permissionHandler == null) {
          this.getServer().getLogger().severe("Null pointer exception, deactivated Permissions.");
          usingPermissions = false;
        } else {
          try {
            return permissionHandler.has((Player)p, permNode);
          } catch(NoClassDefFoundError exc) {
            this.getServer().getLogger().severe("There is no nijikokun Permissions Plugin active, deactivated Permissions.");
            usingPermissions = false;
          }
        }
      }
      return p.isOp() ? defaultValOp : defaultValNonOp;
    }
    public boolean askPermission(CommandSender p, String permNode, boolean defaultValNonOp, boolean defaultValOp, String revokeMessage) {
      if(!hasPermission(p, permNode, defaultValNonOp, defaultValOp)) {
        if(p != null)
          p.sendMessage(revokeMessage);
        return false;
      } else {
        return true;
      }
    }
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
      //firstof casting sender to a Player ...
      Player pSender = null;
      MudkipsPlayer mPlayer = null;
      boolean isConsole = false;
      if(sender instanceof Player) {
        pSender = (Player) sender;
        mPlayer = getMudkipsPlayer(pSender.getName());
      } else {
        isConsole = true;
      }
      //... and universalizing the command
      String rawCommand = command.getName().toLowerCase();
      if(rawCommand.charAt(0) == '/') rawCommand = rawCommand.substring(1, rawCommand.length());
      //Player listing, ignores any arguments
      if(rawCommand.indexOf("who") == 0 || rawCommand.indexOf("online") == 0 || rawCommand.indexOf("playerlist") == 0) {
        if(askPermission(sender, "mudkips.playerlist", true, true, ChatColor.RED + "You are not authorized to see the playerlist!")) {
          String playerListing = playerProvider.playerListing();
          sender.sendMessage(playerListing);
        }
      } else if(rawCommand.indexOf("afk") == 0) {
        if(pSender != null)
          if(askPermission(sender, "mudkips.afk", true, true, ChatColor.RED + "You are not authorized to use the /afk command!")) {
            if(playerProvider.has(pSender)) {
              if(!mPlayer.isAfk()) {
                String afkMessage= StringUtil.concatenate(args, " ", 0);
                mPlayer.toggleAfk(afkMessage);
                  sender.sendMessage(StringUtil.replaceAll(myProps.getProperty("player-afk"), new char[] {'s', 'm'}, new String[] {mPlayer.displayName(), afkMessage}));
              } else {
                long afkTime = mPlayer.toggleAfk();
                if(afkTime > myProps.getIntProperty("afk-return-message-duration")) {
                  chatObj.notification(StringUtil.replaceAll(myProps.getProperty("afk-return-message"), new char[] {'s', 'm'}, new String[] { mPlayer.displayName(), mPlayer.afkMessage()}), mPlayer.getLocation());
                }
                  sender.sendMessage(StringUtil.replaceAll(myProps.getProperty("player-back"), 's', mPlayer.displayName()));
              }
            }
          }
      } //The help command prints out HELP_MSG to the player
        else if(rawCommand.indexOf("help") == 0) {
          if(pSender != null) {
            if(askPermission(sender, "mudkips.help", true, true, ChatColor.RED + "You are not authorized to use the /help command!"))
              mPlayer.sendMessage(myProps.getProperty("help"));
              return true;
          } else {
            return false;
          }
      }
        else if(rawCommand.indexOf("motd") == 0) {
          if(askPermission(sender, "mudkips.motd", true, true, ChatColor.RED + "You are not authorized to use the /motd command!"))
            sendMotd(sender);
      } else if( rawCommand.equals("pto")) {
        if(pSender != null) {
             if(askPermission(pSender, "mudkips.teleport.to", false, true, ChatColor.RED + "You are not authorized to use the /pto command.")) {
               if(args.length >= 1) {
                 Player player = matchPlayer(args[0]);
                 if(player != null) {
                   Location targetLocation = player.getLocation();
                   if(args.length >= 2) {
                     Integer parsedInt = null;
                     try {
                       parsedInt = Integer.parseInt(args[1]);
                     } catch(NumberFormatException exc) { }
                     if(parsedInt != null) {
                       double targetY = targetLocation.getY() + parsedInt;
                       if(targetY < 5) targetY = 5;
                       targetLocation = new Location(targetLocation.getWorld(), targetLocation.getX(), targetY, targetLocation.getZ(), targetLocation.getYaw(), targetLocation.getPitch());
                     }
                   }
                   mPlayer.teleport(targetLocation);
                 } else {
                   pSender.sendMessage(ChatColor.RED + "The Player \"" + args[0] + "\" could not be associated.");
                 }
               } else pSender.sendMessage(ChatColor.RED + "You did not specify a player to teleport to.");
             }
          }
      } else if( rawCommand.equals("phere")) {
        if(pSender != null) {
             if(askPermission(pSender, "mudkips.teleport.here", false, true, ChatColor.RED + "You are not authorized to use the /phere command.")) {
               if(args.length >= 1) {
                 Player player = matchPlayer(args[0]);
                 MudkipsPlayer mPlayerTo = this.getMudkipsPlayer(player.getName());
                 if(player != null && mPlayerTo != null) {
                   Location targetLocation = pSender.getLocation();
                   if(args.length >= 2) {
                     Integer parsedInt = null;
                     try {
                       parsedInt = Integer.parseInt(args[1]);
                     } catch(NumberFormatException exc) { }
                     if(parsedInt != null) {
                       double targetY = targetLocation.getY() + parsedInt;
                       if(targetY < 5) targetY = 5;
                       targetLocation = new Location(targetLocation.getWorld(), targetLocation.getX(), targetY, targetLocation.getZ(), targetLocation.getYaw(), targetLocation.getPitch());
                     }
                   }
                   mPlayerTo.teleport(targetLocation);
                 } else {
                   if(player == null || mPlayerTo == null) {
                     pSender.sendMessage(ChatColor.RED + "Player \""+args[0]+"\" could not be associated.");
                   }
                 }
               } else pSender.sendMessage(ChatColor.RED + "You did not specify a player to teleport here.");
             }
          }
      } else if( rawCommand.equals("tp")) {
        if(askPermission(sender, "mudkips.teleport.tp", false, true, ChatColor.RED + "You are not authorized to use the /tp command.")) {
             if(args.length >= 2) {
               Player playerB = matchPlayer(args[1]);
               Player playerA = matchPlayer(args[0]);
               if(playerA != null) {
                 MudkipsPlayer mPlayerA = getMudkipsPlayer(playerA.getName());
                 if(mPlayerA != null && playerB != null) {
                   mPlayerA.teleport(playerB.getLocation());
                 } else {
                   if(mPlayerA == null && playerB == null) {
                     pSender.sendMessage(ChatColor.RED + "Neither \"" + args[0] + "\" nor \"" + args[1] + "\" could be associated with a player.");
                   } else if(mPlayerA == null) {
                     pSender.sendMessage(ChatColor.RED + "The player \"" + args[0] + "\" could not be associated.");
                   } else if(playerB == null) {
                     pSender.sendMessage(ChatColor.RED + "The player \"" + args[1] + "\" could not be associated.");
                   }
                 }
               } else {
                 sender.sendMessage(ChatColor.RED + "The Player \"" + args[0] + "\" could not be associated.");
               }
             } else {
               sender.sendMessage(ChatColor.RED + "You did not specify a player to teleport to.");
             }
           }
        } else if(rawCommand.equals("loc")) {
        if(askPermission(sender, "mudkips.teleport.loc", false, true, ChatColor.RED + "You are not authorized to use the /loc command.")) {
          if(args.length > 0) {
            if(args.length == 1) { // teleport to a warp
              if(askPermission(sender, "mudkips.teleport.loc.warp",false,true, ChatColor.RED + "You are not authorized to use the /loc command in that manner.")) {
                if(myProps.getBooleanProperty("enable-warps")) {
                  if(pSender != null) {
                    Location targetLoc;
                    if("spawn".equalsIgnoreCase(args[0])) {
                      targetLoc = worldUtil.getDefaultSpawn();
                    } else {
                      targetLoc = warpHandler.getWarp(args[0]);
                    }
                    if(targetLoc != null) {
                      mPlayer.teleport(targetLoc);
                    } else sender.sendMessage(ChatColor.RED + "There is no warp by the name \"" + args[0] + "\"");
                  }
                } else sender.sendMessage(ChatColor.RED + "Warps are not enabled");
              }
            } else if(args.length == 2) {
              if(pSender != null) {
                if(StringUtil.isInteger(args[0]) && StringUtil.isInteger(args[1])) {
                  if(askPermission(sender, "mudkips.teleport.loc.pos",false,true, ChatColor.RED + "You are not authorized to use the /loc command in that manner.")) {
                    int x = 0, z = 0;
                    boolean parsingSuccess = false;
                    try {
                      x = Integer.parseInt(args[0]);
                      z = Integer.parseInt(args[1]);
                      parsingSuccess = true;
                    } catch(NumberFormatException exc) {
                      parsingSuccess = false;
                    }
                    if(parsingSuccess) {
                      Rectangle boundingRect = myProps.getRectangleProperty("teleport-bounding-rect");
                      if(boundingRect != null) {
                        if(boundingRect.contains(x,z)) {
                          mPlayer.teleport(new Location(pSender.getWorld(), x , pSender.getWorld().getHighestBlockYAt(x, z), z));
                          return true;
                        } else {
                          sender.sendMessage(ChatColor.RED + "Coordinates outside of World Borders.");
                          return true;
                        }
                      } else {
                        System.out.println("Could not access Property \"teleport-bounding-rect\"!");
                        return true;
                      }
                    } else {
                      sender.sendMessage(ChatColor.RED + "Coordinates \"" + args[0] + "\", \"" + args[1] + "\" could not be parsed.");
                      return true;
                    }
                  }
                }
              }
              if(myProps.getBooleanProperty("enable-warps")) {
                if(askPermission(sender, "mudkips.teleport.loc.playerwarp",false,true, ChatColor.RED + "You are not authorized to use the /loc command in that manner.")) {
                  MudkipsPlayer playerToTeleport = getMudkipsPlayer(matchPlayer(args[0]).getName());
                  if(playerToTeleport != null) {
                    Location targetLoc;
                    if("spawn".equalsIgnoreCase(args[1])) {
                      targetLoc = worldUtil.getDefaultSpawn();
                    } else {
                      targetLoc = warpHandler.getWarp(args[1]);
                    }
                    if(targetLoc != null) {
                      playerToTeleport.teleport(targetLoc);
                    }  else sender.sendMessage(ChatColor.RED + "There is no warp by the name \"" + args[1] + "\"");
                  } else sender.sendMessage(ChatColor.RED + "Could not determine Player \"" + args[0] + "\"");
                }
              } else sender.sendMessage(ChatColor.RED + "Warps are not enabled");
            } else if(args.length == 3) {
              if(pSender != null) {
                if(StringUtil.isInteger(args[0]) && StringUtil.isInteger(args[1]) && StringUtil.isInteger(args[2])) {
                  if(askPermission(sender, "mudkips.teleport.loc.pos",false,true, ChatColor.RED + "You are not authorized to use the /loc command in that manner.")) {
                    int x = 0, y = 0, z = 0;
                    boolean parsingSuccess = false;
                    try {
                      x = Integer.parseInt(args[0]);
                      y = Integer.parseInt(args[1]);
                      z = Integer.parseInt(args[2]);
                      parsingSuccess = true;
                    } catch(NumberFormatException exc) {
                      parsingSuccess = false;
                    }
                    if(parsingSuccess) {
                      Rectangle boundingRect = myProps.getRectangleProperty("teleport-bounding-rect");
                      if(boundingRect != null) {
                        if(boundingRect.contains(x,z)) {
                          mPlayer.teleport(new Location(pSender.getWorld(), x ,y, z));
                        } else sender.sendMessage(ChatColor.RED + "Coordinates outside of World Borders.");
                      } else {
                        System.out.println("Could not access Property \"teleport-bounding-rect\"!");
                      }
                    } else sender.sendMessage(ChatColor.RED + "Coordinates \"" + args[0] + "\", \"" + args[1] + "\", \"" + args[2] + "\" could not be parsed.");
                  }
                }
              } else {
                if(StringUtil.isInteger(args[1]) && StringUtil.isInteger(args[2])) {
                  if(askPermission(sender, "mudkips.teleport.loc.playerpos",false,true, ChatColor.RED + "You are not authorized to use the /loc command in that manner.")) {
                    MudkipsPlayer playerToTeleport = getMudkipsPlayer(matchPlayer(args[0]).getName());
                    if(playerToTeleport != null) {
                      int x = 0, z = 0;
                      boolean parsingSuccess = false;
                      try {
                        x = Integer.parseInt(args[1]);
                        z = Integer.parseInt(args[2]);
                        parsingSuccess = true;
                      } catch(NumberFormatException exc) {
                        parsingSuccess = false;
                      }
                      if(parsingSuccess) {
                        Rectangle boundingRect = myProps.getRectangleProperty("teleport-bounding-rect");
                        if(boundingRect != null) {
                          if(boundingRect.contains(x,z)) {
                            playerToTeleport.teleport(new Location(playerToTeleport.getPlayer().getWorld(), x , playerToTeleport.getPlayer().getWorld().getHighestBlockYAt(x, z), z));
                          } else sender.sendMessage(ChatColor.RED + "Coordinates outside of World Borders.");
                          } else {
                          System.out.println("Could not access Property \"teleport-bounding-rect\"!");
                          }
                      } else sender.sendMessage(ChatColor.RED + "Coordinates \"" + args[1] + "\", \"" + args[2] + "\" could not be parsed.");
                    } else sender.sendMessage(ChatColor.RED + "Player \"" +args[0] + "\" not found.");
                  }
                } else                
                  sender.sendMessage(ChatColor.RED + "Parameters are not valid Numbers.");
              }
            } else if(args.length == 4) {
              if(StringUtil.isInteger(args[1]) && StringUtil.isInteger(args[2]) && StringUtil.isInteger(args[3])) {
                if(askPermission(sender, "mudkips.teleport.loc.playerpos",false,true, ChatColor.RED + "You are not authorized to use the /loc command in that manner.")) {
                  MudkipsPlayer playerToTeleport = getMudkipsPlayer(matchPlayer(args[0]).getName());
                  if(playerToTeleport != null) {
                    int x = 0, y = 0, z = 0;
                    boolean parsingSuccess = false;
                    try {
                      x = Integer.parseInt(args[1]);
                      y = Integer.parseInt(args[2]);
                      z = Integer.parseInt(args[3]);
                      parsingSuccess = true;
                    } catch(NumberFormatException exc) {
                      parsingSuccess = false;
                    }
                    if(parsingSuccess) {
                      Rectangle boundingRect = myProps.getRectangleProperty("teleport-bounding-rect");
                      if(boundingRect != null) {
                        if(boundingRect.contains(x,z)) {
                          playerToTeleport.teleport(new Location(playerToTeleport.getPlayer().getWorld(), x , y, z));
                        } else sender.sendMessage(ChatColor.RED + "Coordinates outside of World Borders.");
                      } else {
                        System.out.println("Could not access Property \"teleport-bounding-rect\"!");
                      }
                    } else sender.sendMessage(ChatColor.RED + "Coordinates \"" + args[1] + "\", \"" + args[2] + "\", \"" + args[3] + "\" could not be parsed.");
                  } else sender.sendMessage(ChatColor.RED + "Player \"" +args[0] + "\" not found.");
                }
              }
            }
          } else {
             sender.sendMessage(ChatColor.RED + "You did not specify a location to teleport to.");
          }
          }
        } else if(rawCommand.equals("locset")) {
          if(askPermission(sender, "mudkips.teleport.loc.set", false, true, "You are not authorized to use the /locset command!")) {
            if(args.length == 1 && pSender != null) {
              Location loc = pSender.getLocation();
              loc.setY(loc.getY()+1);
              if("spawn".equalsIgnoreCase(args[0])) {
                if(askPermission(sender, "mudkips.teleport.loc.setspawn", false, true, "You are not authorized to use the /locset command for setting spawn!")) {
                  pSender.getWorld().setSpawnLocation(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
                }
              } else {
                warpHandler.setWarp(args[0], loc);
              }
            } else {
              sender.sendMessage("Could not set warp, specifieng a Location in invocation not yet implemented due to lazyness + multiworld compatibility");
            }
          }
        } else if(rawCommand.indexOf("me") == 0) {
          if(pSender != null) {
            if(args.length > 0)
              chatObj.playerChat(pSender, Chat.Type.RPG, args);
            else
              pSender.sendMessage(ChatColor.RED + "You did not specify an RP action.");
          } else if(sender.isOp()) {
            chatObj.consoleChat(sender, Chat.Type.RPG, args);
          }
      }
        else if(rawCommand.indexOf("msg") == 0) {
          if(pSender != null) {
            if(args.length >= 2) {
              if(askPermission(sender, "mudkips.chat.msg", true, true, ChatColor.RED + "You are not authorized to use the /msg command!"))
                chatObj.playerChat(pSender, Chat.Type.MESSAGE, args);
            } else {
                 pSender.sendMessage(ChatColor.RED + "Not enough parameters, receiver and message required.");
            }
          } else if(sender.isOp()) {
            chatObj.consoleChat(sender, Chat.Type.MESSAGE, args);
          }
      } else if(rawCommand.indexOf("pmsg") == 0) {
         if(pSender != null) {
              MudkipsPlayer mP = playerProvider.get(pSender);
           if(args.length > 0) {
             if(askPermission(sender, "mudkips.chat.pmsg", true, true, ChatColor.RED + "You are not authorized to use the /pmsg command!"))
               chatObj.playerChat(pSender, Chat.Type.MESSAGE_PERSIST, args);
           } else {
             if(mP != null) {
               if(mP.inPrivateChat()) {
                 String chatPartner = mP.getPrivateChatPartner();
                 if(chatPartner != null)
                     pSender.sendMessage(StringUtil.replaceAll(myProps.getProperty("private-chat-leave"),'s', chatPartner));
                 mP.setPrivateChatPartner(null);
               } else
                 pSender.sendMessage(ChatColor.RED + "Can't do anything without playername nor message");
             } else
                 pSender.sendMessage(ChatColor.RED + "Unable to locate you in the playerlist. " + ChatColor.GRAY + "(maybe rejoining in will fix it)");
           }
         } else {
           sender.sendMessage(ChatColor.RED + "lulz, unexpected usage of pmsg Command.");
         }
      } else if(rawCommand.indexOf("weather") == 0) { //ugh this is ugly, we should use commands /rain and /thunder instead
         if(askPermission(sender, "mudkips.weather", false, true, ChatColor.RED + "You are not authorized to use the /weather command!")) { //pSender == null || pSender.isOp()
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
               sender.sendMessage(StringUtil.concatenate(output, "\n", 0));
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
                 paramThunder = args[2].toLowerCase();
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
         }
      }
       else if(rawCommand.equalsIgnoreCase("w") || rawCommand.equalsIgnoreCase("whisper")) {
          if(pSender != null) {
            if(args.length > 0)
              if(askPermission(sender, "mudkips.chat.whisper", true, true, ChatColor.RED + "You are not authorized to whisper!"))
                chatObj.playerChat(pSender, Chat.Type.WHISPER, args);
             else
              pSender.sendMessage(ChatColor.RED + "You did not whisper anything.");
           } else if(sender.isOp()) {
             //sender.sendMessage("It seems, that Gods are incapable of whispering.");
             chatObj.consoleChat(sender, Chat.Type.WHISPER, args);
           }
        }
       else if(rawCommand.indexOf("shout") == 0 || rawCommand.equalsIgnoreCase("s")) {
           if(pSender != null) {
             if(args.length > 0)
               if(askPermission(sender, "mudkips.chat.shout", true, true, ChatColor.RED + "You are not authorized to shout!"))
                 chatObj.playerChat(pSender, Chat.Type.SHOUT, args);
              else
               pSender.sendMessage(ChatColor.RED + "You did not shout anything.");
            } else if(sender.isOp()) {
              chatObj.consoleChat(sender, Chat.Type.SHOUT, args);
            }
        }
       else if(rawCommand.equalsIgnoreCase("announce")) {
         if(pSender != null) {
           if(args.length > 0) {
             if(askPermission(sender, "mudkips.announce", false, true, ChatColor.RED + "You are not authorized to use the /announce command!"))
               chatObj.playerChat(pSender, Chat.Type.ANNOUNCE, args);
           } else {
             pSender.sendMessage(ChatColor.RED + "You did not announce anything.");
           }
         } else {
           chatObj.consoleChat(sender, Chat.Type.ANNOUNCE, args);
         }
       } else if(rawCommand.equalsIgnoreCase("info")) {
         if(askPermission(sender, "mudkips.info.invoke", true, true, ChatColor.RED + "You are not authorized to use the /info command!")) {
           String playerName = null;
           if(args.length < 1) {
             if(pSender != null) {
               playerName = pSender.getName();
             } else {
               sender.sendMessage("Can't display any information, parameter required.");
               return false;
             }
           } else {
             Player p = matchPlayer(args[0]);
             if(p != null)
               playerName = p.getName();
           }
           if(playerName != null) {
               MudkipsPlayer mPlayerInfo = getMudkipsPlayer(playerName);
               if(mPlayerInfo != null) {
               StringBuilder buf = new StringBuilder();
             //mPlayer.afkMessage()
               if(!mPlayerInfo.displayName().equals(mPlayerInfo.getName())) {
                 buf.append(mPlayerInfo.displayName());
                 buf.append(" (");
                 buf.append(mPlayerInfo.getName());
                 buf.append(")");
               } else {
                 buf.append(mPlayerInfo.getName());
               }
               if(hasPermission(sender, "mudkips.info.ip", false, true))
                 buf.append("[" + mPlayerInfo.getPlayer().getAddress().getHostName() + "]");
               buf.append("\n");
               if(hasPermission(sender, "mudkips.info.afk", true, true))
                 if(mPlayerInfo.isAfk())
                   buf.append(this.afkNotification(mPlayerInfo) + "\n" + ChatColor.WHITE);
               if(hasPermission(sender, "mudkips.info.proximity", true, true)) {
                 if(pSender != null && !pSender.getName().equals(mPlayerInfo.getName())) {
                   if(!pSender.getLocation().getWorld().getName().equals(mPlayerInfo.getLocation().getWorld().getName())) {
                     buf.append("Is in another world.\n");
                   } else {
                      double distance = WorldUtil.calcDistance(pSender.getLocation(), mPlayerInfo.getPlayer().getLocation(), myProps.getBooleanProperty("distance-check-height"));
                      if(distance < myProps.getIntProperty("whisper-propagation-distance")) {
                        buf.append("Is aside you.\n");
                      } else if(distance < myProps.getIntProperty("chat-propagation-distance")) {
                        buf.append("Is somewhere around you.\n");
                      } else {
                        buf.append("Is far away.\n");
                      }
                   }
                 } else if(pSender == null && sender.isOp()) { // console
                   buf.append(mPlayerInfo.getLocation().getWorld().getName());
                   buf.append(Chat.locTag(mPlayerInfo.getLocation()));
                   buf.append("\n");
                 }
               }
               if(hasPermission(sender, "mudkips.pvp", true, true)) {
                 if(mPlayerInfo.isPvpEnabled()) {
                   buf.append("Has PvP enabled.\n");
                 } else {
                   buf.append("Has PvP disabled.\n");
                 }
               }
               if(mPlayerInfo.isJailed()) {
                 buf.append("Is jailed for " + StringUtil.describeDuration(mPlayerInfo.getJailedForDuration(), "en") + ".\n");
               }
               if(mPlayerInfo.getTimesDied() < 1) {
                 buf.append("Never died.\n");
               } else {
                 buf.append("Died " + mPlayerInfo.getTimesDied() + " times.\n");
               }
               if(hasPermission(sender, "mudkips.info.login", true, true)) {
                 double duration = (mPlayerInfo.getPlayer().getWorld().getFullTime()-mPlayerInfo.getInitTime())/24000.00;
                 duration *= 100;
                 duration = (int) duration;
                 duration /= 100;
                 if(duration < 0.02)
                   buf.append("Just logged in.\n");
                 else if(duration >= 0.45 && duration <= 0.55)
                   buf.append("Logged in half a day ago.\n");
                 else
                   buf.append("Logged in " + duration + " days ago.\n");
               }
               if(mPlayer == null) {
                 buf.toString().replaceAll("\n", "     ");
                 sender.sendMessage(buf.toString());
               } else {
                 mPlayer.sendMessage(buf.toString());
               }
             } else {
               sender.sendMessage("Cant associate a Player.");
               return false;
             }
           } else {
             sender.sendMessage("Can't display any information, parameter invalid.");
           }
         }
       }
       else if(rawCommand.equals("say")) {
         if(pSender != null) {
           if(askPermission(sender, "mudkips.chat.say", true, true, ChatColor.RED + "You are not authorized to say anything!"))
             chatObj.playerChat(pSender, Chat.Type.SAY, args);
         } else if(sender.isOp()) { // From Console
           chatObj.consoleChat(sender, Chat.Type.SAY, args);
         }
       }
       else if(rawCommand.equals("spawn")) {
         if(pSender != null) {
           if(askPermission(sender, "mudkips.spawn", true, true, ChatColor.RED + "You are not authorized to use the /spawn command.")) {
             if(mPlayer.fightCooldownElapsed()) {
               if(!mPlayer.isJailed()) {
                 Location targetLoc = mPlayer.getHomeBed();
                 if(targetLoc != null) {
                   if(!worldUtil.isCertainBlock(targetLoc,org.bukkit.Material.BED_BLOCK)) {
                     targetLoc = null;
                     setHomeBed(pSender, targetLoc);
                   }
                 }
                 if(targetLoc == null) {
                   targetLoc = worldUtil.getDefaultSpawn();
                 }
                 if(!myProps.getBooleanProperty("travel-with-equipment") && (!pSender.getWorld().getName().equals(targetLoc.getWorld().getName()) && (mPlayer.isEquipped() || mPlayer.armorEquipped()))) {
                   mPlayer.sendMessage(ChatColor.YELLOW + "Notice: No travelling with items!");
                 } else {
                   mPlayer.teleport(targetLoc);
                 }
               } else {
                 Location jailLocation = warpHandler.getWarp("jail");
                 if(jailLocation != null) {
                   mPlayer.teleport(jailLocation);
                 } else {
                   mPlayer.sendMessage(ChatColor.YELLOW + "Notice: No teleporting while being jailed!");
                 }
               }
             } else {
               mPlayer.sendMessage("You may not fight another and then flee of the revenge by beaming!");
             }
           }
         }
       }
       else if(rawCommand.equals("rename")) {
         if(pSender != null) { // Some user wants to be renamed
           mPlayer.sendMessage(ChatColor.YELLOW+"Unfortunately that feature is not yet available for users, only from console, poke me, "+ChatColor.AQUA+"Quimoniz"+ChatColor.YELLOW+", to be less lazy!");
           //ToDo: Do some stuff, actually, add the rename request to a queue to be confirmed by a moderator/admin, to avoid names like "cunt", "dick", or such stuff which can not be filtered like "5uck-my-d1ck"
           //mPlayer -> RenameRequest(concatenate(args,"-",0))
           //if(askPermission(sender, "mudkips.rename.invoke", true, true, ChatColor.RED + "You are not authorized to rename")) {
             //mPlayer.renameRequest(concatenate(args,"-",0));
           //}
         } else if(sender.isOp()) {// Server is renaming a player
           if(args.length >= 2) {
             MudkipsPlayer mPlayerToRename = getMudkipsPlayer(matchPlayer(args[0]).getName());
             mPlayerToRename.setAlterName(StringUtil.concatenate(args, " ", 1));
           }
         }
       } else if(rawCommand.equals("mob")) {
         if(askPermission(sender, "mudkips.mob", false, true, ChatColor.RED + "You may not spawn mobs!")) {
           if(args.length > 0) {
             String mobName = args[0];
             final int MAX_COUNT = 50;
             int mobCount = -1;
             String colorName = null;
             CreatureType mobType = null;
             Location loc = null;
             if(pSender != null)
               loc = pSender.getLocation();
             String warpName = null;
             String playerName = null;
             String playerTarget = null;
             Player pTarget = null;
             boolean creeperPowered = false;
             for(int i = 1; i < args.length; i++) {
               if("-c".equals(args[i])) {
                 if(args.length > (i+1)) {
                   colorName = args[i+1];
                   i++;
                 }
               } else if("-l".equals(args[i])) {
                 if(args.length > (i+1)) {
                   warpName = args[i+1];
                   i++;
                 }
               } else if("-n".equals(args[i])) {
                 if(args.length > (i+1)) {
                   if(StringUtil.isInteger(args[i+1])) {
                     try {
                       mobCount = Integer.parseInt(args[i+1]);
                     } catch(NumberFormatException exc) { }
                   }
                   i++;
                 }
               } else if("-e".equals(args[i])) {
                 creeperPowered = true;
               } else if("-p".equals(args[i])) {
                 if(args.length > (i+1)) {
                   playerName = args[i+1];
                   i++;
                 }
               } else if("-t".equals(args[i])) {
                 if(args.length > (i+1)) {
                   playerTarget = args[i+1];
                   i++;
                 }
               }
             }
             if(playerName != null) {
               Player p = this.matchPlayer(playerName);
               if(p != null) {
                 loc = p.getLocation();
               }
             }
             if(playerTarget != null) {
               pTarget = this.matchPlayer(playerTarget);
             }
             if(warpName != null) {
               loc = this.warpHandler.getWarp(warpName);
             }
             if(loc == null) {
               sender.sendMessage("No location specified for spawning the mob!");
               return false;
             }
             if(mobCount < 1) mobCount = 1;
             else if(mobCount > MAX_COUNT) mobCount = MAX_COUNT;
             if(mobName.length() >= 2) {
               mobName = mobName.substring(0,1).toUpperCase() + mobName.substring(1).toLowerCase();
             } else if(mobName.length() == 1) {
               mobName = mobName.toUpperCase();
             }
             mobType = CreatureType.fromName(mobName);
             if(mobType == null) {
               StringBuilder buf = new StringBuilder("Available Mob Types: ");
               boolean firstElement = true; 
               for(CreatureType curType : CreatureType.values()) {
                 if(!firstElement) {
                   buf.append(", ");
                 } else {
                   firstElement = false;
                 }
                 buf.append(curType.toString());
               }
               if(mPlayer != null) {
                 mPlayer.sendMessage(buf.toString());
               } else {
                 sender.sendMessage(buf.toString());
               }
               return false;
             }
             DyeColor sheepColor = null;
             if(colorName != null && mobType.equals(CreatureType.SHEEP)) {
               if("BLACK".equals(colorName)) {
                 sheepColor = DyeColor.BLACK;
               } else if("BLUE".equalsIgnoreCase(colorName)) {
                 sheepColor = DyeColor.BLUE;
               } else if("BROWN".equalsIgnoreCase(colorName)) {
                 sheepColor = DyeColor.BROWN;
               } else if("CYAN".equalsIgnoreCase(colorName)) {
                 sheepColor = DyeColor.CYAN;
               } else if("GRAY".equalsIgnoreCase(colorName)) {
                 sheepColor = DyeColor.GRAY;
               } else if("GREEN".equalsIgnoreCase(colorName)) {
                 sheepColor = DyeColor.GREEN;
               } else if("LIGHT_BLUE".equalsIgnoreCase(colorName)) {
                 sheepColor = DyeColor.LIGHT_BLUE;
               } else if("LIME".equalsIgnoreCase(colorName)) {
                 sheepColor = DyeColor.LIME;
               } else if("MAGENTA".equalsIgnoreCase(colorName)) {
                 sheepColor = DyeColor.MAGENTA;
               } else if("ORANGE".equalsIgnoreCase(colorName)) {
                 sheepColor = DyeColor.ORANGE;
               } else if("PINK".equalsIgnoreCase(colorName)) {
                 sheepColor = DyeColor.PINK;
               } else if("PURPLE".equalsIgnoreCase(colorName)) {
                 sheepColor = DyeColor.PURPLE;
               } else if("RED".equalsIgnoreCase(colorName)) {
                 sheepColor = DyeColor.RED;
               } else if("SILVER".equalsIgnoreCase(colorName)) {
                 sheepColor = DyeColor.SILVER;
               } else if("WHITE".equalsIgnoreCase(colorName)) {
                 sheepColor = DyeColor.WHITE;
               } else if("YELLOW".equalsIgnoreCase(colorName)) {
                 sheepColor = DyeColor.YELLOW;
               }
             }
             for(int i = 0; i < mobCount; i++) {
               LivingEntity mob = loc.getWorld().spawnCreature(loc, mobType);
               if(mob != null) {
                 if(pTarget != null && mob instanceof Creature) {
                   ((Creature)mob).setTarget(pTarget);
                 }
                 if(mob instanceof Sheep && sheepColor != null) {
                   Sheep sheep = (Sheep)mob;
                   sheep.setColor(sheepColor);
                 } else if(mob instanceof Creeper && creeperPowered) {
                   Creeper creeper = (Creeper)mob;
                   creeper.setPowered(true);
                 }
               }
             }
           } else {
             if(mPlayer != null) {
               mPlayer.sendMessage("Command to spawn mobs.\nUsage: " + command.getUsage());
             } else {
               sender.sendMessage("Command to spawn mobs.\nUsage: " + command.getUsage());
             }
           }
         }
       }
       else if(rawCommand.equals("slap")) {
         if(askPermission(sender, "mudkips.slaponce", false, true, ChatColor.RED + "You may only get slapped!")) {
           if(args.length > 0) {
             Player matchedReceiver = matchPlayer(args[0]);
             if(matchedReceiver != null) {
               MudkipsPlayer mToSlap = this.getMudkipsPlayer(matchedReceiver.getName());
               if(mToSlap != null) {
                 mToSlap.slap();
               } else {
                 sender.sendMessage(ChatColor.RED + "No one to slap!");
               }
             } else {
              sender.sendMessage(ChatColor.RED + "No one to slap!");
             }
           } else {
             sender.sendMessage(ChatColor.RED + "No one to slap!");
           }
         }
       }
       else if(rawCommand.equals("slappy")) {
         if(askPermission(sender, "mudkips.slappy", false, true, ChatColor.RED + "You may only get slapped!")) {
           if(args.length > 0) {
             Player matchedReceiver = matchPlayer(args[0]);
             if(matchedReceiver != null) {
               MudkipsPlayer mToSlap = this.getMudkipsPlayer(matchedReceiver.getName());
               if(mToSlap != null) {
                 if(mToSlap.toggleSlappy()) {
                   mPlayer.sendMessage(ChatColor.YELLOW + "\"" + mToSlap.getName() + "\" will now be slapped each time he shouts.");
                 } else {
                   mPlayer.sendMessage(ChatColor.YELLOW + "\"" + mToSlap.getName() + "\" will no longer be slapped each time he shouts.");
                 }
               } else {
                 sender.sendMessage(ChatColor.RED + "No one to slap!");
               }
             } else {
              sender.sendMessage(ChatColor.RED + "No one to slap!");
             }
           } else {
             sender.sendMessage(ChatColor.RED + "No one to slap!");
           }
         }
       }
       else if(rawCommand.equals("date")) {
         if(askPermission(sender, "mudkips.date.get", true, true, ChatColor.RED + "You may not look up the current date!")) {
           sender.sendMessage(dateProvider.getDate(mPlayer.getWorld()));
         }
       }
       else if(rawCommand.equals("mudkips")) {
         if(sender.isOp()) {
           if(args.length > 0) {
             if("fix".equalsIgnoreCase(args[0])) {
               if(args.length > 1) {
                 String player = matchPlayer(args[1]).getName();
                 this.playerProvider.fix(player);
               }
             }
           } else {
             sender.sendMessage("Mudkips Plugin, by Quimoniz");
           }
         } else {
           sender.sendMessage(ChatColor.RED + "You are not authorized");
         }
       } else if(rawCommand.equals("portal")) {
         if(pSender == null) {
           sender.sendMessage("No action can be performed");
         } else if(portalHandler == null){
           sender.sendMessage("PortalHandler is not loaded");
         } else if(askPermission(sender, "mudkips.portal", false, true, ChatColor.RED + "You may not set a portal's destination.")) {
           if(args.length >= 2) {
             boolean destinationIsWarp = false;
             String warpName = null;
             Location destinationLocation = null;
             if("-w".equalsIgnoreCase(args[0]) || "--warp".equalsIgnoreCase(args[0])) {
               if(args.length > 2) {
                 warpName = StringUtil.concatenate(args, " ", 1);
               } else {
                 warpName = args[1];
               }
               destinationIsWarp = true;
             } else if("-l".equalsIgnoreCase(args[0]) || "--location".equalsIgnoreCase(args[0])) {
               if(args.length >= 5) {
                 World destinationWorld = worldUtil.attainWorld(args[1]);
                 if(destinationWorld != null && StringUtil.isRealNumber(args[2]) && StringUtil.isRealNumber(args[3]) && StringUtil.isRealNumber(args[4])) {
                   try {
                     destinationLocation = new Location(destinationWorld, Double.parseDouble(args[2]), Double.parseDouble(args[3]), Double.parseDouble(args[4]));
                   } catch(Exception exc) { }
                 }
               } else if(args.length == 4) {
                 if(StringUtil.isRealNumber(args[2]) && StringUtil.isRealNumber(args[3]) && StringUtil.isRealNumber(args[4])) {
                   try {
                     destinationLocation = new Location(pSender.getWorld(), Double.parseDouble(args[2]), Double.parseDouble(args[3]), Double.parseDouble(args[4]));
                   } catch(Exception exc) { }
                 }
               }
             }
             Block targetBlock = pSender.getTargetBlock(null,50);
             if(targetBlock.getType() == Material.PORTAL) {
               if("-r".equalsIgnoreCase(args[0]) || "--remove".equalsIgnoreCase(args[0])) {
                 portalHandler.removePortal(targetBlock);
               } else if(destinationIsWarp) {
                 if(warpHandler.getWarp(warpName) != null) {
                   portalHandler.registerPortal(targetBlock, warpName);
                 } else {
                   pSender.sendMessage("Can not register Portal, destination warp \"" + warpName + "\" does not exist!");
                 }
               } else {
                 if(destinationLocation.getBlockY() < 1) {
                   destinationLocation.setY(destinationLocation.getWorld().getHighestBlockYAt(destinationLocation));
                 }
                 portalHandler.registerPortal(targetBlock, destinationLocation);
               }
             }
           } else {
             pSender.sendMessage(ChatColor.RED + "Insufficient parameter count(no destination).");
           }
         }
       } else if(rawCommand.equals("pvp")) {
         if(mPlayer != null) {
           if(askPermission(sender, "mudkips.pvp", true, true, "You may not use this command.")) {
             if(args.length > 0) {
               if(askPermission(sender, "mudkips.pvp.change", true, true, "I am sorry, I can't let you do that.")) {
                 Boolean passedArgument = null;
                 if("on".equalsIgnoreCase(args[0]) || "true".equalsIgnoreCase(args[0]) || "1".equalsIgnoreCase(args[0]) || "enable".equalsIgnoreCase(args[0]) || "yes".equalsIgnoreCase(args[0])) {
                   passedArgument = new Boolean(true);
                 } else if("off".equalsIgnoreCase(args[0]) || "false".equalsIgnoreCase(args[0]) || "0".equalsIgnoreCase(args[0]) || "disable".equalsIgnoreCase(args[0]) || "no".equalsIgnoreCase(args[0])) {
                   passedArgument = new Boolean(false);
                 }
                 if(passedArgument != null) {
                   if(passedArgument.booleanValue()) {
                     mPlayer.setPvpEnabled(true);
                     mPlayer.sendMessage(ChatColor.YELLOW + "Notice: PvP was enabled.");
                   } else {
                     if(!mPlayer.fightCooldownElapsed()) {
                       mPlayer.sendMessage(ChatColor.RED + "The Cooldown did not pass by, wait a bit to disable pvp.");
                     } else {
                       mPlayer.setPvpEnabled(false);
                       mPlayer.sendMessage(ChatColor.YELLOW + "Notice: PvP was disabled.");
                     }
                   }
                 } else {
                   mPlayer.sendMessage(ChatColor.YELLOW + "Notice: Could not parse the Arguments.");
                 }
               }
             } else {
               mPlayer.sendMessage(ChatColor.YELLOW + "PvP is " + (mPlayer.isPvpEnabled()?"enabled":"disabled") + " for you." + (hasPermission(sender, "mudkips.pvp.change", true, true)?("\n"+ChatColor.YELLOW+"You may "+(mPlayer.isPvpEnabled()?"disable it by typing \"/pvp off\"":"enable it by typing /pvp on")):""));
             }
           }
         }
       } else if(rawCommand.equals("inventory")) {
         if(askPermission(sender, "mudkips.inventory", false, true, "You may not use that command.")) {
           if(args.length > 0) {
             Player p = this.matchPlayer(args[0]);
             if(p != null) {
               ItemStack[] inventory = new ItemStack[36];
               //ArrayList<ItemStack> list = new ArrayList<ItemStack>(36);
               int stackCount = 0;
               for(ItemStack stack : p.getInventory().getContents()) {
                 if(stack != null) {
                   if(stack.getType().isBlock()) {
                     boolean couldAdd = false;
                     for(int i = 0; i<stackCount; i++) {
                       if(inventory[i].getTypeId() == stack.getTypeId() && inventory[i].getData().equals(stack.getData())) {
                         inventory[i].setAmount(inventory[i].getAmount() + stack.getAmount());
                         couldAdd = true;
                         break;
                       }
                     }
                     if(!couldAdd)
                       inventory[stackCount++] = new ItemStack(stack.getTypeId(), stack.getAmount(), stack.getDurability(), stack.getData().getData());
                   } else {
                     inventory[stackCount++] = new ItemStack(stack.getTypeId(), stack.getAmount(), stack.getDurability(), stack.getData().getData());
                   }
                 }
               }
               if(stackCount > 0) { 
                 int longestNameA = 0, longestNameB = 0, rowSize = (int)((stackCount+1)/2.00);
                 int deciDigitsA = 1, deciDigitsB = 1;
                 for(int i = 0; i < stackCount; i++) {
                   int materialNameLength = inventory[i].getType().toString().length();
                   int deciDigits = Integer.toString(inventory[i].getAmount()).length();
                   if(i < rowSize) {
                     if(materialNameLength > longestNameA)
                       longestNameA = materialNameLength;
                     if(deciDigits > deciDigitsA)
                       deciDigitsA = deciDigits;
                   } else {
                     if(materialNameLength > longestNameB)
                       longestNameB = materialNameLength;
                     if(deciDigits > deciDigitsB)
                       deciDigitsB = deciDigits;
                   }
                 }
                 StringBuilder buf = new StringBuilder("Inventory of " + p.getName() + ": ");
                 for(int i = 0; i < rowSize; i++) {
                   StringBuilder currentLine = new StringBuilder();
                   Material mat = inventory[i].getType();
                   String materialName = mat.toString();
                   int amount = inventory[i].getAmount();
                   int deciDigits = Integer.toString(amount).length();
                   boolean monospace = !(sender instanceof Player);
                   int count = monospace?0:StringUtil.count(materialName, "I");
                   boolean obtainable = !WorldUtil.notObtainables.contains(mat.getId());
                   if(!obtainable)
                   currentLine.append(ChatColor.RED);
                   if(count < 1) {
                     currentLine.append(StringUtil.fillUp(materialName, materialName.length(), '_', longestNameA - materialName.length()));
                   } else {
                     currentLine.append(StringUtil.fillUp(StringUtil.fillUp(materialName, materialName.length(), '_', longestNameA - materialName.length()), longestNameA, '.', count));
                   }
                   if(!obtainable)
                     currentLine.append(ChatColor.WHITE);
                   currentLine.append(": ");
                   boolean tooMuch = amount > mat.getMaxStackSize() && !mat.isBlock(); 
                   if(tooMuch)
                     currentLine.append(ChatColor.RED);
                   currentLine.append(StringUtil.fillUp(Integer.toString(amount), 0, '0', deciDigitsA - deciDigits));
                   if(tooMuch)
                     currentLine.append(ChatColor.WHITE);
                   currentLine.append("  | ");
                   if((rowSize+i) < stackCount) {
                     mat = inventory[rowSize+i].getType();
                     materialName = mat.toString();
                     amount = inventory[rowSize+i].getAmount();
                     deciDigits = Integer.toString(amount).length();
                     count = monospace?0:StringUtil.count(materialName, "I");
                     obtainable = !WorldUtil.notObtainables.contains(mat.getId());
                     if(!obtainable)
                       currentLine.append(ChatColor.RED);
                     if(count < 1) {
                       currentLine.append(StringUtil.fillUp(materialName, materialName.length(), '_', longestNameB - materialName.length()));
                     } else {
                       currentLine.append(StringUtil.fillUp(StringUtil.fillUp(materialName, materialName.length(), '_', longestNameB - materialName.length()), longestNameB, '.', count));
                     }
                     if(!obtainable)
                       currentLine.append(ChatColor.RED);
                     currentLine.append(": ");
                     tooMuch = amount > mat.getMaxStackSize() && !mat.isBlock(); 
                     if(tooMuch)
                       currentLine.append(ChatColor.RED);
                     currentLine.append(StringUtil.fillUp(Integer.toString(amount), 0, '0', deciDigitsB - deciDigits));
                   }
                   buf.append("\n");
                   buf.append(currentLine);
                 }
                 if(mPlayer != null) {
                   mPlayer.sendMessage(buf.toString());
                 } else {
                   sender.sendMessage(buf.toString());
                 }
                 return true;
               } else {
                 if(mPlayer != null) {
                   mPlayer.sendMessage(p.getName()+"'s Inventory is empty.");
                 } else {
                   sender.sendMessage(p.getName()+"'s Inventory is empty.");
                 }
                 return true;
               }
             } else {
               sender.sendMessage(ChatColor.RED + "Player \""+args[0]+"\" could not be associated.");
             }
           } else {
             sender.sendMessage(ChatColor.RED + "No player specified");
           }
         }
       } else if(rawCommand.equalsIgnoreCase("vicinity")) {
         if(askPermission(sender, "mudkips.vicinity", true, true, "You may not check if players are in your chat vicinity.")) {
           if(sender.isOp() && !(sender instanceof org.bukkit.craftbukkit.entity.CraftPlayer)) {
             sender.sendMessage("Sorry, no vicinity command for you, console");
             return true;
           } else {
             if(myProps.getIntProperty("chat-propagation-distance") < 1) {
               StringBuilder buf = new StringBuilder(ChatColor.YELLOW + "Players in your chat vicinity: ");
               boolean isFirstPlayer = true;
               Player [] players = null;
               if(myProps.getIntProperty("chat-propagation.distance")==0) {
                 players = pSender.getWorld().getPlayers().toArray(new Player[0]); 
               } else {
                 players = this.getServer().getOnlinePlayers();
               }
               for(Player p : players) {
                 if(isFirstPlayer) {
                   isFirstPlayer = false;
                 } else {
                   buf.append(", ");
                 }
                 buf.append(p.getDisplayName());
               }
               if(mPlayer != null) {
                 mPlayer.sendMessage(buf.toString());
               } else {
                 sender.sendMessage(buf.toString());
               }
               return true;
             } else {
               List<Player> players = pSender.getWorld().getPlayers();
               Location originLoc = pSender.getLocation();
               StringBuilder buf = new StringBuilder(ChatColor.YELLOW + "Players in your chat vicinity: ");
               boolean isFirstPlayer = true;
               for(Player p : players) {
                 if(WorldUtil.calcDistance(originLoc, p.getLocation(), myProps.getBooleanProperty("distance-check-height")) < myProps.getIntProperty("chat-propagation-distance")) {
                   if(isFirstPlayer) {
                     isFirstPlayer = false;
                   } else {
                     buf.append(", ");
                   }
                   buf.append(p.getDisplayName());
                 }
               }
               if(mPlayer != null) {
                 mPlayer.sendMessage(buf.toString());
               } else {
                 sender.sendMessage(buf.toString());
               }
               return true;
             }
           }
         }
       } else if(rawCommand.equalsIgnoreCase("kick")) {
         if(isConsole) {
           if(args.length > 0) {
             Player p = matchPlayer(args[0]);
             p.kickPlayer(StringUtil.concatenate(args, " ", 1));
           } else {
             sender.sendMessage(ChatColor.RED + "No parameters given.");
             sender.sendMessage(ChatColor.YELLOW + "Usage: /kick <player>");
           }
         } else if(askPermission(sender, "mudkips.kick", false, true, "You may not kick other players.")) {
           if(args.length > 0) {
             Player p = matchPlayer(args[0]);
             if(mPlayer.kickCooldownElapsed()) {
               p.kickPlayer(StringUtil.concatenate(args, " ", 1));
               mPlayer.kicked();
             } else {
               mPlayer.sendMessage(ChatColor.RED + "The kick Cooldown did not elapse.");
             }
           } else {
             mPlayer.sendMessage(ChatColor.RED + "No parameters given.\n"+ChatColor.YELLOW+"Usage: /kick <player>");
           }
         }
       } else if(rawCommand.equalsIgnoreCase("tempban")) {
         if(myProps.getBooleanProperty("enable-bans")) {
           if(args.length >= 2) {
             if(askPermission(sender, "mudkips.tempban", false, true, "You may not tempban.")) {
               if(banHandler != null) {
                 String reason = "You are temporarily banned.";
                 String playerToBan = args[0];
                 if(args.length >= 3) {
                   reason = StringUtil.concatenate(args, " ", 2);
                 }
                 long banDuration = StringUtil.parseDuration(args[1]);
                 if(isConsole) {
                   banHandler.banPlayer(playerToBan, banDuration, reason);
                   Player p = getServer().getPlayer(playerToBan);
                   if(p != null) p.kickPlayer(reason);
                   return true;
                 } else if(mPlayer != null) {
                   if(mPlayer.banCooldownElapsed()) {
                     banHandler.banPlayer(playerToBan, banDuration, reason);
                     Player p = getServer().getPlayer(playerToBan);
                     if(p != null) p.kickPlayer(reason);
                     return true;
                   }
                 }
               } else {
                 sender.sendMessage(ChatColor.RED + "BanHandler is not loaded, could not perform command!");
                 this.getServer().getLogger().severe("BanHandler is null, 'though \"enable-bans\" is true!");
                 return false;
               }
             }
           } else {
             sender.sendMessage(ChatColor.RED + "Not enough parameters!");
             return true;
           }
         } else {
           sender.sendMessage(ChatColor.RED + "Tempbanning is disabled!");
           return true;
         }
       } else if(rawCommand.equalsIgnoreCase("jail")) {
         if(myProps.getBooleanProperty("enable-jail")) {
           if(askPermission(sender, "mudkips.jail", false, true, "You may not jail others!")) {
             if(args.length >= 1) {
               MudkipsPlayer mPlayerToJail = getMudkipsPlayer(args[0]);
               if(mPlayerToJail != null) {
                 if(args.length >= 2) {
                   long time = StringUtil.parseDuration(args[1]);
                   mPlayerToJail.jail(time);
                   return true;
                 }
               } else {
                 sender.sendMessage(ChatColor.YELLOW + "Notice: Could not associate Player \"" + args[0] + "\"");
                 return true;
               }
             } else {
               sender.sendMessage(ChatColor.YELLOW + "Notice: Not enough Parameters.");
               return true;
             }
           }
         } else {
           sender.sendMessage(ChatColor.YELLOW + "Notice: Jailing is disabled.");
           return true;
         }
       } else if(rawCommand.equalsIgnoreCase("kill")) {
         if(isConsole) {
           if(args.length >= 1) {
             MudkipsPlayer playerToKill = this.getMudkipsPlayer(args[0]);
             if(playerToKill != null) {
               playerToKill.kill(null);
             } else {
               sender.sendMessage(ChatColor.YELLOW + "Notice: Could not associate Player \"" + args[0] + "\"");
             }
           } else {
              sender.sendMessage(ChatColor.YELLOW + "Notice: Not enough parameters.");
           }
         } else {
           if(mPlayer != null) {
             boolean killSelf = hasPermission(sender, "mudkips.kill.self", true, true),
                     killOther = hasPermission(sender, "mudkips.kill.other", false, true); 
             if(!killOther && killSelf) {
               mPlayer.kill(mPlayer);
             } else {
               if(killOther && args.length >= 1) {
                 MudkipsPlayer playerToKill = getMudkipsPlayer(args[0]);
                 if(playerToKill != null) {
                   playerToKill.kill(mPlayer);
                 } else {
                   mPlayer.sendMessage(ChatColor.YELLOW + "Notice: Could not associate Player \"" + args[0] + "\"");
                 }
               } else if(!killSelf) {
                 mPlayer.sendMessage(ChatColor.YELLOW + "Notice: You may not kill yourself that easily.");
               } else if(killSelf) {
                 mPlayer.kill(mPlayer);
               }
             }
           } else {
             sender.sendMessage(ChatColor.YELLOW + "Notice: Can not perform command, sorry.");
           }
         }
       }
      return true;
    }
  public void saveProperties() {
    myProps.save();
  }
  public void setHomeBed(Player p, Location bed) {
    MudkipsPlayer mPlayer = playerProvider.get(p);
    if(mPlayer != null) {
      mPlayer.setHomeBed(bed);
    }
  }
  public void playerJoin(PlayerJoinEvent event) {
    event.setJoinMessage(null);
    this.getServer().getScheduler().scheduleAsyncDelayedTask(this, new DelayedLoginEvent(this, event), 1);
  }
  public void delayedLoginHandle(PlayerJoinEvent event) {
    Player p = event.getPlayer();
    playerProvider.playerJoin(p);
    sendMotd(p);
    MudkipsPlayer mPlayer = getMudkipsPlayer(p.getName());
    if(mPlayer != null) {
      int propagationDistance = myProps.getIntProperty("join-quit-propagation-distance");
      String msg = StringUtil.replaceAll(myProps.getProperty("join-message"), new char[] {'s', 'm'}, new String[] { p.getName() , mPlayer.displayName()});
      chatObj.sendMessageAround(msg, p.getLocation(), propagationDistance);
    }
  }
  public void playerQuit(PlayerQuitEvent event) {
    Player p = event.getPlayer();
    MudkipsPlayer mPlayer = getMudkipsPlayer(p.getName());
    if(mPlayer != null) {
      playerProvider.playerQuit(p.getName());
      int propagationDistance = myProps.getIntProperty("join-quit-propagation-distance");
      String msg = StringUtil.replaceAll(myProps.getProperty("quit-message"), new char[] {'s', 'm'}, new String[] { p.getName() , mPlayer.displayName()});
      if(propagationDistance  == -1)
        event.setQuitMessage(msg);
      else {
        event.setQuitMessage(null);
        chatObj.sendMessageAround(msg, p.getLocation(), propagationDistance);
      }
    } else {
      this.getServer().getLogger().warning("Could not associate MudkipsPlayer with \"" + p.getName() + "\"");
    }
  }
  public void playerPreLogin(PlayerPreLoginEvent e) {
    if(banHandler != null) {
      String reason = banHandler.getBanned(e.getName());
      if(reason != null) {
        e.disallow(PlayerPreLoginEvent.Result.KICK_OTHER, reason);
      }
    }
  }
  public void playerDied(org.bukkit.event.entity.PlayerDeathEvent e) {
    Player p = (Player) e.getEntity();
    MudkipsPlayer mPlayer = getMudkipsPlayer(p.getName());
    if(mPlayer != null) {
      mPlayer.died(e);
    } else {
      this.getServer().getLogger().warning("Could not associate MudkipsPlayer with \"" + p.getName() + "\"");
    }
    e.setDeathMessage(null);
  }
  public void playerChat(PlayerChatEvent e) {
    Player p = e.getPlayer();
    String msg = e.getMessage();
   MudkipsPlayer mP = playerProvider.get(p);
   if( mP != null) {
     if(mP.inPrivateChat()) {
       if(askPermission(p, "mudkips.chat.message", true, true, ChatColor.RED + "You are not authorized to message others!"))
         chatObj.playerChat(p, Chat.Type.PERSIST, new String[] {msg});
       e.setCancelled(true);
     }
   }
   if(!e.isCancelled()) {
     if(askPermission(p, "mudkips.chat.say", true, true, ChatColor.RED + "You are not authorized to say anything!")) {
       chatObj.playerChat(p, Chat.Type.SAY, new String[] {msg});
     }
     e.setCancelled(true);
    }
  }
  public boolean playerFight(Player attacker, Player victim, int damage) {
    MudkipsPlayer mAttacker = getMudkipsPlayer(attacker.getName()), mVictim = getMudkipsPlayer(victim.getName());
    if(mAttacker == null || mVictim == null) {
      return myProps.getBooleanProperty("pvp-enabled", true);
    }
    return mAttacker.playerFight(mVictim, damage);
  }
  public void portaling(PlayerPortalEvent e) {
    if(portalHandler != null) {
      portalHandler.onPlayerPortal(e);
    }
  }
  public void playerTeleport(PlayerTeleportEvent e) {
    MudkipsPlayer mPlayer = this.playerProvider.lookup(e.getPlayer().getName());
    if(mPlayer != null) {
      if(!mPlayer.mayTeleport(e.getFrom(), e.getTo())) {
        e.setCancelled(true);
      }
    }
  }
  public void playerRespawn(PlayerRespawnEvent e) {
    MudkipsPlayer mPlayer = getMudkipsPlayer(e.getPlayer().getName());
    if(mPlayer != null) {
      mPlayer.respawn(e);
    } else {
      this.getServer().getLogger().warning("Could not associate MudkipsPlayer with \"" + e.getPlayer().getName() + "\"");
    }
  }
  public void playerMentioned(MudkipsPlayer mP, Player byPlayer) {
    if(mP.isAfk()) {
      byPlayer.sendMessage(afkNotification(mP));
    }
  }
  public String afkNotification(MudkipsPlayer playerAfk) {
//    if(playerAfk.isAfk())
     return StringUtil.replaceAll(myProps.getProperty("afk-message"), new char[] {'s', 'm'}, new String[] { playerAfk.displayName(), playerAfk.afkMessage()});
      
  }
  public MudkipsPlayer getMudkipsPlayer(String playerName) {
    return playerProvider.get(playerName);
  }
  public MudkipsPlayer getMudkipsPlayer(Player player) {
    if(player != null) {
      return playerProvider.get(player.getName());
    } else {
      return null;
    }
  }
  public void sendMotd(CommandSender p) {
    try {
      if(p instanceof Player) {
        MudkipsPlayer mPlayer = getMudkipsPlayer(((Player)p).getName());
        if(mPlayer == null) {
          p.sendMessage(StringUtil.replaceAll(myProps.getProperty("motd"), 's', ((Player) p).getDisplayName()));
        } else {
          p.sendMessage(StringUtil.replaceAll(myProps.getProperty("motd"), 's', mPlayer.displayName()));
        }
      } else {
        p.sendMessage(StringUtil.replaceAll(myProps.getProperty("motd"), 's', myProps.getProperty("chat-server-name")));
      }
    } catch(NullPointerException exc) {
      this.getServer().getLogger().log(Level.WARNING, "NullPointerException while sending MOTD!");
      exc.printStackTrace();
    }
  }
  //Suggestion by winterschwert: Make it so, that abbreviation is only allowed for the beginning of the name, so that 'schwert' wouldnt trigger on player 'winterschwert', but 'winter' would
  public Player matchPlayer(String playerName) {
    String playerNameLC = playerName.toLowerCase();
    java.util.List<Player> playerList = this.getServer().matchPlayer(playerName);
    if(playerList.size() > 0) {
      for(Player pMatch : playerList) {
        if(pMatch.getName().toLowerCase().equals(playerNameLC) || (pMatch.getName().toLowerCase().indexOf(playerNameLC) == 0 && ((pMatch.getName().length()*0.2) < playerName.length()) && (playerName.length() >= 2))) {
          return pMatch;
        }
      }
    }
    if(playerProvider.has(playerName)) {
      return playerProvider.get(playerName).getPlayer();
    } else {
      MudkipsPlayer mPlayer = playerProvider.getByAlterName(playerName);
      if(mPlayer != null) {
        return mPlayer.getPlayer();
      } else {
        return null;
      }
    }
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
      }
      worldToSetThunderIn.setThundering(true);
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

  protected MudkipsPlayerProvider getPlayerProvider() {
    return this.playerProvider;
  }
}

