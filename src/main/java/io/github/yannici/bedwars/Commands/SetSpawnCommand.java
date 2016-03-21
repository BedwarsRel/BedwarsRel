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
import io.github.yannici.bedwars.Game.Team;

public class SetSpawnCommand extends BaseCommand implements ICommand {

	public SetSpawnCommand(Main plugin) {
		super(plugin);
	}

	@Override
	public String getCommand() {
		return "setspawn";
	}

	@Override
	public String getName() {
		return Main._l("commands.setspawn.name");
	}

	@Override
	public String getDescription() {
		return Main._l("commands.setspawn.desc");
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

		Team team = game.getTeam(args.get(1));
		if (team == null) {
			player.sendMessage(ChatWriter.pluginMessage(ChatColor.RED + Main._l("errors.teamnotfound")));
			return false;
		}

		team.setSpawnLocation(player.getLocation());
		player.sendMessage(ChatWriter.pluginMessage(ChatColor.GREEN + Main._l("success.spawnset",
				ImmutableMap.of("team", team.getChatColor() + team.getDisplayName() + ChatColor.GREEN))));
		return true;
	}

	@Override
	public String getPermission() {
		return "setup";
	}

}
