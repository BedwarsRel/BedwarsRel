package io.github.yannici.bedwars.Commands;

import java.util.ArrayList;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import io.github.yannici.bedwars.Main;
import io.github.yannici.bedwars.Game.Game;

public class LeaveGameCommand extends BaseCommand {

	public LeaveGameCommand(Main plugin) {
		super(plugin);
	}

	@Override
	public String getCommand() {
		return "leave";
	}

	@Override
	public String getName() {
		return Main._l("commands.leave.name");
	}

	@Override
	public String getDescription() {
		return Main._l("commands.leave.desc");
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
		Game game = Main.getInstance().getGameManager().getGameOfPlayer(player);

		if (game == null) {
			return true;
		}

		game.playerLeave(player, false);
		return true;
	}

	@Override
	public String getPermission() {
		return "base";
	}

}
