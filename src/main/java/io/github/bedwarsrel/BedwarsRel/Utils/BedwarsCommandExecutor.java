package io.github.bedwarsrel.BedwarsRel.Utils;

import io.github.bedwarsrel.BedwarsRel.Commands.BaseCommand;
import io.github.bedwarsrel.BedwarsRel.Events.BedwarsCommandExecutedEvent;
import io.github.bedwarsrel.BedwarsRel.Events.BedwarsExecuteCommandEvent;
import io.github.bedwarsrel.BedwarsRel.Main;
import java.util.ArrayList;
import java.util.Arrays;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class BedwarsCommandExecutor implements CommandExecutor {

  private Main plugin = null;

  public BedwarsCommandExecutor(Main plugin) {
    super();

    this.plugin = plugin;
  }

  @Override
  public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
    if (!cmd.getName().equals("bw")) {
      return false;
    }

    if (args.length < 1) {
      return false;
    }

    String command = args[0];
    ArrayList<String> arguments = new ArrayList<String>(Arrays.asList(args));
    arguments.remove(0);

    for (BaseCommand bCommand : this.plugin.getCommands()) {
      if (bCommand.getCommand().equalsIgnoreCase(command)) {
        if (bCommand.getArguments().length > arguments.size()) {
          sender.sendMessage(
              ChatWriter.pluginMessage(ChatColor.RED + Main._l(sender, "errors.argumentslength")));
          return false;
        }

        BedwarsExecuteCommandEvent commandEvent =
            new BedwarsExecuteCommandEvent(sender, bCommand, arguments);
        Main.getInstance().getServer().getPluginManager().callEvent(commandEvent);

        if (commandEvent.isCancelled()) {
          return true;
        }

        boolean result = bCommand.execute(sender, arguments);

        BedwarsCommandExecutedEvent executedEvent =
            new BedwarsCommandExecutedEvent(sender, bCommand, arguments, result);
        Main.getInstance().getServer().getPluginManager().callEvent(executedEvent);

        return result;
      }
    }

    return false;
  }

}
