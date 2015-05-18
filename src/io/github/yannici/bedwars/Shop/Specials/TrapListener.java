package io.github.yannici.bedwars.Shop.Specials;

import io.github.yannici.bedwars.Game.Game;
import io.github.yannici.bedwars.Game.GameState;
import io.github.yannici.bedwars.Game.Team;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;

public class TrapListener implements Listener {
	
	public TrapListener() {
		super();
	}
	
	@EventHandler
	public void onInteract(PlayerInteractEvent interact) {
		Player player = interact.getPlayer();
		Game game = Game.getGameOfPlayer(player);
		
		if(game == null) {
			return;
		}
		
		if(game.getState() != GameState.RUNNING) {
			return;
		}
		
		Team team = Game.getPlayerTeam(player, game);
		Trap tmpTrap = new Trap();
		if(interact.getAction() != Action.PHYSICAL
				|| interact.getMaterial() != tmpTrap.getItemMaterial()) {
			return;
		}
		
		// get trapped trap ;)
		for(SpecialItem item : game.getSpecialItems()) {
			if(!(item instanceof Trap)) {
				continue;
			}
			
			Trap trap = (Trap)item;
			if(!trap.getLocation().equals(player.getLocation())) {
				continue;
			}
			
			if(trap.getPlacedTeam().equals(team)) {
				interact.setCancelled(true);
				return;
			}
			
			trap.activate(player);
			return;
		}
	}
	
	@EventHandler
	public void onPlace(BlockPlaceEvent place) {
		if(place.isCancelled()) {
			return;
		}
		
		Player player = place.getPlayer();
		Game game = Game.getGameOfPlayer(player);
		
		if(game == null) {
			return;
		}
		
		if(game.getState() != GameState.RUNNING) {
			return;
		}
		
		Team team = Game.getPlayerTeam(player, game);
		if(team == null) {
			place.setCancelled(true);
			place.setBuild(false);
			return;
		}
		
		Trap trap = new Trap();
		trap.create(game, team, place.getBlockPlaced().getLocation());
	}

}
