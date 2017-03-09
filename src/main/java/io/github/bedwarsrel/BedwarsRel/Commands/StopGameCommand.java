package io.github.bedwarsrel.BedwarsRel.Commands;

import com.google.common.collect.ImmutableMap;
import io.github.bedwarsrel.BedwarsRel.Game.Game;
import io.github.bedwarsrel.BedwarsRel.Main;
import io.github.bedwarsrel.BedwarsRel.Utils.ChatWriter;
import java.util.ArrayList;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class StopGameCommand extends BaseCommand implements ICommand {

  public StopGameCommand(Main plugin) {
    super(plugin);
  }

  @Override
  public boolean execute(CommandSender sender, ArrayList<String> args) {
    if (!sender.hasPermission("bw." + this.getPermission())) {
      return false;
    }

    Game game = null;

    if (args.size() == 0) {
      game = this.getPlugin().getGameManager().getGameOfPlayer((Player) sender);

      if (game == null) {
        sender.sendMessage(
            ChatWriter.pluginMessage(ChatColor.RED + Main._l(sender, "errors.notingame")));
        return false;
      }
    }

    if (args.size() != 0) {
      game = this.getPlugin().getGameManager().getGame(args.get(0));

      if (game == null) {
        sender.sendMessage(ChatWriter.pluginMessage(ChatColor.RED
            + Main
            ._l(sender, "errors.gamenotfound", ImmutableMap.of("game", args.get(0).toString()))));
        return false;
      }
    }

    if (!game.stop()) {
      sender
          .sendMessage(
              ChatWriter.pluginMessage(ChatColor.RED + Main._l(sender, "errors.gamenotrunning")));
      return false;
    }

    sender.sendMessage(
        ChatWriter.pluginMessage(ChatColor.GREEN + Main._l(sender, "success.stopped")));
    return true;
  }

  @Override
  public String[] getArguments() {
    return new String[]{};
  }

  @Override
  public String getCommand() {
    return "stop";
  }

  @Override
  public String getDescription() {
    return Main._l("commands.stop.desc");
  }

  @Override
  public String getName() {
    return Main._l("commands.stop.name");
  }

  @Override
  public String getPermission() {
    return "setup";
  }

}
