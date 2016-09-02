package io.github.bedwarsrel.BedwarsRel.Commands;

import java.util.ArrayList;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import com.google.common.collect.ImmutableMap;

import io.github.bedwarsrel.BedwarsRel.Main;
import io.github.bedwarsrel.BedwarsRel.Game.Game;
import io.github.bedwarsrel.BedwarsRel.Game.GameState;
import io.github.bedwarsrel.BedwarsRel.Utils.ChatWriter;

public class ClearSpawnerCommand extends BaseCommand implements ICommand {

  public ClearSpawnerCommand(Main plugin) {
    super(plugin);
  }

  @Override
  public String getCommand() {
    return "clearspawner";
  }

  @Override
  public String getName() {
    return Main._l("commands.clearspawner.name");
  }

  @Override
  public String getDescription() {
    return Main._l("commands.clearspawner.desc");
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
          + Main._l("errors.gamenotfound", ImmutableMap.of("game", args.get(0).toString()))));
      return false;
    }

    if (game.getState() == GameState.RUNNING) {
      sender.sendMessage(
          ChatWriter.pluginMessage(ChatColor.RED + Main._l("errors.notwhilegamerunning")));
      return false;
    }

    game.getRessourceSpawner().clear();
    sender
        .sendMessage(ChatWriter.pluginMessage(ChatColor.GREEN + Main._l("success.spawnercleared")));
    return true;
  }

  @Override
  public String getPermission() {
    return "setup";
  }

}
