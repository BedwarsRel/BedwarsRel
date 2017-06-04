package io.github.bedwarsrel.commands;

import io.github.bedwarsrel.BedwarsRel;
import io.github.bedwarsrel.updater.ConfigUpdater;
import io.github.bedwarsrel.utils.ChatWriter;
import java.io.File;
import java.util.ArrayList;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

public class ReloadCommand extends BaseCommand {

  public ReloadCommand(BedwarsRel plugin) {
    super(plugin);
  }

  @Override
  public boolean execute(CommandSender sender, ArrayList<String> args) {
    if (!sender.hasPermission(this.getPermission())) {
      return false;
    }

    File config = new File(BedwarsRel.getInstance().getDataFolder(), "config.yml");
    String command = "";

    if (args.size() > 0) {
      command = args.get(0);
    } else {
      command = "all";
    }

    if (command.equalsIgnoreCase("all")) {
      // save default config
      if (!config.exists()) {
        BedwarsRel.getInstance().saveDefaultConfig();
      }

      BedwarsRel.getInstance().loadConfigInUTF();

      BedwarsRel.getInstance().getConfig().options().copyDefaults(true);
      BedwarsRel.getInstance().getConfig().options().copyHeader(true);

      ConfigUpdater configUpdater = new ConfigUpdater();
      configUpdater.addConfigs();
      BedwarsRel.getInstance().saveConfiguration();
      BedwarsRel.getInstance().loadConfigInUTF();
      BedwarsRel.getInstance().loadShop();

      if (BedwarsRel.getInstance().isHologramsEnabled()
          && BedwarsRel.getInstance().getHolographicInteractor() != null) {
        BedwarsRel.getInstance().getHolographicInteractor().loadHolograms();
      }

      BedwarsRel.getInstance().reloadLocalization();
      BedwarsRel.getInstance().getGameManager().reloadGames();
    } else if (command.equalsIgnoreCase("shop")) {
      BedwarsRel.getInstance().loadShop();
    } else if (command.equalsIgnoreCase("games")) {
      BedwarsRel.getInstance().getGameManager().reloadGames();
    } else if (command.equalsIgnoreCase("holo")) {
      if (BedwarsRel.getInstance().isHologramsEnabled()) {
        BedwarsRel.getInstance().getHolographicInteractor().loadHolograms();
      }
    } else if (command.equalsIgnoreCase("config")) {
      // save default config
      if (!config.exists()) {
        BedwarsRel.getInstance().saveDefaultConfig();
      }

      BedwarsRel.getInstance().loadConfigInUTF();

      BedwarsRel.getInstance().getConfig().options().copyDefaults(true);
      BedwarsRel.getInstance().getConfig().options().copyHeader(true);

      ConfigUpdater configUpdater = new ConfigUpdater();
      configUpdater.addConfigs();
      BedwarsRel.getInstance().saveConfiguration();
      BedwarsRel.getInstance().loadConfigInUTF();
    } else if (command.equalsIgnoreCase("locale")) {
      BedwarsRel.getInstance().reloadLocalization();
    } else {
      return false;
    }

    sender.sendMessage(
        ChatWriter.pluginMessage(ChatColor.GREEN + BedwarsRel._l(sender, "success.reloadconfig")));
    return true;
  }

  @Override
  public String[] getArguments() {
    return new String[]{};
  }

  @Override
  public String getCommand() {
    return "reload";
  }

  @Override
  public String getDescription() {
    return BedwarsRel._l("commands.reload.desc");
  }

  @Override
  public String getName() {
    return BedwarsRel._l("commands.reload.name");
  }

  @Override
  public String getPermission() {
    return "setup";
  }

}
