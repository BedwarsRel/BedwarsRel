package io.github.bedwarsrel.statistics;

public enum StorageType {
  DATABASE("database"), YAML("yaml"), CUSTOM("custom");

  private String name = null;

  StorageType(String configName) {
    this.name = configName;
  }

  public static StorageType getByName(String name) {
    for (StorageType type : values()) {
      if (type.getName().equals(name)) {
        return type;
      }
    }

    return YAML;
  }

  public String getName() {
    return this.name;
  }
}
