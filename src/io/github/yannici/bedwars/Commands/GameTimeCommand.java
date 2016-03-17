package io.github.yannici.bedwars.Commands;

import java.util.ArrayList;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.google.common.collect.ImmutableMap;

import io.github.yannici.bedwars.ChatWriter;
import io.github.yannici.bedwars.Main;
import io.github.yannici.bedwars.Utils;
import io.github.yannici.bedwars.Game.Game;
import io.github.yannici.bedwars.Game.GameState;

public class GameTimeCommand extends BaseCommand implements ICommand {

	public GameTimeCommand(Main plugin) {
		super(plugin);
	}

	@Override
	public String getCommand() {
		return "gametime";
	}

	@Override
	public String getName() {
		return Main._l("commands.gametime.name");
	}

	@Override
	public String getDescription() {
		return Main._l("commands.gametime.desc");
	}

	@Override
	public String[] getArguments() {
		return new String[] { "game", "time" };
	}

	@Override
	public boolean execute(CommandSender sender, ArrayList<String> args) {
		if (!sender.hasPermission("bw." + this.getPermission())) {
			return false;
		}

		Player player = (Player) sender;

		Game game = this.getPlugin().getGameManager().getGame(args.get(0));
		String gametime = args.get(1).toString();

		if (game == null) {
			player.sendMessage(ChatWriter.pluginMessage(
					ChatColor.RED + Main._l("errors.gamenotfound", ImmutableMap.of("game", args.get(0).toString()))));
			return false;
		}

		if (game.getState() == GameState.RUNNING) {
			sender.sendMessage(ChatWriter.pluginMessage(ChatColor.RED + Main._l("errors.notwhilegamerunning")));
			return false;
		}

		if (!Utils.isNumber(gametime) && !gametime.equals("day") && !gametime.equals("night")) {
			player.sendMessage(ChatWriter.pluginMessage(ChatColor.RED + Main._l("errors.timeincorrect")));
			return true;
		}

		int time = 1000;
		if (gametime.equals("day")) {
			time = 6000;
		} else if (gametime.equals("night")) {
			time = 18000;
		} else {
			time = Integer.valueOf(gametime);
		}

		game.setTime(time);
		player.sendMessage(ChatWriter.pluginMessage(ChatColor.GREEN + Main._l("success.gametimeset")));
		return true;
	}

	@Override
	public String getPermission() {
		return "setup";
	}

}
