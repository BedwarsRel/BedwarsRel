package io.github.yannici.bedwars.Commands;

import java.util.ArrayList;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import com.google.common.collect.ImmutableMap;

import io.github.yannici.bedwars.ChatWriter;
import io.github.yannici.bedwars.Main;
import io.github.yannici.bedwars.Game.Game;
import io.github.yannici.bedwars.Game.GameState;
import io.github.yannici.bedwars.Game.TeamColor;

public class AddTeamCommand extends BaseCommand {

	public AddTeamCommand(Main plugin) {
		super(plugin);
	}

	@Override
	public String getCommand() {
		return "addteam";
	}

	@Override
	public String getName() {
		return Main._l("commands.addteam.name");
	}

	@Override
	public String getDescription() {
		return Main._l("commands.addteam.desc");
	}

	@Override
	public String[] getArguments() {
		return new String[] { "game", "name", "color", "maxplayers" };
	}

	@Override
	public boolean execute(CommandSender sender, ArrayList<String> args) {
		if (!sender.hasPermission("bw." + this.getPermission())) {
			return false;
		}

		Game game = this.getPlugin().getGameManager().getGame(args.get(0));
		String name = args.get(1);
		String color = args.get(2);
		String maxPlayers = args.get(3);

		TeamColor tColor = TeamColor.valueOf(color.toUpperCase());

		if (game == null) {
			sender.sendMessage(ChatWriter.pluginMessage(
					ChatColor.RED + Main._l("errors.gamenotfound", ImmutableMap.of("game", args.get(0).toString()))));
			return false;
		}

		if (game.getState() != GameState.STOPPED) {
			sender.sendMessage(ChatWriter.pluginMessage(ChatColor.RED + Main._l("errors.notwhilegamerunning")));
			return false;
		}

		int playerMax = Integer.parseInt(maxPlayers);

		if (playerMax < 1 || playerMax > 24) {
			sender.sendMessage(ChatWriter.pluginMessage(ChatColor.RED + Main._l("errors.playeramount")));
			return false;
		}

		if (tColor == null) {
			sender.sendMessage(ChatWriter.pluginMessage(ChatColor.RED + Main._l("errors.teamcolornotallowed")));
			return false;
		}

		if (name.length() < 3 || name.length() > 20) {
			sender.sendMessage(ChatWriter.pluginMessage(ChatColor.RED + Main._l("errors.teamnamelength")));
			return false;
		}

		if (game.getTeam(name) != null) {
			sender.sendMessage(ChatWriter.pluginMessage(ChatColor.RED + Main._l("errors.teamnameinuse")));
			return false;
		}

		game.addTeam(name, tColor, playerMax);
		sender.sendMessage(ChatWriter
				.pluginMessage(ChatColor.GREEN + Main._l("success.teamadded", ImmutableMap.of("team", name))));
		return true;
	}

	@Override
	public String getPermission() {
		return "setup";
	}

}
