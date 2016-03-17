package io.github.yannici.bedwars;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.scoreboard.ScoreboardManager;

import com.google.common.collect.ImmutableMap;

import io.github.yannici.bedwars.Commands.AddGameCommand;
import io.github.yannici.bedwars.Commands.AddHoloCommand;
import io.github.yannici.bedwars.Commands.AddTeamCommand;
import io.github.yannici.bedwars.Commands.AddTeamJoinCommand;
import io.github.yannici.bedwars.Commands.BaseCommand;
import io.github.yannici.bedwars.Commands.ClearSpawnerCommand;
import io.github.yannici.bedwars.Commands.GameTimeCommand;
import io.github.yannici.bedwars.Commands.HelpCommand;
import io.github.yannici.bedwars.Commands.JoinGameCommand;
import io.github.yannici.bedwars.Commands.KickCommand;
import io.github.yannici.bedwars.Commands.LeaveGameCommand;
import io.github.yannici.bedwars.Commands.ListGamesCommand;
import io.github.yannici.bedwars.Commands.RegionNameCommand;
import io.github.yannici.bedwars.Commands.ReloadCommand;
import io.github.yannici.bedwars.Commands.RemoveGameCommand;
import io.github.yannici.bedwars.Commands.RemoveHoloCommand;
import io.github.yannici.bedwars.Commands.RemoveTeamCommand;
import io.github.yannici.bedwars.Commands.SaveGameCommand;
import io.github.yannici.bedwars.Commands.SetAutobalanceCommand;
import io.github.yannici.bedwars.Commands.SetBedCommand;
import io.github.yannici.bedwars.Commands.SetBuilderCommand;
import io.github.yannici.bedwars.Commands.SetGameBlockCommand;
import io.github.yannici.bedwars.Commands.SetLobbyCommand;
import io.github.yannici.bedwars.Commands.SetMainLobbyCommand;
import io.github.yannici.bedwars.Commands.SetMinPlayersCommand;
import io.github.yannici.bedwars.Commands.SetRegionCommand;
import io.github.yannici.bedwars.Commands.SetSpawnCommand;
import io.github.yannici.bedwars.Commands.SetSpawnerCommand;
import io.github.yannici.bedwars.Commands.SetTargetCommand;
import io.github.yannici.bedwars.Commands.StartGameCommand;
import io.github.yannici.bedwars.Commands.StatsCommand;
import io.github.yannici.bedwars.Commands.StopGameCommand;
import io.github.yannici.bedwars.Database.DatabaseManager;
import io.github.yannici.bedwars.Game.Game;
import io.github.yannici.bedwars.Game.GameLobbyCountdownRule;
import io.github.yannici.bedwars.Game.GameManager;
import io.github.yannici.bedwars.Game.GameState;
import io.github.yannici.bedwars.Game.RessourceSpawner;
import io.github.yannici.bedwars.Game.Team;
import io.github.yannici.bedwars.Listener.BlockListener;
import io.github.yannici.bedwars.Listener.ChunkListener;
import io.github.yannici.bedwars.Listener.EntityListener;
import io.github.yannici.bedwars.Listener.HangingListener;
import io.github.yannici.bedwars.Listener.Player19Listener;
import io.github.yannici.bedwars.Listener.PlayerListener;
import io.github.yannici.bedwars.Listener.ServerListener;
import io.github.yannici.bedwars.Listener.SignListener;
import io.github.yannici.bedwars.Listener.WeatherListener;
import io.github.yannici.bedwars.Localization.LocalizationConfig;
import io.github.yannici.bedwars.Shop.Specials.SpecialItem;
import io.github.yannici.bedwars.Statistics.PlayerStatisticManager;
import io.github.yannici.bedwars.Statistics.StorageType;
import io.github.yannici.bedwars.Updater.ConfigUpdater;
import io.github.yannici.bedwars.Updater.DatabaseUpdater;
import io.github.yannici.bedwars.Updater.PluginUpdater;
import io.github.yannici.bedwars.Updater.PluginUpdater.UpdateCallback;
import io.github.yannici.bedwars.Updater.PluginUpdater.UpdateResult;

