package io.github.yannici.bedwars.Shop.Specials;

import java.util.ArrayList;
import java.util.List;

import io.github.yannici.bedwars.ChatWriter;
import io.github.yannici.bedwars.Main;
import io.github.yannici.bedwars.Game.Game;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import com.google.common.collect.ImmutableMap;

public class RescuePlatform extends SpecialItem {
    
	private int livingTime = 0;
    private Player activatedPlayer = null;
    private List<Block> platformBlocks = null;
    private BukkitTask task = null;
    private Game game = null;
    
    public RescuePlatform() {
        super();
        
        this.platformBlocks = new ArrayList<Block>();
    }

    @Override
    public Material getItemMaterial() {
        return Material.BLAZE_ROD;
    }
    
    public int getLivingTime() {
		return this.livingTime;
	}
    
    @Override
    public boolean executeEvent(Event event) {
    	if(!(event instanceof PlayerInteractEvent)) {
    		return false;
    	}
    	
    	PlayerInteractEvent ev = (PlayerInteractEvent)event;
        if(super.returnPlayerEvent(ev.getPlayer())) {
            return false;
        }
        
        Player player = ev.getPlayer();
        Game game = Game.getGameOfPlayer(player);
        
        for(SpecialItem item : game.getSpecialItems()) {
        	if(!(item instanceof RescuePlatform)) {
        		continue;
        	}
        	
        	RescuePlatform platform = (RescuePlatform)item;
        	if(!platform.getPlayer().equals(player)) {
        		continue;
        	}
        	
        	RescuePlatform platformItem = (RescuePlatform)item;
        	
        	int waitleft = Main.getInstance().getConfig().getInt("specials.rescue-platform.useing-wait-time", 20) - platformItem.getLivingTime();
        	player.sendMessage(ChatWriter.pluginMessage(Main._l("ingame.specials.rescue-platform.left", ImmutableMap.of("time", String.valueOf(waitleft)))));
        	return false;
        }
        
        if(player.getLocation().getBlock().getRelative(BlockFace.DOWN).getType() != Material.AIR) {
            player.sendMessage(ChatWriter.pluginMessage(ChatColor.RED + Main._l("errors.notinair")));
            return false;
        }
        
        Location mid = player.getLocation().clone();
        mid.setY(mid.getY()-1.0D);
        
        ItemStack usedStack = player.getInventory().getItemInHand();
        usedStack.setAmount(usedStack.getAmount()-1);
        player.getInventory().setItem(player.getInventory().getHeldItemSlot(), usedStack);
        player.updateInventory();
        for(BlockFace face : BlockFace.values()) {
            if(face.equals(BlockFace.DOWN) || face.equals(BlockFace.UP)) {
                continue;
            }
            
            Block placed = mid.getBlock().getRelative(face);
            if(placed.getType() != Material.AIR) {
                continue;
            }
            
            placed.setType(Material.GLASS);
            
            game.getRegion().addPlacedUnbreakableBlock(placed, null);
            this.platformBlocks.add(placed);
        }
        
        this.activatedPlayer = player;
        this.game = game;
        this.runTask();
        
        this.game.addSpecialItem(this);
        return true;
    }
    
    public Player getPlayer() {
    	return this.activatedPlayer;
    }
    
    public void runTask() {
    	if(Main.getInstance().getConfig().getInt("specials.rescue-platform.break-time", 10) == 0
    			&& Main.getInstance().getConfig().getInt("specials.rescue-platform.useing-wait-time", 20) == 0) {
    		// not break and no wait time ;)
    		return;
    	}
    	
        this.task = new BukkitRunnable() {
			
			@Override
			public void run() {
				RescuePlatform.this.livingTime++;
				if(RescuePlatform.this.livingTime == Main.getInstance().getConfig().getInt("specials.rescue-platform.break-time", 10)) {
					for(Block block : RescuePlatform.this.platformBlocks) {
						block.getChunk().load(true);
						block.setType(Material.AIR);
						RescuePlatform.this.game.getRegion().removePlacedUnbreakableBlock(block);
					}
				}
				
				int wait = Main.getInstance().getConfig().getInt("specials.rescue-platform.useing-wait-time", 20);
				if(RescuePlatform.this.livingTime >= wait
						&& wait > 0) {
					RescuePlatform.this.game.removeRunningTask(this);
					RescuePlatform.this.game.removeSpecialItem(RescuePlatform.this);
					RescuePlatform.this.task = null;
					this.cancel();
					return;
				}
				
				if(wait <= 0 && RescuePlatform.this.livingTime >= Main.getInstance().getConfig().getInt("specials.rescue-platform.break-time", 10)) {
					RescuePlatform.this.game.removeRunningTask(this);
					RescuePlatform.this.game.removeSpecialItem(RescuePlatform.this);
					RescuePlatform.this.task = null;
					this.cancel();
					return;
				}
			}
		}.runTaskTimer(Main.getInstance(), 20L, 20L);
		this.game.addRunningTask(this.task);
    }

}
