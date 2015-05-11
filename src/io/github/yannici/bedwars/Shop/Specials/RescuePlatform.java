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

public class RescuePlatform extends SpecialItem {
    
    private int livingTime = 0;
    private Player activatedPlayer = null;
    private List<Block> platformBlocks = null;
    
    public RescuePlatform() {
        super();
        
        this.platformBlocks = new ArrayList<Block>();
    }

    @Override
    public Material getItemMaterial() {
        return Material.BLAZE_ROD;
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
        
        if(player.getLocation().getBlock().getRelative(BlockFace.DOWN).getType() != Material.AIR) {
            player.sendMessage(ChatWriter.pluginMessage(ChatColor.RED + Main._l("errors.notinair")));
            return false;
        }
        
        Location mid = player.getLocation().clone();
        mid.setY(mid.getY()-1.0D);
        
        player.getInventory().remove(new ItemStack(this.getItemMaterial(), 1));
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
            platformBlocks.add(placed);
        }
        
        this.activatedPlayer = player;
        return true;
    }
    
    public void cycle() {
        this.livingTime++;
    }

}
