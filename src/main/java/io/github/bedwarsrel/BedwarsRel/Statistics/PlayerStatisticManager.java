package io.github.bedwarsrel.BedwarsRel.Statistics;

import io.github.bedwarsrel.BedwarsRel.Events.BedwarsSavePlayerStatisticEvent;
import io.github.bedwarsrel.BedwarsRel.Main;
import io.github.bedwarsrel.BedwarsRel.Utils.ChatWriter;
import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

public class PlayerStatisticManager {

  private static final String CREATE_TABLE_SQL =
      "CREATE TABLE IF NOT EXISTS `" + Main.getInstance().getDatabaseManager().getTablePrefix()
          + "stats_players` (`kills` int(11) NOT NULL DEFAULT '0', `wins` int(11) NOT NULL DEFAULT '0', `score` int(11) NOT NULL DEFAULT '0', `games` int(11) NOT NULL DEFAULT '0', `loses` int(11) NOT NULL DEFAULT '0', `name` varchar(255) NOT NULL, `destroyedBeds` int(11) NOT NULL DEFAULT '0', `uuid` varchar(255) NOT NULL, `deaths` int(11) NOT NULL DEFAULT '0', PRIMARY KEY (`uuid`))";
  private static final String READ_OBJECT_SQL =
      "SELECT * FROM " + Main.getInstance().getDatabaseManager().getTablePrefix()
          + "stats_players WHERE uuid = ? LIMIT 1";
  private static final String WRITE_OBJECT_SQL =
      "INSERT INTO " + Main.getInstance().getDatabaseManager().getTablePrefix()
          + "stats_players(uuid, name, deaths, destroyedBeds, games, kills, loses, score, wins) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?) ON DUPLICATE KEY UPDATE uuid=VALUES(uuid),name=VALUES(name),deaths=deaths+VALUES(deaths),destroyedBeds=destroyedBeds+VALUES(destroyedBeds),games=games+VALUES(games),kills=kills+VALUES(kills),loses=loses+VALUES(loses),score=score+VALUES(score),wins=wins+VALUES(wins)";
  private File databaseFile = null;
  private FileConfiguration fileDatabase = null;
  private Map<UUID, PlayerStatistic> playerStatistic = null;

  public PlayerStatisticManager() {
    this.playerStatistic = new HashMap<>();
    this.fileDatabase = null;
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

    lines.add(this.getStatisticLine("name", playerStatistic.getName(), null, withPrefix, nameColor,
        valueColor));
    lines.add(this.getStatisticLine("kills", playerStatistic.getKills(),
        playerStatistic.getCurrentKills(), withPrefix, nameColor,
        valueColor));
    lines.add(this.getStatisticLine("deaths", playerStatistic.getDeaths(),
        playerStatistic.getCurrentDeaths(), withPrefix, nameColor,
        valueColor));
    Double kdDifference = playerStatistic.getKD() - playerStatistic.getCurrentKD();
    DecimalFormat df = new DecimalFormat("#.##");
    kdDifference = Double.valueOf(df.format(kdDifference));
    lines.add(
        this.getStatisticLine("kd", playerStatistic.getKD(), kdDifference, withPrefix, nameColor,
            valueColor));
    lines.add(this.getStatisticLine("wins", playerStatistic.getWins(),
        playerStatistic.getCurrentWins(), withPrefix, nameColor,
        valueColor));
    lines.add(this.getStatisticLine("loses", playerStatistic.getLoses(),
        playerStatistic.getCurrentLoses(), withPrefix, nameColor,
        valueColor));
    lines.add(this.getStatisticLine("destroyedBeds", playerStatistic.getDestroyedBeds(),
        playerStatistic.getCurrentDestroyedBeds(), withPrefix, nameColor,
        valueColor));
    lines.add(this.getStatisticLine("score", playerStatistic.getScore(),
        playerStatistic.getCurrentScore(), withPrefix, nameColor,
        valueColor));

    return lines;
  }

  private String getComparisonString(Double value) {
    if (value > 0) {
      return ChatColor.GREEN + "+" + value;
    } else if (value < 0) {
      return ChatColor.RED + String.valueOf(value);
    } else {
      return String.valueOf(value);
    }
  }

  private String getComparisonString(Integer value) {
    if (value > 0) {
      return ChatColor.GREEN + "+" + value;
    } else if (value < 0) {
      return ChatColor.RED + String.valueOf(value);
    } else {
      return String.valueOf(value);
    }
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

  private String getStatisticLine(String name, Object value1, Object value2, Boolean withPrefix,
      String nameColor,
      String valueColor) {
    String line;
    if (value2 != null && value2 instanceof Integer && (int) value2 != 0) {
      line = nameColor + Main._l("stats." + name) + ": "
          + valueColor + value1 + " " + this.getComparisonString((int) value2);
    } else if (value2 != null && value2 instanceof Double && (double) value2 != 0.00) {
      line = nameColor + Main._l("stats." + name) + ": "
          + valueColor + value1 + " " + this.getComparisonString((double) value2);
    } else {
      line = nameColor + Main._l("stats." + name) + ": "
          + valueColor + value1;
    }
    if (withPrefix) {
      line = ChatWriter.pluginMessage(line);
    }
    return line;
  }

  public void initialize() {
    if (!Main.getInstance().getBooleanConfig("statistics.enabled", false)) {
      return;
    }

    if (Main.getInstance().getStatisticStorageType() == StorageType.YAML) {
      File file = new File(Main.getInstance().getDataFolder() + "/database/bw_stats_players.yml");
      this.loadYml(file);
    }

    if (Main.getInstance().getStatisticStorageType() == StorageType.DATABASE) {
      this.initializeDatabase();
    }
  }

  public void initializeDatabase() {
    Main.getInstance().getServer().getConsoleSender().sendMessage(
        ChatWriter.pluginMessage(ChatColor.GREEN + "Loading Statistics from Database ..."));

    try {
      Connection connection = Main.getInstance().getDatabaseManager().getConnection();
      connection.setAutoCommit(false);
      PreparedStatement preparedStatement = connection.prepareStatement(CREATE_TABLE_SQL);
      preparedStatement.executeUpdate();
      connection.commit();
      preparedStatement.close();
      connection.close();
    } catch (Exception ex) {
      Main.getInstance().getBugsnag().notify(ex);
      ex.printStackTrace();
    }

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

  private PlayerStatistic loadStatistic(UUID uuid) {
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
      Map<OfflinePlayer, PlayerStatistic> map = new HashMap<>();

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
    try {
      Connection connection = Main.getInstance().getDatabaseManager().getConnection();
      connection.setAutoCommit(false);

      PreparedStatement preparedStatement = connection.prepareStatement(WRITE_OBJECT_SQL);

      preparedStatement.setString(1, playerStatistic.getId().toString());
      preparedStatement.setString(2, playerStatistic.getName());
      preparedStatement.setInt(3, playerStatistic.getCurrentDeaths());
      preparedStatement.setInt(4, playerStatistic.getCurrentDestroyedBeds());
      preparedStatement.setInt(5, playerStatistic.getCurrentGames());
      preparedStatement.setInt(6, playerStatistic.getCurrentKills());
      preparedStatement.setInt(7, playerStatistic.getCurrentLoses());
      preparedStatement.setInt(8, playerStatistic.getCurrentScore());
      preparedStatement.setInt(9, playerStatistic.getCurrentWins());
      preparedStatement.executeUpdate();
      connection.commit();
      preparedStatement.close();
      connection.close();
      playerStatistic.addCurrentValues();
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
    statistic.addCurrentValues();
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
      this.playerStatistic.remove(player.getUniqueId());
    }
  }


}
