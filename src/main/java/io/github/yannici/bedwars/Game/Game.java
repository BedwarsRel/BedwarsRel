package io.github.yannici.bedwars.Game;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.DyeColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.material.Bed;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.util.Vector;

import com.google.common.collect.ImmutableMap;

import io.github.yannici.bedwars.ChatWriter;
import io.github.yannici.bedwars.Main;
import io.github.yannici.bedwars.Utils;
import io.github.yannici.bedwars.Events.BedwarsGameStartEvent;
import io.github.yannici.bedwars.Events.BedwarsPlayerJoinEvent;
import io.github.yannici.bedwars.Events.BedwarsPlayerJoinedEvent;
import io.github.yannici.bedwars.Events.BedwarsPlayerLeaveEvent;
import io.github.yannici.bedwars.Events.BedwarsSaveGameEvent;
import io.github.yannici.bedwars.Shop.NewItemShop;
import io.github.yannici.bedwars.Shop.Specials.SpecialItem;
import io.github.yannici.bedwars.Statistics.PlayerStatistic;
import io.github.yannici.bedwars.Villager.MerchantCategory;
import io.github.yannici.bedwars.Villager.MerchantCategoryComparator;

public class Game {

	private String name = null;
	private List<RessourceSpawner> resSpawner = null;
	private List<BukkitTask> runningTasks = null;
	private GameState state = null;
	private HashMap<String, Team> teams = null;
	private List<Team> playingTeams = null;
	private List<Player> freePlayers = null;
	private int minPlayers = 0;
	private Region region = null;
	private Location lobby = null;
	private HashMap<Player, PlayerStorage> storages = null;
	private Scoreboard scoreboard = null;
	private GameLobbyCountdown glc = null;
	private HashMap<Material, MerchantCategory> itemshop = null;
	private List<MerchantCategory> orderedItemshop = null;
	private GameCycle cycle = null;
	private Location mainLobby = null;
	private HashMap<Location, GameJoinSign> joinSigns = null;
	private int timeLeft = 0;
	private boolean isOver = false;
	private boolean isStopping = false;

	private Location hologramLocation = null;

	private boolean autobalance = false;

	private List<String> recordHolders = null;
	private int record = 0;
	private int length = 0;

	private String builder = null;

	private Map<Player, PlayerSettings> playerSettings = null;

	private List<SpecialItem> currentSpecials = null;

	private int time = 1000;

	private Map<Player, Player> playerDamages = null;

	private Map<Player, RespawnProtectionRunnable> respawnProtected = null;

	private String regionName = null;

	// Itemshops
	private HashMap<Player, NewItemShop> newItemShops = null;
	private List<Player> useOldItemShop = null;

	private YamlConfiguration config = null;

	private Location loc1 = null;
	private Location loc2 = null;

	private Material targetMaterial = null;

	public Game(String name) {
		super();

		this.name = name;
		this.runningTasks = new ArrayList<BukkitTask>();

		this.freePlayers = new ArrayList<Player>();
		this.resSpawner = new ArrayList<RessourceSpawner>();
		this.teams = new HashMap<String, Team>();
		this.playingTeams = new ArrayList<Team>();

		this.storages = new HashMap<Player, PlayerStorage>();
		this.state = GameState.STOPPED;
		this.scoreboard = Main.getInstance().getScoreboardManager().getNewScoreboard();

		this.glc = null;
		this.joinSigns = new HashMap<Location, GameJoinSign>();
		this.timeLeft = Main.getInstance().getMaxLength();
		this.isOver = false;
		this.newItemShops = new HashMap<Player, NewItemShop>();
		this.useOldItemShop = new ArrayList<Player>();
		this.respawnProtected = new HashMap<Player, RespawnProtectionRunnable>();
		this.playerDamages = new HashMap<Player, Player>();
		this.currentSpecials = new ArrayList<SpecialItem>();

		this.record = Main.getInstance().getMaxLength();
		this.length = Main.getInstance().getMaxLength();
		this.recordHolders = new ArrayList<String>();

		this.playerSettings = new HashMap<Player, PlayerSettings>();

		this.autobalance = Main.getInstance().getBooleanConfig("global-autobalance", false);

		if (Main.getInstance().isBungee()) {
			this.cycle = new BungeeGameCycle(this);
		} else {
			this.cycle = new SingleGameCycle(this);
		}
	}

	/*
	 * STATIC
	 */

	public static String getPlayerWithTeamString(Player player, Team team, ChatColor before) {
		if (Main.getInstance().getBooleanConfig("teamname-in-chat", true)) {
			return player.getDisplayName() + before + " (" + team.getChatColor() + team.getDisplayName() + before + ")";
		}

		return player.getDisplayName() + before;
	}

	public static String getPlayerWithTeamString(Player player, Team team, ChatColor before, String playerAdding) {
		if (Main.getInstance().getBooleanConfig("teamname-in-chat", true)) {
			return player.getDisplayName() + before + playerAdding + before + " (" + team.getChatColor()
					+ team.getDisplayName() + before + ")";
		}

		return player.getDisplayName() + before + playerAdding + before;
	}

	public static String bedLostString() {
		return "\u2718";
	}

	public static String bedExistString() {
		return "\u2714";
	}

	/*
	 * PUBLIC
	 */

	public boolean run(CommandSender sender) {
		if (this.state != GameState.STOPPED) {
			sender.sendMessage(ChatWriter.pluginMessage(ChatColor.RED + Main._l("errors.cantstartagain")));
			return false;
		}

		GameCheckCode gcc = this.checkGame();
		if (gcc != GameCheckCode.OK) {
			sender.sendMessage(ChatWriter.pluginMessage(ChatColor.RED + gcc.getCodeMessage()));
			return false;
		}

		if (sender instanceof Player) {
			sender.sendMessage(ChatWriter.pluginMessage(ChatColor.GREEN + Main._l("success.gamerun")));
		}

		this.isStopping = false;
		this.state = GameState.WAITING;
		this.updateSigns();
		return true;
	}

	public boolean start(CommandSender sender) {
		if (this.state != GameState.WAITING) {
			sender.sendMessage(ChatWriter.pluginMessage(ChatColor.RED + Main._l("errors.startoutofwaiting")));
			return false;
		}

		BedwarsGameStartEvent startEvent = new BedwarsGameStartEvent(this);
		Main.getInstance().getServer().getPluginManager().callEvent(startEvent);

		if (startEvent.isCancelled()) {
			return false;
		}

		this.isOver = false;
		this.broadcast(ChatColor.GREEN + Main._l("ingame.gamestarting"));

		// load shop categories again (if shop was changed)
		this.loadItemShopCategories();

		this.runningTasks.clear();
		this.cleanUsersInventory();
		this.clearProtections();
		this.moveFreePlayersToTeam();
		this.makeTeamsReady();

		this.cycle.onGameStart();
		this.startRessourceSpawners();

		// Update world time before game starts
		this.getRegion().getWorld().setTime(this.time);

		this.teleportPlayersToTeamSpawn();

		this.state = GameState.RUNNING;
		this.updateScoreboard();

		if (Main.getInstance().getBooleanConfig("store-game-records", true)) {
			this.displayRecord();
		}

		this.startTimerCountdown();

		if (Main.getInstance().getBooleanConfig("titles.map.enabled", false)) {
			this.displayMapInfo();
		}

		this.updateSigns();

		if (Main.getInstance().getBooleanConfig("global-messages", true)) {
			Main.getInstance().getServer().broadcastMessage(ChatWriter.pluginMessage(ChatColor.GREEN
					+ Main._l("ingame.gamestarted", ImmutableMap.of("game", this.getRegion().getName()))));
		}
		return true;
	}

