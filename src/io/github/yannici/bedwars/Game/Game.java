package io.github.yannici.bedwars.Game;

import io.github.yannici.bedwars.ChatWriter;
import io.github.yannici.bedwars.Main;
import io.github.yannici.bedwars.Utils;
import io.github.yannici.bedwars.Events.BedwarsGameStartEvent;
import io.github.yannici.bedwars.Events.BedwarsPlayerJoinEvent;
import io.github.yannici.bedwars.Events.BedwarsPlayerJoinedEvent;
import io.github.yannici.bedwars.Events.BedwarsPlayerLeaveEvent;
import io.github.yannici.bedwars.Events.BedwarsSaveGameEvent;
import io.github.yannici.bedwars.Shop.NewItemShop;
import io.github.yannici.bedwars.Statistics.PlayerStatistic;
import io.github.yannici.bedwars.Villager.MerchantCategory;
import io.github.yannici.bedwars.Villager.MerchantCategoryComparator;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

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
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;

import com.google.common.collect.ImmutableMap;

public class Game {

	private String name = null;
	private List<RessourceSpawner> resSpawner = null;
	private List<BukkitTask> runningTasks = null;
	private GameState state = null;
	private HashMap<String, Team> teams = null;
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

	private Main plugin = null;

	public Game(Main plugin, String name) {
		super();

		this.plugin = plugin;
		this.name = name;
		this.runningTasks = new ArrayList<BukkitTask>();

		this.freePlayers = new ArrayList<Player>();
		this.resSpawner = new ArrayList<RessourceSpawner>();
		this.teams = new HashMap<String, Team>();
		this.storages = new HashMap<Player, PlayerStorage>();
		this.state = GameState.STOPPED;
		this.scoreboard = Main.getInstance().getScoreboardManager()
				.getNewScoreboard();
		this.glc = null;
		this.joinSigns = new HashMap<Location, GameJoinSign>();
		this.timeLeft = Main.getInstance().getMaxLength();
		this.isOver = false;
		this.newItemShops = new HashMap<Player, NewItemShop>();
		this.useOldItemShop = new ArrayList<Player>();
		this.respawnProtected = new HashMap<Player, RespawnProtectionRunnable>();
		this.playerDamages = new HashMap<Player, Player>();
		
		if (Main.getInstance().isBungee()) {
			this.cycle = new BungeeGameCycle(this);
		} else {
			this.cycle = new SingleGameCycle(this);
		}
	}

	/*
	 * STATIC
	 */

	public static String getPlayerWithTeamString(Player player, Team team,
			ChatColor before) {
		return player.getDisplayName() + before + " (" + team.getDisplayName()
				+ before + ")";
	}

	public static String bedLostString() {
		return ChatColor.RED + "\u2718 ";
	}

	public static String bedExistString() {
		return ChatColor.GREEN + "\u2714 ";
	}

	public static Game getGameOfPlayer(Player p) {
		for (Game g : Main.getInstance().getGameManager().getGames()) {
			if (g.isInGame(p)) {
				return g;
			}
		}

		return null;
	}

	public static Team getPlayerTeam(Player p, Game g) {
		for (Team team : g.getTeams().values()) {
			if (team.isInTeam(p)) {
				return team;
			}
		}

		return null;
	}

	public static Team getTeamOfBed(Game g, Block bed) {
		for (Team team : g.getTeams().values()) {
			if (team.getHeadBed().equals(bed)) {
				return team;
			}
		}

		return null;
	}

	/*
	 * PUBLIC
	 */

	public boolean run(CommandSender sender) {
		if (this.state != GameState.STOPPED) {
			sender.sendMessage(ChatWriter.pluginMessage(ChatColor.RED
					+ Main._l("errors.cantstartagain")));
			return false;
		}
		
		// load bed chunks
		if(this.region != null) {
			for(Team team : this.teams.values()) {
				if(team.getHeadBed() == null && team.getFeedBed() == null) {
					continue;
				}
				
				if(team.getHeadBed() != null) {
					team.getHeadBed().getChunk().load(true);
				}
				
				if(team.getFeedBed() != null) {
					team.getFeedBed().getChunk().load(true);
				}
			}
		}

		GameCheckCode gcc = this.checkGame();
		if (gcc != GameCheckCode.OK) {
			sender.sendMessage(ChatWriter.pluginMessage(ChatColor.RED
					+ gcc.getCodeMessage()));
			return false;
		}
		
		this.loadItemShopCategories();
		
		if(sender instanceof Player) {
		    sender.sendMessage(ChatWriter.pluginMessage(ChatColor.GREEN
	                + Main._l("success.gamerun")));
		}
		
		this.isStopping = false;
		this.state = GameState.WAITING;
		this.updateSigns();
		return true;
	}

