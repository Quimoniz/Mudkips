package org.quimoniz.mudkips.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.Material;
import org.bukkit.event.block.BlockPlaceEvent;
import java.util.HashSet;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.block.Block;
import org.bukkit.event.block.BlockSpreadEvent;
import org.quimoniz.mudkips.Mudkips;
import org.quimoniz.mudkips.player.MudkipsPlayer;
import org.bukkit.event.block.BlockEvent;

public class BlockInteractListener implements Listener { 
  private Mudkips mudkips;
  //oh yes, it could be done more efficiently with instead using a HashSet, and the simple occurence of a String value would determine if it was set to TRUE. However this is more nice, and HashMap performs very well, on average below 1/10 millisecond per put operation, maybe memory usage might be an issue with it..
  //did it.. 
  //well speaking of optimization, I could keep a count of loaded worlds, and save that, in order to make it on reload accessible for all the HashMap and HashSet initializations.. 
  private HashSet<String> blockJailBreak = new HashSet<String>(), blockJailPlace = new HashSet<String>();
  private HashSet<Integer> blockFirePlace = null;
  private boolean blockFirePlacementCompletly = false;
  private HashSet<Integer> blockBurningIds = null;
  private boolean blockBurningCompletly = false;
  private HashSet<String> tallGrassMakesGrass = new HashSet<String>();
  public boolean blockIgniteWasRegistered = false;
  public boolean blockPlacementWasRegistered = false;
  public boolean blockObsidianDigging = false;
  public BlockInteractListener(Mudkips mudkips) {
    this.mudkips = mudkips;
  }
  //TODO: remove this obsolete method, breaking is checked with onBlockBreak 
  @EventHandler public void onBlockDamage(BlockDamageEvent e) {
    if(e.isCancelled()) return;
	if(e.getBlock().getType() == Material.OBSIDIAN) {
	  Player p = e.getPlayer();
	  if(p != null) {
		if(p.getItemInHand().getType() != Material.DIAMOND_PICKAXE) {
		  e.setCancelled(true);
		}
	  }
	}
  }
  @EventHandler public void onBlockBreak(BlockBreakEvent e) {
    if(e.isCancelled()) return;
    if(blockObsidianDigging) {
	  if(e.getBlock().getType() == Material.OBSIDIAN) {
	    Player p = e.getPlayer();
	    if(p != null) {
		  if(p.getItemInHand().getType() != Material.DIAMOND_PICKAXE) {
		    e.setCancelled(true);
		  }
	    }
	  }
    }
    if(containsKey(blockJailBreak, e)) {
      MudkipsPlayer mPlayer = mudkips.getMudkipsPlayer(e.getPlayer().getName());
      if(mPlayer != null) {
        if(mPlayer.isJailed()) {
          e.setCancelled(true);
        }
      }
    }
  }
  @EventHandler public void onBlockPlace(BlockPlaceEvent e) {
    if(e.isCancelled()) return;
    if(containsKey(blockJailPlace, e)) {
      MudkipsPlayer mPlayer = mudkips.getMudkipsPlayer(e.getPlayer().getName());
      if(mPlayer != null) {
        if(mPlayer.isJailed()) {
          e.setCancelled(true);
          return;
        }
      }
    }
    if(e.getBlockPlaced().getTypeId() == 51) {
//      System.out.println("Fire is placed!");
      if(blockFirePlacementCompletly) {
        e.setCancelled(true);
//        System.out.println("Fire is cancelled COMPLETLY");
        return;
      }
      if(blockFirePlace != null && blockFirePlace.contains(new Integer(e.getBlockAgainst().getTypeId()))) {
        e.setCancelled(true);
//        System.out.println("Fire is cancelled SPECIFICALLY");
        return;
      }
    } else if(e.getBlockPlaced().getTypeId() == 31 && containsKey(tallGrassMakesGrass, e)) {
      Block blockBelow = e.getBlockPlaced().getRelative(0, -1, 0);
      if(blockBelow != null && blockBelow.getTypeId() == 3) {
        blockBelow.setTypeId(2, true);
      }
    }
  }
  @EventHandler public void onBlockBurn(BlockBurnEvent e) {
    if(blockBurningCompletly) {
      e.setCancelled(true);
    } else {
      if(blockBurningIds != null && blockBurningIds.contains(e.getBlock().getTypeId())) {
        e.setCancelled(true);
      }
    }
  }
  @EventHandler public void onBlockIgnite(BlockIgniteEvent e) {
//    System.out.println(e.getCause()+" Ignition!");
    switch(e.getCause()) {
      case FLINT_AND_STEEL: //TODO: Actually stop fire placement according to blockFirePlace and blockFirePlacementCompletly
        Player player = e.getPlayer();
        if(player != null) {
          HashSet<Byte> hashSet = new HashSet<Byte>(3);
          hashSet.add(new Byte("51"));
          hashSet.add(new Byte("0"));
          Block blockAgainst = player.getTargetBlock(hashSet, 6);
          if(blockAgainst == null) {
            e.setCancelled(true);
            return;
          } else {
//            System.out.println("Ignition at " + blockTag(e.getBlock()) + ": Block Ids(-2 to +2):" + e.getBlock().getRelative(0, -2, 0).getTypeId() + "," + e.getBlock().getRelative(0, -1, 0).getTypeId()+ "," + e.getBlock().getRelative(0, 0, 0).getTypeId()+ "," + e.getBlock().getRelative(0, 1, 0).getTypeId()+ "," + e.getBlock().getRelative(0, 2, 0).getTypeId());
            if(blockFirePlacementCompletly) {
              e.setCancelled(true);
//              System.out.println("Ignitionis cancelled COMPLETLY!");
            } else {
              if(blockBurningIds != null && blockFirePlace.contains(blockAgainst.getTypeId())) {
                e.setCancelled(true);
//                System.out.println("Ignition cancelled SPECIFICALLY(id:"+blockAgainst.getTypeId()+"), " + (blockBurningIds.contains(blockAgainst.getTypeId())?"contained in BlockList":"NOT contained in BlockList"));
              }
            }
          }
        }
        break;
      case LAVA:
        e.setCancelled(!fireAllowed(e.getBlock()));
        break;
      case SPREAD:
        e.setCancelled(!fireAllowed(e.getBlock()));
        break;
    }
  }
  public static String blockTag(org.bukkit.block.Block b) {
	return b.getType().toString() + " [" + b.getX() + "," + b.getY() + "," + b.getZ() + "]";
  }
  public static String blockTag(org.bukkit.block.BlockState b) {
	return b.getType().toString() + " [" + b.getX() + "," + b.getY() + "," + b.getZ() + "]";
  }
  public static String locTag(org.bukkit.Location loc) {
	return "[" + loc.getBlockX() + "," + loc.getBlockY() + "," + loc.getBlockZ() + "]";
  }
  private boolean fireAllowed(Block fireBlock) {
    int matches = 0;
//    java.util.LinkedList<String> list = new java.util.LinkedList<String>();
    for(Block curBlock : getBlocksAround(fireBlock)) {
      if(curBlock != null && !blockBurningIds.contains(curBlock.getTypeId())) {
        matches++;
//        list.add(curBlock.getType().toString());
      }
    }
    if(matches < 1) {
//      System.out.println("Spread Declined.");
      return false;
    } else {
//      System.out.println("Spread Accepted, matches:"+list);
      return true;
    }
  }
  public static Block[] getBlocksAround(Block centerBlock) {
    Block[] blocksAround = new Block[6];
    blocksAround[0] = centerBlock.getRelative( 0,-1, 0);
    blocksAround[1] = centerBlock.getRelative( 1, 0, 0);
    blocksAround[2] = centerBlock.getRelative( 0, 0, 1);
    blocksAround[3] = centerBlock.getRelative(-1, 0, 0);
    blocksAround[4] = centerBlock.getRelative( 0, 0,-1);
    blocksAround[5] = centerBlock.getRelative( 0, 1, 0);
    return blocksAround;
  }
  public void blockJailPlace(String worldName, boolean watchIt) {
    if(watchIt) this.blockJailPlace.add(worldName);
  }
  public void blockJailBreak(String worldName, boolean watchIt) {
    if(watchIt) this.blockJailBreak.add(worldName);
  }
  public void blockFirePlace(java.util.List<Integer> blocksAgainst) {
    if(blocksAgainst != null) {
      blockFirePlace = new HashSet<Integer>(blocksAgainst);
    } else {
      blockFirePlace = null; 
    }
  }
  public void blockAllFirePlace(boolean blockCompletly) {
    blockFirePlacementCompletly = blockCompletly;
  }
  public void blockBurning(java.util.List<Integer> blocksUninflammable) {
    if(blocksUninflammable != null) {
      this.blockBurningIds = new HashSet(blocksUninflammable);
    } else {
      this.blockBurningIds = null;
    }
  }
  public void blockAllBurning(boolean blockCompletly) {
    blockBurningCompletly = blockCompletly;
  }
  public void tallGrassMakesGrassBlock(String worldName, boolean makes) {
    if(makes) tallGrassMakesGrass.add(worldName);
  }
  private boolean containsKey(HashSet<String> worldNames, BlockEvent event) {
    if(event == null || event.getBlock() == null || event.getBlock().getWorld() == null)
      throw new IllegalArgumentException("Illegal BlockEvent, it has no associated World!");
    return worldNames.contains(event.getBlock().getWorld().getName());
  }
}
