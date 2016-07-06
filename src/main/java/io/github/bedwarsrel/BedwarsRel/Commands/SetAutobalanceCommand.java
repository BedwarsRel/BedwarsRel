package io.github.bedwarsrel.BedwarsRel.Commands;

import java.util.ArrayList;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.google.common.collect.ImmutableMap;

import io.github.bedwarsrel.BedwarsRel.ChatWriter;
import io.github.bedwarsrel.BedwarsRel.Main;
import io.github.bedwarsrel.BedwarsRel.Game.Game;
import io.github.bedwarsrel.BedwarsRel.Game.GameState;

public class SetAutobalanceCommand extends BaseCommand implements ICommand {

  public SetAutobalanceCommand(Main plugin) {
    super(plugin);
  }

  @Override
  public String getCommand() {
    return "setautobalance";
  }

  @Override
  public String getName() {
    return Main._l("commands.setautobalance.name");
  }

  @Override
  public String getDescription() {
    return Main._l("commands.setautobalance.desc");
  }

  @Override
  public String[] getArguments() {
    return new String[] {"game", "value"};
  }

  @Override
  public boolean execute(CommandSender sender, ArrayList<String> args) {
    if (!sender.hasPermission("bw." + this.getPermission())) {
      return false;
    }

    Player player = (Player) sender;

    Game game = this.getPlugin().getGameManager().getGame(args.get(0));
    String value = args.get(1).toString().trim();

    if (game == null) {
      player.sendMessage(ChatWriter.pluginMessage(ChatColor.RED
          + Main._l("errors.gamenotfound", ImmutableMap.of("game", args.get(0).toString()))));
      return false;
    }

    if (game.getState() == GameState.RUNNING) {
      sender.sendMessage(
          ChatWriter.pluginMessage(ChatColor.RED + Main._l("errors.notwhilegamerunning")));
      return false;
    }

    if (!value.equalsIgnoreCase("true") && !value.equalsIgnoreCase("false")
        && !value.equalsIgnoreCase("off") && !value.equalsIgnoreCase("on")
        && !value.equalsIgnoreCase("1") && !value.equalsIgnoreCase("0")) {
      player
          .sendMessage(ChatWriter.pluginMessage(ChatColor.RED + Main._l("errors.wrongvalueonoff")));
      return true;
    }

    boolean autobalance = false;
    if (value.equalsIgnoreCase("true") || value.equalsIgnoreCase("on")
        || value.equalsIgnoreCase("1")) {
      autobalance = true;
    }

    game.setAutobalance(autobalance);

    if (autobalance) {
      player.sendMessage(
          ChatWriter.pluginMessage(ChatColor.GREEN + Main._l("success.autobalanceseton")));
    } else {
      player.sendMessage(
          ChatWriter.pluginMessage(ChatColor.GREEN + Main._l("success.autobalancesetoff")));
    }
    return true;
  }

  @Override
  public String getPermission() {
    return "setup";
  }

}
