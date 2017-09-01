package io.github.bedwarsrel;

import com.bugsnag.Bugsnag;
import com.bugsnag.Report;
import com.bugsnag.callbacks.Callback;
import com.google.common.collect.ImmutableMap;
import io.github.bedwarsrel.commands.AddGameCommand;
import io.github.bedwarsrel.commands.AddHoloCommand;
import io.github.bedwarsrel.commands.AddTeamCommand;
import io.github.bedwarsrel.commands.AddTeamJoinCommand;
import io.github.bedwarsrel.commands.BaseCommand;
import io.github.bedwarsrel.commands.ClearSpawnerCommand;
import io.github.bedwarsrel.commands.DebugPasteCommand;
import io.github.bedwarsrel.commands.GameTimeCommand;
import io.github.bedwarsrel.commands.HelpCommand;
import io.github.bedwarsrel.commands.ItemsPasteCommand;
import io.github.bedwarsrel.commands.JoinGameCommand;
import io.github.bedwarsrel.commands.KickCommand;
import io.github.bedwarsrel.commands.LeaveGameCommand;
import io.github.bedwarsrel.commands.ListGamesCommand;
import io.github.bedwarsrel.commands.RegionNameCommand;
import io.github.bedwarsrel.commands.ReloadCommand;
import io.github.bedwarsrel.commands.RemoveGameCommand;
import io.github.bedwarsrel.commands.RemoveHoloCommand;
import io.github.bedwarsrel.commands.RemoveTeamCommand;
import io.github.bedwarsrel.commands.SaveGameCommand;
import io.github.bedwarsrel.commands.SetAutobalanceCommand;
import io.github.bedwarsrel.commands.SetBedCommand;
import io.github.bedwarsrel.commands.SetBuilderCommand;
import io.github.bedwarsrel.commands.SetGameBlockCommand;
import io.github.bedwarsrel.commands.SetLobbyCommand;
import io.github.bedwarsrel.commands.SetMainLobbyCommand;
import io.github.bedwarsrel.commands.SetMinPlayersCommand;
import io.github.bedwarsrel.commands.SetRegionCommand;
import io.github.bedwarsrel.commands.SetSpawnCommand;
import io.github.bedwarsrel.commands.SetSpawnerCommand;
import io.github.bedwarsrel.commands.SetTargetCommand;
import io.github.bedwarsrel.commands.StartGameCommand;
import io.github.bedwarsrel.commands.StatsCommand;
import io.github.bedwarsrel.commands.StopGameCommand;
import io.github.bedwarsrel.database.DatabaseManager;
import io.github.bedwarsrel.database.MysqlDatabaseManager;
import io.github.bedwarsrel.database.YamlDatabaseManager;
import io.github.bedwarsrel.game.Game;
import io.github.bedwarsrel.game.GameManager;
import io.github.bedwarsrel.game.GameState;
import io.github.bedwarsrel.game.ResourceSpawner;
import io.github.bedwarsrel.game.Team;
import io.github.bedwarsrel.listener.BlockListener;
import io.github.bedwarsrel.listener.ChunkListener;
import io.github.bedwarsrel.listener.EntityListener;
import io.github.bedwarsrel.listener.HangingListener;
import io.github.bedwarsrel.listener.PlayerListener;
import io.github.bedwarsrel.listener.PlayerSpigotListener;
import io.github.bedwarsrel.listener.ServerListener;
import io.github.bedwarsrel.listener.SignListener;
import io.github.bedwarsrel.listener.WeatherListener;
import io.github.bedwarsrel.listener.events.EntityPickupItemEventListener;
import io.github.bedwarsrel.listener.events.PlayerPickUpItemEventListener;
import io.github.bedwarsrel.listener.events.PlayerSwapHandItemsEventListener;
import io.github.bedwarsrel.localization.LocalizationConfig;
import io.github.bedwarsrel.shop.Specials.SpecialItem;
import io.github.bedwarsrel.statistics.PlayerStatistic;
import io.github.bedwarsrel.statistics.PlayerStatisticManager;
import io.github.bedwarsrel.statistics.StorageType;
import io.github.bedwarsrel.updater.ConfigUpdater;
import io.github.bedwarsrel.updater.PluginUpdater;
import io.github.bedwarsrel.updater.PluginUpdater.UpdateCallback;
import io.github.bedwarsrel.updater.PluginUpdater.UpdateResult;
import io.github.bedwarsrel.utils.BStatsMetrics;
import io.github.bedwarsrel.utils.BedwarsCommandExecutor;
import io.github.bedwarsrel.utils.ChatWriter;
import io.github.bedwarsrel.utils.McStatsMetrics;
import io.github.bedwarsrel.utils.SupportData;
import io.github.bedwarsrel.utils.Utils;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.scoreboard.ScoreboardManager;