	public boolean stop() {
		if (this.state == GameState.STOPPED) {
			return false;
		}

		this.isStopping = true;

		this.stopWorkers();
		this.clearProtections();
		this.kickAllPlayers();
		this.resetRegion();
		this.state = GameState.STOPPED;
		this.updateSigns();

		this.isStopping = false;
		return true;
	}

	public boolean isInGame(Player p) {
		for (Team t : this.teams.values()) {
			if (t.isInTeam(p)) {
				return true;
			}
		}

		return this.freePlayers.contains(p);
	}

	public void addRessourceSpawner(RessourceSpawner rs) {
		this.resSpawner.add(rs);
	}

	public List<RessourceSpawner> getRessourceSpawner() {
		return this.resSpawner;
	}

	public void setLoc(Location loc, String type) {
		if (type.equalsIgnoreCase("loc1")) {
			this.loc1 = loc;
		} else {
			this.loc2 = loc;
		}
	}

	public Team getPlayerTeam(Player p) {
		for (Team team : this.getTeams().values()) {
			if (team.isInTeam(p)) {
				return team;
			}
		}

		return null;
	}

	public boolean saveGame(CommandSender sender, boolean direct) {
		BedwarsSaveGameEvent saveEvent = new BedwarsSaveGameEvent(this, sender);
		Main.getInstance().getServer().getPluginManager().callEvent(saveEvent);

		if (saveEvent.isCancelled()) {
			return true;
		}

		GameCheckCode check = this.checkGame();

		if (check != GameCheckCode.OK) {
			sender.sendMessage(ChatWriter.pluginMessage(ChatColor.RED + check.getCodeMessage()));
			return false;
		}

		File gameConfig = new File(
				Main.getInstance().getDataFolder() + "/" + GameManager.gamesPath + "/" + this.name + "/game.yml");
		gameConfig.mkdirs();

		if (gameConfig.exists()) {
			gameConfig.delete();
		}

		this.saveRegion(direct);
		this.createGameConfig(gameConfig);

		return true;
	}

	public int getMaxPlayers() {
		int max = 0;
		for (Team t : this.teams.values()) {
			max += t.getMaxPlayers();
		}

		return max;
	}

	public Team getTeamOfBed(Block bed) {
		for (Team team : this.getTeams().values()) {
			if (team.getFeetTarget() == null) {
				if (team.getHeadTarget().equals(bed)) {
					return team;
				}
			} else {
				if (team.getHeadTarget().equals(bed) || team.getFeetTarget().equals(bed)) {
					return team;
				}
			}
		}

		return null;
	}

	public int getCurrentPlayerAmount() {
		int amount = 0;
		for (Team t : this.teams.values()) {
			amount += t.getPlayers().size();
		}

		return amount;
	}

	public int getPlayerAmount() {
		return this.getPlayers().size();
	}

	public boolean isFull() {
		return (this.getMaxPlayers() <= this.getPlayerAmount());
	}

	public void addTeam(String name, TeamColor color, int maxPlayers) {
		org.bukkit.scoreboard.Team newTeam = this.scoreboard.registerNewTeam(name);
		newTeam.setDisplayName(name);
		newTeam.setPrefix(color.getChatColor().toString());

		Team theTeam = new Team(name, color, maxPlayers, newTeam);
		this.teams.put(name, theTeam);
	}

	public boolean isAutobalanceEnabled() {
		if (Main.getInstance().getBooleanConfig("global-autobalance", false)) {
			return true;
		}

		return this.autobalance;
	}

	public void addTeam(Team team) {
		org.bukkit.scoreboard.Team newTeam = this.scoreboard.registerNewTeam(team.getName());
		newTeam.setDisplayName(team.getName());
		newTeam.setPrefix(team.getChatColor().toString());

		team.setScoreboardTeam(newTeam);

		this.teams.put(team.getName(), team);
	}

	public void toSpectator(Player player) {
		final Player p = player;

		Team playerTeam = this.getPlayerTeam(player);
		if (playerTeam != null) {
			playerTeam.removePlayer(player);
		}

		if (!this.freePlayers.contains(player)) {
			this.freePlayers.add(player);
		}

		PlayerStorage storage = this.getPlayerStorage(player);
		if (storage != null) {
			storage.clean();
		} else {
			storage = this.addPlayerStorage(player);
			storage.store();
			storage.clean();
		}

		final Location location = this.getPlayerTeleportLocation(p);

		if (!p.getLocation().getWorld().equals(location.getWorld())) {
			this.getPlayerSettings(p).setTeleporting(true);
			if (Main.getInstance().isBungee()) {
				new BukkitRunnable() {

					@Override
					public void run() {
						p.teleport(location);
					}

				}.runTaskLater(Main.getInstance(), 10L);

			} else {
				p.teleport(location);
			}
		}

		new BukkitRunnable() {

			@Override
			public void run() {
				Game.this.setPlayerGameMode(p);
				Game.this.setPlayerVisibility(p);
			}

		}.runTaskLater(Main.getInstance(), 15L);

		// Leave Game (Slimeball)
		ItemStack leaveGame = new ItemStack(Material.SLIME_BALL, 1);
		ItemMeta im = leaveGame.getItemMeta();
		im.setDisplayName(Main._l("lobby.leavegame"));
		leaveGame.setItemMeta(im);
		p.getInventory().setItem(8, leaveGame);

		if (this.getCycle() instanceof BungeeGameCycle && this.getCycle().isEndGameRunning()
				&& Main.getInstance().getBooleanConfig("bungeecord.endgame-in-lobby", true)) {
			p.updateInventory();
			return;
		}

		// Teleport to player (Compass)
		ItemStack teleportPlayer = new ItemStack(Material.COMPASS, 1);
		im = teleportPlayer.getItemMeta();
		im.setDisplayName(Main._l("ingame.spectate"));
		teleportPlayer.setItemMeta(im);
		p.getInventory().setItem(0, teleportPlayer);

		p.updateInventory();
		this.updateScoreboard();

	}

	public Location getPlayerTeleportLocation(Player player) {
		if (this.isSpectator(player)
				&& !(this.getCycle() instanceof BungeeGameCycle && this.getCycle().isEndGameRunning()
						&& Main.getInstance().getBooleanConfig("bungeecord.endgame-in-lobby", true))) {
			return ((Team) this.teams.values().toArray()[Utils.randInt(0, this.teams.size() - 1)]).getSpawnLocation();
		}

		if (this.getPlayerTeam(player) != null
				&& !(this.getCycle() instanceof BungeeGameCycle && this.getCycle().isEndGameRunning()
						&& Main.getInstance().getBooleanConfig("bungeecord.endgame-in-lobby", true))) {
			return this.getPlayerTeam(player).getSpawnLocation();
		}

		return this.getLobby();
	}

