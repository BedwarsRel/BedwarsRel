package io.github.yannici.bedwarsreloaded.Listener;

import io.github.yannici.bedwarsreloaded.Main;
import io.github.yannici.bedwarsreloaded.Game.Game;
import io.github.yannici.bedwarsreloaded.Game.GameState;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerBedEnterEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerToggleFlightEvent;

public class PlayerListener extends BaseListener {

	public PlayerListener() {
		super();
	}
	
	/*
	 * GLOBAL
	 */

	@EventHandler
	public void onJoin(PlayerJoinEvent je) {
		FileConfiguration cfg = Main.getInstance().getConfig();
		Player player = je.getPlayer();
		
		if(cfg.contains("bungeecord")) {
			if(cfg.getBoolean("bungeecord")) {
				Game game = Main.getInstance().getGameManager().getGames().get(0);
				game.playerJoins(player);
			}
		}
	}
	
	/*
	 * GAME
	 */
	
	private void inGameInteractEntity(PlayerInteractEntityEvent iee, Game game, Player player) {
		if (!iee.getRightClicked().getType().equals(EntityType.VILLAGER)) {
	      return;
	    }
		
		// TODO: Create Itemshop and open it
		iee.setCancelled(true);
	}
	
	/*
	 * LOBBY & GAME
	 */
	
	public void onSleep(PlayerBedEnterEvent bee) {
		Player p = bee.getPlayer();
		
		Game g = Game.getGameOfPlayer(p);
		if(g == null) {
			return;
		}
		
		if(g.getState() == GameState.STOPPED) {
			return;
		}
		
		bee.setCancelled(true);
	}
	
	@EventHandler
	public void onInteractEntity(PlayerInteractEntityEvent iee) {
		Player p = iee.getPlayer();
		Game g = Game.getGameOfPlayer(p);
		if(g == null) {
			return;
		}
		
		if(g.getState() == GameState.WAITING) {
			iee.setCancelled(true);
		}
		
		if(g.getState() == GameState.RUNNING) {
			this.inGameInteractEntity(iee, g, p);
		}
 	}
	
	public void onFly(PlayerToggleFlightEvent tfe) {
		Player p = tfe.getPlayer();
		
		Game g = Game.getGameOfPlayer(p);
		if(g == null) {
			return;
		}
		
		if(g.getState() == GameState.STOPPED) {
			return;
		}
		
		tfe.setCancelled(true);
	}

	/*
	 * LOBBY
	 */
	
	@EventHandler
	public void onDrop(PlayerDropItemEvent die) {
		Player p = die.getPlayer();
		Game g = Game.getGameOfPlayer(p);
		if(g == null) {
			return;
		}
		
		if(g.getState() != GameState.WAITING) {
			return;
		}
		
		die.setCancelled(true);
	}
	
	@EventHandler
	public void onDamage(EntityDamageEvent ede) {
		if(!(ede.getEntity() instanceof Player)) {
			return;
		}
		
		Player p = (Player)ede.getEntity();
		Game g = Game.getGameOfPlayer(p);
		if(g == null) {
			return;
		}
		
		if(g.getState() != GameState.WAITING) {
			return;
		}
		
		if (ede.getCause() == EntityDamageEvent.DamageCause.VOID) {
		    p.teleport(g.getLobby());
		}
		
		ede.setCancelled(true);
	}

}
