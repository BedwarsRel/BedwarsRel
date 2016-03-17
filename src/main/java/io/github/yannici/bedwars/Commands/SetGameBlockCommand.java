package io.github.yannici.bedwars.Commands;

import java.util.ArrayList;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;

import com.google.common.collect.ImmutableMap;

import io.github.yannici.bedwars.ChatWriter;
import io.github.yannici.bedwars.Main;
import io.github.yannici.bedwars.Utils;
import io.github.yannici.bedwars.Game.Game;
import io.github.yannici.bedwars.Game.GameState;

public class SetGameBlockCommand extends BaseCommand implements ICommand {

	public SetGameBlockCommand(Main plugin) {
		super(plugin);
	}

	@Override
	public String getCommand() {
		return "setgameblock";
	}

	@Override
	public String getName() {
		return Main._l("commands.setgameblock.name");
	}

	@Override
	public String getDescription() {
		return Main._l("commands.setgameblock.desc");
	}

	@Override
	public String[] getArguments() {
		return new String[] { "game", "blocktype" };
	}

	@Override
	public boolean execute(CommandSender sender, ArrayList<String> args) {
		if (!sender.hasPermission("bw." + this.getPermission())) {
			return false;
		}

		Game game = this.getPlugin().getGameManager().getGame(args.get(0));
		String material = args.get(1).toString();

		if (game == null) {
			sender.sendMessage(ChatWriter.pluginMessage(
					ChatColor.RED + Main._l("errors.gamenotfound", ImmutableMap.of("game", args.get(0).toString()))));
			return false;
		}

		if (game.getState() == GameState.RUNNING) {
			sender.sendMessage(ChatWriter.pluginMessage(ChatColor.RED + Main._l("errors.notwhilegamerunning")));
			return false;
		}

		Material targetMaterial = Utils.parseMaterial(material);
		if (targetMaterial == null && !material.equals("DEFAULT")) {
			sender.sendMessage(ChatWriter.pluginMessage(ChatColor.RED + Main._l("errors.novalidmaterial")));
			return true;
		}

		if (material.equalsIgnoreCase("DEFAULT")) {
			game.setTargetMaterial(null);
		} else {
			game.setTargetMaterial(targetMaterial);
		}

		sender.sendMessage(ChatWriter.pluginMessage(ChatColor.GREEN + Main._l("success.materialset")));
		return true;
	}

	@Override
	public String getPermission() {
		return "setup";
	}

}
