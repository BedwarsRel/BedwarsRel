package io.github.bedwarsrel.statistics;

import io.github.bedwarsrel.BedwarsRel;
import io.github.bedwarsrel.events.BedwarsSavePlayerStatisticEvent;
import io.github.bedwarsrel.utils.ChatWriter;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.Getter;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;

public class PlayerStatisticManager {

  @Getter
  private Map<UUID, PlayerStatistic> playerStatistics;

  public PlayerStatisticManager() {
    this.playerStatistics = new HashMap<>();
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

    if (!this.playerStatistics.containsKey(player.getUniqueId())) {
      return this.loadStatistic(player.getUniqueId());
    }

    return this.playerStatistics.get(player.getUniqueId());
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


  public PlayerStatistic loadStatistic(UUID uuid) {
    if (this.playerStatistics.containsKey(uuid)) {
      return this.playerStatistics.get(uuid);
    }

    PlayerStatistic statistic = BedwarsRel.getInstance().getDatabaseManager().loadStatistic(uuid);
    if(statistic != null && statistic.getId() == null){
      statistic.setId(uuid);
    }
    this.playerStatistics.put(uuid, statistic);

    return statistic;
  }



  public void storeStatistic(PlayerStatistic statistic) {
    BedwarsSavePlayerStatisticEvent savePlayerStatisticEvent =
        new BedwarsSavePlayerStatisticEvent(statistic);
    BedwarsRel.getInstance().getServer().getPluginManager().callEvent(savePlayerStatisticEvent);

    if (savePlayerStatisticEvent.isCancelled()) {
      return;
    }

    BedwarsRel.getInstance().getDatabaseManager().storeStatistic(statistic);
  }

  public void unloadStatistic(OfflinePlayer player) {
    if (BedwarsRel.getInstance().getStatisticStorageType() != StorageType.YAML) {
      this.playerStatistics.remove(player.getUniqueId());
    }
  }


}
