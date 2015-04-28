package io.github.yannici.bedwars;

import io.github.yannici.bedwars.Commands.*;
import io.github.yannici.bedwars.Database.DatabaseManager;
import io.github.yannici.bedwars.Game.Game;
import io.github.yannici.bedwars.Game.GameLobbyCountdownRule;
import io.github.yannici.bedwars.Game.GameManager;
import io.github.yannici.bedwars.Game.GameState;
import io.github.yannici.bedwars.Game.RessourceSpawner;
import io.github.yannici.bedwars.Game.Team;
import io.github.yannici.bedwars.Listener.BlockListener;
import io.github.yannici.bedwars.Listener.EntityListener;
import io.github.yannici.bedwars.Listener.PlayerListener;
import io.github.yannici.bedwars.Listener.ServerListener;
import io.github.yannici.bedwars.Listener.SignListener;
import io.github.yannici.bedwars.Listener.WeatherListener;
import io.github.yannici.bedwars.Localization.LocalizationConfig;
import io.github.yannici.bedwars.Statistics.StorageType;
import io.github.yannici.bedwars.Statistics.PlayerStatisticManager;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.scoreboard.ScoreboardManager;

import com.google.common.collect.ImmutableMap;

public class Main extends JavaPlugin {

	private static Main instance = null;

	private ArrayList<BaseCommand> commands = new ArrayList<BaseCommand>();
	private BukkitTask timeTask = null;
	private Package craftbukkit = null;
	private Package minecraft = null;
	private String version = null;
	private LocalizationConfig localization = null;
	private DatabaseManager dbManager = null;
	
	private PlayerStatisticManager playerStatisticManager = null;
	
	private ScoreboardManager scoreboardManager = null;
	private GameManager gameManager = null;

	public Main() {
		this.registerConfigurationClasses();
	}

    @Override
	public void onEnable() {
		Main.instance = this;
		
		this.loadDatabase();
		
		this.craftbukkit = this.getCraftBukkit();
		this.minecraft = this.getMinecraftPackage();
		this.version = this.loadVersion();

		this.registerCommands();
		this.registerListener();

		this.gameManager = new GameManager(this);
		this.saveDefaultConfig();
		this.loadStatistics();
		this.localization = this.loadLocalization();

		// Loading
		this.scoreboardManager = Bukkit.getScoreboardManager();
		this.gameManager.loadGames();
		this.startTimeListener();
		this.startMetricsIfEnabled();
	}
	
	@Override
	public void onDisable() {
		this.stopTimeListener();
		this.gameManager.unloadGames();
		this.cleanDatabase();
	}
	
	public PlayerStatisticManager getPlayerStatisticManager() {
	    return this.playerStatisticManager;
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
		if(!this.getBooleanConfig("statistics.enabled", false)
				|| !this.getStringConfig("statistics.storage","yaml").equals("database")) {
			return;
		}
		
		this.getServer().getConsoleSender().sendMessage(ChatWriter.pluginMessage(ChatColor.GREEN + "Initialize database ..."));
		
		this.loadingRequiredLibs();
		
		String host = this.getStringConfig("database.host", null);
		int port = this.getIntConfig("database.port", 3306);
		String user = this.getStringConfig("database.user", null);
		String password = this.getStringConfig("database.password", null);
		String db = this.getStringConfig("database.db", null);
		
		if(host == null || user == null || password == null || db == null) {
			return;
		}
		
		this.dbManager = new DatabaseManager(host, port, user, password, db);
		this.dbManager.initialize();
		
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
		if(this.dbManager != null) {
			this.dbManager.cleanUp();
		}
	}
	
