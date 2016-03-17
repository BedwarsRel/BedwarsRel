package io.github.yannici.bedwars.Commands;

import java.util.ArrayList;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import com.google.common.collect.ImmutableMap;

import io.github.yannici.bedwars.ChatWriter;
import io.github.yannici.bedwars.Main;
import io.github.yannici.bedwars.Utils;
import io.github.yannici.bedwars.Game.Game;

public class AddGameCommand extends BaseCommand {

	public AddGameCommand(Main plugin) {
		super(plugin);
	}

	@Override
	public String getCommand() {
		return "addgame";
	}

	@Override
	public String getName() {
		return Main._l("commands.addgame.name");
	}

	@Override
	public String getDescription() {
		return Main._l("commands.addgame.desc");
	}

	@Override
	public String[] getArguments() {
		return new String[] { "name", "minplayers" };
	}

	@Override
	public boolean execute(CommandSender sender, ArrayList<String> args) {
		if (!sender.hasPermission("bw." + this.getPermission())) {
			return false;
		}

		Game addGame = this.getPlugin().getGameManager().addGame(args.get(0));
		String minPlayers = args.get(1);

		if (!Utils.isNumber(minPlayers)) {
			sender.sendMessage(ChatWriter.pluginMessage(ChatColor.RED + Main._l("errors.minplayersmustnumber")));
			return false;
		}

		if (addGame == null) {
			sender.sendMessage(ChatWriter.pluginMessage(ChatColor.RED + Main._l("errors.gameexists")));
			return false;
		}

		int min = Integer.parseInt(minPlayers);
		if (min <= 0) {
			min = 1;
		}

		addGame.setMinPlayers(min);
		sender.sendMessage(ChatWriter.pluginMessage(
				ChatColor.GREEN + Main._l("success.gameadded", ImmutableMap.of("game", args.get(0).toString()))));
		return true;
	}

	@Override
	public String getPermission() {
		return "setup";
	}

}
