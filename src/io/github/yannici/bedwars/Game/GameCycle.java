package io.github.yannici.bedwars.Game;

import java.util.Collection;

import io.github.yannici.bedwars.Main;
import io.github.yannici.bedwars.Utils;
import io.github.yannici.bedwars.Events.BedwarsGameOverEvent;
import io.github.yannici.bedwars.Events.BedwarsPlayerKilledEvent;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerRespawnEvent;

import com.google.common.collect.ImmutableMap;

public abstract class GameCycle {

	private Game game = null;
	private boolean endGameRunning = false;

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

	public abstract void onGameOver(GameOverTask task);

	private void runGameOver(Team winner) {
		BedwarsGameOverEvent overEvent = new BedwarsGameOverEvent(
				this.getGame(), winner);
		Main.getInstance().getServer().getPluginManager().callEvent(overEvent);

		if (overEvent.isCancelled()) {
			return;
		}

		this.getGame().stopWorkers();
		this.setEndGameRunning(true);
		int delay = Main.getInstance().getConfig().getInt("gameoverdelay"); // configurable
																			// delay
		GameOverTask gameOver = new GameOverTask(this, delay, winner);
		gameOver.runTaskTimer(Main.getInstance(), 0L, 20L);
	}

	public void checkGameOver() {
		if (!Main.getInstance().isEnabled()) {
			return;
		}

		Team winner = this.getGame().isOver();
		if (winner != null) {
			if (this.isEndGameRunning() == false) {
				this.runGameOver(winner);
			}
		} else {
			if (this.getGame().getTeamPlayers().size() == 0
					|| this.getGame().isOverSet()) {
				this.runGameOver(null);
			}
		}
	}

	public void onPlayerRespawn(PlayerRespawnEvent pre, Player player) {
		Team team = Game.getPlayerTeam(player, this.getGame());
		if (team == null) {
		    if(this.getGame().isSpectator(player)) {
		        Collection<Team> teams = this.getGame().getTeams().values();
		        pre.setRespawnLocation(((Team)teams.toArray()[Utils.randInt(0, teams.size()-1)]).getSpawnLocation());
		    }
			return;
		}

		if (team.isDead()) {
			PlayerStorage storage = this.getGame().getPlayerStorage(player);

			if (Main.getInstance().spectationEnabled()) {
				if (storage != null) {
					if (storage.getLeft() != null) {
						pre.setRespawnLocation(team.getSpawnLocation());
					}
				}

				this.getGame().toSpectator(player);
			} else {
				if(!Main.getInstance().toMainLobby()) {
					if (storage != null) {
						if (storage.getLeft() != null) {
							pre.setRespawnLocation(storage.getLeft());
						}
					}
				} else {
					if(this.getGame().getMainLobby() != null) {
						pre.setRespawnLocation(this.getGame().getMainLobby());
					} else {
						if (storage != null) {
							if (storage.getLeft() != null) {
								pre.setRespawnLocation(storage.getLeft());
							}
						}
					}
				}

				this.getGame().playerLeave(player);
			}

		} else {
			if(Main.getInstance().getRespawnProtectionTime() > 0) {
				RespawnProtectionRunnable protection = this.getGame().addProtection(player);
				protection.runProtection();
			}
			pre.setRespawnLocation(team.getSpawnLocation());
		}
	}

	public void onPlayerDies(Player player, Player killer) {
		BedwarsPlayerKilledEvent killedEvent = new BedwarsPlayerKilledEvent(
				this.getGame(), player, killer);
		Main.getInstance().getServer().getPluginManager()
				.callEvent(killedEvent);

		Team deathTeam = Game.getPlayerTeam(player, this.getGame());
		if (killer == null) {
			this.getGame()
					.broadcast(
							ChatColor.GOLD
									+ Main._l("ingame.player.died",
											ImmutableMap.of("player", Game
													.getPlayerWithTeamString(
															player, deathTeam,
															ChatColor.GOLD))));
			this.checkGameOver();
			return;
		}

		Team killerTeam = Game.getPlayerTeam(killer, this.getGame());
		if (killerTeam == null) {
			this.getGame()
					.broadcast(
							ChatColor.GOLD
									+ Main._l("ingame.player.died",
											ImmutableMap.of("player", Game
													.getPlayerWithTeamString(
															player, deathTeam,
															ChatColor.GOLD))));
			this.checkGameOver();
			return;
		}

		this.getGame().broadcast(
				ChatColor.GOLD
						+ Main._l("ingame.player.killed", ImmutableMap.of(
								"killer", Game.getPlayerWithTeamString(killer,
										killerTeam, ChatColor.GOLD), "player",
								Game.getPlayerWithTeamString(player, deathTeam,
										ChatColor.GOLD))));

		this.checkGameOver();
	}

	public void setEndGameRunning(boolean running) {
		this.endGameRunning = running;
	}

	public boolean isEndGameRunning() {
		return this.endGameRunning;
	}

}
