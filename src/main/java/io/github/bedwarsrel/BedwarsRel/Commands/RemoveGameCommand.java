package io.github.bedwarsrel.BedwarsRel.Commands;

import java.util.ArrayList;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import com.google.common.collect.ImmutableMap;

import io.github.bedwarsrel.BedwarsRel.Main;
import io.github.bedwarsrel.BedwarsRel.Game.Game;
import io.github.bedwarsrel.BedwarsRel.Game.GameState;
import io.github.bedwarsrel.BedwarsRel.Utils.ChatWriter;

public class RemoveGameCommand extends BaseCommand {

  public RemoveGameCommand(Main plugin) {
    super(plugin);
  }

  @Override
  public String getCommand() {
    return "removegame";
  }

  @Override
  public String getName() {
    return Main._l("commands.removegame.name");
  }

  @Override
  public String getDescription() {
    return Main._l("commands.removegame.desc");
  }

  @Override
  public String[] getArguments() {
    return new String[] {"game"};
  }

  @Override
  public boolean execute(CommandSender sender, ArrayList<String> args) {
    if (!sender.hasPermission("bw." + this.getPermission())) {
      return false;
    }

    Game game = this.getPlugin().getGameManager().getGame(args.get(0));

    if (game == null) {
      sender.sendMessage(ChatWriter.pluginMessage(ChatColor.RED
          + Main._l(sender, "errors.gamenotfound", ImmutableMap.of("game", args.get(0).toString()))));
      return false;
    }

    if (game.getState() == GameState.RUNNING) {
      sender.sendMessage(
          ChatWriter.pluginMessage(ChatColor.RED + Main._l(sender, "errors.notwhilegamerunning")));
      return false;
    }

    Main.getInstance().getGameManager().unloadGame(game);
    Main.getInstance().getGameManager().removeGame(game);
    sender.sendMessage(ChatWriter.pluginMessage(ChatColor.GREEN + Main._l(sender, "success.gameremoved")));
    return true;
  }

  @Override
  public String getPermission() {
    return "setup";
  }

}