public class BedwarsRel extends JavaPlugin {

  public static int PROJECT_ID = 91743;
  private static BedwarsRel instance = null;
  private static Boolean locationSerializable = null;
  private List<Material> breakableTypes = null;
  @Getter
  private Bugsnag bugsnag;
  private ArrayList<BaseCommand> commands = new ArrayList<>();
  private Package craftbukkit = null;
  @Getter
  @Setter
  private DatabaseManager databaseManager = null;
  @Getter
  private GameManager gameManager = null;
  private IHologramInteraction holographicInteraction = null;
  private boolean isSpigot = false;
  @Getter
  private HashMap<String, LocalizationConfig> localization = new HashMap<>();
  private Package minecraft = null;
  @Getter
  private HashMap<UUID, String> playerLocales = new HashMap<>();
  private PlayerStatisticManager playerStatisticManager = null;
  private ScoreboardManager scoreboardManager = null;
  private YamlConfiguration shopConfig = null;
  private BukkitTask timeTask = null;
  private BukkitTask updateChecker = null;
  private String version = null;

  public static String _l(CommandSender commandSender, String key, String singularValue,
      Map<String, String> params) {
    return BedwarsRel
        ._l(BedwarsRel.getInstance().getSenderLocale(commandSender), key, singularValue, params);
  }

  public static String _l(String locale, String key, String singularValue,
      Map<String, String> params) {
    if ("1".equals(params.get(singularValue))) {
      return BedwarsRel._l(locale, key + "-one", params);
    }
    return BedwarsRel._l(locale, key, params);
  }

  public static String _l(CommandSender commandSender, String key, Map<String, String> params) {
    return BedwarsRel._l(BedwarsRel.getInstance().getSenderLocale(commandSender), key, params);
  }

  public static String _l(String locale, String key, Map<String, String> params) {
    if (!BedwarsRel.getInstance().localization.containsKey(locale)) {
      BedwarsRel.getInstance().loadLocalization(locale);
    }
    return (String) BedwarsRel.getInstance().getLocalization().get(locale).get(key, params);
  }

  public static String _l(CommandSender commandSender, String key) {
    return BedwarsRel._l(BedwarsRel.getInstance().getSenderLocale(commandSender), key);
  }

  public static String _l(String key) {
    return BedwarsRel._l(BedwarsRel.getInstance().getConfig().getString("locale"), key);
  }

  public static String _l(String locale, String key) {
    if (!BedwarsRel.getInstance().localization.containsKey(locale)) {
      BedwarsRel.getInstance().loadLocalization(locale);
    }
    return (String) BedwarsRel.getInstance().getLocalization().get(locale).get(key);
  }

  public static BedwarsRel getInstance() {
    return BedwarsRel.instance;
  }

