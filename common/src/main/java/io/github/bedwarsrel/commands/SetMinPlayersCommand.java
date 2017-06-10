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

public class SetMinPlayersCommand extends BaseCommand implements ICommand {

  public SetMinPlayersCommand(BedwarsRel plugin) {
    super(plugin);
  }

  @Override
  public boolean execute(CommandSender sender, ArrayList<String> args) {
    if (!sender.hasPermission("bw." + this.getPermission())) {
      return false;
    }

    Game game = this.getPlugin().getGameManager().getGame(args.get(0));
    String minplayers = args.get(1).toString();

    if (game == null) {
      sender.sendMessage(ChatWriter.pluginMessage(ChatColor.RED
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

    if (!Utils.isNumber(minplayers)) {
      sender.sendMessage(
          ChatWriter
              .pluginMessage(ChatColor.RED + BedwarsRel._l(sender, "errors.minplayersnumeric")));
      return true;
    }

    game.setMinPlayers(Integer.valueOf(minplayers));
    sender
        .sendMessage(
            ChatWriter
                .pluginMessage(ChatColor.GREEN + BedwarsRel._l(sender, "success.minplayersset")));
    return true;
  }

  @Override
  public String[] getArguments() {
    return new String[]{"game", "players"};
  }

  @Override
  public String getCommand() {
    return "setminplayers";
  }

  @Override
  public String getDescription() {
    return BedwarsRel._l("commands.setminplayers.desc");
  }

  @Override
  public String getName() {
    return BedwarsRel._l("commands.setminplayers.name");
  }

  @Override
  public String getPermission() {
    return "setup";
  }

}
