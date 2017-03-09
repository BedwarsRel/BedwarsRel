package io.github.bedwarsrel.BedwarsRel.Commands;

import java.util.ArrayList;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.google.common.collect.ImmutableMap;

import io.github.bedwarsrel.BedwarsRel.Main;
import io.github.bedwarsrel.BedwarsRel.Game.Game;
import io.github.bedwarsrel.BedwarsRel.Game.GameState;
import io.github.bedwarsrel.BedwarsRel.Game.Team;
import io.github.bedwarsrel.BedwarsRel.Utils.ChatWriter;

public class SetSpawnCommand extends BaseCommand implements ICommand {

  public SetSpawnCommand(Main plugin) {
    super(plugin);
  }

  @Override
  public String getCommand() {
    return "setspawn";
  }

  @Override
  public String getName() {
    return Main._l("commands.setspawn.name");
  }

  @Override
  public String getDescription() {
    return Main._l("commands.setspawn.desc");
  }

  @Override
  public String[] getArguments() {
    return new String[] {"game", "team"};
  }

  @Override
  public boolean execute(CommandSender sender, ArrayList<String> args) {
    if (!super.hasPermission(sender)) {
      return false;
    }

    Player player = (Player) sender;

    Game game = this.getPlugin().getGameManager().getGame(args.get(0));
    if (game == null) {
      player.sendMessage(ChatWriter.pluginMessage(ChatColor.RED
          + Main._l(player, "errors.gamenotfound", ImmutableMap.of("game", args.get(0).toString()))));
      return false;
    }

    if (game.getState() == GameState.RUNNING) {
      sender.sendMessage(
          ChatWriter.pluginMessage(ChatColor.RED + Main._l(sender, "errors.notwhilegamerunning")));
      return false;
    }

    Team team = game.getTeam(args.get(1));
    if (team == null) {
      player.sendMessage(ChatWriter.pluginMessage(ChatColor.RED + Main._l(player,"errors.teamnotfound")));
      return false;
    }

    team.setSpawnLocation(player.getLocation());
    player.sendMessage(ChatWriter.pluginMessage(ChatColor.GREEN + Main._l(player, "success.spawnset",
        ImmutableMap.of("team", team.getChatColor() + team.getDisplayName() + ChatColor.GREEN))));
    return true;
  }

  @Override
  public String getPermission() {
    return "setup";
  }

}
