package io.github.yannici.bedwars.Commands;

import io.github.yannici.bedwars.ChatWriter;
import io.github.yannici.bedwars.Main;
import io.github.yannici.bedwars.UUIDFetcher;
import io.github.yannici.bedwars.Statistics.PlayerStatistic;
import io.github.yannici.bedwars.Statistics.StatField;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.google.common.collect.ImmutableMap;

public class StatsCommand extends BaseCommand implements ICommand {

    public StatsCommand(Main plugin) {
        super(plugin);
    }

    @Override
    public String getCommand() {
        return "stats";
    }

    @Override
    public String getName() {
        return Main._l("commands.stats.name");
    }

    @Override
    public String getDescription() {
        return Main._l("commands.stats.desc");
    }

    @Override
    public String[] getArguments() {
        return new String[] {};
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

        player.sendMessage(ChatWriter.pluginMessage(ChatColor.GREEN
                + "----------- " + Main._l("stats.header") + " -----------"));

        if (args.size() == 1) {
            String playerStats = args.get(0).toString();
            OfflinePlayer offPlayer = Main.getInstance().getServer()
                    .getPlayerExact(playerStats);

            if (offPlayer != null) {
                player.sendMessage(ChatWriter.pluginMessage(ChatColor.GRAY
                        + Main._l("stats.name") + ": " + ChatColor.YELLOW
                        + offPlayer.getName()));
                PlayerStatistic statistic = Main.getInstance()
                        .getPlayerStatisticManager().getStatistic(offPlayer);
                if (statistic == null) {
                    player.sendMessage(ChatWriter.pluginMessage(ChatColor.RED
                            + Main._l("stats.statsnotfound",
                                    ImmutableMap.of("player", playerStats))));
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
                            + Main._l(
                                    "stats.statsnotfound",
                                    ImmutableMap.of("player",
                                            playerStats))));
                    return true;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            offPlayer = Main.getInstance().getServer()
                    .getOfflinePlayer(offUUID);
            if (offPlayer == null) {
                player.sendMessage(ChatWriter.pluginMessage(ChatColor.RED
                        + Main._l("stats.statsnotfound",
                                ImmutableMap.of("player", playerStats))));
                return true;
            }

            PlayerStatistic statistic = Main.getInstance()
                    .getPlayerStatisticManager().getStatistic(offPlayer);
            if (statistic == null) {
                player.sendMessage(ChatWriter.pluginMessage(ChatColor.RED
                        + Main._l("stats.statsnotfound",
                                ImmutableMap.of("player", offPlayer.getName()))));
                return true;
            }

            this.sendStats(player, statistic);
            return true;
        } else if (args.size() == 0) {
            PlayerStatistic statistic = Main.getInstance()
                    .getPlayerStatisticManager().getStatistic(player);
            if (statistic == null) {
                player.sendMessage(ChatWriter.pluginMessage(ChatColor.RED
                        + Main._l("stats.statsnotfound",
                                ImmutableMap.of("player", player.getName()))));
                return true;
            }

            this.sendStats(player, statistic);
            return true;
        }

        return false;
    }

    private void sendStats(Player player, PlayerStatistic statistic) {
        HashMap<StatField, Method> values = new HashMap<StatField, Method>();
        List<StatField> ordered = new ArrayList<StatField>();
        
        for (Method method : statistic.getClass().getMethods()) {
            if(!method.isAnnotationPresent(StatField.class)) {
                continue;
            }
            
            StatField stat = method.getAnnotation(StatField.class);
            if(stat != null) {
                values.put(stat, method);
                ordered.add(stat);
            }
        }
        
        Comparator<StatField> statComparator = null;
        statComparator = new Comparator<StatField>() {

            @Override
            public int compare(StatField o1, StatField o2) {
                return Integer.valueOf(o1.order()).compareTo(Integer.valueOf(o2.order()));
            }
        };
        
        Collections.sort(ordered, statComparator);
        
        for(StatField statField : ordered) {
            Method valueMethod = values.get(statField);
            
            try {
                player.sendMessage(ChatWriter.pluginMessage(ChatColor.GRAY
                        + Main._l("stats." + statField.name()) + ": "
                        + ChatColor.YELLOW + valueMethod.invoke(statistic, new Object[]{})));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public String getPermission() {
        return "base";
    }

}
