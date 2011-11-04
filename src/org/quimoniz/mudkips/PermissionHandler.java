package org.quimoniz.mudkips;

import java.util.HashSet;
import java.util.Arrays;
import java.util.logging.Level;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.Server;

public class PermissionHandler {
  public enum PermissionType {
          NONE(0), BUKKIT(1), NIJIKOKUN(2);
          private int type = 0;
          private PermissionType(int val) {
            type=val;
          }
          private static HashSet[] typeLists;
          static {
            typeLists = new HashSet[3];
            typeLists[0] = new java.util.HashSet<String>(Arrays.asList("none","void","null","0","off","disabled","deactivated","deactivate","mudkips","no","cats","false"));
            typeLists[1] = new java.util.HashSet<String>(Arrays.asList("bukkit","bukkit's","default","on","enable","enabled","1","superperms","superperm","build1000","build1000perm","build1000perms","1000perm","1000perms","activate","activated","true"));
            typeLists[2] = new java.util.HashSet<String>(Arrays.asList("nijiko","nijikokun","nijikokunperm","nijikokunperms","yeti","theyeti","yetipermission","yetipermissions","phoenix","phoenixpermissions","legacy","thepluginoftomorrow"));
          }
          public static PermissionType byName(String permName) {
            StringBuilder paramBuf = new StringBuilder(permName.toLowerCase());
            int spacePos = 0;
            while((spacePos = paramBuf.indexOf(" ", spacePos)) >= 0) {
              paramBuf.deleteCharAt(spacePos);
            }
            String rawParam = paramBuf.toString();
            if(typeLists[0].contains(rawParam)) {
              return PermissionType.NONE;
            } else if(typeLists[1].contains(rawParam)) {
              return PermissionType.BUKKIT;
            } else if(typeLists[2].contains(rawParam)) {
              return PermissionType.NIJIKOKUN;
            } else {
              return null;
            }
          }
  };
  private PermissionType type = null;
  private Object permissionObj;
  private Server server;
  public PermissionHandler(Server server) {
    this(server, null);
  }
  public PermissionHandler(Server server, String permType) {
    if(permType != null) {
      type = PermissionType.byName(permType);
    }
    if(type == null) {
      boolean isNijikokun = false;
      boolean foundPermissionPlugin = false;
      for(org.bukkit.plugin.Plugin curPlugin : server.getPluginManager().getPlugins()) {
        String pluginName = curPlugin.getDescription().getName().toLowerCase();
        if(pluginName.indexOf("permission")>-1 || pluginName.indexOf("perms")>-1) {
          foundPermissionPlugin = true;
          try {
            com.nijikokun.bukkit.Permissions.Permissions nijikokunPerm;
            isNijikokun = true;
          } catch(NoClassDefFoundError exc) { }
        }
      }
      if(foundPermissionPlugin) {
        if(isNijikokun) {
          type = PermissionType.NIJIKOKUN;
          System.out.println("Found a Nijikokun Permissions Plugin! Switching to Nijikokun Permissions.");
        } else {
          type = PermissionType.BUKKIT;
          System.out.println("Found a Permissions Plugin! Switching to Bukkit's inbuilt permission System \"Superperms\"!");
        }
      } else {
        System.out.println("Found no Permissions plugin, using NONE!");
      }
      type = PermissionType.NONE;
    }
  }
    private boolean setupPermissions() {
      switch(type) {
        case BUKKIT:
          //Bukkit default Permissions "SuperPerms" dont require setup
          return true;
        case NIJIKOKUN:
          if(permissionObj == null) {
            Plugin permPlugin = server.getPluginManager().getPlugin("Permissions");
            if(permPlugin != null) {
              this.permissionObj = ((com.nijikokun.bukkit.Permissions.Permissions)permPlugin).getHandler();
              return true;
            }
            if(permissionObj == null) {
              server.getLogger().log(Level.SEVERE, "Mudkips couldn't obtain Permissions Handler!");
              return false;
            } else { // This case can not be reached! However it produces error "This method must return [..]" if omitted
              return true;
            }
          } else return true;
        case NONE:
        default:
          return true;
      }
    }
    public boolean hasPermission(CommandSender p, String permNode, boolean defaultValNonOp, boolean defaultValOp) {
      switch(type) {
        case BUKKIT:
          return p.hasPermission(permNode);
        case NIJIKOKUN:
          if(permissionObj == null) {
            setupPermissions();
          }
          if(permissionObj == null) {
            server.getLogger().severe("Null pointer exception, switched from Nijikokun to no Permissions.");
            type = PermissionType.NONE;
            return hasPermission(p, permNode, defaultValNonOp, defaultValOp);
          } else {
            try {
              return ((com.nijiko.permissions.PermissionHandler)permissionObj).has((Player)p, permNode);
            } catch(NoClassDefFoundError exc) {
              server.getLogger().severe("There is no nijikokun Permissions Plugin active, switched off Permissions.");
              type = PermissionType.NONE;
              return hasPermission(p, permNode, defaultValNonOp, defaultValOp);
            }
          }
        case NONE:
        default:
          if(p == null) return defaultValNonOp;
          if(!(p instanceof Player) && p.isOp()) {// Console is Op
            return defaultValOp;
          }
          return p.isOp() ? defaultValOp : defaultValNonOp;
      }
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
}
