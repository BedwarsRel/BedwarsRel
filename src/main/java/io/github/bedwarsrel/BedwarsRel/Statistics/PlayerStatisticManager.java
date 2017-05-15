package io.github.bedwarsrel.BedwarsRel.Statistics;

import io.github.bedwarsrel.BedwarsRel.Database.DatabaseManager;
import io.github.bedwarsrel.BedwarsRel.Events.BedwarsSavePlayerStatisticEvent;
import io.github.bedwarsrel.BedwarsRel.Main;
import io.github.bedwarsrel.BedwarsRel.Utils.ChatWriter;
import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

public class PlayerStatisticManager {

  static final String WRITE_OBJECT_SQL = "INSERT INTO bw_stats_players(uuid, name, deaths, destroyedBeds, kills, loses, score, wins) VALUES (?, ?, ?, ?, ?, ?, ?, ?) ON DUPLICATE KEY UPDATE uuid=VALUES(uuid),name=VALUES(name),deaths=VALUES(deaths),destroyedBeds=VALUES(destroyedBeds),kills=VALUES(kills),loses=VALUES(loses),score=VALUES(score),wins=VALUES(wins)";
  static final String READ_OBJECT_SQL = "SELECT * FROM bw_stats_players WHERE uuid = ? LIMIT 1";

  private File databaseFile = null;
  private FileConfiguration fileDatabase = null;
  private Map<UUID, PlayerStatistic> playerStatistic = null;

  public PlayerStatisticManager() {
    this.playerStatistic = new HashMap<>();
    this.fileDatabase = null;
  }

  public PlayerStatistic getStatistic(OfflinePlayer player) {
    if (player == null) {
      return null;
    }

    if (!this.playerStatistic.containsKey(player.getUniqueId())) {
      return this.loadStatistic(player.getUniqueId());
    }

    return this.playerStatistic.get(player.getUniqueId());
  }


  public void initialize() {
    if (!Main.getInstance().getBooleanConfig("statistics.enabled", false)) {
      return;
    }

    if (Main.getInstance().getStatisticStorageType() == StorageType.YAML) {
      File file = new File(Main.getInstance().getDataFolder() + "/database/"
          + DatabaseManager.DBPrefix + "stats_players.yml");
      this.loadYml(file);
    }

    if (Main.getInstance().getStatisticStorageType() == StorageType.DATABASE) {
      this.initializeDatabase();
    }
  }

  public void initializeDatabase() {
    Main.getInstance().getServer().getConsoleSender().sendMessage(
        ChatWriter.pluginMessage(ChatColor.GREEN + "Loading Statistics from Database ..."));
  }

  private PlayerStatistic loadDatabaseStatistic(UUID uuid) {
    if (this.playerStatistic.containsKey(uuid)) {
      return this.playerStatistic.get(uuid);
    }

    HashMap<String, Object> deserialize = new HashMap<>();
    try {
      Connection connection = Main.getInstance().getDatabaseManager().getConnection();

      PreparedStatement preparedStatement = connection.prepareStatement(READ_OBJECT_SQL);
      preparedStatement.setString(1, uuid.toString());
      ResultSet resultSet = preparedStatement.executeQuery();

      ResultSetMetaData meta = resultSet.getMetaData();
      while (resultSet.next()) {
        for (int i = 1; i <= meta.getColumnCount(); i++) {
          String key = meta.getColumnName(i);
          Object value = resultSet.getObject(key);
          deserialize.put(key, value);
        }
      }

      resultSet.close();
      preparedStatement.close();
      connection.close();
    } catch (SQLException e) {
      e.printStackTrace();
    }

    PlayerStatistic playerStatistic;

    if (deserialize.isEmpty()) {
      playerStatistic = new PlayerStatistic(uuid);
    } else {
      playerStatistic = new PlayerStatistic(deserialize);
    }

    this.playerStatistic.put(playerStatistic.getId(), playerStatistic);
    return playerStatistic;
  }

  public PlayerStatistic loadStatistic(UUID uuid) {
    if (Main.getInstance().getStatisticStorageType() == StorageType.YAML) {
      return this.loadYamlStatistic(uuid);
    } else {
      return this.loadDatabaseStatistic(uuid);
    }
  }

  private PlayerStatistic loadYamlStatistic(UUID uuid) {

    if (this.fileDatabase == null || !this.fileDatabase.contains("data." + uuid.toString())) {
      PlayerStatistic playerStatistic = new PlayerStatistic(uuid);
      this.playerStatistic.put(uuid, playerStatistic);
      return playerStatistic;
    }

    HashMap<String, Object> deserialize = new HashMap<>();
    deserialize.putAll(
        this.fileDatabase.getConfigurationSection("data." + uuid.toString()).getValues(false));
    PlayerStatistic playerStatistic = new PlayerStatistic(deserialize);
    playerStatistic.setId(uuid);
    Player player = Main.getInstance().getServer().getPlayer(uuid);
    if (player != null && !playerStatistic.getName().equals(player.getName())) {
      playerStatistic.setName(player.getName());
    }
    this.playerStatistic.put(uuid, playerStatistic);
    return playerStatistic;
  }

