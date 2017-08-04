package io.github.bedwarsrel.listener.events;

import io.github.bedwarsrel.BedwarsRel;
import io.github.bedwarsrel.game.BungeeGameCycle;
import io.github.bedwarsrel.game.Game;
import io.github.bedwarsrel.game.GameState;
import io.github.bedwarsrel.listener.BaseListener;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;

public class PlayerSwapHandItemsEventListener extends BaseListener {

  @EventHandler
  public void onPlayerSwapHandItems(PlayerSwapHandItemsEvent event) {
    Player player = event.getPlayer();
    Game game = BedwarsRel.getInstance().getGameManager().getGameOfPlayer(player);

    if (game == null) {
      return;
    }

    if (game.getState() == GameState.WAITING
        || (game.getCycle() instanceof BungeeGameCycle && game.getCycle().isEndGameRunning()
        && BedwarsRel.getInstance().getBooleanConfig("bungeecord.endgame-in-lobby", true))) {
      event.setCancelled(true);
      return;
    }
  }

}
