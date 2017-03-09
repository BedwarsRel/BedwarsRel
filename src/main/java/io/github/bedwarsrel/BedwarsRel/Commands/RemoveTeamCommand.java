package io.github.bedwarsrel.BedwarsRel.Commands;

import com.google.common.collect.ImmutableMap;
import io.github.bedwarsrel.BedwarsRel.Game.Game;
import io.github.bedwarsrel.BedwarsRel.Game.GameState;
import io.github.bedwarsrel.BedwarsRel.Game.Team;
import io.github.bedwarsrel.BedwarsRel.Main;
import io.github.bedwarsrel.BedwarsRel.Utils.ChatWriter;
import java.util.ArrayList;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

public class RemoveTeamCommand extends BaseCommand {

  public RemoveTeamCommand(Main plugin) {
    super(plugin);
  }

  @Override
  public boolean execute(CommandSender sender, ArrayList<String> args) {
    if (!sender.hasPermission("bw." + this.getPermission())) {
      return false;
    }

    Game game = this.getPlugin().getGameManager().getGame(args.get(0));
    String name = args.get(1);

    if (game == null) {
      sender.sendMessage(ChatWriter.pluginMessage(ChatColor.RED
          + Main
          ._l(sender, "errors.gamenotfound", ImmutableMap.of("game", args.get(0).toString()))));
      return false;
    }

    if (game.getState() != GameState.STOPPED) {
      sender.sendMessage(
          ChatWriter.pluginMessage(ChatColor.RED + Main._l(sender, "errors.notwhilegamerunning")));
      return false;
    }

    Team theTeam = game.getTeam(name);
    if (theTeam == null) {
      sender.sendMessage(
          ChatWriter.pluginMessage(ChatColor.RED + Main._l(sender, "errors.teamnotfound")));
      return false;
    }

    game.removeTeam(theTeam);
    sender.sendMessage(
        ChatWriter.pluginMessage(ChatColor.GREEN + Main._l(sender, "success.teamremoved")));
    return true;
  }

  @Override
  public String[] getArguments() {
    return new String[]{"game", "name"};
  }

  @Override
  public String getCommand() {
    return "removeteam";
  }

  @Override
  public String getDescription() {
    return Main._l("commands.removeteam.desc");
  }

  @Override
  public String getName() {
    return Main._l("commands.removeteam.name");
  }

  @Override
  public String getPermission() {
    return "setup";
  }

}
