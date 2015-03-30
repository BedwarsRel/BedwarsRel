package io.github.yannici.bedwarsreloaded.Game;

import io.github.yannici.bedwarsreloaded.ChatWriter;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

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
			this.getGame().playerLeave(player);
		} else {
			pre.setRespawnLocation(team.getSpawnLocation());
		}
	}
	
	public void onPlayerDies(Player player, Player killer) {
		
		Team deathTeam = Game.getPlayerTeam(player, this.getGame());
		if(killer == null) {
			this.getGame().broadcast(ChatWriter.pluginMessage(ChatColor.GOLD + ">> " + this.getPlayerWithTeamString(player, deathTeam) + ChatColor.GOLD + " died!"));
			return;
		}
		
		Team killerTeam = Game.getPlayerTeam(killer, this.getGame());
		
		this.getGame().broadcast(ChatWriter.pluginMessage(ChatColor.GOLD + ">> " + this.getPlayerWithTeamString(killer, killerTeam) + ChatColor.GOLD + " killed " + this.getPlayerWithTeamString(player, deathTeam) + ChatColor.GOLD + "!"));
	}
	
	private String getPlayerWithTeamString(Player player, Team team) {
		return team.getChatColor() + player.getDisplayName() + " (" + team.getName() + ")";
	}
	
}