public class Main extends JavaPlugin {

	private static Main instance = null;

	public static int PROJECT_ID = 91743;

	private ArrayList<BaseCommand> commands = new ArrayList<BaseCommand>();
	private BukkitTask timeTask = null;
	private Package craftbukkit = null;
	private Package minecraft = null;
	private String version = null;
	private LocalizationConfig localization = null;
	private DatabaseManager dbManager = null;
	private BukkitTask updateChecker = null;

	private List<Material> breakableTypes = null;
	private YamlConfiguration shopConfig = null;

	private HolographicDisplaysInteraction holographicInteraction = null;

	private boolean isSpigot = false;

	private static Boolean locationSerializable = null;

	private PlayerStatisticManager playerStatisticManager = null;

	private ScoreboardManager scoreboardManager = null;
	private GameManager gameManager = null;

	@Override
	public void onEnable() {
		Main.instance = this;

		// register classes
		this.registerConfigurationClasses();

		// save default config
		this.saveDefaultConfig();
		this.loadConfigInUTF();

		this.getConfig().options().copyDefaults(true);
		this.getConfig().options().copyHeader(true);

		ConfigUpdater configUpdater = new ConfigUpdater();
		configUpdater.addConfigs();
		this.saveConfiguration();
		this.loadConfigInUTF();
		this.loadShop();

		this.isSpigot = this.getIsSpigot();
		this.loadDatabase();

		this.craftbukkit = this.getCraftBukkit();
		this.minecraft = this.getMinecraftPackage();
		this.version = this.loadVersion();

		this.registerCommands();
		this.registerListener();

		this.gameManager = new GameManager();

		// bungeecord
		if (Main.getInstance().isBungee()) {
			this.getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");
		}

		this.loadStatistics();
		this.localization = this.loadLocalization();

		this.checkUpdates();

		// Loading
		this.scoreboardManager = Bukkit.getScoreboardManager();
		this.gameManager.loadGames();
		this.startTimeListener();
		this.startMetricsIfEnabled();

		// holograms
		if (this.isHologramsEnabled()) {
			this.holographicInteraction = new HolographicDisplaysInteraction();
			this.holographicInteraction.loadHolograms();
		}
	}

	@Override
	public void onDisable() {
		this.stopTimeListener();
		this.gameManager.unloadGames();
		this.cleanDatabase();

		if (this.isHologramsEnabled() && this.holographicInteraction != null) {
			this.holographicInteraction.unloadHolograms();
		}
	}

