package io.github.yannici.bedwars.Commands;

import java.util.ArrayList;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import com.google.common.collect.ImmutableMap;

import io.github.yannici.bedwars.ChatWriter;
import io.github.yannici.bedwars.Main;
import io.github.yannici.bedwars.Game.Game;
import io.github.yannici.bedwars.Game.GameState;
import io.github.yannici.bedwars.Game.Team;
import io.github.yannici.bedwars.Game.TeamJoinMetaDataValue;

public class AddTeamJoinCommand extends BaseCommand {

	public AddTeamJoinCommand(Main plugin) {
		super(plugin);
	}

	@Override
	public String getPermission() {
		return "setup";
	}

	@Override
	public String getCommand() {
		return "addteamjoin";
	}

	@Override
	public String getName() {
		return Main._l("commands.addteamjoin.name");
	}

	@Override
	public String getDescription() {
		return Main._l("commands.addteamjoin.desc");
	}

	@Override
	public String[] getArguments() {
		return new String[] { "game", "team" };
	}

	@Override
	public boolean execute(CommandSender sender, ArrayList<String> args) {
		if (!super.hasPermission(sender)) {
			return false;
		}

		Player player = (Player) sender;
		String team = args.get(1);

		Game game = this.getPlugin().getGameManager().getGame(args.get(0));
		if (game == null) {
			player.sendMessage(ChatWriter.pluginMessage(
					ChatColor.RED + Main._l("errors.gamenotfound", ImmutableMap.of("game", args.get(0).toString()))));
			return false;
		}

		if (game.getState() == GameState.RUNNING) {
			sender.sendMessage(ChatWriter.pluginMessage(ChatColor.RED + Main._l("errors.notwhilegamerunning")));
			return false;
		}

		Team gameTeam = game.getTeam(team);

		if (gameTeam == null) {
			player.sendMessage(ChatWriter.pluginMessage(ChatColor.RED + Main._l("errors.teamnotfound")));
			return false;
		}

		// only in lobby
		if (game.getLobby() == null || !player.getWorld().equals(game.getLobby().getWorld())) {
			player.sendMessage(ChatWriter.pluginMessage(ChatColor.RED + Main._l("errors.mustbeinlobbyworld")));
			return false;
		}

		if (player.hasMetadata("bw-addteamjoin")) {
			player.removeMetadata("bw-addteamjoin", Main.getInstance());
		}

		player.setMetadata("bw-addteamjoin", new TeamJoinMetaDataValue(gameTeam));
		final Player runnablePlayer = player;

		new BukkitRunnable() {

			@Override
			public void run() {
				try {
					if (!runnablePlayer.hasMetadata("bw-addteamjoin")) {
						return;
					}

					runnablePlayer.removeMetadata("bw-addteamjoin", Main.getInstance());
				} catch (Exception ex) {
					// just ignore
				}
			}
		}.runTaskLater(Main.getInstance(), 20L * 10L);

		player.sendMessage(ChatWriter.pluginMessage(ChatColor.GREEN + Main._l("success.selectteamjoinentity")));
		return true;
	}

}
