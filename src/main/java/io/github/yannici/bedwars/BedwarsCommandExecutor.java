package io.github.yannici.bedwars;

import java.util.ArrayList;
import java.util.Arrays;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import io.github.yannici.bedwars.Commands.BaseCommand;
import io.github.yannici.bedwars.Events.BedwarsCommandExecutedEvent;
import io.github.yannici.bedwars.Events.BedwarsExecuteCommandEvent;

public class BedwarsCommandExecutor implements CommandExecutor {

	private Main plugin = null;

	public BedwarsCommandExecutor(Main plugin) {
		super();

		this.plugin = plugin;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
		if (!cmd.getName().equalsIgnoreCase("bw")) {
			return false;
		}

		if (args.length < 1) {
			return false;
		}

		String command = args[0].toString();
		ArrayList<String> arguments = new ArrayList<String>(Arrays.asList(args));
		arguments.remove(0);

		for (BaseCommand bCommand : this.plugin.getCommands()) {
			if (bCommand.getCommand().equalsIgnoreCase(command)) {
				if (bCommand.getArguments().length > arguments.size()) {
					sender.sendMessage(ChatWriter.pluginMessage(ChatColor.RED + Main._l("errors.argumentslength")));
					return false;
				}

				BedwarsExecuteCommandEvent commandEvent = new BedwarsExecuteCommandEvent(sender, bCommand, arguments);
				Main.getInstance().getServer().getPluginManager().callEvent(commandEvent);

				if (commandEvent.isCancelled()) {
					return true;
				}

				boolean result = bCommand.execute(sender, arguments);

				BedwarsCommandExecutedEvent executedEvent = new BedwarsCommandExecutedEvent(sender, bCommand, arguments,
						result);
				Main.getInstance().getServer().getPluginManager().callEvent(executedEvent);

				return result;
			}
		}

		return false;
	}

}
