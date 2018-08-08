package io.github.bedwarsrel.database;

import io.github.bedwarsrel.statistics.PlayerStatistic;
import java.util.UUID;

public interface DatabaseManager {

  void initialize();

  void initializePlayerStatistics();

  PlayerStatistic loadStatistic(UUID uuid);

  void storeStatistic(PlayerStatistic statistic);


}
