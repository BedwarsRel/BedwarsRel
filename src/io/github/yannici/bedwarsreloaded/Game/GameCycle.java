package io.github.yannici.bedwarsreloaded.Game;

import io.github.yannici.bedwarsreloaded.Main;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.scheduler.BukkitRunnable;

public abstract class GameCycle {
	
	private Game game = null;
	
	public GameCycle(Game game) {
		this.game = game;
	}

	public Game getGame() {
		return game;
	}
	
	public abstract void onGameStart();
	
	public abstract void onGameEnds();
	
	public abstract void onPlayerLeave(Player player);
	
	public abstract void onGameLoaded();
	
	public abstract boolean onPlayerJoins(Player player);
	
	public void onPlayerRespawn(PlayerRespawnEvent pre, Player player) {
		Team team = Game.getPlayerTeam(player, this.getGame());
		if(team == null) {
			return;
		}
		
		if(team.isDead()) {
			// TODO: Player to spectator
		    
		    PlayerStorage storage = this.getGame().getPlayerStorage(player);
		    if(storage != null) {
		        if(storage.getLeft() != null) {
		            pre.setRespawnLocation(storage.getLeft());
		        }
		    }
		    
			this.getGame().playerLeave(player);
		} else {
			pre.setRespawnLocation(team.getSpawnLocation());
		}
	}
	
	public void onPlayerDies(Player player, Player killer) {
		
		Team deathTeam = Game.getPlayerTeam(player, this.getGame());
		if(killer == null) {
			this.getGame().broadcast(ChatColor.GOLD + Game.getPlayerWithTeamString(player, deathTeam) + ChatColor.GOLD + " died!");
			return;
		}
		
		Team killerTeam = Game.getPlayerTeam(killer, this.getGame());
		if(killerTeam == null) {
		    this.getGame().broadcast(ChatColor.GOLD + Game.getPlayerWithTeamString(player, deathTeam) + ChatColor.GOLD + " died!");
            return;
		}
		
		this.getGame().broadcast(ChatColor.GOLD + Game.getPlayerWithTeamString(killer, killerTeam) + ChatColor.GOLD + " killed " + Game.getPlayerWithTeamString(player, deathTeam) + ChatColor.GOLD + "!");
		
		if(this.getGame().isOver()) {
		    new BukkitRunnable() {
                
                @Override
                public void run() {
                    GameCycle.this.getGame().kickAllPlayers();
                }
            }.runTaskLater(Main.getInstance(), 200); // 10 sek delay
		}
	}
	
}