  private void loadYml(File ymlFile) {
    try {
      Main.getInstance().getServer().getConsoleSender().sendMessage(
          ChatWriter.pluginMessage(ChatColor.GREEN + "Loading statistics from YAML-File ..."));

      YamlConfiguration config = null;
      Map<OfflinePlayer, PlayerStatistic> map = new HashMap<OfflinePlayer, PlayerStatistic>();

      this.databaseFile = ymlFile;

      if (!ymlFile.exists()) {
        ymlFile.getParentFile().mkdirs();
        ymlFile.createNewFile();

        config = new YamlConfiguration();
        config.createSection("data");
        config.save(ymlFile);
      } else {
        config = YamlConfiguration.loadConfiguration(ymlFile);
      }

      this.fileDatabase = config;

    } catch (Exception ex) {
      Main.getInstance().getBugsnag().notify(ex);
      ex.printStackTrace();
    }

    Main.getInstance().getServer().getConsoleSender()
        .sendMessage(ChatWriter.pluginMessage(ChatColor.GREEN + "Done!"));
  }

  private void storeDatabaseStatistic(PlayerStatistic playerStatistic) {

    Map<String, Object> statistic = playerStatistic.serialize();

    try {
      Connection connection = Main.getInstance().getDatabaseManager().getConnection();
      connection.setAutoCommit(false);

      PreparedStatement preparedStatement = connection.prepareStatement(WRITE_OBJECT_SQL);

      preparedStatement.setString(1, playerStatistic.getId().toString());
      preparedStatement.setString(2, playerStatistic.getName());
      preparedStatement.setInt(3, playerStatistic.getDeaths());
      preparedStatement.setInt(4, playerStatistic.getDestroyedBeds());
      preparedStatement.setInt(5, playerStatistic.getKills());
      preparedStatement.setInt(6, playerStatistic.getLoses());
      preparedStatement.setInt(7, playerStatistic.getScore());
      preparedStatement.setInt(8, playerStatistic.getWins());
      preparedStatement.executeUpdate();
      connection.commit();
      preparedStatement.close();
      connection.close();
    } catch (SQLException e) {
      e.printStackTrace();
    }

  }

  public void storeStatistic(PlayerStatistic statistic) {
    BedwarsSavePlayerStatisticEvent savePlayerStatisticEvent =
        new BedwarsSavePlayerStatisticEvent(statistic);
    Main.getInstance().getServer().getPluginManager().callEvent(savePlayerStatisticEvent);

    if (savePlayerStatisticEvent.isCancelled()) {
      return;
    }

    if (Main.getInstance().getStatisticStorageType() == StorageType.YAML) {
      this.storeYamlStatistic(statistic);
    } else {
      this.storeDatabaseStatistic(statistic);
    }
  }

  private synchronized void storeYamlStatistic(PlayerStatistic statistic) {
    this.fileDatabase.set("data." + statistic.getId().toString(), statistic.serialize());
    try {
      this.fileDatabase.save(this.databaseFile);
    } catch (Exception ex) {
      Main.getInstance().getBugsnag().notify(ex);
      Main.getInstance().getServer().getConsoleSender().sendMessage(ChatWriter.pluginMessage(
          ChatColor.RED + "Couldn't store statistic data for player with uuid: " + statistic.getId()
              .toString()));
    }
  }

  public void unloadStatistic(OfflinePlayer player) {
    if (Main.getInstance().getStatisticStorageType() != StorageType.YAML) {
      this.playerStatistic.remove(player);
    }
  }


  public List<String> createStatisticLines(PlayerStatistic playerStatistic, boolean withPrefix,
      ChatColor nameColor,
      ChatColor valueColor) {
    return this.createStatisticLines(playerStatistic, withPrefix, nameColor.toString(),
        valueColor.toString());
  }

  public List<String> createStatisticLines(PlayerStatistic playerStatistic, boolean withPrefix,
      String nameColor,
      String valueColor) {
    List<String> lines = new ArrayList<>();

    for (Entry<String, Object> entry : playerStatistic.serialize().entrySet()) {
      if (withPrefix) {
        lines.add(ChatWriter.pluginMessage(nameColor + Main._l("stats." + entry.getKey()) + ": "
            + valueColor + entry.getValue().toString()));
      } else {
        lines.add(nameColor + Main._l("stats." + entry.getKey()) + ": "
            + valueColor + entry.getValue().toString());
      }
    }

    return lines;
  }


}
