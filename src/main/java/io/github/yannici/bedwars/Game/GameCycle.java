package io.github.yannici.bedwars.Game;

import java.lang.reflect.Method;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.scheduler.BukkitRunnable;

import com.google.common.collect.ImmutableMap;

import io.github.yannici.bedwars.Main;
import io.github.yannici.bedwars.Utils;
import io.github.yannici.bedwars.Events.BedwarsGameOverEvent;
import io.github.yannici.bedwars.Events.BedwarsPlayerKilledEvent;
import io.github.yannici.bedwars.Shop.Specials.RescuePlatform;
import io.github.yannici.bedwars.Shop.Specials.SpecialItem;
import io.github.yannici.bedwars.Statistics.PlayerStatistic;

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

	private boolean storeRecords(boolean storeHolders, Team winner) {
		int playTime = this.getGame().getLength() - this.getGame().getTimeLeft();
		boolean throughBed = false;

		if (playTime <= this.getGame().getRecord()) {

			// check for winning through bed destroy
			for (Team team : this.getGame().getPlayingTeams()) {
				if (team.isDead(this.getGame())) {
					throughBed = true;
					break;
				}
			}

			if (!throughBed) {
				this.getGame().broadcast(Main._l("ingame.record-nobeddestroy"));
				return false;
			}

			if (storeHolders) {
				if (playTime < this.getGame().getRecord()) {
					this.getGame().getRecordHolders().clear();
				}

				for (Player player : winner.getPlayers()) {
					this.getGame().addRecordHolder(player.getName());
				}
			}

			this.getGame().setRecord(playTime);
			this.getGame().saveRecord();

			this.getGame().broadcast(Main._l("ingame.newrecord", ImmutableMap.of("record",
					this.getGame().getFormattedRecord(), "team", winner.getChatColor() + winner.getDisplayName())));
			return true;
		}

		return false;
	}

	private String winTitleReplace(String str, Team winner) {
		int playTime = this.getGame().getLength() - this.getGame().getTimeLeft();
		String formattedTime = Utils.getFormattedTime(playTime);

		str = str.replace("$time$", formattedTime);

		if (winner == null) {
			return str;
		}

		str = str.replace("$team$", winner.getChatColor() + winner.getDisplayName());
		return str;
	}

	@SuppressWarnings("unchecked")
	private void runGameOver(Team winner) {
		BedwarsGameOverEvent overEvent = new BedwarsGameOverEvent(this.getGame(), winner);
		Main.getInstance().getServer().getPluginManager().callEvent(overEvent);

		if (overEvent.isCancelled()) {
			return;
		}

		this.getGame().stopWorkers();
		this.setEndGameRunning(true);

		// new record?
		boolean storeRecords = Main.getInstance().getBooleanConfig("store-game-records", true);
		boolean storeHolders = Main.getInstance().getBooleanConfig("store-game-records-holder", true);
		boolean madeRecord = false;
		if (storeRecords && winner != null) {
			madeRecord = this.storeRecords(storeHolders, winner);
		}

		int delay = Main.getInstance().getConfig().getInt("gameoverdelay"); // configurable
																			// delay
		String title = this.winTitleReplace(Main._l("ingame.title.win-title"), winner);
		String subtitle = this.winTitleReplace(Main._l("ingame.title.win-subtitle"), winner);

		if (Main.getInstance().statisticsEnabled() || Main.getInstance().getBooleanConfig("rewards.enabled", false)
				|| (Main.getInstance().getBooleanConfig("titles.win.enabled", true)
						&& (!title.equals("") || !subtitle.equals("")))) {
			if (winner != null) {
				for (Player player : winner.getPlayers()) {
					if (Main.getInstance().getBooleanConfig("titles.win.enabled", true)
							&& (!title.equals("") || !subtitle.equals(""))) {
						try {
							Class<?> clazz = Class.forName("io.github.yannici.bedwars.Com."
									+ Main.getInstance().getCurrentVersion() + ".Title");

							if (!title.equals("")) {
								double titleFadeIn = Main.getInstance().getConfig()
										.getDouble("titles.win.title-fade-in", 1.5);
								double titleStay = Main.getInstance().getConfig().getDouble("titles.win.title-stay",
										5.0);
								double titleFadeOut = Main.getInstance().getConfig()
										.getDouble("titles.win.title-fade-out", 2.0);
								Method showTitle = clazz.getDeclaredMethod("showTitle", Player.class, String.class,
										double.class, double.class, double.class);

								showTitle.invoke(null, player, title, titleFadeIn, titleStay, titleFadeOut);
							}

							if (!subtitle.equals("")) {
								double subTitleFadeIn = Main.getInstance().getConfig()
										.getDouble("titles.win.subtitle-fade-in", 1.5);
								double subTitleStay = Main.getInstance().getConfig()
										.getDouble("titles.win.subtitle-stay", 5.0);
								double subTitleFadeOut = Main.getInstance().getConfig()
										.getDouble("titles.win.subtitle-fade-out", 2.0);
								Method showSubTitle = clazz.getDeclaredMethod("showSubTitle", Player.class,
										String.class, double.class, double.class, double.class);

								showSubTitle.invoke(null, player, subtitle, subTitleFadeIn, subTitleStay,
										subTitleFadeOut);
							}
						} catch (Exception ex) {
							ex.printStackTrace();
						}
					}

					if (Main.getInstance().getBooleanConfig("rewards.enabled", false)) {
						List<String> commands = new ArrayList<String>();
						commands = (List<String>) Main.getInstance().getConfig().getList("rewards.player-win");
						Main.getInstance().dispatchRewardCommands(commands, this.getRewardPlaceholders(player));
					}

					if (Main.getInstance().statisticsEnabled()) {
						PlayerStatistic statistic = Main.getInstance().getPlayerStatisticManager().getStatistic(player);
						statistic.setWins(statistic.getWins() + 1);
						statistic.addCurrentScore(Main.getInstance().getIntConfig("statistics.scores.win", 50));

						if (madeRecord) {
							statistic.addCurrentScore(Main.getInstance().getIntConfig("statistics.scores.record", 100));
						}
					}
				}
			}

			for (Player player : this.game.getPlayers()) {
				if (this.game.isSpectator(player)) {
					continue;
				}

				if (Main.getInstance().getBooleanConfig("rewards.enabled", false)) {
					List<String> commands = new ArrayList<String>();
					commands = (List<String>) Main.getInstance().getConfig().getList("rewards.player-end-game");
					Main.getInstance().dispatchRewardCommands(commands, this.getRewardPlaceholders(player));
				}
			}
		}

		this.getGame().getPlayingTeams().clear();

		GameOverTask gameOver = new GameOverTask(this, delay, winner);
		gameOver.runTaskTimer(Main.getInstance(), 0L, 20L);
	}

	private Map<String, String> getRewardPlaceholders(Player player) {
		Map<String, String> placeholders = new HashMap<String, String>();

		placeholders.put("{player}", player.getName());
		if (Main.getInstance().statisticsEnabled()) {
			PlayerStatistic statistic = Main.getInstance().getPlayerStatisticManager().getStatistic(player);
			placeholders.put("{score}", String.valueOf(statistic.getCurrentScore()));
		}

		return placeholders;
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
			if ((this.getGame().getTeamPlayers().size() == 0 || this.getGame().isOverSet())
					&& this.isEndGameRunning() == false) {
				this.runGameOver(null);
			}
		}
	}

	public void onPlayerRespawn(PlayerRespawnEvent pre, Player player) {
		Team team = this.getGame().getPlayerTeam(player);

		// reset damager
		this.getGame().setPlayerDamager(player, null);

		if (team == null) {
			if (this.getGame().isSpectator(player)) {
				Collection<Team> teams = this.getGame().getTeams().values();
				pre.setRespawnLocation(((Team) teams.toArray()[Utils.randInt(0, teams.size() - 1)]).getSpawnLocation());
			}
			return;
		}

		if (team.isDead(this.getGame())) {
			PlayerStorage storage = this.getGame().getPlayerStorage(player);

			if (Main.getInstance().statisticsEnabled()) {
				PlayerStatistic statistic = Main.getInstance().getPlayerStatisticManager().getStatistic(player);
				statistic.setLoses(statistic.getLoses() + 1);
			}

			if (Main.getInstance().spectationEnabled()) {
				if (storage != null) {
					if (storage.getLeft() != null) {
						pre.setRespawnLocation(team.getSpawnLocation());
					}
				}

				this.getGame().toSpectator(player);
			} else {
				if (this.game.getCycle() instanceof BungeeGameCycle) {
					this.getGame().playerLeave(player, false);
					return;
				}

				if (!Main.getInstance().toMainLobby()) {
					if (storage != null) {
						if (storage.getLeft() != null) {
							pre.setRespawnLocation(storage.getLeft());
						}
					}
				} else {
					if (this.getGame().getMainLobby() != null) {
						pre.setRespawnLocation(this.getGame().getMainLobby());
					} else {
						if (storage != null) {
							if (storage.getLeft() != null) {
								pre.setRespawnLocation(storage.getLeft());
							}
						}
					}
				}

				this.getGame().playerLeave(player, false);
			}

		} else {
			if (Main.getInstance().getRespawnProtectionTime() > 0) {
				RespawnProtectionRunnable protection = this.getGame().addProtection(player);
				protection.runProtection();
			}
			pre.setRespawnLocation(team.getSpawnLocation());
		}

		new BukkitRunnable() {

			@Override
			public void run() {
				GameCycle.this.checkGameOver();
			}
		}.runTaskLater(Main.getInstance(), 20L);

	}

	public void onPlayerDies(Player player, Player killer) {
		if (this.isEndGameRunning()) {
			return;
		}

		BedwarsPlayerKilledEvent killedEvent = new BedwarsPlayerKilledEvent(this.getGame(), player, killer);
		Main.getInstance().getServer().getPluginManager().callEvent(killedEvent);

		PlayerStatistic diePlayer = null;
		PlayerStatistic killerPlayer = null;

		Iterator<SpecialItem> itemIterator = this.game.getSpecialItems().iterator();
		while (itemIterator.hasNext()) {
			SpecialItem item = itemIterator.next();
			if (!(item instanceof RescuePlatform)) {
				continue;
			}

			RescuePlatform rescue = (RescuePlatform) item;
			if (rescue.getPlayer().equals(player)) {
				itemIterator.remove();
			}
		}

		Team deathTeam = this.getGame().getPlayerTeam(player);
		if (Main.getInstance().statisticsEnabled()) {
			diePlayer = Main.getInstance().getPlayerStatisticManager().getStatistic(player);

			boolean onlyOnBedDestroy = Main.getInstance().getBooleanConfig("statistics.bed-destroyed-kills", false);
			boolean teamIsDead = deathTeam.isDead(this.getGame());

			if ((onlyOnBedDestroy && teamIsDead) || !onlyOnBedDestroy) {
				diePlayer.setDeaths(diePlayer.getDeaths() + 1);
				diePlayer.addCurrentScore(Main.getInstance().getIntConfig("statistics.scores.die", 0));
			}

			if (killer != null) {
				if ((onlyOnBedDestroy && teamIsDead) || !onlyOnBedDestroy) {
					killerPlayer = Main.getInstance().getPlayerStatisticManager().getStatistic(killer);
					if (killerPlayer != null) {
						killerPlayer.setKills(killerPlayer.getKills() + 1);
						killerPlayer.addCurrentScore(Main.getInstance().getIntConfig("statistics.scores.kill", 10));
					}
				}
			}

			// dispatch reward commands directly
			if (Main.getInstance().getBooleanConfig("rewards.enabled", false) && killer != null) {
				if ((onlyOnBedDestroy && teamIsDead) || !onlyOnBedDestroy) {
					List<String> commands = Main.getInstance().getConfig().getStringList("rewards.player-kill");
					Main.getInstance().dispatchRewardCommands(commands, ImmutableMap.of("{player}", killer.getName(),
							"{score}", String.valueOf(Main.getInstance().getIntConfig("statistics.scores.kill", 10))));
				}
			}
		}

		if (killer == null) {
			this.getGame().broadcast(ChatColor.GOLD + Main._l("ingame.player.died",
					ImmutableMap.of("player", Game.getPlayerWithTeamString(player, deathTeam, ChatColor.GOLD))));

			this.sendTeamDeadMessage(deathTeam);
			this.checkGameOver();
			return;
		}

		Team killerTeam = this.getGame().getPlayerTeam(killer);
		if (killerTeam == null) {
			this.getGame().broadcast(ChatColor.GOLD + Main._l("ingame.player.died",
					ImmutableMap.of("player", Game.getPlayerWithTeamString(player, deathTeam, ChatColor.GOLD))));
			this.sendTeamDeadMessage(deathTeam);
			this.checkGameOver();
			return;
		}

		String hearts = "";
		DecimalFormat format = new DecimalFormat("#.#");
		double health = ((double) killer.getHealth()) / ((double) killer.getMaxHealth())
				* ((double) killer.getHealthScale());
		if (Main.getInstance().getBooleanConfig("hearts-on-death", true)) {

			hearts = "[" + ChatColor.RED + "\u2764" + format.format(health) + ChatColor.GOLD + "]";
		}

		this.getGame()
				.broadcast(ChatColor.GOLD + Main._l("ingame.player.killed",
						ImmutableMap.of("killer",
								Game.getPlayerWithTeamString(killer, killerTeam, ChatColor.GOLD, hearts), "player",
								Game.getPlayerWithTeamString(player, deathTeam, ChatColor.GOLD))));

		this.sendTeamDeadMessage(deathTeam);
		this.checkGameOver();
	}

	private void sendTeamDeadMessage(Team deathTeam) {
		if (deathTeam.getPlayers().size() == 1 && deathTeam.isDead(this.getGame())) {
			this.getGame().broadcast(ChatColor.RED + Main._l("ingame.team-dead",
					ImmutableMap.of("team", deathTeam.getChatColor() + deathTeam.getDisplayName() + ChatColor.RED)));
		}
	}

	public void setEndGameRunning(boolean running) {
		this.endGameRunning = running;
	}

	public boolean isEndGameRunning() {
		return this.endGameRunning;
	}

}
