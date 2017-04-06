package io.github.bedwarsrel.BedwarsRel.Statistics;

public enum StorageType {
  DATABASE("database"), YAML("yaml");

  private String name = null;

  StorageType(String configName) {
    this.name = configName;
  }

  public static StorageType getByName(String name) {
    for (StorageType type : StorageType.values()) {
      if (type.getName().equals(name)) {
        return type;
      }
    }

    return StorageType.YAML;
  }

  public String getName() {
    return this.name;
  }
}
