package io.github.yannici.bedwars.Shop.Specials;

import io.github.yannici.bedwars.ChatWriter;
import io.github.yannici.bedwars.Main;
import io.github.yannici.bedwars.Utils;
import io.github.yannici.bedwars.Game.Game;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.google.common.collect.ImmutableMap;

public class ProtectionWall extends SpecialItem {
	
	private List<Block> wallBlocks = null;
	private Player owner = null;
	private Game game = null;
	private int livingTime = 0;
	
	public ProtectionWall() {
		super();
		this.wallBlocks = new ArrayList<Block>();
		this.owner = null;
		this.game = null;
	}

	@Override
	public Material getItemMaterial() {
		return Utils.getMaterialByConfig("specials.protection-wall.item", Material.BRICK);
	}
	
	public List<Block> getWallBlocks() {
		return this.wallBlocks;
	}

	@Override
	public Material getActivatedMaterial() {
		return null;
	}
	
	public Player getOwner() {
		return this.owner;
	}

	public Game getGame() {
		return this.game;
	}

	public int getLivingTime() {
		return this.livingTime;
	}

	@SuppressWarnings("unused")
	public void create(Player player, Game game) {
		this.owner = player;
		this.game = game;
		
		int breakTime = Main.getInstance().getIntConfig("specials.protection-wall.break-time", 0);
		int waitTime = Main.getInstance().getIntConfig("specials.protection-wall.wait-time", 20);
		int width = Main.getInstance().getIntConfig("specials.protection-wall.width", 4);
		int height = Main.getInstance().getIntConfig("specials.protection-wall.height", 4);
		int distance = Main.getInstance().getIntConfig("specials.protection-wall.distance", 2);
		boolean canBreak = Main.getInstance().getBooleanConfig("specials.protection-wall.can-break", true);
		Material blockMaterial = Utils.getMaterialByConfig("specials.protection-wall.block", Material.SANDSTONE);
		
		if(breakTime > 0 || waitTime > 0) {
			ProtectionWall livingWall = this.getLivingWall();
			if(livingWall != null) {
				int waitLeft = waitTime - livingWall.getLivingTime();
	            player.sendMessage(ChatWriter.pluginMessage(Main._l("ingame.specials.protection-wall.left", ImmutableMap.of("time", String.valueOf(waitLeft)))));
	            return;
			}
		}
		
		Location wallLocation = Utils.getDirectionLocation(player.getLocation(), distance);
		ItemStack usedStack = player.getInventory().getItemInHand();
        usedStack.setAmount(usedStack.getAmount()-1);
        player.getInventory().setItem(player.getInventory().getHeldItemSlot(), usedStack);
        player.updateInventory();
        
        /*BlockFace face = wallLocation.getBlock().getFace(player.getLocation().getBlock());
        for(int w = 0; w < width; w++) {
        	for(int h = 0; h < height; h++) {
        		
        	}
        }*/
	}
	
	private ProtectionWall getLivingWall() {
		for(SpecialItem item : game.getSpecialItems()) {
            if(!(item instanceof ProtectionWall)) {
                continue;
            }
            
            ProtectionWall wall = (ProtectionWall)item;
            if(!wall.getOwner().equals(this.getOwner())) {
                continue;
            }
            
            return wall;
        }
		
		return null;
	}

}