	private void loadingRequiredLibs() {
		try {
            final File[] libs = new File[] {
                    new File(this.getDataFolder() + "/lib/", "c3p0-0.9.5.jar"),
                    new File(this.getDataFolder() + "/lib/", "mchange-commons-java-0.2.9.jar")};
            for (final File lib : libs) {
                if (!lib.exists()) {
                    JarUtils.extractFromJar(lib.getName(),
                            lib.getAbsolutePath());
                }
            }
            for (final File lib : libs) {
                if (!lib.exists()) {
                    this.getLogger().warning(
                            "There was a critical error loading bedwars plugin! Could not find lib: "
                                    + lib.getName());
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
        final URLClassLoader sysloader = (URLClassLoader) ClassLoader
                .getSystemClassLoader();
        final Class<URLClassLoader> sysclass = URLClassLoader.class;
        try {
            final Method method = sysclass.getDeclaredMethod("addURL",
                    new Class[] { URL.class });
            method.setAccessible(true);
            method.invoke(sysloader, new Object[] { url });
        } catch (final Throwable t) {
            t.printStackTrace();
            throw new IOException("Error adding " + url
                    + " to system classloader");
        }
    }
	
	public DatabaseManager getDatabaseManager() {
		return this.dbManager;
	}

	public boolean isSpigot() {
		try {
			Package spigotPackage = Package.getPackage("org.spigotmc");
			if (spigotPackage == null) {
				return false;
			}

			return true;
		} catch (Exception e) {
			return false;
		}

	}
	
	public int getIntConfig(String key, int defaultInt) {
		FileConfiguration config = this.getConfig();
		if(config.contains(key)) {
			if(config.isInt(key)) {
				return config.getInt(key);
			}
		}
		
		return defaultInt;
	}
	
	public String getStringConfig(String key, String defaultString) {
		FileConfiguration config = this.getConfig();
		if(config.contains(key)) {
			if(config.isString(key)) {
				return config.getString(key);
			}
		}
		
		return defaultString;
	}
	
	public boolean getBooleanConfig(String key, boolean defaultBool) {
		FileConfiguration config = this.getConfig();
		if(config.contains(key)) {
			if(config.isBoolean(key)) {
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
						+ Bukkit.getServer().getClass().getPackage().getName()
								.replace(".", ",").split(",")[3]);
			} else {
				return this.craftbukkit;
			}
		} catch (Exception ex) {
			this.getServer()
					.getConsoleSender()
					.sendMessage(
							ChatWriter.pluginMessage(ChatColor.RED
									+ Main._l("errors.packagenotfound",
											ImmutableMap.of("package",
													"craftbukkit"))));
			return null;
		}
	}

	public Package getMinecraftPackage() {
		try {
			if (this.minecraft == null) {
				return Package.getPackage("net.minecraft.server."
						+ Bukkit.getServer().getClass().getPackage().getName()
								.replace(".", ",").split(",")[3]);
			} else {
				return this.minecraft;
			}
		} catch (Exception ex) {
			this.getServer()
					.getConsoleSender()
					.sendMessage(
							ChatWriter.pluginMessage(ChatColor.RED
									+ Main._l("errors.packagenotfound",
											ImmutableMap.of("package",
													"minecraft server"))));
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
			this.getServer()
					.getConsoleSender()
					.sendMessage(
							ChatWriter.pluginMessage(ChatColor.RED
									+ Main._l("errors.classnotfound",
											ImmutableMap.of("package",
													"craftbukkit", "class",
													classname))));
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
			this.getServer()
					.getConsoleSender()
					.sendMessage(
							ChatWriter.pluginMessage(ChatColor.RED
									+ Main._l("errors.classnotfound",
											ImmutableMap.of("package",
													"minecraft server",
													"class", classname))));
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
		if(this.getConfig().contains("plugin-metrics")) {
			if(this.getConfig().isBoolean("plugin-metrics")) {
				return this.getConfig().getBoolean("plugin-metrics");
			}
		}
		
		return false;
	}
	
	public void startMetricsIfEnabled() {
		if(this.metricsEnabled()) {
			try {
		        Metrics metrics = new Metrics(this);
		        metrics.start();
			} catch(Exception ex) {
				this.getServer().getConsoleSender().sendMessage(ChatWriter.pluginMessage(ChatColor.RED + "Metrics are enabled, but couldn't send data!"));
			}
		}
	}

	public String getFallbackLocale() {
		return "en";
	}
	
	public boolean allPlayersBackToMainLobby() {
        if(this.getConfig().contains("endgame.all-players-to-mainlobby")) {
            if(this.getConfig().isBoolean("endgame.all-players-to-mainlobby")) {
                return this.getConfig().getBoolean("endgame.all-players-to-mainlobby");
            }
        }
        
        return false;
    }
	
	public List<String> getAllowedCommands() {
		FileConfiguration config = this.getConfig();
		if(config.contains("allowed-commands")) {
			if(config.isList("allowed-commands")) {
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
		new EntityListener();
		new ServerListener();
		new SignListener();
	}

	private void registerConfigurationClasses() {
		ConfigurationSerialization.registerClass(RessourceSpawner.class,
				"RessourceSpawner");
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

		this.getCommand("bw").setExecutor(new BedwarsCommandExecutor(this));
	}

	public ArrayList<BaseCommand> getCommands() {
		return this.commands;
	}

	private ArrayList<BaseCommand> filterCommandsByPermission(
			ArrayList<BaseCommand> commands, String permission) {
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
		ArrayList<BaseCommand> commands = (ArrayList<BaseCommand>) this.commands
				.clone();
		commands = this.filterCommandsByPermission(commands, "base");

		return commands;
	}

	@SuppressWarnings("unchecked")
	public ArrayList<BaseCommand> getSetupCommands() {
		ArrayList<BaseCommand> commands = (ArrayList<BaseCommand>) this.commands
				.clone();
		commands = this.filterCommandsByPermission(commands, "setup");

		return commands;
	}

	public GameManager getGameManager() {
		return this.gameManager;
	}

	private void startTimeListener() {
		this.timeTask = this.getServer().getScheduler()
				.runTaskTimer(this, new Runnable() {

					@Override
					public void run() {
						for (Game g : Main.getInstance().getGameManager()
								.getGames()) {
							if (g.getState() == GameState.RUNNING) {
								g.getRegion().getWorld().setTime(g.getTime());
							}
						}
					}
				}, (long) 5 * 20, (long) 5 * 20);
	}

	public static String _l(String localeKey, Map<String, String> params) {
		return (String) Main.getInstance().getLocalization()
				.get(localeKey, params);
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
	}

	public void reloadLocalization() {
		this.localization.saveLocales(false);
		this.localization.loadLocale(this.getConfig().getString("locale"),
				false);
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
				return this.getConfig().getInt("gamelength") * 60 * 60;
			}
		}

		// fallback time is 60 minutes
		return 60 * 60 * 60;
	}

	public Integer getRespawnProtectionTime() {
		FileConfiguration config = this.getConfig();
		if(config.contains("respawn-protection")) {
			if(config.isInt("respawn-protection")) {
				return config.getInt("respawn-protection");
			}
		}
		
		return 0;
	}

}
