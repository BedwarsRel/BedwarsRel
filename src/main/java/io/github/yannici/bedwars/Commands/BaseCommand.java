package io.github.yannici.bedwars.Commands;

import java.util.ArrayList;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import io.github.yannici.bedwars.ChatWriter;
import io.github.yannici.bedwars.Main;

public abstract class BaseCommand implements ICommand {

	private Main plugin = null;

	public BaseCommand(Main plugin) {
		this.plugin = plugin;
	}

	protected Main getPlugin() {
		return this.plugin;
	}

	@Override
	public abstract String getCommand();

	@Override
	public abstract String getName();

	@Override
	public abstract String getDescription();

	@Override
	public abstract String[] getArguments();

	@Override
	public abstract boolean execute(CommandSender sender, ArrayList<String> args);

	@Override
	public boolean hasPermission(CommandSender sender) {
		if (!(sender instanceof Player)) {
			sender.sendMessage(ChatWriter.pluginMessage("Only players should execute this command!"));
			return false;
		}

		if (!sender.hasPermission("bw." + this.getPermission())) {
			sender.sendMessage(
					ChatWriter.pluginMessage(ChatColor.RED + "You don't have permission to execute this command!"));
			return false;
		}

		return true;
	}

}
