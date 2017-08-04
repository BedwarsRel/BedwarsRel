package io.github.bedwarsrel.statistics;

import io.github.bedwarsrel.BedwarsRel;
import io.github.bedwarsrel.events.BedwarsSavePlayerStatisticEvent;
import io.github.bedwarsrel.utils.ChatWriter;
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
    lines.add(this.getStatisticLine("kills",
        playerStatistic.getKills() + playerStatistic.getCurrentKills(),
        playerStatistic.getCurrentKills(), withPrefix, nameColor,
        valueColor));
    lines.add(this.getStatisticLine("deaths",
        playerStatistic.getDeaths() + playerStatistic.getCurrentDeaths(),
        playerStatistic.getCurrentDeaths(), withPrefix, nameColor,
        valueColor));
    Double kdDifference = playerStatistic.getCurrentKD() - playerStatistic.getKD();
    DecimalFormat df = new DecimalFormat("#.##");
    kdDifference = Double.valueOf(df.format(kdDifference));
    lines.add(
        this.getStatisticLine("kd", playerStatistic.getCurrentKD(), kdDifference, withPrefix,
            nameColor,
            valueColor));
    lines.add(
        this.getStatisticLine("wins", playerStatistic.getWins() + playerStatistic.getCurrentWins(),
            playerStatistic.getCurrentWins(), withPrefix, nameColor,
            valueColor));
    lines.add(this.getStatisticLine("loses",
        playerStatistic.getLoses() + playerStatistic.getCurrentLoses(),
        playerStatistic.getCurrentLoses(), withPrefix, nameColor,
        valueColor));
    lines.add(this.getStatisticLine("games",
        playerStatistic.getGames() + playerStatistic.getCurrentGames(),
        playerStatistic.getCurrentGames(), withPrefix, nameColor,
        valueColor));
    lines.add(this.getStatisticLine("destroyedBeds",
        playerStatistic.getDestroyedBeds() + playerStatistic.getCurrentDestroyedBeds(),
        playerStatistic.getCurrentDestroyedBeds(), withPrefix, nameColor,
        valueColor));
    lines.add(this.getStatisticLine("score",
        playerStatistic.getScore() + playerStatistic.getCurrentScore(),
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
      line = nameColor + BedwarsRel._l("stats." + name) + ": "
          + valueColor + value1 + " " + nameColor + "(" + this.getComparisonString((int) value2)
          + nameColor + ")";
    } else if (value2 != null && value2 instanceof Double && (double) value2 != 0.00) {
      line = nameColor + BedwarsRel._l("stats." + name) + ": "
          + valueColor + value1 + " " + nameColor + "(" + this.getComparisonString((double) value2)
          + nameColor + ")";
    } else {
      line = nameColor + BedwarsRel._l("stats." + name) + ": "
          + valueColor + value1;
    }
    if (withPrefix) {
      line = ChatWriter.pluginMessage(line);
    }
    return line;
  }

  public void initialize() {
    if (!BedwarsRel.getInstance().getBooleanConfig("statistics.enabled", false)) {
      return;
    }

    if (BedwarsRel.getInstance().getStatisticStorageType() == StorageType.YAML) {
      File file = new File(
          BedwarsRel.getInstance().getDataFolder() + "/database/bw_stats_players.yml");
      this.loadYml(file);
    }

    if (BedwarsRel.getInstance().getStatisticStorageType() == StorageType.DATABASE) {
      this.initializeDatabase();
    }
  }

  public void initializeDatabase() {
    BedwarsRel.getInstance().getServer().getConsoleSender().sendMessage(
        ChatWriter.pluginMessage(ChatColor.GREEN + "Loading statistics from database ..."));

    try {
      Connection connection = BedwarsRel.getInstance().getDatabaseManager().getConnection();
      connection.setAutoCommit(false);
      PreparedStatement preparedStatement = connection
          .prepareStatement(BedwarsRel.getInstance().getDatabaseManager().getCreateTableSql());
      preparedStatement.executeUpdate();
      connection.commit();
      preparedStatement.close();
      connection.close();
    } catch (Exception ex) {
      BedwarsRel.getInstance().getBugsnag().notify(ex);
      ex.printStackTrace();
    }

  }

  private PlayerStatistic loadDatabaseStatistic(UUID uuid) {
    if (this.playerStatistic.containsKey(uuid)) {
      return this.playerStatistic.get(uuid);
    }
    HashMap<String, Object> deserialize = new HashMap<>();

    try {
      Connection connection = BedwarsRel.getInstance().getDatabaseManager().getConnection();
      PreparedStatement preparedStatement = connection
          .prepareStatement(BedwarsRel.getInstance().getDatabaseManager().getReadObjectSql());
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
    Player player = BedwarsRel.getInstance().getServer().getPlayer(uuid);
    if (player != null && !playerStatistic.getName().equals(player.getName())) {
      playerStatistic.setName(player.getName());
    }

    this.playerStatistic.put(playerStatistic.getId(), playerStatistic);
    return playerStatistic;
  }

  public PlayerStatistic loadStatistic(UUID uuid) {
    if (BedwarsRel.getInstance().getStatisticStorageType() == StorageType.YAML) {
      return this.loadYamlStatistic(uuid);
    } else {
      return this.loadDatabaseStatistic(uuid);
    }
  }

  private PlayerStatistic loadYamlStatistic(UUID uuid) {

    if (this.fileDatabase == null || !this.fileDatabase.contains("data." + uuid.toString())
        || this.fileDatabase.getConfigurationSection("data." + uuid.toString()).getValues(false)
        == null) {
      PlayerStatistic playerStatistic = new PlayerStatistic(uuid);
      this.playerStatistic.put(uuid, playerStatistic);
      return playerStatistic;
    }

    HashMap<String, Object> deserialize = new HashMap<>();
    deserialize.putAll(
        this.fileDatabase.getConfigurationSection("data." + uuid.toString()).getValues(false));
    PlayerStatistic playerStatistic = new PlayerStatistic(deserialize);
    playerStatistic.setId(uuid);
    Player player = BedwarsRel.getInstance().getServer().getPlayer(uuid);
    if (player != null && !playerStatistic.getName().equals(player.getName())) {
      playerStatistic.setName(player.getName());
    }
    this.playerStatistic.put(uuid, playerStatistic);
    return playerStatistic;
  }

  private void loadYml(File ymlFile) {
    try {
      BedwarsRel.getInstance().getServer().getConsoleSender().sendMessage(
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
      BedwarsRel.getInstance().getBugsnag().notify(ex);
      ex.printStackTrace();
    }

    BedwarsRel.getInstance().getServer().getConsoleSender()
        .sendMessage(ChatWriter.pluginMessage(ChatColor.GREEN + "Done!"));
  }

  private void storeDatabaseStatistic(PlayerStatistic playerStatistic) {
    try {
      Connection connection = BedwarsRel.getInstance().getDatabaseManager().getConnection();
      connection.setAutoCommit(false);

      PreparedStatement preparedStatement = connection
          .prepareStatement(BedwarsRel.getInstance().getDatabaseManager().getWriteObjectSql());

      preparedStatement.setString(1, playerStatistic.getId().toString());
      preparedStatement.setString(2, playerStatistic.getName());
      preparedStatement.setInt(3, playerStatistic.getCurrentDeaths());
      preparedStatement.setInt(4, playerStatistic.getCurrentDestroyedBeds());
      preparedStatement.setInt(5, playerStatistic.getCurrentKills());
      preparedStatement.setInt(6, playerStatistic.getCurrentLoses());
      preparedStatement.setInt(7, playerStatistic.getCurrentScore());
      preparedStatement.setInt(8, playerStatistic.getCurrentWins());
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
    BedwarsRel.getInstance().getServer().getPluginManager().callEvent(savePlayerStatisticEvent);

    if (savePlayerStatisticEvent.isCancelled()) {
      return;
    }

    if (BedwarsRel.getInstance().getStatisticStorageType() == StorageType.YAML) {
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
      BedwarsRel.getInstance().getBugsnag().notify(ex);
      BedwarsRel.getInstance().getServer().getConsoleSender().sendMessage(ChatWriter.pluginMessage(
          ChatColor.RED + "Couldn't store statistic data for player with uuid: " + statistic.getId()
              .toString()));
    }
  }

  public void unloadStatistic(OfflinePlayer player) {
    if (BedwarsRel.getInstance().getStatisticStorageType() != StorageType.YAML) {
      this.playerStatistic.remove(player.getUniqueId());
    }
  }


}
