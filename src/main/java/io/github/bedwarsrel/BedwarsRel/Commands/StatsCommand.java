package io.github.bedwarsrel.BedwarsRel.Commands;

import com.google.common.collect.ImmutableMap;
import io.github.bedwarsrel.BedwarsRel.Main;
import io.github.bedwarsrel.BedwarsRel.Statistics.PlayerStatistic;
import io.github.bedwarsrel.BedwarsRel.Utils.ChatWriter;
import io.github.bedwarsrel.BedwarsRel.Utils.UUIDFetcher;
import java.util.ArrayList;
import java.util.UUID;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class StatsCommand extends BaseCommand implements ICommand {

  public StatsCommand(Main plugin) {
    super(plugin);
  }

  @Override
  public boolean execute(CommandSender sender, ArrayList<String> args) {
    if (!super.hasPermission(sender)) {
      return false;
    }

    Player player = (Player) sender;

    if (!player.hasPermission("bw.otherstats") && args.size() > 0) {
      args.clear();
    }

    player.sendMessage(ChatWriter.pluginMessage(
        ChatColor.GREEN + "----------- " + Main._l(player, "stats.header") + " -----------"));

    if (args.size() == 1) {
      String playerStats = args.get(0).toString();
      OfflinePlayer offPlayer = Main.getInstance().getServer().getPlayerExact(playerStats);

      if (offPlayer != null) {
        PlayerStatistic statistic =
            Main.getInstance().getPlayerStatisticManager().getStatistic(offPlayer);
        if (statistic == null) {
          player.sendMessage(ChatWriter.pluginMessage(ChatColor.RED
              + Main._l(player, "stats.statsnotfound", ImmutableMap.of("player", playerStats))));
          return true;
        }

        this.sendStats(player, statistic);
        return true;
      }

      UUID offUUID = null;
      try {
        offUUID = UUIDFetcher.getUUIDOf(playerStats);
        if (offUUID == null) {
          player.sendMessage(ChatWriter.pluginMessage(ChatColor.RED
              + Main._l(player, "stats.statsnotfound", ImmutableMap.of("player", playerStats))));
          return true;
        }
      } catch (Exception e) {
        Main.getInstance().getBugsnag().notify(e);
        e.printStackTrace();
      }

      offPlayer = Main.getInstance().getServer().getOfflinePlayer(offUUID);
      if (offPlayer == null) {
        player.sendMessage(ChatWriter.pluginMessage(ChatColor.RED
            + Main._l(player, "stats.statsnotfound", ImmutableMap.of("player", playerStats))));
        return true;
      }

      PlayerStatistic statistic =
          Main.getInstance().getPlayerStatisticManager().getStatistic(offPlayer);
      if (statistic == null) {
        player.sendMessage(ChatWriter.pluginMessage(ChatColor.RED
            + Main
            ._l(player, "stats.statsnotfound", ImmutableMap.of("player", offPlayer.getName()))));
        return true;
      }

      this.sendStats(player, statistic);
      return true;
    } else if (args.size() == 0) {
      PlayerStatistic statistic =
          Main.getInstance().getPlayerStatisticManager().getStatistic(player);
      if (statistic == null) {
        player.sendMessage(ChatWriter.pluginMessage(ChatColor.RED
            + Main._l(player, "stats.statsnotfound", ImmutableMap.of("player", player.getName()))));
        return true;
      }

      this.sendStats(player, statistic);
      return true;
    }

    return false;
  }

  @Override
  public String[] getArguments() {
    return new String[]{};
  }

  @Override
  public String getCommand() {
    return "stats";
  }

  @Override
  public String getDescription() {
    return Main._l("commands.stats.desc");
  }

  @Override
  public String getName() {
    return Main._l("commands.stats.name");
  }

  @Override
  public String getPermission() {
    return "base";
  }

  private void sendStats(Player player, PlayerStatistic statistic) {
    for (String line : Main.getInstance().getPlayerStatisticManager()
        .createStatisticLines(statistic, false, ChatColor.GRAY, ChatColor.YELLOW)) {
      player.sendMessage(line);
    }
  }

}