	@SuppressWarnings("deprecation")
	public void setPlayerGameMode(Player player) {
		if (this.isSpectator(player)
				&& !(this.getCycle() instanceof BungeeGameCycle && this.getCycle().isEndGameRunning()
						&& Main.getInstance().getBooleanConfig("bungeecord.endgame-in-lobby", true))) {

			player.setAllowFlight(true);
			player.setFlying(true);

			// 1.7 compatible
			try {
				player.setGameMode(GameMode.valueOf("SPECTATOR"));
			} catch (Exception ex) {
				player.setGameMode(GameMode.SURVIVAL);
			}
		} else {
			GameMode gameMode = null;
			try {
				gameMode = GameMode.getByValue(Main.getInstance().getIntConfig("lobby-gamemode", 0));
			} catch (Exception ex) {
				// not valid gamemode
			}

			if (gameMode == null) {
				gameMode = GameMode.SURVIVAL;
			}
			player.setGameMode(gameMode);
		}
	}

	public void setPlayerVisibility(Player player) {
		ArrayList<Player> players = new ArrayList<Player>();
		players.addAll(this.getPlayers());

		if (this.state == GameState.RUNNING
				&& !(this.getCycle() instanceof BungeeGameCycle && this.getCycle().isEndGameRunning()
						&& Main.getInstance().getBooleanConfig("bungeecord.endgame-in-lobby", true))) {
			if (this.isSpectator(player)) {
				if (player.getGameMode().equals(GameMode.SURVIVAL)) {
					for (Player playerInGame : players) {
						playerInGame.hidePlayer(player);
						player.showPlayer(playerInGame);
					}
				} else {
					for (Player teamPlayer : this.getTeamPlayers()) {
						teamPlayer.hidePlayer(player);
						player.showPlayer(teamPlayer);
					}
					for (Player freePlayer : this.getFreePlayers()) {
						freePlayer.showPlayer(player);
						player.showPlayer(freePlayer);
					}
				}
			} else {
				for (Player playerInGame : players) {
					playerInGame.showPlayer(player);
					player.showPlayer(playerInGame);
				}
			}
		} else {
			for (Player playerInGame : players) {
				if (!playerInGame.equals(player)) {
					playerInGame.showPlayer(player);
					player.showPlayer(playerInGame);
				}
			}
		}

	}

	public boolean isSpectator(Player player) {
		return (this.getState() == GameState.RUNNING && this.freePlayers.contains(player));
	}

	public boolean playerJoins(final Player p) {

		if (this.state == GameState.STOPPED
				|| (this.state == GameState.RUNNING && !Main.getInstance().spectationEnabled())) {
			if (this.cycle instanceof BungeeGameCycle) {
				((BungeeGameCycle) this.cycle).sendBungeeMessage(p,
						ChatWriter.pluginMessage(ChatColor.RED + Main._l("errors.cantjoingame")));
			} else {
				p.sendMessage(ChatWriter.pluginMessage(ChatColor.RED + Main._l("errors.cantjoingame")));
			}
			return false;
		}

		if (!this.cycle.onPlayerJoins(p)) {
			return false;
		}

		BedwarsPlayerJoinEvent joiningEvent = new BedwarsPlayerJoinEvent(this, p);
		Main.getInstance().getServer().getPluginManager().callEvent(joiningEvent);

		if (joiningEvent.isCancelled()) {
			return false;
		}

		Main.getInstance().getGameManager().addGamePlayer(p, this);
		if (Main.getInstance().statisticsEnabled()) {
			// load statistics
			Main.getInstance().getPlayerStatisticManager().getStatistic(p);
		}

		// add damager and set it to null
		this.playerDamages.put(p, null);

		// add player settings
		this.addPlayerSettings(p);

		new BukkitRunnable() {

			@Override
			public void run() {
				for (Player playerInGame : Game.this.getPlayers()) {
					playerInGame.hidePlayer(p);
					p.hidePlayer(playerInGame);
				}
			}

		}.runTaskLater(Main.getInstance(), 5L);

		if (this.state == GameState.RUNNING) {
			this.toSpectator(p);
			this.displayMapInfo(p);
		} else {

			PlayerStorage storage = this.addPlayerStorage(p);
			storage.store();
			storage.clean();

			if (!Main.getInstance().isBungee()) {
				final Location location = this.getPlayerTeleportLocation(p);
				if (!p.getLocation().equals(location)) {
					this.getPlayerSettings(p).setTeleporting(true);
					if (Main.getInstance().isBungee()) {
						new BukkitRunnable() {

							@Override
							public void run() {
								p.teleport(location);
							}

						}.runTaskLater(Main.getInstance(), 10L);
					} else {
						p.teleport(location);
					}
				}
			}

			storage.loadLobbyInventory(this);

			new BukkitRunnable() {

				@Override
				public void run() {
					Game.this.setPlayerGameMode(p);
					Game.this.setPlayerVisibility(p);
				}

			}.runTaskLater(Main.getInstance(), 15L);

			this.broadcast(ChatColor.GREEN
					+ Main._l("lobby.playerjoin", ImmutableMap.of("player", p.getDisplayName() + ChatColor.GREEN)));

			if (!this.isAutobalanceEnabled()) {
				this.freePlayers.add(p);
			} else {
				Team team = this.getLowestTeam();
				team.addPlayer(p);
			}

			if (Main.getInstance().getBooleanConfig("store-game-records", true)) {
				this.displayRecord(p);
			}

			GameLobbyCountdownRule rule = Main.getInstance().getLobbyCountdownRule();
			if (rule == GameLobbyCountdownRule.PLAYERS_IN_GAME
					|| rule == GameLobbyCountdownRule.ENOUGH_TEAMS_AND_PLAYERS) {
				if (rule.isRuleMet(this)) {
					if (this.glc == null) {
						this.glc = new GameLobbyCountdown(this);
						this.glc.setRule(rule);
						this.glc.runTaskTimer(Main.getInstance(), 20L, 20L);
					}
				} else {
					int playersNeeded = this.getMinPlayers() - this.getPlayerAmount();
					this.broadcast(ChatColor.GREEN + Main._l("lobby.moreplayersneeded", "count",
							ImmutableMap.of("count", String.valueOf(playersNeeded))));

				}
			}
		}

		BedwarsPlayerJoinedEvent joinEvent = new BedwarsPlayerJoinedEvent(this, p);
		Main.getInstance().getServer().getPluginManager().callEvent(joinEvent);

		this.updateScoreboard();
		this.updateSigns();
		return true;

	}

