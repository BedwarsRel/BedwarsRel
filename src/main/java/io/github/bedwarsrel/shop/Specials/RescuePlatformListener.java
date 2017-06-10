package io.github.bedwarsrel.shop.Specials;

import io.github.bedwarsrel.BedwarsRel;
import io.github.bedwarsrel.game.Game;
import io.github.bedwarsrel.game.GameState;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

public class RescuePlatformListener implements Listener {

  @EventHandler
  public void onInteract(PlayerInteractEvent ev) {
    if (ev.getAction().equals(Action.LEFT_CLICK_AIR)
        || ev.getAction().equals(Action.LEFT_CLICK_BLOCK)) {
      return;
    }

    Player player = ev.getPlayer();
    Game game = BedwarsRel.getInstance().getGameManager().getGameOfPlayer(player);

    if (game == null) {
      return;
    }

    if (game.getState() != GameState.RUNNING) {
      return;
    }

    RescuePlatform platform = new RescuePlatform();
    if (!ev.getMaterial().equals(platform.getItemMaterial())) {
      return;
    }

    platform.create(player, game);
  }

}
