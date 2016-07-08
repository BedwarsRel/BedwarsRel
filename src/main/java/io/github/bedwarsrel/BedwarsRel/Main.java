package io.github.bedwarsrel.BedwarsRel;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandMap;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.plugin.SimplePluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.scoreboard.ScoreboardManager;

import com.bugsnag.BeforeNotify;
import com.bugsnag.Client;
import com.google.common.collect.ImmutableMap;

import io.github.bedwarsrel.BedwarsRel.Commands.AddGameCommand;
import io.github.bedwarsrel.BedwarsRel.Commands.AddHoloCommand;
import io.github.bedwarsrel.BedwarsRel.Commands.AddTeamCommand;
import io.github.bedwarsrel.BedwarsRel.Commands.AddTeamJoinCommand;
import io.github.bedwarsrel.BedwarsRel.Commands.BaseCommand;
import io.github.bedwarsrel.BedwarsRel.Commands.ClearSpawnerCommand;
import io.github.bedwarsrel.BedwarsRel.Commands.GameTimeCommand;
import io.github.bedwarsrel.BedwarsRel.Commands.HelpCommand;
import io.github.bedwarsrel.BedwarsRel.Commands.JoinGameCommand;
import io.github.bedwarsrel.BedwarsRel.Commands.KickCommand;
import io.github.bedwarsrel.BedwarsRel.Commands.LeaveGameCommand;
import io.github.bedwarsrel.BedwarsRel.Commands.ListGamesCommand;
import io.github.bedwarsrel.BedwarsRel.Commands.RegionNameCommand;
import io.github.bedwarsrel.BedwarsRel.Commands.ReloadCommand;
import io.github.bedwarsrel.BedwarsRel.Commands.RemoveGameCommand;
import io.github.bedwarsrel.BedwarsRel.Commands.RemoveHoloCommand;
import io.github.bedwarsrel.BedwarsRel.Commands.RemoveTeamCommand;
import io.github.bedwarsrel.BedwarsRel.Commands.SaveGameCommand;
import io.github.bedwarsrel.BedwarsRel.Commands.SetAutobalanceCommand;
import io.github.bedwarsrel.BedwarsRel.Commands.SetBedCommand;
import io.github.bedwarsrel.BedwarsRel.Commands.SetBuilderCommand;
import io.github.bedwarsrel.BedwarsRel.Commands.SetGameBlockCommand;
import io.github.bedwarsrel.BedwarsRel.Commands.SetLobbyCommand;
import io.github.bedwarsrel.BedwarsRel.Commands.SetMainLobbyCommand;
import io.github.bedwarsrel.BedwarsRel.Commands.SetMinPlayersCommand;
import io.github.bedwarsrel.BedwarsRel.Commands.SetRegionCommand;
import io.github.bedwarsrel.BedwarsRel.Commands.SetSpawnCommand;
import io.github.bedwarsrel.BedwarsRel.Commands.SetSpawnerCommand;
import io.github.bedwarsrel.BedwarsRel.Commands.SetTargetCommand;
import io.github.bedwarsrel.BedwarsRel.Commands.StartGameCommand;
import io.github.bedwarsrel.BedwarsRel.Commands.StatsCommand;
import io.github.bedwarsrel.BedwarsRel.Commands.StopGameCommand;
import io.github.bedwarsrel.BedwarsRel.Database.DatabaseManager;
import io.github.bedwarsrel.BedwarsRel.Game.Game;
import io.github.bedwarsrel.BedwarsRel.Game.GameManager;
import io.github.bedwarsrel.BedwarsRel.Game.GameState;
import io.github.bedwarsrel.BedwarsRel.Game.RessourceSpawner;
import io.github.bedwarsrel.BedwarsRel.Game.Team;
import io.github.bedwarsrel.BedwarsRel.Listener.BlockListener;
import io.github.bedwarsrel.BedwarsRel.Listener.ChunkListener;
import io.github.bedwarsrel.BedwarsRel.Listener.EntityListener;
import io.github.bedwarsrel.BedwarsRel.Listener.HangingListener;
import io.github.bedwarsrel.BedwarsRel.Listener.Player19Listener;
import io.github.bedwarsrel.BedwarsRel.Listener.PlayerListener;
import io.github.bedwarsrel.BedwarsRel.Listener.PlayerSpigotListener;
import io.github.bedwarsrel.BedwarsRel.Listener.ServerListener;
import io.github.bedwarsrel.BedwarsRel.Listener.SignListener;
import io.github.bedwarsrel.BedwarsRel.Listener.WeatherListener;
import io.github.bedwarsrel.BedwarsRel.Localization.LocalizationConfig;
import io.github.bedwarsrel.BedwarsRel.Shop.Specials.SpecialItem;
import io.github.bedwarsrel.BedwarsRel.Statistics.PlayerStatisticManager;
import io.github.bedwarsrel.BedwarsRel.Statistics.StorageType;
import io.github.bedwarsrel.BedwarsRel.Updater.ConfigUpdater;
import io.github.bedwarsrel.BedwarsRel.Updater.DatabaseUpdater;
import io.github.bedwarsrel.BedwarsRel.Updater.PluginUpdater;
import io.github.bedwarsrel.BedwarsRel.Updater.PluginUpdater.UpdateCallback;
import io.github.bedwarsrel.BedwarsRel.Updater.PluginUpdater.UpdateResult;
import lombok.Getter;

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

  private IHologramInteraction holographicInteraction = null;

  private boolean isSpigot = false;

  private static Boolean locationSerializable = null;

  private PlayerStatisticManager playerStatisticManager = null;

  private ScoreboardManager scoreboardManager = null;
  private GameManager gameManager = null;
  @Getter
  private Client bugsnag;

  @Override
  public void onEnable() {
    Main.instance = this;

    this.registerBugsnag();

    // register classes
    this.registerConfigurationClasses();

    // save default config
    this.saveDefaultConfig();
    this.loadConfigInUTF();

    this.getConfig().options().copyDefaults(true);
    this.getConfig().options().copyHeader(true);

    this.craftbukkit = this.getCraftBukkit();
    this.minecraft = this.getMinecraftPackage();
    this.version = this.loadVersion();

    ConfigUpdater configUpdater = new ConfigUpdater();
    configUpdater.addConfigs();
    this.saveConfiguration();
    this.loadConfigInUTF();

    if (this.getBooleanConfig("send-error-data", true)) {
      this.enableBugsnag();
    } else {
      this.disableBugsnag();
    }

    configUpdater.updateShop();
    this.loadShop();

    this.isSpigot = this.getIsSpigot();
    this.loadDatabase();

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
      if (this.getServer().getPluginManager().isPluginEnabled("HologramAPI")) {
        this.holographicInteraction = new HologramAPIInteraction();
      } else if (this.getServer().getPluginManager().isPluginEnabled("HolographicDisplays")) {
        this.holographicInteraction = new HolographicDisplaysInteraction();
      }
      this.holographicInteraction.loadHolograms();
    }
  }

  private void registerBugsnag() {
    this.bugsnag = new Client("c23593c1e2f40fc0da36564af1bd00c6");
    this.bugsnag.setAppVersion(this.getDescription().getVersion());
  }

  private void enableBugsnag() {
    this.bugsnag.addBeforeNotify(new BeforeNotify() {
      @Override
      public boolean run(com.bugsnag.Error error) {
        SupportData supportData = new SupportData();
        error.addToTab("user", "id", supportData.getIdentifier());
        error.addToTab("Server", "Version", supportData.getServerVersion());
        error.addToTab("Server", "Version Bukkit", supportData.getBukkitVersion());
        error.addToTab("Server", "Server Mode", supportData.getServerMode());
        error.addToTab("Server", "Plugins", supportData.getPlugins());
        return true;
      }
    });
  }

  private void disableBugsnag() {
    this.bugsnag.addBeforeNotify(new BeforeNotify() {
      @Override
      public boolean run(com.bugsnag.Error error) {
        return false;
      }
    });
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
      BufferedReader reader =
          new BufferedReader(new InputStreamReader(new FileInputStream(configFile), "UTF-8"));
      this.getConfig().load(reader);
    } catch (Exception e) {
      Main.getInstance().getBugsnag().notify(e);
      e.printStackTrace();
    }

    if (this.getConfig() == null) {
      return;
    }

    // load breakable materials
    this.breakableTypes = new ArrayList<Material>();
    for (String material : this.getConfig().getStringList("breakable-blocks.list")) {
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
        Main.getInstance().getBugsnag().notify(e);
        e.printStackTrace();
      }
    }

    this.shopConfig = new YamlConfiguration();

    try {
      BufferedReader reader =
          new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF-8"));
      this.shopConfig.load(reader);
    } catch (Exception e) {
      Main.getInstance().getBugsnag().notify(e);
      this.getServer().getConsoleSender().sendMessage(
          ChatWriter.pluginMessage(ChatColor.RED + "Couldn't load shop! Error in parsing shop!"));
      e.printStackTrace();
    }
  }

  public void dispatchRewardCommands(List<String> commands, Map<String, String> replacements) {
    for (String command : commands) {
      command = command.trim();
      if ("".equals(command)) {
        continue;
      }

      if ("none".equalsIgnoreCase(command)) {
        break;
      }

      if (command.startsWith("/")) {
        command = command.substring(1);
      }

      for (Entry<String, String> entry : replacements.entrySet()) {
        command = command.replace(entry.getKey(), entry.getValue());
      }

      Main.getInstance().getServer()
          .dispatchCommand(Main.getInstance().getServer().getConsoleSender(), command);
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
      Main.getInstance().getBugsnag().notify(ex);
      ex.printStackTrace();
    }
  }

  public Class<?> getVersionRelatedClass(String className) {
    try {
      Class<?> clazz = Class.forName(
          "io.github.bedwarsrel.BedwarsRel.Com." + this.getCurrentVersion() + "." + className);
      return clazz;
    } catch (Exception ex) {
      Main.getInstance().getBugsnag().notify(ex);
      this.getServer().getConsoleSender()
          .sendMessage(ChatWriter.pluginMessage(ChatColor.RED
              + "Couldn't find version related class io.github.bedwarsrel.BedwarsRel.Com."
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
      Main.getInstance().getBugsnag().notify(ex);
      ex.printStackTrace();
    }

    return null;
  }

  public boolean isBreakableType(Material type) {
    return ((Main.getInstance().getConfig().getBoolean("breakable-blocks.use-as-blacklist")
        && !this.breakableTypes.contains(type))
        || (!Main.getInstance().getConfig().getBoolean("breakable-blocks.use-as-blacklist")
            && this.breakableTypes.contains(type)));
  }

  public boolean isMineshafterPresent() {
    try {
      Class.forName("mineshafter.MineServer");
      return true;
    } catch (Exception e) {
      // NO ERROR
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
      Main.getInstance().getBugsnag().notify(ex);
      this.getServer().getConsoleSender().sendMessage(
          ChatWriter.pluginMessage(ChatColor.RED + "Check for updates not successful: Error!"));
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
        || !"database".equals(this.getStringConfig("statistics.storage", "yaml"))) {
      return;
    }

    this.getServer().getConsoleSender()
        .sendMessage(ChatWriter.pluginMessage(ChatColor.GREEN + "Initialize database ..."));

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

    this.getServer().getConsoleSender()
        .sendMessage(ChatWriter.pluginMessage(ChatColor.GREEN + "Done."));
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

  public DatabaseManager getDatabaseManager() {
    return this.dbManager;
  }

  public boolean isSpigot() {
    return this.isSpigot;
  }

  private boolean getIsSpigot() {
    try {
      Package spigotPackage = Package.getPackage("org.spigotmc");
      return (spigotPackage != null);
    } catch (Exception e) {
      Main.getInstance().getBugsnag().notify(e);
    }

    return false;
  }

  public int getIntConfig(String key, int defaultInt) {
    FileConfiguration config = this.getConfig();
    if (config.contains(key) && config.isInt(key)) {
      return config.getInt(key);
    }
    return defaultInt;
  }

  public String getStringConfig(String key, String defaultString) {
    FileConfiguration config = this.getConfig();
    if (config.contains(key) && config.isString(key)) {
      return config.getString(key);
    }
    return defaultString;
  }

  public boolean getBooleanConfig(String key, boolean defaultBool) {
    FileConfiguration config = this.getConfig();
    if (config.contains(key) && config.isBoolean(key)) {
      return config.getBoolean(key);
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
      Main.getInstance().getBugsnag().notify(ex);
      this.getServer().getConsoleSender().sendMessage(ChatWriter.pluginMessage(ChatColor.RED
          + Main._l("errors.packagenotfound", ImmutableMap.of("package", "craftbukkit"))));
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
      Main.getInstance().getBugsnag().notify(ex);
      this.getServer().getConsoleSender().sendMessage(ChatWriter.pluginMessage(ChatColor.RED
          + Main._l("errors.packagenotfound", ImmutableMap.of("package", "minecraft server"))));
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
      Main.getInstance().getBugsnag().notify(ex);
      this.getServer().getConsoleSender()
          .sendMessage(ChatWriter.pluginMessage(ChatColor.RED + Main._l("errors.classnotfound",
              ImmutableMap.of("package", "craftbukkit", "class", classname))));
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
      Main.getInstance().getBugsnag().notify(ex);
      this.getServer().getConsoleSender()
          .sendMessage(ChatWriter.pluginMessage(ChatColor.RED + Main._l("errors.classnotfound",
              ImmutableMap.of("package", "minecraft server", "class", classname))));
      return null;
    }
  }

  public boolean metricsEnabled() {
    if (this.getConfig().contains("plugin-metrics")
        && this.getConfig().isBoolean("plugin-metrics")) {
      return this.getConfig().getBoolean("plugin-metrics");
    }

    return false;
  }

  public void startMetricsIfEnabled() {
    if (this.metricsEnabled()) {
      try {
        Metrics metrics = new Metrics(this);
        metrics.start();
      } catch (Exception ex) {
        Main.getInstance().getBugsnag().notify(ex);
        this.getServer().getConsoleSender().sendMessage(ChatWriter
            .pluginMessage(ChatColor.RED + "Metrics are enabled, but couldn't send data!"));
      }
    }
  }

  public String getFallbackLocale() {
    return "en_US";
  }

  public boolean allPlayersBackToMainLobby() {
    if (this.getConfig().contains("endgame.all-players-to-mainlobby")
        && this.getConfig().isBoolean("endgame.all-players-to-mainlobby")) {
      return this.getConfig().getBoolean("endgame.all-players-to-mainlobby");
    }

    return false;

  }

  public List<String> getAllowedCommands() {
    FileConfiguration config = this.getConfig();
    if (config.contains("allowed-commands") && config.isList("allowed-commands")) {
      return config.getStringList("allowed-commands");
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
    if (Main.getInstance().getCurrentVersion().startsWith("v1_9")
        || Main.getInstance().getCurrentVersion().startsWith("v1_10")) {
      new Player19Listener();
    }
    new HangingListener();
    new EntityListener();
    new ServerListener();
    new SignListener();
    new ChunkListener();

    if (this.isSpigot()) {
      new PlayerSpigotListener();
    }

    SpecialItem.loadSpecials();
  }

  private void registerConfigurationClasses() {
    ConfigurationSerialization.registerClass(RessourceSpawner.class, "RessourceSpawner");
    ConfigurationSerialization.registerClass(Team.class, "Team");
  }

  private void registerCommands() {
    final BedwarsCommandExecutor executor = new BedwarsCommandExecutor(this);
    
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
    
    Command command = new Command(this.getStringConfig("command-prefix", "bw")) {
      
      @Override
      public boolean execute(CommandSender sender, String cmd, String[] args) {
        return executor.onCommand(sender, this, cmd, args);
      }
    };

    this.registerCommand(command);
  }

  private void registerCommand(Command command) {
    try {
      Field cMap = SimplePluginManager.class.getDeclaredField("commandMap");
      cMap.setAccessible(true);
      CommandMap map = (CommandMap) cMap.get(this.getServer().getPluginManager());
      map.register(command.getName(), command);

    } catch (NoSuchFieldException e) { // Should never be called
      e.printStackTrace();
    } catch (IllegalAccessException e) { // Set accessable to true, so unless something crazy
                                         // happened, should never be called
      e.printStackTrace();
    }
  }


  public ArrayList<BaseCommand> getCommands() {
    return this.commands;
  }

  private ArrayList<BaseCommand> filterCommandsByPermission(ArrayList<BaseCommand> commands,
      String permission) {
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
    if ("1".equals(params.get(singularValue))) {
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
    if (this.getConfig().contains("spectation-enabled")
        && this.getConfig().isBoolean("spectation-enabled")) {
      return this.getConfig().getBoolean("spectation-enabled");
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
    if (this.getConfig().contains("gamelength") && this.getConfig().isInt("gamelength")) {
      return this.getConfig().getInt("gamelength") * 60;
    }

    // fallback time is 60 minutes
    return 60 * 60;
  }

  public Integer getRespawnProtectionTime() {
    FileConfiguration config = this.getConfig();
    if (config.contains("respawn-protection") && config.isInt("respawn-protection")) {
      return config.getInt("respawn-protection");
    }
    return 0;
  }

  public boolean isLocationSerializable() {
    if (Main.locationSerializable == null) {
      try {
        Location.class.getMethod("serialize");
        Main.locationSerializable = true;
      } catch (Exception ex) {
        Main.getInstance().getBugsnag().notify(ex);
        Main.locationSerializable = false;
      }
    }

    return Main.locationSerializable;
  }

  public FileConfiguration getShopConfig() {
    return this.shopConfig;
  }

  public boolean isHologramsEnabled() {
    if (this.getServer().getPluginManager().isPluginEnabled("HologramAPI")
        || this.getServer().getPluginManager().isPluginEnabled("HolographicDisplays")) {
      return true;
    }
    return false;
  }

  public IHologramInteraction getHolographicInteractor() {
    return this.holographicInteraction;
  }

}