	public boolean playerLeave(Player p, boolean kicked) {
		this.getPlayerSettings(p).setTeleporting(true);
		BedwarsPlayerLeaveEvent leaveEvent = new BedwarsPlayerLeaveEvent(this, p);
		Main.getInstance().getServer().getPluginManager().callEvent(leaveEvent);

		Team team = this.getPlayerTeam(p);

		PlayerStatistic statistic = null;
		if (Main.getInstance().statisticsEnabled()) {
			statistic = Main.getInstance().getPlayerStatisticManager().getStatistic(p);
		}

		if (this.isSpectator(p)) {
			if (!this.getCycle().isEndGameRunning()) {
				for (Player player : this.getPlayers()) {
					if (player.equals(p)) {
						continue;
					}

					player.showPlayer(p);
					p.showPlayer(player);
				}
			}
		} else {
			if (this.state == GameState.RUNNING && !this.getCycle().isEndGameRunning()) {
				if (!team.isDead(this) && !p.isDead()) {
					if (Main.getInstance().statisticsEnabled()) {
						statistic.setLoses(statistic.getLoses() + 1);
						statistic.addCurrentScore(Main.getInstance().getIntConfig("statistics.scores.lose", 0));
					}
				}
			}
		}

		Main.getInstance().getGameManager().removeGamePlayer(p);

		if (this.isProtected(p)) {
			this.removeProtection(p);
		}

		this.playerDamages.remove(p);
		if (team != null) {
			team.removePlayer(p);
			if (kicked) {
				this.broadcast(ChatColor.RED + Main._l("ingame.player.kicked", ImmutableMap.of("player",
						Game.getPlayerWithTeamString(p, team, ChatColor.RED) + ChatColor.RED)));
			} else {
				this.broadcast(ChatColor.RED + Main._l("ingame.player.left", ImmutableMap.of("player",
						Game.getPlayerWithTeamString(p, team, ChatColor.RED) + ChatColor.RED)));
			}
		}

		if (this.freePlayers.contains(p)) {
			this.freePlayers.remove(p);
		}

		if (Main.getInstance().isBungee()) {
			this.cycle.onPlayerLeave(p);
		}

		if (Main.getInstance().statisticsEnabled()) {
			// store statistics and unload
			statistic.setScore(statistic.getScore() + statistic.getCurrentScore());
			statistic.setCurrentScore(0);
			statistic.store();

			if (Main.getInstance().isHologramsEnabled() && Main.getInstance().getHolographicInteractor() != null) {
				Main.getInstance().getHolographicInteractor().updateHolograms(p);
			}

			if (Main.getInstance().getBooleanConfig("statistics.show-on-game-end", true)) {
				Main.getInstance().getServer().dispatchCommand(p, "bw stats");
			}

			Main.getInstance().getPlayerStatisticManager().unloadStatistic(p);
		}

		PlayerStorage storage = this.storages.get(p);
		storage.clean();
		storage.restore();

		this.playerSettings.remove(p);
		this.updateScoreboard();

		p.setScoreboard(Main.getInstance().getScoreboardManager().getMainScoreboard());

		this.removeNewItemShop(p);
		this.notUseOldShop(p);

		if (!Main.getInstance().isBungee() && p.isOnline()) {
			if (kicked) {
				p.sendMessage(ChatWriter.pluginMessage(ChatColor.RED + Main._l("ingame.player.waskicked")));
			} else {
				p.sendMessage(ChatWriter.pluginMessage(ChatColor.GREEN + Main._l("success.left")));
			}
		}

		if (!Main.getInstance().isBungee()) {
			this.cycle.onPlayerLeave(p);
		}

		this.updateSigns();
		this.storages.remove(p);
		return true;
	}

	public PlayerStorage addPlayerStorage(Player p) {
		PlayerStorage storage = new PlayerStorage(p);
		this.storages.put(p, storage);

		return storage;
	}

	public void broadcast(String message) {
		for (Player p : this.getPlayers()) {
			if (p.isOnline()) {
				p.sendMessage(ChatWriter.pluginMessage(message));
			}
		}
	}

	public void broadcastSound(Sound sound, float volume, float pitch) {
		for (Player p : this.getPlayers()) {
			if (p.isOnline()) {
				p.playSound(p.getLocation(), sound, volume, pitch);
			}
		}
	}

	public void broadcastSound(Sound sound, float volume, float pitch, List<Player> players) {
		for (Player p : players) {
			if (p.isOnline()) {
				p.playSound(p.getLocation(), sound, volume, pitch);
			}
		}
	}

	public void broadcast(String message, List<Player> players) {
		for (Player p : players) {
			if (p.isOnline()) {
				p.sendMessage(ChatWriter.pluginMessage(message));
			}
		}
	}

	public void addRunningTask(BukkitTask task) {
		this.runningTasks.add(task);
	}

	public boolean handleDestroyTargetMaterial(Player p, Block block) {
		Team team = this.getPlayerTeam(p);
		if (team == null) {
			return false;
		}

		Team bedDestroyTeam = null;
		Block bedBlock = team.getHeadTarget();

		if (block.getType().equals(Material.BED_BLOCK)) {
			Block breakBlock = block;
			Block neighbor = null;
			Bed breakBed = (Bed) breakBlock.getState().getData();

			if (!breakBed.isHeadOfBed()) {
				neighbor = breakBlock;
				breakBlock = Utils.getBedNeighbor(neighbor);
			} else {
				neighbor = Utils.getBedNeighbor(breakBlock);
			}

			if (bedBlock.equals(breakBlock)) {
				p.sendMessage(ChatWriter.pluginMessage(ChatColor.RED + Main._l("ingame.blocks.ownbeddestroy")));
				return false;
			}

			bedDestroyTeam = this.getTeamOfBed(breakBlock);
			if (bedDestroyTeam == null) {
				return false;
			}

			neighbor.getDrops().clear();
			neighbor.setType(Material.AIR);
			breakBlock.getDrops().clear();
			breakBlock.setType(Material.AIR);
		} else {
			if (bedBlock.equals(block)) {
				p.sendMessage(ChatWriter.pluginMessage(ChatColor.RED + Main._l("ingame.blocks.ownbeddestroy")));
				return false;
			}

			bedDestroyTeam = this.getTeamOfBed(block);
			if (bedDestroyTeam == null) {
				return false;
			}

			block.getDrops().clear();
			block.setType(Material.AIR);
		}

		// set statistics
		if (Main.getInstance().statisticsEnabled()) {
			PlayerStatistic statistic = Main.getInstance().getPlayerStatisticManager().getStatistic(p);
			statistic.setDestroyedBeds(statistic.getDestroyedBeds() + 1);
			statistic.addCurrentScore(Main.getInstance().getIntConfig("statistics.scores.bed-destroy", 25));
		}

		// reward when destroy bed
		if (Main.getInstance().getBooleanConfig("rewards.enabled", false)) {
			List<String> commands = Main.getInstance().getConfig().getStringList("rewards.player-destroy-bed");
			Main.getInstance().dispatchRewardCommands(commands, ImmutableMap.of("{player}", p.getName(), "{score}",
					String.valueOf(Main.getInstance().getIntConfig("statistics.scores.bed-destroy", 25))));
		}

		this.broadcast(
				ChatColor.RED + Main._l("ingame.blocks.beddestroyed",
						ImmutableMap.of("team",
								bedDestroyTeam.getChatColor() + bedDestroyTeam.getName() + ChatColor.RED, "player",
								Game.getPlayerWithTeamString(p, team, ChatColor.RED))));

		this.broadcastSound(
				Sound.valueOf(Main.getInstance().getStringConfig("bed-sound", "ENDERDRAGON_GROWL").toUpperCase()),
				30.0F, 10.0F);
		this.updateScoreboard();
		return true;
	}

