package io.github.bedwarsrel.BedwarsRel.Listener;

import java.util.ArrayList;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.spigotmc.event.player.PlayerSpawnLocationEvent;

import io.github.bedwarsrel.BedwarsRel.Main;
import io.github.bedwarsrel.BedwarsRel.Game.Game;

public class PlayerSpigotListener extends BaseListener {
	
  @EventHandler(priority = EventPriority.HIGH)
  public void onPlayerSpawnLocation(PlayerSpawnLocationEvent event) {
    if (Main.getInstance().isBungee()) {
      Player player = event.getPlayer();

      ArrayList<Game> games = Main.getInstance().getGameManager().getGames();
      if (games.size() == 0) {
        return;
      }

      Game firstGame = games.get(0);

      event.setSpawnLocation(firstGame.getPlayerTeleportLocation(player));
    }
  }

}
