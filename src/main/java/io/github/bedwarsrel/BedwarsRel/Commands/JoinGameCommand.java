package io.github.bedwarsrel.BedwarsRel.Commands;

import com.google.common.collect.ImmutableMap;
import io.github.bedwarsrel.BedwarsRel.Game.Game;
import io.github.bedwarsrel.BedwarsRel.Game.GameState;
import io.github.bedwarsrel.BedwarsRel.Main;
import io.github.bedwarsrel.BedwarsRel.Utils.ChatWriter;
import io.github.bedwarsrel.BedwarsRel.Utils.Utils;
import java.util.ArrayList;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class JoinGameCommand extends BaseCommand {

  public JoinGameCommand(Main plugin) {
    super(plugin);
  }

  @Override
  public boolean execute(CommandSender sender, ArrayList<String> args) {
    if (!super.hasPermission(sender)) {
      return false;
    }

    Player player = (Player) sender;
    Game game = this.getPlugin().getGameManager().getGame(args.get(0));
    Game gameOfPlayer = Main.getInstance().getGameManager().getGameOfPlayer(player);

    if (gameOfPlayer != null) {
      if (gameOfPlayer.getState() == GameState.RUNNING) {
        sender.sendMessage(
            ChatWriter.pluginMessage(ChatColor.RED + Main._l(sender, "errors.notwhileingame")));
        return false;
      }

      if (gameOfPlayer.getState() == GameState.WAITING) {
        gameOfPlayer.playerLeave(player, false);
      }
    }

    if (game == null) {
      if (!args.get(0).equalsIgnoreCase("random")) {
        sender.sendMessage(ChatWriter.pluginMessage(ChatColor.RED
            + Main
            ._l(sender, "errors.gamenotfound", ImmutableMap.of("game", args.get(0).toString()))));
        return true;
      }

      ArrayList<Game> games = new ArrayList<>();
      for (Game g : this.getPlugin().getGameManager().getGames()) {
        if (g.getState() == GameState.WAITING) {
          games.add(g);
        }
      }
      if (games.size() == 0) {
        sender.sendMessage(
            ChatWriter.pluginMessage(ChatColor.RED + Main._l(sender, "errors.nofreegames")));
        return true;
      }
      game = games.get(Utils.randInt(0, games.size() - 1));
    }

    if (game.playerJoins(player)) {
      sender.sendMessage(
          ChatWriter.pluginMessage(ChatColor.GREEN + Main._l(sender, "success.joined")));
    }
    return true;
  }

  @Override
  public String[] getArguments() {
    return new String[]{"game"};
  }

  @Override
  public String getCommand() {
    return "join";
  }

  @Override
  public String getDescription() {
    return Main._l("commands.join.desc");
  }

  @Override
  public String getName() {
    return Main._l("commands.join.name");
  }

  @Override
  public String getPermission() {
    return "base";
  }

}