	public void saveRecord() {
		File gameConfig = new File(
				Main.getInstance().getDataFolder() + "/" + GameManager.gamesPath + "/" + this.name + "/game.yml");

		if (!gameConfig.exists()) {
			return;
		}

		this.config.set("record", this.record);
		if (Main.getInstance().getBooleanConfig("store-game-records-holder", true)) {
			this.config.set("record-holders", this.recordHolders);
		}

		try {
			this.config.save(gameConfig);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public GameCheckCode checkGame() {
		if (this.loc1 == null || this.loc2 == null) {
			return GameCheckCode.LOC_NOT_SET_ERROR;
		}

		if (this.teams == null) {
			return GameCheckCode.TEAM_SIZE_LOW_ERROR;
		}

		if (this.teams.size() <= 1) {
			return GameCheckCode.TEAM_SIZE_LOW_ERROR;
		}

		GameCheckCode teamCheck = this.checkTeams();
		if (teamCheck != GameCheckCode.OK) {
			return teamCheck;
		}

		if (this.getRessourceSpawner().size() == 0) {
			return GameCheckCode.NO_RES_SPAWNER_ERROR;
		}

		if (this.lobby == null) {
			return GameCheckCode.NO_LOBBY_SET;
		}

		if (Main.getInstance().toMainLobby()) {
			if (this.mainLobby == null) {
				return GameCheckCode.NO_MAIN_LOBBY_SET;
			}
		}

		return GameCheckCode.OK;
	}

	public void openSpectatorCompass(Player player) {
		if (!this.isSpectator(player)) {
			return;
		}

		int teamplayers = this.getTeamPlayers().size();
		int nom = (teamplayers % 9 == 0) ? 9 : (teamplayers % 9);
		int size = teamplayers + (9 - nom);
		Inventory compass = Bukkit.createInventory(null, size, Main._l("ingame.spectator"));
		for (Team t : this.getTeams().values()) {
			for (Player p : t.getPlayers()) {
				ItemStack head = new ItemStack(Material.SKULL_ITEM, 1, (short) 3);
				SkullMeta meta = (SkullMeta) head.getItemMeta();
				meta.setDisplayName(t.getChatColor() + p.getDisplayName());
				meta.setLore(Arrays.asList(t.getChatColor() + t.getDisplayName()));
				meta.setOwner(p.getName());
				head.setItemMeta(meta);

				compass.addItem(head);
			}
		}

		player.openInventory(compass);
	}

	public void nonFreePlayer(Player p) {
		if (this.freePlayers.contains(p)) {
			this.freePlayers.remove(p);
		}
	}

	public void loadItemShopCategories() {
		this.itemshop = MerchantCategory.loadCategories(Main.getInstance().getShopConfig());
		this.orderedItemshop = this.loadOrderedItemShopCategories();
	}

	public NewItemShop openNewItemShop(Player player) {
		NewItemShop newShop = new NewItemShop(this.orderedItemshop);
		this.newItemShops.put(player, newShop);

		return newShop;
	}

	private List<MerchantCategory> loadOrderedItemShopCategories() {
		List<MerchantCategory> list = new ArrayList<MerchantCategory>(this.itemshop.values());
		Collections.sort(list, new MerchantCategoryComparator());
		return list;
	}

	public void kickAllPlayers() {
		for (Player p : this.getPlayers()) {
			this.playerLeave(p, false);
		}
	}

	public Team getTeamOfEnderChest(Block chest) {
		for (Team team : this.teams.values()) {
			if (team.getChests().contains(chest)) {
				return team;
			}
		}

		return null;
	}

	public void resetRegion() {
		if (this.region == null) {
			return;
		}

		this.region.reset(this);
	}

	private String formatScoreboardTitle() {
		String format = Main.getInstance().getStringConfig("scoreboard.format-title", "&e$region$&f - $time$");

		// replaces
		format = format.replace("$region$", this.getRegion().getName());
		format = format.replace("$game$", this.name);
		format = format.replace("$time$", this.getFormattedTimeLeft());

		return ChatColor.translateAlternateColorCodes('&', format);
	}

	private String formatScoreboardTeam(Team team, boolean destroyed) {
		String format = null;

		if (team == null) {
			return "";
		}

		if (destroyed) {
			format = Main.getInstance().getStringConfig("scoreboard.format-bed-destroyed", "&c$status$ $team$");
		} else {
			format = Main.getInstance().getStringConfig("scoreboard.format-bed-alive", "&a$status$ $team$");
		}

		format = format.replace("$status$", (destroyed) ? Game.bedLostString() : Game.bedExistString());
		format = format.replace("$team$", team.getChatColor() + team.getName());

		return ChatColor.translateAlternateColorCodes('&', format);
	}

	private String formatLobbyScoreboardString(String str) {
		str = str.replace("$regionname$", this.region.getName());
		str = str.replace("$gamename$", this.name);
		str = str.replace("$players$", String.valueOf(this.getPlayerAmount()));
		str = str.replace("$maxplayers$", String.valueOf(this.getMaxPlayers()));

		return ChatColor.translateAlternateColorCodes('&', str);
	}

	private void updateLobbyScoreboard() {
		this.scoreboard.clearSlot(DisplaySlot.SIDEBAR);

		Objective obj = this.scoreboard.getObjective("lobby");
		if (obj != null) {
			obj.unregister();
		}

		obj = this.scoreboard.registerNewObjective("lobby", "dummy");
		obj.setDisplaySlot(DisplaySlot.SIDEBAR);
		obj.setDisplayName(this.formatLobbyScoreboardString(
				Main.getInstance().getStringConfig("lobby-scoreboard.title", "&eBEDWARS")));

		List<String> rows = Main.getInstance().getConfig().getStringList("lobby-scoreboard.content");
		int rowMax = rows.size();
		if (rows == null || rows.isEmpty()) {
			return;
		}

		for (String row : rows) {
			if (row.trim().equals("")) {
				for (int i = 0; i <= rowMax; i++) {
					row = row + " ";
				}
			}

			Score score = obj.getScore(this.formatLobbyScoreboardString(row));
			score.setScore(rowMax);
			rowMax--;
		}

		for (Player player : this.getPlayers()) {
			player.setScoreboard(this.scoreboard);
		}
	}

	public void updateScoreboard() {
		if (this.state == GameState.WAITING && Main.getInstance().getBooleanConfig("lobby-scoreboard.enabled", true)) {
			this.updateLobbyScoreboard();
			return;
		}

		Objective obj = this.scoreboard.getObjective("display");
		if (obj == null) {
			obj = this.scoreboard.registerNewObjective("display", "dummy");
		}

		obj.setDisplaySlot(DisplaySlot.SIDEBAR);
		obj.setDisplayName(this.formatScoreboardTitle());

		for (Team t : this.teams.values()) {
			this.scoreboard.resetScores(this.formatScoreboardTeam(t, false));
			this.scoreboard.resetScores(this.formatScoreboardTeam(t, true));

			boolean teamDead = (t.isDead(this) && this.getState() == GameState.RUNNING) ? true : false;
			Score score = obj.getScore(this.formatScoreboardTeam(t, teamDead));
			score.setScore(t.getPlayers().size());
		}

		for (Player player : this.getPlayers()) {
			player.setScoreboard(this.scoreboard);
		}
	}

	public void setScoreboard(Scoreboard sb) {
		this.scoreboard = sb;
	}

	public Team isOver() {
		if (this.isOver || this.state != GameState.RUNNING) {
			return null;
		}

		ArrayList<Player> players = this.getTeamPlayers();
		ArrayList<Team> teams = new ArrayList<>();

		if (players.size() == 0 || players.isEmpty()) {
			return null;
		}

		for (Player player : players) {
			Team playerTeam = this.getPlayerTeam(player);
			if (teams.contains(playerTeam)) {
				continue;
			}

			if (!player.isDead()) {
				teams.add(playerTeam);
			} else if (!playerTeam.isDead(this)) {
				teams.add(playerTeam);
			}
		}

		if (teams.size() == 1) {
			return teams.get(0);
		} else {
			return null;
		}
	}

	public void resetScoreboard() {
		this.timeLeft = Main.getInstance().getMaxLength();
		this.length = this.timeLeft;
		this.scoreboard.clearSlot(DisplaySlot.SIDEBAR);
	}

	public void addJoinSign(Location signLocation) {
		if (this.joinSigns.containsKey(signLocation)) {
			this.joinSigns.remove(signLocation);
		}

		this.joinSigns.put(signLocation, new GameJoinSign(this, signLocation));
		this.updateSignConfig();
	}

	public void removeJoinSign(Location location) {
		this.joinSigns.remove(location);
		this.updateSignConfig();
	}

	private void updateSignConfig() {
		try {
			File config = new File(
					Main.getInstance().getDataFolder() + "/" + GameManager.gamesPath + "/" + this.name + "/sign.yml");

			YamlConfiguration cfg = new YamlConfiguration();
			if (config.exists()) {
				cfg = YamlConfiguration.loadConfiguration(config);
			}

			List<Map<String, Object>> locList = new ArrayList<Map<String, Object>>();
			for (Location loc : this.joinSigns.keySet()) {
				locList.add(Utils.locationSerialize(loc));
			}

			cfg.set("signs", locList);
			cfg.save(config);
		} catch (Exception ex) {
			Main.getInstance().getServer().getConsoleSender()
					.sendMessage(ChatWriter.pluginMessage(ChatColor.RED + Main._l("errors.savesign")));
		}
	}

	public void updateSigns() {
		boolean removedItem = false;

		Iterator<GameJoinSign> iterator = Game.this.joinSigns.values().iterator();
		while (iterator.hasNext()) {
			GameJoinSign sign = iterator.next();

			Chunk signChunk = sign.getSign().getLocation().getChunk();
			if (!signChunk.isLoaded()) {
				signChunk.load(true);
			}

			if (sign.getSign() == null) {
				iterator.remove();
				removedItem = true;
				continue;
			}

			Block signBlock = sign.getSign().getLocation().getBlock();
			if (!(signBlock.getState() instanceof Sign)) {
				iterator.remove();
				removedItem = true;
				continue;
			}
			sign.updateSign();
		}

		if (removedItem) {
			Game.this.updateSignConfig();
		}
	}

	public void stopWorkers() {
		for (BukkitTask task : this.runningTasks) {
			try {
				task.cancel();
			} catch (Exception ex) {
				// already cancelled
			}
		}

		this.runningTasks.clear();
	}

	public boolean isProtected(Player player) {
		return (this.respawnProtected.containsKey(player) && this.getState() == GameState.RUNNING);
	}

	public void clearProtections() {
		for (RespawnProtectionRunnable protection : this.respawnProtected.values()) {
			try {
				protection.cancel();
			} catch (Exception ex) {
				// isn't running, ignore
			}
		}

		this.respawnProtected.clear();
	}

	public void removeTeam(Team team) {
		this.teams.remove(team.getName());
		this.updateSigns();
	}

	public void removeRunningTask(BukkitTask task) {
		this.runningTasks.remove(task);
	}

	public void removeRunningTask(BukkitRunnable bukkitRunnable) {
		this.runningTasks.remove(bukkitRunnable);
	}

	public String getFormattedRecord() {
		return Utils.getFormattedTime(this.record);
	}

	/*
	 * GETTER / SETTER
	 */

	public void setTime(int time) {
		this.time = time;
	}

	public int getTime() {
		return this.time;
	}

	public int getTimeLeft() {
		return this.timeLeft;
	}

	public void setRecord(int int1) {
		this.record = int1;
	}

	public int getRecord() {
		return this.record;
	}

	public List<SpecialItem> getSpecialItems() {
		return this.currentSpecials;
	}

	public Map<Player, Player> getPlayerDamages() {
		return this.playerDamages;
	}

	public Player getPlayerDamager(Player p) {
		return this.playerDamages.get(p);
	}

	public void setPlayerDamager(Player p, Player damager) {
		this.playerDamages.remove(p);
		this.playerDamages.put(p, damager);
	}

	public void removeProtection(Player player) {
		RespawnProtectionRunnable rpr = this.respawnProtected.get(player);
		if (rpr == null) {
			return;
		}

		try {
			rpr.cancel();
		} catch (Exception ex) {
			// isn't running, ignore
		}

		this.respawnProtected.remove(player);
	}

	public RespawnProtectionRunnable addProtection(Player player) {
		RespawnProtectionRunnable rpr = new RespawnProtectionRunnable(this, player,
				Main.getInstance().getRespawnProtectionTime());
		this.respawnProtected.put(player, rpr);

		return rpr;
	}

	public List<String> getRecordHolders() {
		return this.recordHolders;
	}

	public void addRecordHolder(String holder) {
		this.recordHolders.add(holder);
	}

	public boolean isStopping() {
		return this.isStopping;
	}

	public String getBuilder() {
		return this.builder;
	}

	public void setBuilder(String builder) {
		this.builder = builder;
	}

	public Material getTargetMaterial() {
		if (this.targetMaterial == null) {
			return Utils.getMaterialByConfig("game-block", Material.BED_BLOCK);
		}

		return this.targetMaterial;
	}

	public void setTargetMaterial(Material targetMaterial) {
		this.targetMaterial = targetMaterial;
	}

	public List<MerchantCategory> getOrderedItemShopCategories() {
		return this.orderedItemshop;
	}

	public void setGameLobbyCountdown(GameLobbyCountdown countdown) {
		this.glc = countdown;
	}

	public boolean isUsingOldShop(Player player) {
		return (this.useOldItemShop.contains(player));
	}

	public void notUseOldShop(Player player) {
		this.useOldItemShop.remove(player);
	}

	public void useOldShop(Player player) {
		this.useOldItemShop.add(player);
	}

	public boolean isOverSet() {
		return this.isOver;
	}

	public HashMap<Location, GameJoinSign> getSigns() {
		return this.joinSigns;
	}

	public GameCycle getCycle() {
		return this.cycle;
	}

	public void setItemShopCategories(HashMap<Material, MerchantCategory> cats) {
		this.itemshop = cats;
	}

	public HashMap<Material, MerchantCategory> getItemShopCategories() {
		return this.itemshop;
	}

	public Team getTeamByDyeColor(DyeColor dyeColor) {
		for (Team t : this.teams.values()) {
			if (t.getColor().getDyeColor().equals(dyeColor)) {
				return t;
			}
		}

		return null;
	}

	public HashMap<String, Team> getTeams() {
		return this.teams;
	}

	public Region getRegion() {
		return this.region;
	}

	public ArrayList<Player> getTeamPlayers() {
		ArrayList<Player> players = new ArrayList<>();

		for (Team team : this.teams.values()) {
			players.addAll(team.getPlayers());
		}

		return players;
	}

	public ArrayList<Player> getPlayers() {
		ArrayList<Player> players = new ArrayList<>();

		players.addAll(this.freePlayers);

		for (Team team : this.teams.values()) {
			players.addAll(team.getPlayers());
		}

		return players;
	}

	public List<Player> getNonVipPlayers() {
		List<Player> players = this.getPlayers();

		Iterator<Player> playerIterator = players.iterator();
		while (playerIterator.hasNext()) {
			Player player = playerIterator.next();
			if (player.hasPermission("bw.vip.joinfull") || player.hasPermission("bw.vip.forcestart")
					|| player.hasPermission("bw.vip")) {
				playerIterator.remove();
			}
		}

		return players;
	}

	public int getMinPlayers() {
		return this.minPlayers;
	}

	public GameState getState() {
		return state;
	}

	public void setState(GameState state) {
		this.state = state;
		this.updateSigns();
	}

	public String getName() {
		return this.name;
	}

	public void setRegion(Region region) {
		this.region = region;
	}

	public void setMinPlayers(int players) {
		int max = this.getMaxPlayers();
		if (max < players && max > 0) {
			players = max;
		}

		this.minPlayers = players;
	}

	public Location getLobby() {
		return this.lobby;
	}

	public void setLobby(Player sender) {
		Location lobby = sender.getLocation();

		if (this.region != null) {
			if (this.region.getWorld().equals(lobby.getWorld())) {
				sender.sendMessage(ChatWriter.pluginMessage(ChatColor.RED + Main._l("errors.lobbyongameworld")));
				return;
			}
		}

		this.lobby = lobby;
		sender.sendMessage(ChatWriter.pluginMessage(ChatColor.GREEN + Main._l("success.lobbyset")));
	}

	public void setLobby(Location lobby) {
		if (this.region != null) {
			if (this.region.getWorld().equals(lobby.getWorld())) {
				Main.getInstance().getServer().getConsoleSender()
						.sendMessage(ChatWriter.pluginMessage(ChatColor.RED + Main._l("errors.lobbyongameworld")));
				return;
			}
		}

		this.lobby = lobby;
	}

	public Team getTeam(String name) {
		return this.teams.get(name);
	}

	public List<Team> getPlayingTeams() {
		return this.playingTeams;
	}

	public void removePlayerSettings(Player player) {
		this.playerSettings.remove(player);
	}

	public void addPlayerSettings(Player player) {
		this.playerSettings.put(player, new PlayerSettings(player));
	}

	public PlayerSettings getPlayerSettings(Player player) {
		return this.playerSettings.get(player);
	}

	public PlayerStorage getPlayerStorage(Player p) {
		return this.storages.get(p);
	}

	public void setConfig(YamlConfiguration config) {
		this.config = config;
	}

	public YamlConfiguration getConfig() {
		return this.config;
	}

	public void addSpecialItem(SpecialItem item) {
		this.currentSpecials.add(item);
	}

	public void removeSpecialItem(SpecialItem item) {
		this.currentSpecials.remove(item);
	}

	public Location getMainLobby() {
		return this.mainLobby;
	}

	public void setMainLobby(Location location) {
		this.mainLobby = location;
	}

	public NewItemShop getNewItemShop(Player player) {
		return this.newItemShops.get(player);
	}

	public void removeNewItemShop(Player player) {
		if (!this.newItemShops.containsKey(player)) {
			return;
		}

		this.newItemShops.remove(player);
	}

	public List<Player> getFreePlayers() {
		return this.freePlayers;
	}

	public List<Player> getFreePlayersClone() {
		List<Player> players = new ArrayList<Player>();
		if (this.freePlayers.size() > 0) {
			players.addAll(this.freePlayers);
		}

		return players;
	}

	public GameLobbyCountdown getLobbyCountdown() {
		return this.glc;
	}

	public void setLobbyCountdown(GameLobbyCountdown glc) {
		this.glc = glc;
	}

	public void setRegionName(String name) {
		this.regionName = name;
	}

	public int getLength() {
		return this.length;
	}

	public void setLength(int length) {
		this.length = length;
	}

	public void setAutobalance(boolean autobalance) {
		this.autobalance = autobalance;
	}

	/*
	 * PRIVATE
	 */

	private void displayMapInfo() {
		for (Player player : this.getPlayers()) {
			this.displayMapInfo(player);
		}
	}

	private void displayMapInfo(Player player) {
		try {
			Class<?> clazz = Class
					.forName("io.github.yannici.bedwars.Com." + Main.getInstance().getCurrentVersion() + ".Title");
			Method showTitle = clazz.getMethod("showTitle", Player.class, String.class, double.class, double.class,
					double.class);
			double titleFadeIn = Main.getInstance().getConfig().getDouble("titles.map.title-fade-in");
			double titleStay = Main.getInstance().getConfig().getDouble("titles.map.title-stay");
			double titleFadeOut = Main.getInstance().getConfig().getDouble("titles.map.title-fade-out");

			showTitle.invoke(null, player, this.getRegion().getName(), titleFadeIn, titleStay, titleFadeOut);

			if (this.builder != null) {
				Method showSubTitle = clazz.getMethod("showSubTitle", Player.class, String.class, double.class,
						double.class, double.class);
				double subtitleFadeIn = Main.getInstance().getConfig().getDouble("titles.map.subtitle-fade-in");
				double subtitleStay = Main.getInstance().getConfig().getDouble("titles.map.subtitle-stay");
				double subtitleFadeOut = Main.getInstance().getConfig().getDouble("titles.map.subtitle-fade-out");

				showSubTitle.invoke(null, player,
						Main._l("ingame.title.map-builder",
								ImmutableMap.of("builder", ChatColor.translateAlternateColorCodes('&', this.builder))),
						subtitleFadeIn, subtitleStay, subtitleFadeOut);
			}

		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	private void displayRecord() {
		for (Player player : this.getPlayers()) {
			this.displayRecord(player);
		}
	}

	private void displayRecord(Player player) {
		boolean displayHolders = Main.getInstance().getBooleanConfig("store-game-records-holder", true);

		if (displayHolders && this.getRecordHolders().size() > 0) {
			StringBuilder holders = new StringBuilder();

			for (String holder : this.recordHolders) {
				if (holders.length() == 0) {
					holders.append(ChatColor.WHITE + holder);
				} else {
					holders.append(ChatColor.GOLD + ", " + ChatColor.WHITE + holder);
				}
			}

			player.sendMessage(ChatWriter.pluginMessage(Main._l("ingame.record-with-holders",
					ImmutableMap.of("record", this.getFormattedRecord(), "holders", holders.toString()))));
		} else {
			player.sendMessage(ChatWriter
					.pluginMessage(Main._l("ingame.record", ImmutableMap.of("record", this.getFormattedRecord()))));
		}
	}

	private void makeTeamsReady() {
		this.playingTeams.clear();

		for (Team team : this.teams.values()) {
			team.getScoreboardTeam().setAllowFriendlyFire(Main.getInstance().getConfig().getBoolean("friendlyfire"));
			if (team.getPlayers().size() == 0) {
				if (team.getFeetTarget() != null) {
					team.getFeetTarget().setType(Material.AIR);
				}

				team.getHeadTarget().setType(Material.AIR);
			} else {

				this.playingTeams.add(team);
			}
		}

		this.updateScoreboard();
	}

	private void cleanUsersInventory() {
		for (PlayerStorage storage : this.storages.values()) {
			storage.clean();
		}
	}

	private void createGameConfig(File config) {
		YamlConfiguration yml = new YamlConfiguration();

		yml.set("name", this.name);
		yml.set("world", this.getRegion().getWorld().getName());
		yml.set("loc1", Utils.locationSerialize(this.loc1));
		yml.set("loc2", Utils.locationSerialize(this.loc2));
		yml.set("lobby", Utils.locationSerialize(this.lobby));
		yml.set("minplayers", this.getMinPlayers());

		if (Main.getInstance().getBooleanConfig("store-game-records", true)) {
			yml.set("record", this.record);

			if (Main.getInstance().getBooleanConfig("store-game-records-holder", true)) {
				yml.set("record-holders", this.recordHolders);
			}
		}

		if (this.regionName == null) {
			this.regionName = this.region.getName();
		}

		yml.set("regionname", this.regionName);
		yml.set("time", this.time);

		yml.set("targetmaterial", this.getTargetMaterial().name());
		yml.set("builder", this.builder);

		if (this.hologramLocation != null) {
			yml.set("hololoc", Utils.locationSerialize(this.hologramLocation));
		}

		if (this.mainLobby != null) {
			yml.set("mainlobby", Utils.locationSerialize(this.mainLobby));
		}

		yml.set("autobalance", this.autobalance);

		yml.set("spawner", this.resSpawner);
		yml.createSection("teams", this.teams);

		try {
			yml.save(config);
			this.config = yml;
		} catch (IOException e) {
			Main.getInstance().getLogger().info(ChatWriter.pluginMessage(e.getMessage()));
		}
	}

	private void saveRegion(boolean direct) {
		if (this.region == null || direct) {
			if (this.regionName == null) {
				this.regionName = this.loc1.getWorld().getName();
			}

			this.region = new Region(this.loc1, this.loc2, this.regionName);
		}

		// nametag the villager
		this.region.setVillagerNametag();

		this.updateSigns();
	}

	private void startRessourceSpawners() {
		for (RessourceSpawner rs : this.getRessourceSpawner()) {
			rs.setGame(this);
			this.runningTasks.add(Main.getInstance().getServer().getScheduler().runTaskTimer(Main.getInstance(), rs,
					20L, Math.round((((double) rs.getInterval()) / 1000.0) * 20.0)));
		}
	}

	private void teleportPlayersToTeamSpawn() {
		for (Team team : this.teams.values()) {
			for (Player player : team.getPlayers()) {
				if (!player.getWorld().equals(team.getSpawnLocation().getWorld())) {
					this.getPlayerSettings(player).setTeleporting(true);
				}
				player.setVelocity(new Vector(0, 0, 0));
				player.setFallDistance(0.0F);
				player.teleport(team.getSpawnLocation());
				if (this.getPlayerStorage(player) != null) {
					this.getPlayerStorage(player).clean();
				}
			}
		}
	}

	private Team getLowestTeam() {
		Team lowest = null;
		for (Team team : this.teams.values()) {
			if (lowest == null) {
				lowest = team;
				continue;
			}

			if (team.getPlayers().size() < lowest.getPlayers().size()) {
				lowest = team;
			}
		}

		return lowest;
	}

	private void moveFreePlayersToTeam() {
		for (Player player : this.freePlayers) {
			Team lowest = this.getLowestTeam();
			lowest.addPlayer(player);
		}

		this.freePlayers = new ArrayList<Player>();
		this.updateScoreboard();
	}

	private GameCheckCode checkTeams() {
		for (Team t : this.teams.values()) {
			if (t.getSpawnLocation() == null) {
				return GameCheckCode.TEAMS_WITHOUT_SPAWNS;
			}

			Material targetMaterial = this.getTargetMaterial();

			if (targetMaterial.equals(Material.BED_BLOCK)) {
				if ((t.getHeadTarget() == null || t.getFeetTarget() == null)
						|| (!Utils.isBedBlock(t.getHeadTarget()) || !Utils.isBedBlock(t.getFeetTarget()))) {
					return GameCheckCode.TEAM_NO_WRONG_BED;
				}
			} else {
				if (t.getHeadTarget() == null) {
					return GameCheckCode.TEAM_NO_WRONG_TARGET;
				}

				if (!t.getHeadTarget().getType().equals(targetMaterial)) {
					return GameCheckCode.TEAM_NO_WRONG_TARGET;
				}
			}

		}

		return GameCheckCode.OK;
	}

	private void updateScoreboardTimer() {
		Objective obj = this.scoreboard.getObjective("display");
		if (obj == null) {
			obj = this.scoreboard.registerNewObjective("display", "dummy");
		}

		obj.setDisplayName(this.formatScoreboardTitle());

		for (Player player : this.getPlayers()) {
			player.setScoreboard(this.scoreboard);
		}
	}

	private String getFormattedTimeLeft() {
		int min = 0;
		int sec = 0;
		String minStr = "";
		String secStr = "";

		min = (int) Math.floor(this.timeLeft / 60);
		sec = this.timeLeft % 60;

		minStr = (min < 10) ? "0" + String.valueOf(min) : String.valueOf(min);
		secStr = (sec < 10) ? "0" + String.valueOf(sec) : String.valueOf(sec);

		return minStr + ":" + secStr;
	}

	public void playerJoinTeam(Player player, Team team) {
		if (team.getPlayers().size() >= team.getMaxPlayers()) {
			player.sendMessage(ChatWriter.pluginMessage(ChatColor.RED + Main._l("errors.teamfull")));
			return;
		}

		if (team.addPlayer(player)) {
			this.nonFreePlayer(player);

			// Team color chestplate
			ItemStack chestplate = new ItemStack(Material.LEATHER_CHESTPLATE, 1);
			LeatherArmorMeta meta = (LeatherArmorMeta) chestplate.getItemMeta();
			meta.setColor(team.getColor().getColor());
			meta.setDisplayName(team.getChatColor() + team.getDisplayName());
			chestplate.setItemMeta(meta);

			player.getInventory().setItem(7, chestplate);
			player.updateInventory();
		} else {
			player.sendMessage(ChatWriter.pluginMessage(ChatColor.RED + Main._l("errors.teamfull")));
			return;
		}

		this.updateScoreboard();

		GameLobbyCountdownRule rule = Main.getInstance().getLobbyCountdownRule();
		if (rule == GameLobbyCountdownRule.TEAMS_HAVE_PLAYERS
				|| rule == GameLobbyCountdownRule.ENOUGH_TEAMS_AND_PLAYERS) {
			if (rule.isRuleMet(this)) {
				if (this.getLobbyCountdown() == null) {
					GameLobbyCountdown lobbyCountdown = new GameLobbyCountdown(this);
					lobbyCountdown.setRule(rule);
					lobbyCountdown.runTaskTimer(Main.getInstance(), 20L, 20L);
					this.setLobbyCountdown(lobbyCountdown);
				}
			}
		}

		team.equipPlayerWithLeather(player);
		player.sendMessage(ChatWriter.pluginMessage(ChatColor.GREEN
				+ Main._l("lobby.teamjoined", ImmutableMap.of("team", team.getDisplayName() + ChatColor.GREEN))));
	}

	private void startTimerCountdown() {
		this.timeLeft = Main.getInstance().getMaxLength();
		this.length = Main.getInstance().getMaxLength();
		BukkitRunnable task = new BukkitRunnable() {

			@Override
			public void run() {
				Game.this.updateScoreboardTimer();
				if (Game.this.timeLeft == 0) {
					Game.this.isOver = true;
					Game.this.getCycle().checkGameOver();
					this.cancel();
					return;
				}

				Game.this.timeLeft--;
			}
		};

		this.runningTasks.add(task.runTaskTimer(Main.getInstance(), 0L, 20L));
	}
}
