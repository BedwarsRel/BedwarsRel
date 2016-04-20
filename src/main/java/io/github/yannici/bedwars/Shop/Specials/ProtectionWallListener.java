package io.github.yannici.bedwars.Shop.Specials;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import io.github.yannici.bedwars.Main;
import io.github.yannici.bedwars.Game.Game;
import io.github.yannici.bedwars.Game.GameState;

public class ProtectionWallListener implements Listener {

	public ProtectionWallListener() {
		super();
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onInteract(PlayerInteractEvent interact) {
		if (interact.getAction().equals(Action.LEFT_CLICK_AIR)
				|| interact.getAction().equals(Action.LEFT_CLICK_BLOCK)) {
			return;
		}

		if (interact.getMaterial() == null) {
			return;
		}

		ProtectionWall wall = new ProtectionWall();
		if (interact.getMaterial() != wall.getItemMaterial()) {
			return;
		}
		
		if(interact.getItem().getItemMeta().getDisplayName() == null) {
			return;
		}

		Game game = Main.getInstance().getGameManager().getGameOfPlayer(interact.getPlayer());
		if (game == null) {
			return;
		}

		if (game.getState() != GameState.RUNNING) {
			return;
		}

		if (game.isSpectator(interact.getPlayer())) {
			return;
		}

		wall.create(interact.getPlayer(), game);
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlace(BlockPlaceEvent place) {
	    if(place.isCancelled()) {
	        return;
	    }
	    
	    ProtectionWall wall = new ProtectionWall();
	    if(place.getBlock().getType() != wall.getItemMaterial()) {
	        return;
	    }
	    
	    if(place.getItemInHand().getItemMeta().getDisplayName() == null){
	    	return;
	    }
	    
	    Game game = Main.getInstance().getGameManager().getGameOfPlayer(place.getPlayer());
	    if(game == null) {
	        return;
	    }
	    
	    if(game.getState() != GameState.RUNNING) {
	        return;
	    }
	    
	    if(game.isSpectator(place.getPlayer())) {
	    	place.setBuild(false);
	    	place.setCancelled(true);
            return;
        }
	    
	    place.setBuild(false);
	    place.setCancelled(true);
	}

}
