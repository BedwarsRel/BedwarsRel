package io.github.yannici.bedwars.Commands;

import java.util.ArrayList;
import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.google.common.collect.ImmutableMap;

import io.github.yannici.bedwars.ChatWriter;
import io.github.yannici.bedwars.Main;
import io.github.yannici.bedwars.UUIDFetcher;
import io.github.yannici.bedwars.Statistics.PlayerStatistic;

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

		player.sendMessage(
				ChatWriter.pluginMessage(ChatColor.GREEN + "----------- " + Main._l("stats.header") + " -----------"));

		if (args.size() == 1) {
			String playerStats = args.get(0).toString();
			OfflinePlayer offPlayer = Main.getInstance().getServer().getPlayerExact(playerStats);

			if (offPlayer != null) {
				player.sendMessage(ChatWriter.pluginMessage(
						ChatColor.GRAY + Main._l("stats.name") + ": " + ChatColor.YELLOW + offPlayer.getName()));
				PlayerStatistic statistic = Main.getInstance().getPlayerStatisticManager().getStatistic(offPlayer);
				if (statistic == null) {
					player.sendMessage(ChatWriter.pluginMessage(
							ChatColor.RED + Main._l("stats.statsnotfound", ImmutableMap.of("player", playerStats))));
					return true;
				}

				this.sendStats(player, statistic);
				return true;
			}

			UUID offUUID = null;
			try {
				offUUID = UUIDFetcher.getUUIDOf(playerStats);
				if (offUUID == null) {
					player.sendMessage(ChatWriter.pluginMessage(
							ChatColor.RED + Main._l("stats.statsnotfound", ImmutableMap.of("player", playerStats))));
					return true;
				}
			} catch (Exception e) {
				e.printStackTrace();
			}

			offPlayer = Main.getInstance().getServer().getOfflinePlayer(offUUID);
			if (offPlayer == null) {
				player.sendMessage(ChatWriter.pluginMessage(
						ChatColor.RED + Main._l("stats.statsnotfound", ImmutableMap.of("player", playerStats))));
				return true;
			}

			PlayerStatistic statistic = Main.getInstance().getPlayerStatisticManager().getStatistic(offPlayer);
			if (statistic == null) {
				player.sendMessage(ChatWriter.pluginMessage(ChatColor.RED
						+ Main._l("stats.statsnotfound", ImmutableMap.of("player", offPlayer.getName()))));
				return true;
			}

			this.sendStats(player, statistic);
			return true;
		} else if (args.size() == 0) {
			PlayerStatistic statistic = Main.getInstance().getPlayerStatisticManager().getStatistic(player);
			if (statistic == null) {
				player.sendMessage(ChatWriter.pluginMessage(
						ChatColor.RED + Main._l("stats.statsnotfound", ImmutableMap.of("player", player.getName()))));
				return true;
			}

			this.sendStats(player, statistic);
			return true;
		}

		return false;
	}

	private void sendStats(Player player, PlayerStatistic statistic) {
		for (String line : statistic.createStatisticLines(false, ChatColor.GRAY, ChatColor.YELLOW)) {
			player.sendMessage(line);
		}
	}

	@Override
	public String getPermission() {
		return "base";
	}

}