	public boolean start(CommandSender sender) {
		if (this.state != GameState.WAITING) {
			sender.sendMessage(ChatWriter.pluginMessage(ChatColor.RED
					+ Main._l("errors.startoutofwaiting")));
			return false;
		}

		BedwarsGameStartEvent startEvent = new BedwarsGameStartEvent(this);
		Main.getInstance().getServer().getPluginManager().callEvent(startEvent);

		if (startEvent.isCancelled()) {
			return false;
		}

		this.isOver = false;
		this.broadcast(ChatColor.GREEN + Main._l("ingame.gamestarting"));
		
		this.setTeamsFriendlyFire();
		this.cleanUsersInventory();
		this.clearProtections();
		this.moveFreePlayersToTeam();
		
		// set statistics
		for(Player player : this.getTeamPlayers()) {
		    PlayerStatistic statistic = Main.getInstance().getPlayerStatisticManager().getStatistic(player);
			if(statistic != null) {
				statistic.setGames(statistic.getGames()+1);
			}
		}

		this.cycle.onGameStart();
		
		this.startRessourceSpawners();
		
		this.resetRegion();
		// Update world time before game starts
		this.getRegion().getWorld().setTime(this.time);

		this.teleportPlayersToTeamSpawn();
		this.setPlayersScoreboard();

		this.startTimerCountdown();

		this.state = GameState.RUNNING;

		this.updateSigns();
		this.getPlugin()
				.getServer()
				.broadcastMessage(
						ChatWriter.pluginMessage(ChatColor.GREEN
								+ Main._l("ingame.gamestarted",
										ImmutableMap.of("game", this.getRegion().getName()))));
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

	public void addRessourceSpawner(int interval, Location location,
			ItemStack stack) {
		this.resSpawner.add(new RessourceSpawner(this, interval, location,
				stack));
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

	public boolean saveGame(CommandSender sender, boolean direct) {
		BedwarsSaveGameEvent saveEvent = new BedwarsSaveGameEvent(this, sender);
		Main.getInstance().getServer().getPluginManager().callEvent(saveEvent);

		if (saveEvent.isCancelled()) {
			return true;
		}

		GameCheckCode check = this.checkGame();

		if (check != GameCheckCode.OK) {
			sender.sendMessage(ChatWriter.pluginMessage(ChatColor.RED
					+ check.getCodeMessage()));
			return false;
		}

		File gameConfig = new File(this.getPlugin().getDataFolder() + "/"
				+ GameManager.gamesPath + "/" + this.name + "/game.yml");
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
		org.bukkit.scoreboard.Team newTeam = this.scoreboard
				.registerNewTeam(name);
		newTeam.setDisplayName(name);
		newTeam.setPrefix(color.getChatColor().toString());

		Team theTeam = new Team(name, color, maxPlayers, newTeam);
		this.teams.put(name, theTeam);
	}

	public void addTeam(Team team) {
		org.bukkit.scoreboard.Team newTeam = this.scoreboard
				.registerNewTeam(team.getName());
		newTeam.setDisplayName(team.getName());
		newTeam.setPrefix(team.getChatColor().toString());

		team.setScoreboardTeam(newTeam);

		this.teams.put(team.getName(), team);
	}

	public void toSpectator(Player player) {
		final Player p = player;
		
		Team playerTeam = Game.getPlayerTeam(p, this);
		if (playerTeam != null) {
			playerTeam.removePlayer(p);
		}

		if (!this.freePlayers.contains(p)) {
			this.freePlayers.add(p);
		}

		PlayerStorage storage = this.getPlayerStorage(p);
		if (storage != null) {
			storage.clean();
		} else {
			storage = this.addPlayerStorage(p);
			storage.store();
			storage.clean();
		}
		
		new BukkitRunnable() {

			@Override
			public void run() {
				p.setAllowFlight(true);
				p.setFlying(true);
				
				// 1.7 compatible
				try {
					p.setGameMode(GameMode.valueOf("SPECTATOR"));
				} catch(Exception ex) {
					p.setGameMode(GameMode.CREATIVE);
				}

				for (Player pl : Game.this.getPlayers()) {
					if (pl.equals(p)) {
						continue;
					}

					pl.hidePlayer(p);
				}
			}

		}.runTaskLater(Main.getInstance(), 5L);

		// Leave Game (Slimeball)
		ItemStack leaveGame = new ItemStack(Material.SLIME_BALL, 1);
		ItemMeta im = leaveGame.getItemMeta();
		im.setDisplayName(Main._l("lobby.leavegame"));
		leaveGame.setItemMeta(im);
		p.getInventory().setItem(8, leaveGame);

		p.updateInventory();
		this.setPlayersScoreboard();
	}

	public boolean isSpectator(Player player) {
		return (this.getState() == GameState.RUNNING && this.freePlayers
				.contains(player));
	}

	public boolean playerJoins(Player p) {
		if(this.state == GameState.STOPPED) {
			p.sendMessage(ChatWriter.pluginMessage(ChatColor.RED
					+ Main._l("errors.cantjoingame")));
			return false;
		}
		
		if (this.state == GameState.RUNNING
				&& !Main.getInstance().spectationEnabled()) {
			p.sendMessage(ChatWriter.pluginMessage(ChatColor.RED
					+ Main._l("errors.cantjoingame")));
			return false;
		}

		if (!this.cycle.onPlayerJoins(p)) {
			return false;
		}
		
		BedwarsPlayerJoinEvent joiningEvent = new BedwarsPlayerJoinEvent(this, p);
		Main.getInstance().getServer().getPluginManager().callEvent(joiningEvent);
		
		if(joiningEvent.isCancelled()) {
			return false;
		}
		
		if(Main.getInstance().statisticsEnabled()) {
			// load statistics
			Main.getInstance().getPlayerStatisticManager().getStatistic(p);
		}
		
		// add damager and set it to null
		this.playerDamages.put(p, null);
		
		if (this.state == GameState.RUNNING) {
	        this.toSpectator(p);
            p.teleport(((Team) this.teams.values().toArray()[Utils.randInt(0,
                    this.teams.size() - 1)]).getSpawnLocation());
            p.setScoreboard(this.scoreboard);
		} else {
			this.broadcast(ChatColor.GREEN + Main._l("lobby.playerjoin", ImmutableMap.of("player", p.getDisplayName() + ChatColor.GREEN)));
			this.freePlayers.add(p);
			
			PlayerStorage storage = this.addPlayerStorage(p);
			storage.store();
			storage.clean();

			p.teleport(this.lobby);
			storage.loadLobbyInventory();

			GameLobbyCountdownRule rule = Main.getInstance()
					.getLobbyCountdownRule();
			if (rule == GameLobbyCountdownRule.PLAYERS_IN_GAME) {
				if (rule.isRuleMet(this)) {
					if (this.glc == null) {
						this.glc = new GameLobbyCountdown(this);
						this.glc.setRule(rule);
						this.glc.runTaskTimer(Main.getInstance(), 20L, 20L);
					}
				}
			}

			p.setScoreboard(this.scoreboard);
		}
		
		BedwarsPlayerJoinedEvent joinEvent = new BedwarsPlayerJoinedEvent(this, p);
		Main.getInstance().getServer().getPluginManager().callEvent(joinEvent);

		this.updateSigns();
		return true;
	}

	public boolean playerLeave(Player p) {
		BedwarsPlayerLeaveEvent leaveEvent = new BedwarsPlayerLeaveEvent(this,
				p);
		Main.getInstance().getServer().getPluginManager().callEvent(leaveEvent);

		Team team = Game.getPlayerTeam(p, this);
		
		PlayerStatistic statistic = null;
		if(Main.getInstance().statisticsEnabled()) {
			statistic = Main.getInstance().getPlayerStatisticManager().getStatistic(p);
		}

		if (this.isSpectator(p)) {
			for (Player player : this.getPlayers()) {
				if (player.equals(p)) {
					continue;
				}

				player.showPlayer(p);
			}
		} else {
			if(this.state == GameState.RUNNING) {
				if(!team.isDead() && !p.isDead()) {
					if(Main.getInstance().statisticsEnabled()) {
						statistic.setLoses(statistic.getLoses()+1);
						statistic.setScore(statistic.getScore() + Main.getInstance().getIntConfig("statistics.scores.lose", 0));
					}
				}
			}
		}
		
		if(this.isProtected(p)) {
			this.removeProtection(p);
		}
		
		this.playerDamages.remove(p);
		if (team != null) {
			team.removePlayer(p);
			this.broadcast(ChatColor.RED
					+ Main._l("ingame.player.left", ImmutableMap
							.of("player", Game.getPlayerWithTeamString(p, team,
									ChatColor.RED) + ChatColor.RED)));
		}

		if (this.freePlayers.contains(p)) {
			this.freePlayers.remove(p);
		}
		
		if(Main.getInstance().statisticsEnabled()) {
			// store statistics and unload
			statistic.store();
			Main.getInstance().getPlayerStatisticManager().unloadStatistic(p);
		}

		PlayerStorage storage = this.storages.get(p);
		storage.restore();

		p.setScoreboard(Main.getInstance().getScoreboardManager()
				.getNewScoreboard());

		if (this.getState() == GameState.RUNNING) {
			this.setPlayersScoreboard();
		}

		this.removeNewItemShop(p);
		this.notUseOldShop(p);
		
		if (!Main.getInstance().isBungee() && p.isOnline()) {
			p.sendMessage(ChatWriter.pluginMessage(ChatColor.GREEN
					+ Main._l("success.left")));
		}
		
		this.updateSigns();
		
		this.cycle.onPlayerLeave(p);
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

	public void broadcast(String message, ArrayList<Player> players) {
		for (Player p : players) {
			if (p.isOnline()) {
				p.sendMessage(ChatWriter.pluginMessage(message));
			}
		}
	}

	public GameCheckCode checkGame() {
		if (this.loc1 == null || this.loc2 == null) {
			return GameCheckCode.LOC_NOT_SET_ERROR;
		}

		if(this.teams == null) {
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

	public void nonFreePlayer(Player p) {
		if (this.freePlayers.contains(p)) {
			this.freePlayers.remove(p);
		}
	}

	public void loadItemShopCategories() {
		if (this.itemshop != null) {
			return;
		}

		FileConfiguration cfg = Main.getInstance().getConfig();
		this.itemshop = MerchantCategory.loadCategories(cfg);
		this.orderedItemshop = this.loadOrderedItemShopCategories();
	}

	public NewItemShop openNewItemShop(Player player) {
		NewItemShop newShop = new NewItemShop(this.orderedItemshop);
		this.newItemShops.put(player, newShop);

		return newShop;
	}

	private List<MerchantCategory> loadOrderedItemShopCategories() {
		List<MerchantCategory> list = new ArrayList<MerchantCategory>(
				this.itemshop.values());
		Collections.sort(list, new MerchantCategoryComparator());
		return list;
	}

	public void kickAllPlayers() {
		for (Player p : this.getPlayers()) {
			this.playerLeave(p);
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

	public void setPlayersScoreboard() {
		Objective obj = this.scoreboard.getObjective("display");
		if (obj == null) {
			obj = this.scoreboard.registerNewObjective("display", "dummy");
		}

		obj.setDisplaySlot(DisplaySlot.SIDEBAR);
		obj.setDisplayName("Bedwars - " + this.getFormattedTimeLeft());

		for (Team t : this.teams.values()) {
			this.scoreboard.resetScores(Game.bedExistString()
					+ t.getChatColor() + t.getName());
			this.scoreboard.resetScores(Game.bedLostString() + t.getChatColor()
					+ t.getName());

			String teamString = (t.isDead() && this.getState() == GameState.RUNNING) ? Game
					.bedLostString() : Game.bedExistString();
			Score score = obj.getScore(teamString + t.getChatColor()
					+ t.getName());
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
			Team playerTeam = Game.getPlayerTeam(player, this);
			if (teams.contains(playerTeam)) {
				continue;
			}

			if (!player.isDead()) {
				teams.add(playerTeam);
			} else if (!playerTeam.isDead()) {
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
			File config = new File(this.getPlugin().getDataFolder() + "/"
					+ GameManager.gamesPath + "/" + this.name + "/sign.yml");

			YamlConfiguration cfg = new YamlConfiguration();
			if (config.exists()) {
				cfg = YamlConfiguration.loadConfiguration(config);
			}
			
			List<Map<String, Object>> locList = new ArrayList<Map<String, Object>>();
			for(Location loc : this.joinSigns.keySet()) {
				locList.add(Utils.locationSerialize(loc));
			}

			cfg.set("signs", locList);
			cfg.save(config);
		} catch (Exception ex) {
			Main.getInstance()
					.getServer()
					.getConsoleSender()
					.sendMessage(
							ChatWriter.pluginMessage(ChatColor.RED
									+ Main._l("errors.savesign")));
		}
	}

	public void updateSigns() {
		boolean removedItem = false;

		Iterator<GameJoinSign> iterator = this.joinSigns.values().iterator();
		while (iterator.hasNext()) {
			GameJoinSign sign = iterator.next();
			
			Chunk signChunk = sign.getSign().getLocation().getChunk();
			if(!signChunk.isLoaded()) {
				signChunk.load(true);
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
			this.updateSignConfig();
		}
	}

	public void stopWorkers() {
		for (BukkitTask task : this.runningTasks) {
			task.cancel();
		}
		
		this.runningTasks.clear();
	}
	
	public boolean isProtected(Player player) {
		return (this.respawnProtected.containsKey(player) 
				&& this.getState() == GameState.RUNNING);
	}
	
	public void clearProtections() {
		for(RespawnProtectionRunnable protection : this.respawnProtected.values()) {
			try {
				protection.cancel();
			} catch(Exception ex) {
				// isn't running, ignore
			}
		}
		
		this.respawnProtected.clear();
	}
	
	public void removeTeam(Team team) {
		this.teams.remove(team);
		this.updateSigns();
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
		if(rpr == null) {
			return;
		}
		
		try {
			rpr.cancel();
		} catch(Exception ex) {
			// isn't running, ignore
		}
		
		this.respawnProtected.remove(player);
	}
	
	public RespawnProtectionRunnable addProtection(Player player) {
		RespawnProtectionRunnable rpr = new RespawnProtectionRunnable(this, player, Main.getInstance().getRespawnProtectionTime());
		this.respawnProtected.put(player, rpr);
		
		return rpr;
	}
	
	public boolean isStopping() {
	    return this.isStopping;
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

	public Scoreboard getScoreboard() {
		return this.scoreboard;
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
	
	public ArrayList<Player> getNonVipPlayers() {
		ArrayList<Player> players = this.getPlayers();
		
		Iterator<Player> playerIterator = players.iterator();
		while(playerIterator.hasNext()) {
			Player player = playerIterator.next();
			if(player.hasPermission("bw.vip")) {
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

	public Main getPlugin() {
		return this.plugin;
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
				sender.sendMessage(ChatWriter.pluginMessage(ChatColor.RED
						+ Main._l("errors.lobbyongameworld")));
				return;
			}
		}

		this.lobby = lobby;
		sender.sendMessage(ChatWriter.pluginMessage(ChatColor.GREEN
				+ Main._l("success.lobbyset")));
	}

	public void setLobby(Location lobby) {
		if (this.region != null) {
			if (this.region.getWorld().equals(lobby.getWorld())) {
				Main.getInstance()
						.getServer()
						.getConsoleSender()
						.sendMessage(
								ChatWriter.pluginMessage(ChatColor.RED
										+ Main._l("errors.lobbyongameworld")));
				return;
			}
		}

		this.lobby = lobby;
	}

	public Team getTeam(String name) {
		return this.teams.get(name);
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

	public GameLobbyCountdown getLobbyCountdown() {
		return this.glc;
	}
	
	public void setLobbyCountdown(GameLobbyCountdown glc) {
		this.glc = glc;
	}
	
	public void setRegionName(String name) {
		this.regionName = name;
	}

	/*
	 * PRIVATE
	 */

	private void setTeamsFriendlyFire() {
		for (org.bukkit.scoreboard.Team team : this.scoreboard.getTeams()) {
			team.setAllowFriendlyFire(Main.getInstance().getConfig()
					.getBoolean("friendlyfire"));
		}
	}

	private void cleanUsersInventory() {
		for (PlayerStorage storage : this.storages.values()) {
			storage.clean();
		}
	}

	private void createGameConfig(File config) {
		YamlConfiguration yml = new YamlConfiguration();
		HashMap<String, RessourceSpawner> spawnerMap = new HashMap<>();
		int i = 0;

		for (RessourceSpawner rs : this.resSpawner) {
			spawnerMap.put("SPAWNER_" + i, rs);
			i++;
		}

		yml.set("name", this.name);
		yml.set("world", this.getRegion().getWorld().getName());
		yml.set("loc1", Utils.locationSerialize(this.loc1));
		yml.set("loc2", Utils.locationSerialize(this.loc2));
		yml.set("lobby", Utils.locationSerialize(this.lobby));
		yml.set("minplayers", this.getMinPlayers());
		
		if(this.regionName == null) {
			this.regionName = this.region.getWorld().getName();
		}
		
		yml.set("regionname", this.regionName);
		yml.set("time", this.time);
		
		if (this.mainLobby != null) {
			yml.set("mainlobby", Utils.locationSerialize(this.mainLobby));
		}

		yml.createSection("teams", this.teams);
		yml.createSection("spawner", spawnerMap);

		try {
			yml.save(config);
			this.config = yml;
		} catch (IOException e) {
			Main.getInstance().getLogger()
					.info(ChatWriter.pluginMessage(e.getMessage()));
		}
	}

	private void saveRegion(boolean direct) {
		if (this.region == null || direct) {
			if(this.regionName == null) {
				this.regionName = this.loc1.getWorld().getName();
			}
			
			this.region = new Region(this.loc1, this.loc2, this.regionName);
		}
		
		this.updateSigns();
	}

	private void startRessourceSpawners() {
		for (RessourceSpawner rs : this.getRessourceSpawner()) {
			rs.setGame(this);
			this.runningTasks.add(this
					.getPlugin()
					.getServer()
					.getScheduler()
					.runTaskTimer(this.getPlugin(), rs,
							((long) (1000 / 1000) * 20),
							((long) (rs.getInterval() / 1000) * 20)));
		}
	}

	private void teleportPlayersToTeamSpawn() {
		for (Team team : this.teams.values()) {
			for (Player player : team.getPlayers()) {
				player.teleport(team.getSpawnLocation());
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
	}

	private GameCheckCode checkTeams() {
		for (Team t : this.teams.values()) {
			if (t.getSpawnLocation() == null) {
				return GameCheckCode.TEAMS_WITHOUT_SPAWNS;
			}

			if ((t.getHeadBed() == null 
					|| t.getFeedBed() == null)
					|| (!Utils.isBedBlock(t.getHeadBed()) 
							|| !Utils.isBedBlock(t.getFeedBed()))) {
				return GameCheckCode.TEAM_NO_WRONG_BED;
			}
		}

		return GameCheckCode.OK;
	}

	private void updateScoreboardTimer() {
		Objective obj = this.scoreboard.getObjective("display");
		if (obj == null) {
			obj = this.scoreboard.registerNewObjective("display", "dummy");
		}

		obj.setDisplayName("Bedwars - " + this.getFormattedTimeLeft());

		for (Player player : this.getPlayers()) {
			player.setScoreboard(this.scoreboard);
		}
	}

	private String getFormattedTimeLeft() {
		int min = 0;
		int sec = this.timeLeft;
		String minStr = "";
		String secStr = "";

		min = (this.timeLeft >= 60 ? this.timeLeft % 60 : this.timeLeft);
		sec = (sec = (sec / 60)) >= 60 ? sec % 60 : sec;

		minStr = (min < 10) ? "0" + String.valueOf(min) : String.valueOf(min);
		secStr = (sec < 10) ? "0" + String.valueOf(sec) : String.valueOf(sec);

		return secStr + ":" + minStr;
	}

	private void startTimerCountdown() {
		Game.this.timeLeft = Main.getInstance().getMaxLength();
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
