package io.github.yannici.bedwars.Commands;

import java.util.ArrayList;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import io.github.yannici.bedwars.ChatWriter;
import io.github.yannici.bedwars.Main;
import io.github.yannici.bedwars.Game.Game;

public class KickCommand extends BaseCommand implements ICommand {

	public KickCommand(Main plugin) {
		super(plugin);
	}

	@Override
	public String getCommand() {
		return "kick";
	}

	@Override
	public String getName() {
		return Main._l("commands.kick.name");
	}

	@Override
	public String getDescription() {
		return Main._l("commands.kick.desc");
	}

	@Override
	public String[] getArguments() {
		return new String[] { "player" };
	}

	@Override
	public boolean execute(CommandSender sender, ArrayList<String> args) {
		if (!super.hasPermission(sender) && !sender.isOp()) {
			return false;
		}

		Player player = (Player) sender;
		Game game = Main.getInstance().getGameManager().getGameOfPlayer(player);

		// find player
		Player kickPlayer = Main.getInstance().getServer().getPlayer(args.get(0).toString());

		if (game == null) {
			player.sendMessage(ChatWriter.pluginMessage(Main._l("errors.notingameforkick")));
			return true;
		}

		if (kickPlayer == null || !kickPlayer.isOnline()) {
			player.sendMessage(ChatWriter.pluginMessage(Main._l("errors.playernotfound")));
			return true;
		}

		if (!game.isInGame(kickPlayer)) {
			player.sendMessage(ChatWriter.pluginMessage(Main._l("errors.playernotingame")));
			return true;
		}

		game.playerLeave(kickPlayer, true);
		return true;
	}

	@Override
	public String getPermission() {
		return "kick";
	}

}
