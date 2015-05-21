package io.github.yannici.bedwars.Shop.Specials;

import io.github.yannici.bedwars.Main;
import io.github.yannici.bedwars.Game.Game;
import io.github.yannici.bedwars.Game.GameState;
import io.github.yannici.bedwars.Game.Team;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerMoveEvent;

public class TrapListener implements Listener {
	
	public TrapListener() {
		super();
	}
	
	@EventHandler
	public void onMove(PlayerMoveEvent move) {
		Player player = move.getPlayer();
		Game game = Main.getInstance().getGameManager().getGameOfPlayer(player);
		
		if(game == null) {
			return;
		}
		
		if(game.getState() != GameState.RUNNING) {
			return;
		}
		
		if(game.isSpectator(player)) {
			return;
		}
		
		Trap tmpTrap = new Trap();
        if(!move.getTo().getBlock().getType().equals(tmpTrap.getItemMaterial())) {
            return;
        }
		
		Team team = game.getPlayerTeam(player);
		
		// get trapped trap ;)
		for(SpecialItem item : game.getSpecialItems()) {
			if(!(item instanceof Trap)) {
				continue;
			}
			
			Trap trap = (Trap)item;
			if(!trap.getLocation().equals(player.getLocation().getBlock().getLocation())) {
				continue;
			}
			
			if(trap.getPlacedTeam().equals(team)) {
				return;
			}
			
			trap.activate(player);
			return;
		}
	}
	
	@EventHandler(priority = EventPriority.HIGH)
	public void onBreak(BlockBreakEvent br) {
	    if(br.isCancelled()) {
            return;
        }
	    
	    if(!br.getBlock().getType().equals(Material.TRIPWIRE)) {
            return;
        }
        
        Player player = br.getPlayer();
        Game game = Main.getInstance().getGameManager().getGameOfPlayer(player);
        
        if(game == null) {
            return;
        }
        
        if(game.getState() != GameState.RUNNING) {
            return;
        }
        
        br.setCancelled(true);
	}
	
 	@EventHandler
	public void onPlace(BlockPlaceEvent place) {
		if(place.isCancelled()) {
			return;
		}
		
		Player player = place.getPlayer();
		Game game = Main.getInstance().getGameManager().getGameOfPlayer(player);
		
		if(game == null) {
			return;
		}
		
		if(game.getState() != GameState.RUNNING) {
			return;
		}
		
		Team team = game.getPlayerTeam(player);
		if(team == null) {
			place.setCancelled(true);
			place.setBuild(false);
			return;
		}
		
		Trap trap = new Trap();
		trap.create(game, team, place.getBlockPlaced().getLocation());
	}

}