	public void loadConfigInUTF() {
		File configFile = new File(this.getDataFolder(), "config.yml");
		if (!configFile.exists()) {
			return;
		}

		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(configFile), "UTF-8"));
			this.getConfig().load(reader);
		} catch (Exception e) {
			e.printStackTrace();
		}

		if (this.getConfig() == null) {
			return;
		}

		// load breakable materials
		this.breakableTypes = new ArrayList<Material>();
		for (String material : this.getConfig().getStringList("breakable-blocks")) {
			if (material.equalsIgnoreCase("none")) {
				continue;
			}

			Material mat = Utils.parseMaterial(material);
			if (mat == null) {
				continue;
			}

			if (this.breakableTypes.contains(mat)) {
				continue;
			}

			this.breakableTypes.add(mat);
		}
	}

	public void loadShop() {
		File file = new File(Main.getInstance().getDataFolder(), "shop.yml");
		if (!file.exists()) {
			// create default file
			this.saveResource("shop.yml", false);

			// wait until it's really saved
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		this.shopConfig = new YamlConfiguration();

		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF-8"));
			this.shopConfig.load(reader);
		} catch (Exception e) {
			this.getServer().getConsoleSender().sendMessage(
					ChatWriter.pluginMessage(ChatColor.RED + "Couldn't load shop! Error in parsing shop!"));
			e.printStackTrace();
		}
	}

	public void dispatchRewardCommands(List<String> commands, Map<String, String> replacements) {
		for (String command : commands) {
			command = command.trim();
			if (command.equals("")) {
				continue;
			}

			if (command.equalsIgnoreCase("none")) {
				break;
			}

			if (command.startsWith("/")) {
				command = command.substring(1);
			}

			for (Entry<String, String> entry : replacements.entrySet()) {
				command = command.replace(entry.getKey(), entry.getValue());
			}

			Main.getInstance().getServer().dispatchCommand(Main.getInstance().getServer().getConsoleSender(), command);
		}
	}

	public void saveConfiguration() {
		File file = new File(Main.getInstance().getDataFolder(), "config.yml");
		try {
			file.mkdirs();

			String data = this.getYamlDump((YamlConfiguration) this.getConfig());

			FileOutputStream stream = new FileOutputStream(file);
			OutputStreamWriter writer = new OutputStreamWriter(stream, "UTF-8");

			try {
				writer.write(data);
			} finally {
				writer.close();
				stream.close();
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public Class<?> getVersionRelatedClass(String className) {
		try {
			Class<?> clazz = Class
					.forName("io.github.yannici.bedwars.Com." + this.getCurrentVersion() + "." + className);
			return clazz;
		} catch (Exception ex) {
			this.getServer().getConsoleSender()
					.sendMessage(ChatWriter.pluginMessage(
							ChatColor.RED + "Couldn't find version related class io.github.yannici.bedwars.Com."
									+ this.getCurrentVersion() + "." + className));
		}

		return null;
	}

	public String getYamlDump(YamlConfiguration config) {
		try {
			String fullstring = config.saveToString();
			String endstring = fullstring;
			endstring = Utils.unescape_perl_string(fullstring);

			return endstring;
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		return null;
	}

	public boolean isBreakableType(Material type) {
		return (this.breakableTypes.contains(type));
	}

	public boolean isMineshafterPresent() {
		try {
			Class.forName("mineshafter.MineServer");
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	public PlayerStatisticManager getPlayerStatisticManager() {
		return this.playerStatisticManager;
	}

	private void checkUpdates() {
		try {
			if (this.getBooleanConfig("check-updates", true)) {
				this.updateChecker = new BukkitRunnable() {

					@Override
					public void run() {
						final BukkitRunnable task = this;
						UpdateCallback callback = new UpdateCallback() {

							@Override
							public void onFinish(PluginUpdater updater) {
								if (updater.getResult() == UpdateResult.SUCCESS) {
									task.cancel();
								}
							}
						};

						new PluginUpdater(Main.getInstance(), Main.PROJECT_ID, Main.getInstance().getFile(),
								PluginUpdater.UpdateType.DEFAULT, callback,
								Main.getInstance().getBooleanConfig("update-infos", true));
					}

				}.runTaskTimerAsynchronously(Main.getInstance(), 40L, 36000L);
			}
		} catch (Exception ex) {
			this.getServer().getConsoleSender()
					.sendMessage(ChatWriter.pluginMessage(ChatColor.RED + "Check for updates not successful: Error!"));
		}
	}

	private LocalizationConfig loadLocalization() {
		LocalizationConfig config = new LocalizationConfig();
		config.saveLocales(false);

		config.loadLocale(this.getConfig().getString("locale"), false);
		return config;
	}

	private void loadStatistics() {
		this.playerStatisticManager = new PlayerStatisticManager();
		this.playerStatisticManager.initialize();
	}

	private void loadDatabase() {
		if (!this.getBooleanConfig("statistics.enabled", false)
				|| !this.getStringConfig("statistics.storage", "yaml").equals("database")) {
			return;
		}

		this.getServer().getConsoleSender()
				.sendMessage(ChatWriter.pluginMessage(ChatColor.GREEN + "Initialize database ..."));
		this.loadingRequiredLibs();

		String host = this.getStringConfig("database.host", null);
		int port = this.getIntConfig("database.port", 3306);
		String user = this.getStringConfig("database.user", null);
		String password = this.getStringConfig("database.password", null);
		String db = this.getStringConfig("database.db", null);

		if (host == null || user == null || password == null || db == null) {
			return;
		}

		this.dbManager = new DatabaseManager(host, port, user, password, db);
		this.dbManager.initialize();

		this.getServer().getConsoleSender()
				.sendMessage(ChatWriter.pluginMessage(ChatColor.GREEN + "Update database ..."));
		(new DatabaseUpdater()).execute();

		this.getServer().getConsoleSender().sendMessage(ChatWriter.pluginMessage(ChatColor.GREEN + "Done."));
	}

	public StorageType getStatisticStorageType() {
		String storage = this.getStringConfig("statistics.storage", "yaml");
		return StorageType.getByName(storage);
	}

	public boolean statisticsEnabled() {
		return this.getBooleanConfig("statistics.enabled", false);
	}

	private void cleanDatabase() {
		if (this.dbManager != null) {
			this.dbManager.cleanUp();
		}
	}

	private void loadingRequiredLibs() {
		try {
			final File[] libs = new File[] { new File(this.getDataFolder() + "/lib/", "c3p0-0.9.5.jar"),
					new File(this.getDataFolder() + "/lib/", "mchange-commons-java-0.2.9.jar") };
			for (final File lib : libs) {
				if (!lib.exists()) {
					JarUtils.extractFromJar(lib.getName(), lib.getAbsolutePath());
				}
			}
			for (final File lib : libs) {
				if (!lib.exists()) {
					this.getLogger().warning(
							"There was a critical error loading bedwars plugin! Could not find lib: " + lib.getName());
					Bukkit.getServer().getPluginManager().disablePlugin(this);
					return;
				}
				this.addClassPath(JarUtils.getJarUrl(lib));
			}
		} catch (final Exception e) {
			e.printStackTrace();
		}
	}

	private void addClassPath(final URL url) throws IOException {
		final URLClassLoader sysloader = (URLClassLoader) ClassLoader.getSystemClassLoader();
		final Class<URLClassLoader> sysclass = URLClassLoader.class;
		try {
			final Method method = sysclass.getDeclaredMethod("addURL", new Class[] { URL.class });
			method.setAccessible(true);
			method.invoke(sysloader, new Object[] { url });
		} catch (final Throwable t) {
			t.printStackTrace();
			throw new IOException("Error adding " + url + " to system classloader");
		}
	}

	public DatabaseManager getDatabaseManager() {
		return this.dbManager;
	}

	public boolean isSpigot() {
		return this.isSpigot;
	}

	private boolean getIsSpigot() {
		try {
			Package spigotPackage = Package.getPackage("org.spigotmc");
			if (spigotPackage == null) {
				return false;
			}

			return true;
		} catch (Exception e) {
			// nope
		}

		return false;
	}

	public int getIntConfig(String key, int defaultInt) {
		FileConfiguration config = this.getConfig();
		if (config.contains(key)) {
			if (config.isInt(key)) {
				return config.getInt(key);
			}
		}

		return defaultInt;
	}

	public String getStringConfig(String key, String defaultString) {
		FileConfiguration config = this.getConfig();
		if (config.contains(key)) {
			if (config.isString(key)) {
				return config.getString(key);
			}
		}

		return defaultString;
	}

	public boolean getBooleanConfig(String key, boolean defaultBool) {
		FileConfiguration config = this.getConfig();
		if (config.contains(key)) {
			if (config.isBoolean(key)) {
				return config.getBoolean(key);
			}
		}

		return defaultBool;
	}

	public LocalizationConfig getLocalization() {
		return this.localization;
	}

	private String loadVersion() {
		String packName = Bukkit.getServer().getClass().getPackage().getName();
		return packName.substring(packName.lastIndexOf('.') + 1);
	}

	public String getCurrentVersion() {
		return this.version;
	}

	public boolean isBungee() {
		return this.getConfig().getBoolean("bungeecord.enabled");
	}

	public String getBungeeHub() {
		if (this.getConfig().contains("bungeecord.hubserver")) {
			return this.getConfig().getString("bungeecord.hubserver");
		}

		return null;
	}

	public Package getCraftBukkit() {
		try {
			if (this.craftbukkit == null) {
				return Package.getPackage("org.bukkit.craftbukkit."
						+ Bukkit.getServer().getClass().getPackage().getName().replace(".", ",").split(",")[3]);
			} else {
				return this.craftbukkit;
			}
		} catch (Exception ex) {
			this.getServer().getConsoleSender().sendMessage(ChatWriter.pluginMessage(
					ChatColor.RED + Main._l("errors.packagenotfound", ImmutableMap.of("package", "craftbukkit"))));
			return null;
		}
	}

	public Package getMinecraftPackage() {
		try {
			if (this.minecraft == null) {
				return Package.getPackage("net.minecraft.server."
						+ Bukkit.getServer().getClass().getPackage().getName().replace(".", ",").split(",")[3]);
			} else {
				return this.minecraft;
			}
		} catch (Exception ex) {
			this.getServer().getConsoleSender().sendMessage(ChatWriter.pluginMessage(
					ChatColor.RED + Main._l("errors.packagenotfound", ImmutableMap.of("package", "minecraft server"))));
			return null;
		}
	}

	@SuppressWarnings("rawtypes")
	public Class getCraftBukkitClass(String classname) {
		try {
			if (this.craftbukkit == null) {
				this.craftbukkit = this.getCraftBukkit();
			}

			return Class.forName(this.craftbukkit.getName() + "." + classname);
		} catch (Exception ex) {
			this.getServer().getConsoleSender().sendMessage(ChatWriter.pluginMessage(ChatColor.RED
					+ Main._l("errors.classnotfound", ImmutableMap.of("package", "craftbukkit", "class", classname))));
			return null;
		}
	}

	@SuppressWarnings("rawtypes")
	public Class getMinecraftServerClass(String classname) {
		try {
			if (this.minecraft == null) {
				this.minecraft = this.getMinecraftPackage();
			}

			return Class.forName(this.minecraft.getName() + "." + classname);
		} catch (Exception ex) {
			this.getServer().getConsoleSender().sendMessage(ChatWriter.pluginMessage(ChatColor.RED + Main
					._l("errors.classnotfound", ImmutableMap.of("package", "minecraft server", "class", classname))));
			return null;
		}
	}

	public GameLobbyCountdownRule getLobbyCountdownRule() {
		int id = 0;
		if (this.getConfig().contains("lobbycountdown-rule")) {
			if (this.getConfig().isInt("lobbycountdown-rule")) {
				id = this.getConfig().getInt("lobbycountdown-rule");
			}
		}

		return GameLobbyCountdownRule.getById(id);
	}

	public boolean metricsEnabled() {
		if (this.getConfig().contains("plugin-metrics")) {
			if (this.getConfig().isBoolean("plugin-metrics")) {
				return this.getConfig().getBoolean("plugin-metrics");
			}
		}

		return false;
	}

	public void startMetricsIfEnabled() {
		if (this.metricsEnabled()) {
			try {
				Metrics metrics = new Metrics(this);
				metrics.start();
			} catch (Exception ex) {
				this.getServer().getConsoleSender().sendMessage(
						ChatWriter.pluginMessage(ChatColor.RED + "Metrics are enabled, but couldn't send data!"));
			}
		}
	}

	public String getFallbackLocale() {
		return "en";
	}

	public boolean allPlayersBackToMainLobby() {
		if (this.getConfig().contains("endgame.all-players-to-mainlobby")) {
			if (this.getConfig().isBoolean("endgame.all-players-to-mainlobby")) {
				return this.getConfig().getBoolean("endgame.all-players-to-mainlobby");
			}
		}

		return false;
	}

	public List<String> getAllowedCommands() {
		FileConfiguration config = this.getConfig();
		if (config.contains("allowed-commands")) {
			if (config.isList("allowed-commands")) {
				return config.getStringList("allowed-commands");
			}
		}

		return new ArrayList<String>();
	}

	public static Main getInstance() {
		return Main.instance;
	}

	public ScoreboardManager getScoreboardManager() {
		return this.scoreboardManager;
	}

	private void registerListener() {
		new WeatherListener();
		new BlockListener();
		new PlayerListener();
		if (Main.getInstance().getCurrentVersion().startsWith("v1_9")) {
			new Player19Listener();
		}
		new HangingListener();
		new EntityListener();
		new ServerListener();
		new SignListener();
		new ChunkListener();

		SpecialItem.loadSpecials();
	}

	private void registerConfigurationClasses() {
		ConfigurationSerialization.registerClass(RessourceSpawner.class, "RessourceSpawner");
		ConfigurationSerialization.registerClass(Team.class, "Team");
	}

	private void registerCommands() {
		this.commands.add(new HelpCommand(this));
		this.commands.add(new SetSpawnerCommand(this));
		this.commands.add(new AddGameCommand(this));
		this.commands.add(new StartGameCommand(this));
		this.commands.add(new StopGameCommand(this));
		this.commands.add(new SetRegionCommand(this));
		this.commands.add(new AddTeamCommand(this));
		this.commands.add(new SaveGameCommand(this));
		this.commands.add(new JoinGameCommand(this));
		this.commands.add(new SetSpawnCommand(this));
		this.commands.add(new SetLobbyCommand(this));
		this.commands.add(new LeaveGameCommand(this));
		this.commands.add(new SetTargetCommand(this));
		this.commands.add(new SetBedCommand(this));
		this.commands.add(new ReloadCommand(this));
		this.commands.add(new SetMainLobbyCommand(this));
		this.commands.add(new ListGamesCommand(this));
		this.commands.add(new RegionNameCommand(this));
		this.commands.add(new RemoveTeamCommand(this));
		this.commands.add(new RemoveGameCommand(this));
		this.commands.add(new ClearSpawnerCommand(this));
		this.commands.add(new GameTimeCommand(this));
		this.commands.add(new StatsCommand(this));
		this.commands.add(new SetMinPlayersCommand(this));
		this.commands.add(new SetGameBlockCommand(this));
		this.commands.add(new SetBuilderCommand(this));
		this.commands.add(new SetAutobalanceCommand(this));
		this.commands.add(new KickCommand(this));
		this.commands.add(new AddTeamJoinCommand(this));
		this.commands.add(new AddHoloCommand(this));
		this.commands.add(new RemoveHoloCommand(this));

		this.getCommand(this.getStringConfig("command-prefix", "bw")).setExecutor(new BedwarsCommandExecutor(this));
	}

	public ArrayList<BaseCommand> getCommands() {
		return this.commands;
	}

	private ArrayList<BaseCommand> filterCommandsByPermission(ArrayList<BaseCommand> commands, String permission) {
		Iterator<BaseCommand> it = commands.iterator();

		while (it.hasNext()) {
			BaseCommand command = it.next();
			if (!command.getPermission().equals(permission)) {
				it.remove();
			}
		}

		return commands;
	}

	@SuppressWarnings("unchecked")
	public ArrayList<BaseCommand> getBaseCommands() {
		ArrayList<BaseCommand> commands = (ArrayList<BaseCommand>) this.commands.clone();
		commands = this.filterCommandsByPermission(commands, "base");

		return commands;
	}

	@SuppressWarnings("unchecked")
	public ArrayList<BaseCommand> getSetupCommands() {
		ArrayList<BaseCommand> commands = (ArrayList<BaseCommand>) this.commands.clone();
		commands = this.filterCommandsByPermission(commands, "setup");

		return commands;
	}

	@SuppressWarnings("unchecked")
	public ArrayList<BaseCommand> getCommandsByPermission(String permission) {
		ArrayList<BaseCommand> commands = (ArrayList<BaseCommand>) this.commands.clone();
		commands = this.filterCommandsByPermission(commands, permission);

		return commands;
	}

	public GameManager getGameManager() {
		return this.gameManager;
	}

	private void startTimeListener() {
		this.timeTask = this.getServer().getScheduler().runTaskTimer(this, new Runnable() {

			@Override
			public void run() {
				for (Game g : Main.getInstance().getGameManager().getGames()) {
					if (g.getState() == GameState.RUNNING) {
						g.getRegion().getWorld().setTime(g.getTime());
					}
				}
			}
		}, (long) 5 * 20, (long) 5 * 20);
	}

	public static String _l(String localeKey, String singularValue, Map<String, String> params) {
		if (params.get(singularValue).equals("1")) {
			return (String) Main.getInstance().getLocalization().get(localeKey + "-one", params);
		}
		return (String) Main.getInstance().getLocalization().get(localeKey, params);
	}

	public static String _l(String localeKey, Map<String, String> params) {
		return (String) Main.getInstance().getLocalization().get(localeKey, params);
	}

	public static String _l(String localeKey) {
		return (String) Main.getInstance().getLocalization().get(localeKey);
	}

	private void stopTimeListener() {
		try {
			this.timeTask.cancel();
		} catch (Exception ex) {
			// Timer isn't running. Just ignore.
		}

		try {
			this.updateChecker.cancel();
		} catch (Exception ex) {
			// Timer isn't running. Just ignore.
		}
	}

	public void reloadLocalization() {
		this.localization.saveLocales(false);
		this.localization.loadLocale(this.getConfig().getString("locale"), false);
	}

	public boolean spectationEnabled() {
		if (this.getConfig().contains("spectation-enabled")) {
			if (this.getConfig().isBoolean("spectation-enabled")) {
				return this.getConfig().getBoolean("spectation-enabled");
			}
		}

		return true;
	}

	public boolean toMainLobby() {
		if (this.getConfig().contains("endgame.mainlobby-enabled")) {
			return this.getConfig().getBoolean("endgame.mainlobby-enabled");
		}

		return false;
	}

	/**
	 * Returns the max length of a game in seconds
	 * 
	 * @return The length of the game in seconds
	 */
	public int getMaxLength() {
		if (this.getConfig().contains("gamelength")) {
			if (this.getConfig().isInt("gamelength")) {
				return this.getConfig().getInt("gamelength") * 60;
			}
		}

		// fallback time is 60 minutes
		return 60 * 60;
	}

	public Integer getRespawnProtectionTime() {
		FileConfiguration config = this.getConfig();
		if (config.contains("respawn-protection")) {
			if (config.isInt("respawn-protection")) {
				return config.getInt("respawn-protection");
			}
		}

		return 0;
	}

	public boolean isLocationSerializable() {
		if (Main.locationSerializable == null) {
			try {
				Location.class.getMethod("serialize");
				Main.locationSerializable = true;
			} catch (Exception ex) {
				Main.locationSerializable = false;
			}
		}

		return Main.locationSerializable;
	}

	public FileConfiguration getShopConfig() {
		return this.shopConfig;
	}

	public boolean isHologramsEnabled() {
		return this.getServer().getPluginManager().isPluginEnabled("HolographicDisplays");
	}

	public HolographicDisplaysInteraction getHolographicInteractor() {
		return this.holographicInteraction;
	}

}
