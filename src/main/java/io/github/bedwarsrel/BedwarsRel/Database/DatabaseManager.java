package io.github.bedwarsrel.BedwarsRel.Database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.TimeZone;
import lombok.Getter;

public class DatabaseManager {

  @Getter
  private String tablePrefix = "bw_";
  private String database = null;
  private HikariDataSource dataSource = null;
  private String host = null;
  private String password = null;
  private int port = 3306;
  private String user = null;

  public DatabaseManager(String host, int port, String user, String password, String database, String tablePrefix) {
    this.host = host;
    this.port = port;
    this.user = user;
    this.password = password;
    this.database = database;
    this.tablePrefix = tablePrefix;
  }


  public void initialize() {
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
  }

  public Connection getConnection(){
    try {
      return this.dataSource.getConnection();
    } catch (SQLException e) {
      e.printStackTrace();
    }
    return null;
  }

}
