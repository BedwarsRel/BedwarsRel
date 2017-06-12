package io.github.bedwarsrel.commands;

import com.google.common.collect.ImmutableMap;
import io.github.bedwarsrel.BedwarsRel;
import io.github.bedwarsrel.game.Game;
import io.github.bedwarsrel.game.GameState;
import io.github.bedwarsrel.utils.ChatWriter;
import io.github.bedwarsrel.utils.Utils;
import java.util.ArrayList;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class GameTimeCommand extends BaseCommand implements ICommand {

  public GameTimeCommand(BedwarsRel plugin) {
    super(plugin);
  }

  @Override
  public boolean execute(CommandSender sender, ArrayList<String> args) {
    if (!sender.hasPermission("bw." + this.getPermission())) {
      return false;
    }

    Player player = (Player) sender;

    Game game = this.getPlugin().getGameManager().getGame(args.get(0));
    String gametime = args.get(1).toString();

    if (game == null) {
      player.sendMessage(ChatWriter.pluginMessage(ChatColor.RED
          + BedwarsRel
          ._l(sender, "errors.gamenotfound", ImmutableMap.of("game", args.get(0).toString()))));
      return false;
    }

    if (game.getState() == GameState.RUNNING) {
      sender.sendMessage(
          ChatWriter.pluginMessage(ChatColor.RED + BedwarsRel
              ._l(sender, "errors.notwhilegamerunning")));
      return false;
    }

    if (!Utils.isNumber(gametime) && !"day".equals(gametime) && !"night".equals(gametime)) {
      player.sendMessage(
          ChatWriter.pluginMessage(ChatColor.RED + BedwarsRel._l(player, "errors.timeincorrect")));
      return true;
    }

    int time = 1000;
    if ("day".equals(gametime)) {
      time = 6000;
    } else if ("night".equals(gametime)) {
      time = 18000;
    } else {
      time = Integer.valueOf(gametime);
    }

    game.setTime(time);
    player.sendMessage(
        ChatWriter.pluginMessage(ChatColor.GREEN + BedwarsRel._l(player, "success.gametimeset")));
    return true;
  }

  @Override
  public String[] getArguments() {
    return new String[]{"game", "time"};
  }

  @Override
  public String getCommand() {
    return "gametime";
  }

  @Override
  public String getDescription() {
    return BedwarsRel._l("commands.gametime.desc");
  }

  @Override
  public String getName() {
    return BedwarsRel._l("commands.gametime.name");
  }

  @Override
  public String getPermission() {
    return "setup";
  }

}
