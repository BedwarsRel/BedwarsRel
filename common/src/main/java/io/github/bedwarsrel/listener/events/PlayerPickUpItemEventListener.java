package io.github.bedwarsrel.listener.events;

import io.github.bedwarsrel.BedwarsRel;
import io.github.bedwarsrel.game.Game;
import io.github.bedwarsrel.game.GameState;
import io.github.bedwarsrel.listener.BaseListener;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerPickupItemEvent;

public class PlayerPickUpItemEventListener extends BaseListener {

  @EventHandler
  public void onPlayerPickupItem(PlayerPickupItemEvent event) {
    Player player = event.getPlayer();
    Game game = BedwarsRel.getInstance().getGameManager().getGameOfPlayer(player);

    if (game == null) {
      game = BedwarsRel.getInstance().getGameManager().getGameByLocation(player.getLocation());
      if (game == null) {
        return;
      }
    }

    if (game.getState() != GameState.WAITING && game.isInGame(player)) {
      return;
    }

    event.setCancelled(true);
  }

}
