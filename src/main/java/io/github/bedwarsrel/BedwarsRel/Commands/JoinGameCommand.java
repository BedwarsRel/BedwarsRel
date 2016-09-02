package io.github.bedwarsrel.BedwarsRel.Commands;

import java.util.ArrayList;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.google.common.collect.ImmutableMap;

import io.github.bedwarsrel.BedwarsRel.ChatWriter;
import io.github.bedwarsrel.BedwarsRel.Main;
import io.github.bedwarsrel.BedwarsRel.Utils;
import io.github.bedwarsrel.BedwarsRel.Game.Game;
import io.github.bedwarsrel.BedwarsRel.Game.GameState;

public class JoinGameCommand extends BaseCommand {

  public JoinGameCommand(Main plugin) {
    super(plugin);
  }

  @Override
  public String getCommand() {
    return "join";
  }

  @Override
  public String getName() {
    return Main._l("commands.join.name");
  }

  @Override
  public String getDescription() {
    return Main._l("commands.join.desc");
  }

  @Override
  public String[] getArguments() {
    return new String[] {"game"};
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
            ChatWriter.pluginMessage(ChatColor.RED + Main._l("errors.notwhileingame")));
        return false;
      }

      if (gameOfPlayer.getState() == GameState.WAITING) {
        gameOfPlayer.playerLeave(player, false);
      }
    }

    if (game == null) {
      if (!args.get(0).equalsIgnoreCase("random")) {
        sender.sendMessage(ChatWriter.pluginMessage(ChatColor.RED
            + Main._l("errors.gamenotfound", ImmutableMap.of("game", args.get(0).toString()))));
        return true;
      }
      
      ArrayList<Game> games = new ArrayList<>();
      for (Game g : this.getPlugin().getGameManager().getGames()) {
        if (g.getState() == GameState.WAITING) {
          games.add(g);
        }
      }
      if (games.size() == 0) {
        sender.sendMessage(ChatWriter.pluginMessage(ChatColor.RED + Main._l("errors.nofreegames")));
        return true;
      }
      game = games.get(Utils.randInt(0, games.size() - 1));
    }


    if (game.playerJoins(player)) {
      sender.sendMessage(ChatWriter.pluginMessage(ChatColor.GREEN + Main._l("success.joined")));
    }
    return true;
  }

  @Override
  public String getPermission() {
    return "base";
  }

}
