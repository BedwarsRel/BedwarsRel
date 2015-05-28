package io.github.yannici.bedwars.Shop.Specials;

import io.github.yannici.bedwars.Main;
import io.github.yannici.bedwars.Game.Game;
import io.github.yannici.bedwars.Game.GameState;

import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;

public class WarpPowderListener implements Listener {

    public WarpPowderListener() {
        super();
    }
    
    @EventHandler
    public void onInteract(PlayerInteractEvent ev) {
        Player player = ev.getPlayer();
        Game game = Main.getInstance().getGameManager().getGameOfPlayer(player);    
        
        if(game == null) {
            return;
        }
        
        if(game.getState() != GameState.RUNNING) {
            return;
        }
        
        WarpPowder warpPowder = new WarpPowder();
        if(!ev.getMaterial().equals(warpPowder.getItemMaterial())
        		&& !ev.getMaterial().equals(warpPowder.getActivatedMaterial())) {
            return;
        }
        
        WarpPowder powder = null;
    	for(SpecialItem item : game.getSpecialItems()) {
    		if(!(item instanceof WarpPowder)) {
    			continue;
    		}
    		
    		powder = (WarpPowder)item;
    		if(!powder.getPlayer().equals(player)) {
    			powder = null;
    			continue;
    		}
    		break;
    	}
    	
    	if(powder != null) {
    		 if(ev.getMaterial().equals(warpPowder.getActivatedMaterial())) {
	        	powder.cancelTeleport(true);
	        	ev.setCancelled(true);
	        }
    		
    	    return;
    	}
        
        if(player.getLocation().getBlock().getRelative(BlockFace.DOWN).getType() == Material.AIR) {
            return;
        }
        
        warpPowder.setPlayer(player);
        warpPowder.setGame(game);
        warpPowder.runTask();
        ev.setCancelled(true);
    }

}
