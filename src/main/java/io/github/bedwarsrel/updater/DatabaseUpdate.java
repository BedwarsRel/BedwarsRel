package io.github.bedwarsrel.updater;

public class DatabaseUpdate {

  private String sql = null;

  public DatabaseUpdate(String sql) {
    super();

    this.sql = sql;
  }

  public String getSql() {
    return sql;
  }
}
