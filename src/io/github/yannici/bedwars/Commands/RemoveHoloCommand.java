package io.github.yannici.bedwars.Commands;

import java.util.ArrayList;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;

import io.github.yannici.bedwars.ChatWriter;
import io.github.yannici.bedwars.Main;

public class RemoveHoloCommand extends BaseCommand implements ICommand {

	public RemoveHoloCommand(Main plugin) {
		super(plugin);
	}

	@Override
	public String getCommand() {
		return "removeholo";
	}

	@Override
	public String getName() {
		return Main._l("commands.removeholo.name");
	}

	@Override
	public String getDescription() {
		return Main._l("commands.removeholo.desc");
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

		final Player player = (Player) sender;
		player.setMetadata("bw-remove-holo", new FixedMetadataValue(Main.getInstance(), true));
		player.sendMessage(ChatWriter.pluginMessage(ChatColor.GREEN + Main._l("commands.removeholo.explain")));

		Main.getInstance().getServer().getScheduler().runTaskLater(Main.getInstance(), new Runnable() {

			@Override
			public void run() {
				if (player.hasMetadata("bw-remove-holo")) {
					player.removeMetadata("bw-remove-holo", Main.getInstance());
				}
			}

		}, 10L * 20L);
		return true;
	}

	@Override
	public String getPermission() {
		return "setup";
	}

}
