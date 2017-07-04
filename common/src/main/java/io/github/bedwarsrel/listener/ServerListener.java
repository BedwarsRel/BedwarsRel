package io.github.bedwarsrel.listener;

import io.github.bedwarsrel.BedwarsRel;
import io.github.bedwarsrel.game.Game;
import io.github.bedwarsrel.game.GameState;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.server.ServerListPingEvent;

public class ServerListener extends BaseListener {

  private String getCurrentPlayersString(Game game) {
    int currentPlayers = 0;
    if (game.getState() == GameState.RUNNING) {
      currentPlayers = game.getTeamPlayers().size();
    } else if (game.getState() == GameState.WAITING) {
      currentPlayers = game.getPlayers().size();
    } else {
      currentPlayers = 0;
    }

    return String.valueOf(currentPlayers);
  }

  private String getMaxPlayersString(Game game) {
    int maxPlayers = game.getMaxPlayers();
    return String.valueOf(maxPlayers);
  }

  private String getStatus(Game game) {
    String status = null;
    if (game.getState() == GameState.WAITING && game.isFull()) {
      status = ChatColor.RED + BedwarsRel._l("sign.gamestate.full");
    } else {
      status = BedwarsRel._l("sign.gamestate." + game.getState().toString().toLowerCase());
    }

    return status;
  }

  @EventHandler
  public void onServerListPing(ServerListPingEvent slpe) {
    // Only enabled on bungeecord
    if (!BedwarsRel.getInstance().isBungee()) {
      return;
    }
    if (BedwarsRel.getInstance().getGameManager() == null
        || BedwarsRel.getInstance().getGameManager().getGames() == null
        || BedwarsRel.getInstance().getGameManager().getGames().size() == 0) {
      return;
    }

    Game game = BedwarsRel.getInstance().getGameManager().getGames().get(0);

    if (game == null) {
      return;
    }

    switch (game.getState()) {
      case STOPPED:
        slpe.setMotd(replacePlaceholder(game, ChatColor.translateAlternateColorCodes('&',
            BedwarsRel.getInstance().getConfig().getString("bungeecord.motds.stopped"))));
        break;
      case WAITING:
        if (game.isFull()) {
          slpe.setMotd(replacePlaceholder(game, ChatColor.translateAlternateColorCodes('&',
              BedwarsRel.getInstance().getConfig().getString("bungeecord.motds.full"))));
        } else {
          slpe.setMotd(replacePlaceholder(game, ChatColor.translateAlternateColorCodes('&',
              BedwarsRel.getInstance().getConfig().getString("bungeecord.motds.lobby"))));
        }

        break;
      case RUNNING:
        slpe.setMotd(replacePlaceholder(game, ChatColor.translateAlternateColorCodes('&',
            BedwarsRel.getInstance().getConfig().getString("bungeecord.motds.running"))));
        break;
      default:
        slpe.setMotd(ChatColor.RED + "INVALID GAME STATE!");
        break;
    }
  }

  private String replacePlaceholder(Game game, String line) {
    String finalLine = line;

    finalLine = finalLine.replace("$title$", BedwarsRel._l("sign.firstline"));
    finalLine = finalLine.replace("$gamename$", game.getName());
    if (game.getRegion().getName() != null) {
      finalLine = finalLine.replace("$regionname$", game.getRegion().getName());
    } else {
      finalLine = finalLine.replace("$regionname$", game.getName());
    }
    finalLine = finalLine.replace("$maxplayers$", getMaxPlayersString(game));
    finalLine = finalLine.replace("$currentplayers$", getCurrentPlayersString(game));
    finalLine = finalLine.replace("$status$", getStatus(game));

    return finalLine;
  }

}
