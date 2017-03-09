package io.github.bedwarsrel.BedwarsRel.Commands;

import com.google.common.collect.ImmutableMap;
import io.github.bedwarsrel.BedwarsRel.Game.Game;
import io.github.bedwarsrel.BedwarsRel.Game.GameState;
import io.github.bedwarsrel.BedwarsRel.Main;
import io.github.bedwarsrel.BedwarsRel.Utils.ChatWriter;
import java.util.ArrayList;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SetLobbyCommand extends BaseCommand implements ICommand {

  public SetLobbyCommand(Main plugin) {
    super(plugin);
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
          + Main
          ._l(player, "errors.gamenotfound", ImmutableMap.of("game", args.get(0).toString()))));
      return false;
    }

    if (game.getState() != GameState.STOPPED) {
      sender.sendMessage(
          ChatWriter.pluginMessage(ChatColor.RED + Main._l(sender, "errors.notwhilegamerunning")));
      return false;
    }

    game.setLobby(player);
    return true;
  }

  @Override
  public String[] getArguments() {
    return new String[]{"game"};
  }

  @Override
  public String getCommand() {
    return "setlobby";
  }

  @Override
  public String getDescription() {
    return Main._l("commands.setlobby.desc");
  }

  @Override
  public String getName() {
    return Main._l("commands.setlobby.name");
  }

  @Override
  public String getPermission() {
    return "setup";
  }

}
