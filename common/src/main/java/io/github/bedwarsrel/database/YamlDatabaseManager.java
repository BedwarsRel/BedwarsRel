package io.github.bedwarsrel.database;

import io.github.bedwarsrel.BedwarsRel;
import io.github.bedwarsrel.statistics.PlayerStatistic;
import io.github.bedwarsrel.utils.ChatWriter;
import java.io.File;
import java.util.HashMap;
import java.util.UUID;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

public class YamlDatabaseManager implements DatabaseManager {

  private File databaseFile;
  private FileConfiguration fileDatabase;

  @Override
  public void initialize() {
    if (BedwarsRel.getInstance().getBooleanConfig("statistics.enabled", false)) {
      this.initializePlayerStatistics();
    }
  }

  @Override
  public void initializePlayerStatistics() {
    this.databaseFile = new File(
        BedwarsRel.getInstance().getDataFolder() + "/database/bw_stats_players.yml");
    try {
      BedwarsRel.getInstance().getServer().getConsoleSender().sendMessage(
          ChatWriter.pluginMessage(ChatColor.GREEN + "Loading statistics from YAML-File ..."));

      YamlConfiguration config;

      if (!this.databaseFile.exists()) {
        this.databaseFile.getParentFile().mkdirs();
        this.databaseFile.createNewFile();

        config = new YamlConfiguration();
        config.createSection("data");
        config.save(this.databaseFile);
      } else {
        config = YamlConfiguration.loadConfiguration(this.databaseFile);
      }

      this.fileDatabase = config;

    } catch (Exception ex) {
      BedwarsRel.getInstance().getBugsnag().notify(ex);
      ex.printStackTrace();
    }

    BedwarsRel.getInstance().getServer().getConsoleSender()
        .sendMessage(ChatWriter.pluginMessage(ChatColor.GREEN + "Done!"));

  }

  public PlayerStatistic loadStatistic(UUID uuid) {

    if (this.fileDatabase == null || !this.fileDatabase.contains("data." + uuid.toString())
        || this.fileDatabase.getConfigurationSection("data." + uuid.toString()).getValues(false)
        == null) {
      return new PlayerStatistic(uuid);
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
    return playerStatistic;
  }

  public void storeStatistic(PlayerStatistic statistic) {
    this.storeSyncStatistic(statistic);
  }

  private synchronized void storeSyncStatistic(PlayerStatistic statistic) {
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

}
