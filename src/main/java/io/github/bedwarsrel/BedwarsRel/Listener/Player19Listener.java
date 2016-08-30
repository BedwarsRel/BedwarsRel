package io.github.bedwarsrel.BedwarsRel.Listener;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;

import io.github.bedwarsrel.BedwarsRel.Main;
import io.github.bedwarsrel.BedwarsRel.Game.BungeeGameCycle;
import io.github.bedwarsrel.BedwarsRel.Game.Game;
import io.github.bedwarsrel.BedwarsRel.Game.GameState;

public class Player19Listener extends BaseListener {

  @EventHandler
  public void onPlayerSwapHandItems(PlayerSwapHandItemsEvent event) {
    Player player = event.getPlayer();
    Game game = Main.getInstance().getGameManager().getGameOfPlayer(player);

    if (game == null) {
      return;
    }

    if (game.getState() == GameState.WAITING
        || (game.getCycle() instanceof BungeeGameCycle && game.getCycle().isEndGameRunning()
            && Main.getInstance().getBooleanConfig("bungeecord.endgame-in-lobby", true))) {
      event.setCancelled(true);
      return;
    }
  }

}
