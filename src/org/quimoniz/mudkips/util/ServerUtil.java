package org.quimoniz.mudkips.util;

import org.bukkit.Server;
import org.bukkit.craftbukkit.CraftServer;

public class ServerUtil {
  private Server server;
  public ServerUtil(Server server) {
    this.server = server;
  }
  public String getProperty(String key) {
    CraftServer craftServa = null;
    if(server instanceof CraftServer) {
      craftServa = (CraftServer) server;
      net.minecraft.server.MinecraftServer mcServer = craftServa.getServer();
      net.minecraft.server.PropertyManager mcProperties = mcServer.propertyManager;
      return mcProperties.properties.getProperty("level-name");
    } else {
      return null;
    }
  }
}
