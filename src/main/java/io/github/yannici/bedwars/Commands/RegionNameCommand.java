package io.github.yannici.bedwars.Commands;

import java.util.ArrayList;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.google.common.collect.ImmutableMap;

import io.github.yannici.bedwars.ChatWriter;
import io.github.yannici.bedwars.Main;
import io.github.yannici.bedwars.Game.Game;
import io.github.yannici.bedwars.Game.GameState;

public class RegionNameCommand extends BaseCommand implements ICommand {

	public RegionNameCommand(Main plugin) {
		super(plugin);
	}

	@Override
	public String getCommand() {
		return "regionname";
	}

	@Override
	public String getName() {
		return Main._l("commands.regionname.name");
	}

	@Override
	public String getDescription() {
		return Main._l("commands.regionname.desc");
	}

	@Override
	public String[] getArguments() {
		return new String[] { "game", "name" };
	}

	@Override
	public boolean execute(CommandSender sender, ArrayList<String> args) {
		if (!sender.hasPermission("bw." + this.getPermission())) {
			return false;
		}

		Player player = (Player) sender;

		Game game = this.getPlugin().getGameManager().getGame(args.get(0));
		String name = args.get(1).toString();

		if (game == null) {
			player.sendMessage(ChatWriter.pluginMessage(
					ChatColor.RED + Main._l("errors.gamenotfound", ImmutableMap.of("game", args.get(0).toString()))));
			return false;
		}

		if (game.getState() == GameState.RUNNING) {
			sender.sendMessage(ChatWriter.pluginMessage(ChatColor.RED + Main._l("errors.notwhilegamerunning")));
			return false;
		}

		if (name.length() > 15) {
			player.sendMessage(ChatWriter.pluginMessage(ChatColor.RED + Main._l("errors.toolongregionname")));
			return true;
		}

		game.setRegionName(name);
		player.sendMessage(ChatWriter.pluginMessage(ChatColor.GREEN + Main._l("success.regionnameset")));
		return true;
	}

	@Override
	public String getPermission() {
		return "setup";
	}

}
