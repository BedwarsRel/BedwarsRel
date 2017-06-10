package io.github.bedwarsrel.commands;

import com.google.common.collect.ImmutableMap;
import io.github.bedwarsrel.BedwarsRel;
import io.github.bedwarsrel.game.Game;
import io.github.bedwarsrel.game.GameState;
import io.github.bedwarsrel.utils.ChatWriter;
import java.util.ArrayList;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SetAutobalanceCommand extends BaseCommand implements ICommand {

  public SetAutobalanceCommand(BedwarsRel plugin) {
    super(plugin);
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
          + BedwarsRel
          ._l(player, "errors.gamenotfound", ImmutableMap.of("game", args.get(0).toString()))));
      return false;
    }

    if (game.getState() == GameState.RUNNING) {
      sender.sendMessage(
          ChatWriter.pluginMessage(ChatColor.RED + BedwarsRel
              ._l(sender, "errors.notwhilegamerunning")));
      return false;
    }

    if (!value.equalsIgnoreCase("true") && !value.equalsIgnoreCase("false")
        && !value.equalsIgnoreCase("off") && !value.equalsIgnoreCase("on")
        && !value.equalsIgnoreCase("1") && !value.equalsIgnoreCase("0")) {
      player
          .sendMessage(
              ChatWriter.pluginMessage(ChatColor.RED + BedwarsRel
                  ._l(player, "errors.wrongvalueonoff")));
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
          ChatWriter.pluginMessage(ChatColor.GREEN + BedwarsRel
              ._l(player, "success.autobalanceseton")));
    } else {
      player.sendMessage(
          ChatWriter.pluginMessage(ChatColor.GREEN + BedwarsRel
              ._l(player, "success.autobalancesetoff")));
    }
    return true;
  }

  @Override
  public String[] getArguments() {
    return new String[]{"game", "value"};
  }

  @Override
  public String getCommand() {
    return "setautobalance";
  }

  @Override
  public String getDescription() {
    return BedwarsRel._l("commands.setautobalance.desc");
  }

  @Override
  public String getName() {
    return BedwarsRel._l("commands.setautobalance.name");
  }

  @Override
  public String getPermission() {
    return "setup";
  }

}
