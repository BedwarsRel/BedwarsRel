package io.github.yannici.bedwars.Commands;

import java.io.File;
import java.util.ArrayList;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import io.github.yannici.bedwars.ChatWriter;
import io.github.yannici.bedwars.Main;
import io.github.yannici.bedwars.Updater.ConfigUpdater;

public class ReloadCommand extends BaseCommand {

	public ReloadCommand(Main plugin) {
		super(plugin);
	}

	@Override
	public String getPermission() {
		return "setup";
	}

	@Override
	public String getCommand() {
		return "reload";
	}

	@Override
	public String getName() {
		return Main._l("commands.reload.name");
	}

	@Override
	public String getDescription() {
		return Main._l("commands.reload.desc");
	}

	@Override
	public String[] getArguments() {
		return new String[] {};
	}

	@Override
	public boolean execute(CommandSender sender, ArrayList<String> args) {
		if (!sender.hasPermission(this.getPermission())) {
			return false;
		}

		File config = new File(Main.getInstance().getDataFolder(), "config.yml");
		String command = "";

		if (args.size() > 0) {
			command = args.get(0);
		} else {
			command = "all";
		}

		if (command.equalsIgnoreCase("all")) {
			// save default config
			if (!config.exists()) {
				Main.getInstance().saveDefaultConfig();
			}

			Main.getInstance().loadConfigInUTF();

			Main.getInstance().getConfig().options().copyDefaults(true);
			Main.getInstance().getConfig().options().copyHeader(true);

			ConfigUpdater configUpdater = new ConfigUpdater();
			configUpdater.addConfigs();
			Main.getInstance().saveConfiguration();
			Main.getInstance().loadConfigInUTF();
			Main.getInstance().loadShop();

			if (Main.getInstance().isHologramsEnabled() && Main.getInstance().getHolographicInteractor() != null) {
				Main.getInstance().getHolographicInteractor().loadHolograms();
			}

			Main.getInstance().reloadLocalization();
			Main.getInstance().getGameManager().reloadGames();
		} else if (command.equalsIgnoreCase("shop")) {
			Main.getInstance().loadShop();
		} else if (command.equalsIgnoreCase("games")) {
			Main.getInstance().getGameManager().reloadGames();
		} else if (command.equalsIgnoreCase("holo")) {
			if (Main.getInstance().isHologramsEnabled()) {
				Main.getInstance().getHolographicInteractor().loadHolograms();
			}
		} else if (command.equalsIgnoreCase("config")) {
			// save default config
			if (!config.exists()) {
				Main.getInstance().saveDefaultConfig();
			}

			Main.getInstance().loadConfigInUTF();

			Main.getInstance().getConfig().options().copyDefaults(true);
			Main.getInstance().getConfig().options().copyHeader(true);

			ConfigUpdater configUpdater = new ConfigUpdater();
			configUpdater.addConfigs();
			Main.getInstance().saveConfiguration();
			Main.getInstance().loadConfigInUTF();
		} else if (command.equalsIgnoreCase("locale")) {
			Main.getInstance().reloadLocalization();
		} else {
			return false;
		}

		sender.sendMessage(ChatWriter.pluginMessage(ChatColor.GREEN + Main._l("success.reloadconfig")));
		return true;
	}

}