  public boolean allPlayersBackToMainLobby() {
    if (this.getConfig().contains("endgame.all-players-to-mainlobby")
        && this.getConfig().isBoolean("endgame.all-players-to-mainlobby")) {
      return this.getConfig().getBoolean("endgame.all-players-to-mainlobby");
    }

    return false;

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

            new PluginUpdater(
                BedwarsRel.getInstance(), BedwarsRel.PROJECT_ID, BedwarsRel.getInstance().getFile(),
                PluginUpdater.UpdateType.DEFAULT, callback,
                BedwarsRel.getInstance().getBooleanConfig("update-infos", true));
          }

        }.runTaskTimerAsynchronously(BedwarsRel.getInstance(), 40L, 36000L);
      }
    } catch (Exception ex) {
      BedwarsRel.getInstance().getBugsnag().notify(ex);
      this.getServer().getConsoleSender().sendMessage(
          ChatWriter.pluginMessage(ChatColor.RED + "Check for updates not successful: Error!"));
    }
  }


  private void disableBugsnag() {
    this.bugsnag.addCallback(new Callback() {
      @Override
      public void beforeNotify(Report report) {
        report.cancel();
      }
    });
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

      BedwarsRel.getInstance().getServer()
          .dispatchCommand(BedwarsRel.getInstance().getServer().getConsoleSender(), command);
    }
  }

  private void enableBugsnag() {
    this.bugsnag.addCallback(new Callback() {
      @Override
      public void beforeNotify(Report report) {
        Boolean shouldBeSent = false;
        for (StackTraceElement stackTraceElement : report.getException().getStackTrace()) {
          if (stackTraceElement.toString().contains("io.github.bedwarsrel.BedwarsRel")) {
            shouldBeSent = true;
            break;
          }
        }
        if (!shouldBeSent) {
          report.cancel();
        }

        report.setUserId(SupportData.getIdentifier());
        if (!SupportData.getPluginVersionBuild().equalsIgnoreCase("unknown")) {
          report.addToTab("Server", "Version Build",
              BedwarsRel.getInstance().getDescription().getVersion() + " "
                  + SupportData.getPluginVersionBuild());
        }
        report.addToTab("Server", "Version", SupportData.getServerVersion());
        report.addToTab("Server", "Version Bukkit", SupportData.getBukkitVersion());
        report.addToTab("Server", "Server Mode", SupportData.getServerMode());
        report.addToTab("Server", "Plugins", SupportData.getPlugins());
      }
    });
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

  public List<String> getAllowedCommands() {
    FileConfiguration config = this.getConfig();
    if (config.contains("allowed-commands") && config.isList("allowed-commands")) {
      return config.getStringList("allowed-commands");
    }

    return new ArrayList<String>();
  }

  @SuppressWarnings("unchecked")
  public ArrayList<BaseCommand> getBaseCommands() {
    ArrayList<BaseCommand> commands = (ArrayList<BaseCommand>) this.commands.clone();
    commands = this.filterCommandsByPermission(commands, "base");

    return commands;
  }

  public boolean getBooleanConfig(String key, boolean defaultBool) {
    FileConfiguration config = this.getConfig();
    if (config.contains(key) && config.isBoolean(key)) {
      return config.getBoolean(key);
    }
    return defaultBool;
  }

  public String getBungeeHub() {
    if (this.getConfig().contains("bungeecord.hubserver")) {
      return this.getConfig().getString("bungeecord.hubserver");
    }

    return null;
  }

  public ArrayList<BaseCommand> getCommands() {
    return this.commands;
  }

  @SuppressWarnings("unchecked")
  public ArrayList<BaseCommand> getCommandsByPermission(String permission) {
    ArrayList<BaseCommand> commands = (ArrayList<BaseCommand>) this.commands.clone();
    commands = this.filterCommandsByPermission(commands, permission);

    return commands;
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
      BedwarsRel.getInstance().getBugsnag().notify(ex);
      this.getServer().getConsoleSender().sendMessage(ChatWriter.pluginMessage(ChatColor.RED
          + BedwarsRel._l(this.getServer().getConsoleSender(), "errors.packagenotfound",
          ImmutableMap.of("package", "craftbukkit"))));
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
      BedwarsRel.getInstance().getBugsnag().notify(ex);
      this.getServer().getConsoleSender()
          .sendMessage(ChatWriter.pluginMessage(
              ChatColor.RED + BedwarsRel
                  ._l(this.getServer().getConsoleSender(), "errors.classnotfound",
                      ImmutableMap.of("package", "craftbukkit", "class", classname))));
      return null;
    }
  }

  public String getCurrentVersion() {
    return this.version;
  }

  public String getFallbackLocale() {
    return "en_US";
  }

  public IHologramInteraction getHolographicInteractor() {
    return this.holographicInteraction;
  }

  public int getIntConfig(String key, int defaultInt) {
    FileConfiguration config = this.getConfig();
    if (config.contains(key) && config.isInt(key)) {
      return config.getInt(key);
    }
    return defaultInt;
  }

  private boolean getIsSpigot() {
    try {
      Package spigotPackage = Package.getPackage("org.spigotmc");
      return (spigotPackage != null);
    } catch (Exception e) {
      BedwarsRel.getInstance().getBugsnag().notify(e);
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

  public Package getMinecraftPackage() {
    try {
      if (this.minecraft == null) {
        return Package.getPackage("net.minecraft.server."
            + Bukkit.getServer().getClass().getPackage().getName().replace(".", ",").split(",")[3]);
      } else {
        return this.minecraft;
      }
    } catch (Exception ex) {
      BedwarsRel.getInstance().getBugsnag().notify(ex);
      this.getServer().getConsoleSender().sendMessage(ChatWriter.pluginMessage(ChatColor.RED
          + BedwarsRel._l(this.getServer().getConsoleSender(), "errors.packagenotfound",
          ImmutableMap.of("package", "minecraft server"))));
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
      BedwarsRel.getInstance().getBugsnag().notify(ex);
      this.getServer().getConsoleSender()
          .sendMessage(ChatWriter.pluginMessage(
              ChatColor.RED + BedwarsRel
                  ._l(this.getServer().getConsoleSender(), "errors.classnotfound",
                      ImmutableMap.of("package", "minecraft server", "class", classname))));
      return null;
    }
  }

  public String getMissingHoloDependency() {
    if (!BedwarsRel.getInstance().isHologramsEnabled()) {
      String missingHoloDependency = null;
      if (this.getServer().getPluginManager().isPluginEnabled("HologramAPI")
          || this.getServer().getPluginManager().isPluginEnabled("HolographicDisplays")) {
        if (this.getServer().getPluginManager().isPluginEnabled("HologramAPI")) {
          missingHoloDependency = "PacketListenerApi";
          return missingHoloDependency;
        }
        if (this.getServer().getPluginManager().isPluginEnabled("HolographicDisplays")) {
          missingHoloDependency = "ProtocolLib";
          return missingHoloDependency;
        }
      } else {
        missingHoloDependency = "HolographicDisplays and ProtocolLib";
        return missingHoloDependency;
      }
    }
    return null;
  }

  public PlayerStatisticManager getPlayerStatisticManager() {
    return this.playerStatisticManager;
  }

  public Integer getRespawnProtectionTime() {
    FileConfiguration config = this.getConfig();
    if (config.contains("respawn-protection") && config.isInt("respawn-protection")) {
      return config.getInt("respawn-protection");
    }
    return 0;
  }

  public ScoreboardManager getScoreboardManager() {
    return this.scoreboardManager;
  }

  public String getSenderLocale(CommandSender commandSender) {
    String locale = BedwarsRel.getInstance().getConfig().getString("locale");
    if (commandSender instanceof Player) {
      Player player = (Player) commandSender;
      if (BedwarsRel.getInstance().getPlayerLocales().containsKey(player.getUniqueId())) {
        locale = BedwarsRel.getInstance().getPlayerLocales().get(player.getUniqueId());
      }
    }
    return locale;
  }

  @SuppressWarnings("unchecked")
  public ArrayList<BaseCommand> getSetupCommands() {
    ArrayList<BaseCommand> commands = (ArrayList<BaseCommand>) this.commands.clone();
    commands = this.filterCommandsByPermission(commands, "setup");

    return commands;
  }

  public FileConfiguration getShopConfig() {
    return this.shopConfig;
  }

  public StorageType getStatisticStorageType() {
    String storage = this.getStringConfig("statistics.storage", "yaml");
    return StorageType.getByName(storage);
  }

  public String getStringConfig(String key, String defaultString) {
    FileConfiguration config = this.getConfig();
    if (config.contains(key) && config.isString(key)) {
      return config.getString(key);
    }
    return defaultString;
  }

  public Class<?> getVersionRelatedClass(String className) {
    try {
      Class<?> clazz = Class.forName(
          "io.github.bedwarsrel.com." + this.getCurrentVersion().toLowerCase() + "." + className);
      return clazz;
    } catch (Exception ex) {
      BedwarsRel.getInstance().getBugsnag().notify(ex);
      this.getServer().getConsoleSender()
          .sendMessage(ChatWriter.pluginMessage(ChatColor.RED
              + "Couldn't find version related class io.github.bedwarsrel.com."
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
      BedwarsRel.getInstance().getBugsnag().notify(ex);
      ex.printStackTrace();
    }

    return null;
  }

  public boolean isBreakableType(Material type) {
    return ((BedwarsRel.getInstance().getConfig().getBoolean("breakable-blocks.use-as-blacklist")
        && !this.breakableTypes.contains(type))
        || (!BedwarsRel.getInstance().getConfig().getBoolean("breakable-blocks.use-as-blacklist")
        && this.breakableTypes.contains(type)));
  }

  public boolean isBungee() {
    return this.getConfig().getBoolean("bungeecord.enabled");
  }

  public boolean isHologramsEnabled() {
    return (this.getServer().getPluginManager().isPluginEnabled("HologramAPI")
        && this.getServer().getPluginManager().isPluginEnabled("PacketListenerApi"))
        || (this.getServer().getPluginManager().isPluginEnabled("HolographicDisplays")
        && this.getServer().getPluginManager().isPluginEnabled("ProtocolLib"));
  }

  public boolean isLocationSerializable() {
    if (BedwarsRel.locationSerializable == null) {
      try {
        Location.class.getMethod("serialize");
        BedwarsRel.locationSerializable = true;
      } catch (Exception ex) {
        BedwarsRel.getInstance().getBugsnag().notify(ex);
        BedwarsRel.locationSerializable = false;
      }
    }

    return BedwarsRel.locationSerializable;
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

  public boolean isSpigot() {
    return this.isSpigot;
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
      BedwarsRel.getInstance().getBugsnag().notify(e);
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

  private void loadDatabase() {
    if (!this.getBooleanConfig("statistics.enabled", false)) {
      return;
    }

    String host = this.getStringConfig("database.host", null);
    int port = this.getIntConfig("database.port", 3306);
    String user = this.getStringConfig("database.user", null);
    String password = this.getStringConfig("database.password", null);
    String db = this.getStringConfig("database.db", null);
    String tablePrefix = this.getStringConfig("database.table-prefix", "bw_");

    if (BedwarsRel.getInstance().getStatisticStorageType() == StorageType.YAML) {
      this.databaseManager = new YamlDatabaseManager();
    } else if (BedwarsRel.getInstance().getStatisticStorageType() == StorageType.DATABASE) {
      this.databaseManager = new MysqlDatabaseManager(host, port, user, password, db, tablePrefix);
    }

    if (BedwarsRel.getInstance().getStatisticStorageType() != StorageType.YAML
        && BedwarsRel.getInstance().getStatisticStorageType() != StorageType.DATABASE) {
      return;
    }

    this.getServer().getConsoleSender()
        .sendMessage(ChatWriter.pluginMessage(ChatColor.GREEN + "Initialize database ..."));

    this.databaseManager.initialize();
  }

  private void loadLocalization(String locale) {
    if (!this.localization.containsKey(locale)) {
      this.localization.put(locale, new LocalizationConfig(locale));
    }
  }

  public void loadShop() {
    File file = new File(BedwarsRel.getInstance().getDataFolder(), "shop.yml");
    if (!file.exists()) {
      // create default file
      this.saveResource("shop.yml", false);

      // wait until it's really saved
      try {
        Thread.sleep(100);
      } catch (InterruptedException e) {
        BedwarsRel.getInstance().getBugsnag().notify(e);
        e.printStackTrace();
      }
    }

    this.shopConfig = new YamlConfiguration();

    try {
      BufferedReader reader =
          new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF-8"));
      this.shopConfig.load(reader);
    } catch (Exception e) {
      BedwarsRel.getInstance().getBugsnag().notify(e);
      this.getServer().getConsoleSender().sendMessage(
          ChatWriter.pluginMessage(ChatColor.RED + "Couldn't load shop! Error in parsing shop!"));
      e.printStackTrace();
    }
  }

  private void loadStatistics() {
    this.playerStatisticManager = new PlayerStatisticManager();
  }

  private String loadVersion() {
    String packName = Bukkit.getServer().getClass().getPackage().getName();
    return packName.substring(packName.lastIndexOf('.') + 1);
  }

  public boolean metricsEnabled() {
    if (this.getConfig().contains("plugin-metrics")
        && this.getConfig().isBoolean("plugin-metrics")) {
      return this.getConfig().getBoolean("plugin-metrics");
    }

    return false;
  }

  @Override
  public void onDisable() {
    this.stopTimeListener();
    this.gameManager.unloadGames();

    if (this.isHologramsEnabled() && this.holographicInteraction != null) {
      this.holographicInteraction.unloadHolograms();
    }
  }

  @Override
  public void onEnable() {
    BedwarsRel.instance = this;

    if (this.getDescription().getVersion().contains("-SNAPSHOT")
        && System.getProperty("IReallyKnowWhatIAmDoingISwear") == null) {
      this.getServer().getConsoleSender().sendMessage(ChatWriter
          .pluginMessage(ChatColor.RED + "*** Warning, you are using a development build ***"));
      this.getServer().getConsoleSender().sendMessage(ChatWriter
          .pluginMessage(ChatColor.RED + "*** You will get NO support regarding this build ***"));
      this.getServer().getConsoleSender().sendMessage(ChatWriter.pluginMessage(ChatColor.RED
          + "*** Please download a stable build from https://github.com/BedwarsRel/BedwarsRel/releases ***"));
    }

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

    if (this.getBooleanConfig("send-error-data", true) && this.bugsnag != null) {
      this.enableBugsnag();
    } else {
      this.disableBugsnag();
    }

    this.loadShop();

    this.isSpigot = this.getIsSpigot();
    this.loadDatabase();

    this.registerCommands();
    this.registerListener();

    this.gameManager = new GameManager();

    // bungeecord
    if (BedwarsRel.getInstance().isBungee()) {
      this.getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");
    }

    this.loadStatistics();
    this.loadLocalization(this.getConfig().getString("locale"));

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
    try {
      this.bugsnag = new Bugsnag("c23593c1e2f40fc0da36564af1bd00c6");
      this.bugsnag.setAppVersion(SupportData.getPluginVersion());
      this.bugsnag.setProjectPackages("io.github.bedwarsrel");
      this.bugsnag.setReleaseStage(SupportData.getPluginVersionType());
    } catch (Exception e) {
      this.getServer().getConsoleSender().sendMessage(
          ChatWriter.pluginMessage(ChatColor.GOLD + "Couldn't register Bugsnag."));
    }
  }

  private void registerCommands() {
    BedwarsCommandExecutor executor = new BedwarsCommandExecutor(this);

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
    this.commands.add(new DebugPasteCommand(this));
    this.commands.add(new ItemsPasteCommand(this));

    this.getCommand("bw").setExecutor(executor);
  }

  private void registerConfigurationClasses() {
    ConfigurationSerialization.registerClass(ResourceSpawner.class, "RessourceSpawner");
    ConfigurationSerialization.registerClass(Team.class, "Team");
    ConfigurationSerialization.registerClass(PlayerStatistic.class, "PlayerStatistic");
  }

  private void registerListener() {
    new WeatherListener();
    new BlockListener();
    new PlayerListener();
    if (!BedwarsRel.getInstance().getCurrentVersion().startsWith("v1_8")) {
      new PlayerSwapHandItemsEventListener();
    }
    if (BedwarsRel.getInstance().getCurrentVersion().startsWith("v1_8")
        || BedwarsRel.getInstance().getCurrentVersion().startsWith("v1_9") | BedwarsRel
        .getInstance().getCurrentVersion().startsWith("v1_10") || BedwarsRel.getInstance()
        .getCurrentVersion().startsWith("v1_11")) {
      new PlayerPickUpItemEventListener();
    } else {
      new EntityPickupItemEventListener();
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

  public void reloadLocalization() {
    this.localization = new HashMap<>();
    this.loadLocalization(this.getConfig().getString("locale"));
  }

  public void saveConfiguration() {
    File file = new File(BedwarsRel.getInstance().getDataFolder(), "config.yml");
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
      BedwarsRel.getInstance().getBugsnag().notify(ex);
      ex.printStackTrace();
    }
  }

  public boolean spectationEnabled() {
    if (this.getConfig().contains("spectation-enabled")
        && this.getConfig().isBoolean("spectation-enabled")) {
      return this.getConfig().getBoolean("spectation-enabled");
    }
    return true;
  }

  public void startMetricsIfEnabled() {
    if (this.metricsEnabled()) {
      new BStatsMetrics(this);
      try {
        McStatsMetrics mcStatsMetrics = new McStatsMetrics(this);
        mcStatsMetrics.start();
      } catch (Exception ex) {
        BedwarsRel.getInstance().getBugsnag().notify(ex);
        this.getServer().getConsoleSender().sendMessage(ChatWriter
            .pluginMessage(ChatColor.RED + "Metrics are enabled, but couldn't send data!"));
      }
    }
  }

  private void startTimeListener() {
    this.timeTask = this.getServer().getScheduler().runTaskTimer(this, new Runnable() {

      @Override
      public void run() {
        for (Game g : BedwarsRel.getInstance().getGameManager().getGames()) {
          if (g.getState() == GameState.RUNNING) {
            g.getRegion().getWorld().setTime(g.getTime());
          }
        }
      }
    }, (long) 5 * 20, (long) 5 * 20);
  }

  public boolean statisticsEnabled() {
    return this.getBooleanConfig("statistics.enabled", false);
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

  public boolean toMainLobby() {
    if (this.getConfig().contains("endgame.mainlobby-enabled")) {
      return this.getConfig().getBoolean("endgame.mainlobby-enabled");
    }

    return false;
  }

}
