package io.github.bedwarsrel.database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import io.github.bedwarsrel.BedwarsRel;
import io.github.bedwarsrel.statistics.PlayerStatistic;
import io.github.bedwarsrel.utils.ChatWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.TimeZone;
import java.util.UUID;
import lombok.Getter;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class MysqlDatabaseManager implements DatabaseManager {

  private HikariDataSource dataSource;
  private String database;
  private String host;
  private String password;
  private int port = 3306;
  @Getter
  private String tablePrefix = "bw_";
  private String user;

  public MysqlDatabaseManager(String host, int port, String user, String password, String database,
      String tablePrefix) {
    this.host = host;
    this.port = port;
    this.user = user;
    this.password = password;
    this.database = database;
    this.tablePrefix = tablePrefix;
  }

  private Connection getConnection() {
    if(this.dataSource == null){
      return null;
    }
    try {
      return this.dataSource.getConnection();
    } catch (SQLException e) {
      e.printStackTrace();
    }
    return null;
  }

  private String getCreateTableSql() {
    return "CREATE TABLE IF NOT EXISTS `" + this.getTablePrefix()
        + "stats_players` (`kills` int(11) NOT NULL DEFAULT '0', `wins` int(11) NOT NULL DEFAULT '0', `score` int(11) NOT NULL DEFAULT '0', `loses` int(11) NOT NULL DEFAULT '0', `name` varchar(255) NOT NULL, `destroyedBeds` int(11) NOT NULL DEFAULT '0', `uuid` varchar(255) NOT NULL, `deaths` int(11) NOT NULL DEFAULT '0', PRIMARY KEY (`uuid`))";
  }

  private String getReadObjectSql() {
    return "SELECT * FROM " + this.getTablePrefix()
        + "stats_players WHERE uuid = ? LIMIT 1";
  }

  private String getWriteObjectSql() {
    return "INSERT INTO " + this.getTablePrefix()
        + "stats_players(uuid, name, deaths, destroyedBeds, kills, loses, score, wins) VALUES (?, ?, ?, ?, ?, ?, ?, ?) ON DUPLICATE KEY UPDATE uuid=VALUES(uuid),name=VALUES(name),deaths=deaths+VALUES(deaths),destroyedBeds=destroyedBeds+VALUES(destroyedBeds),kills=kills+VALUES(kills),loses=loses+VALUES(loses),score=score+VALUES(score),wins=wins+VALUES(wins)";
  }

  @Override
  public void initialize() {
    if (this.host == null || this.user == null || this.password == null || this.database == null) {
      BedwarsRel.getInstance().getServer().getConsoleSender()
          .sendMessage(ChatWriter.pluginMessage(ChatColor.RED
              + "Could not initialize database connection. Make sure \"host\", \"user\", \"password\" and \"db\" is not null!"));
      return;
    }

    HikariConfig config = new HikariConfig();
    config.setJdbcUrl("jdbc:mysql://" + this.host + ":" + String.valueOf(this.port) + "/"
        + this.database + "?autoReconnect=true&serverTimezone=" + TimeZone
        .getDefault().getID());
    config.setUsername(this.user);
    config.setPassword(this.password);
    config.addDataSourceProperty("cachePrepStmts", "true");
    config.addDataSourceProperty("prepStmtCacheSize", "250");
    config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");

    this.dataSource = new HikariDataSource(config);

    if (BedwarsRel.getInstance().getBooleanConfig("statistics.enabled", false)) {
      this.initializePlayerStatistics();
    }
  }

  @Override
  public void initializePlayerStatistics() {
    BedwarsRel.getInstance().getServer().getConsoleSender().sendMessage(
        ChatWriter.pluginMessage(ChatColor.GREEN + "Loading statistics from database ..."));

    try {
      Connection connection = this.getConnection();
      connection.setAutoCommit(false);
      PreparedStatement preparedStatement = connection
          .prepareStatement(this.getCreateTableSql());
      preparedStatement.executeUpdate();
      connection.commit();
      preparedStatement.close();
      connection.close();
    } catch (Exception ex) {
      BedwarsRel.getInstance().getBugsnag().notify(ex);
      ex.printStackTrace();
    }

    BedwarsRel.getInstance().getServer().getConsoleSender()
        .sendMessage(ChatWriter.pluginMessage(ChatColor.GREEN + "Done!"));
  }

  public PlayerStatistic loadStatistic(UUID uuid) {
    HashMap<String, Object> deserialize = new HashMap<>();

    try {
      Connection connection = this.getConnection();
      PreparedStatement preparedStatement = connection
          .prepareStatement(this.getReadObjectSql());
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

    return playerStatistic;
  }

  @Override
  public void storeStatistic(PlayerStatistic statistic) {
    try {
      Connection connection = this.getConnection();
      connection.setAutoCommit(false);

      PreparedStatement preparedStatement = connection
          .prepareStatement(this.getWriteObjectSql());

      preparedStatement.setString(1, statistic.getId().toString());
      preparedStatement.setString(2, statistic.getName());
      preparedStatement.setInt(3, statistic.getCurrentDeaths());
      preparedStatement.setInt(4, statistic.getCurrentDestroyedBeds());
      preparedStatement.setInt(5, statistic.getCurrentKills());
      preparedStatement.setInt(6, statistic.getCurrentLoses());
      preparedStatement.setInt(7, statistic.getCurrentScore());
      preparedStatement.setInt(8, statistic.getCurrentWins());
      preparedStatement.executeUpdate();
      connection.commit();
      preparedStatement.close();
      connection.close();
      statistic.addCurrentValues();
    } catch (SQLException e) {
      e.printStackTrace();
    }
  }
}
