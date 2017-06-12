package io.github.bedwarsrel.shop.Specials;

import io.github.bedwarsrel.BedwarsRel;
import io.github.bedwarsrel.game.Game;
import io.github.bedwarsrel.game.GameState;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

public class ProtectionWallListener implements Listener {

  @EventHandler(priority = EventPriority.HIGHEST)
  public void onInteract(PlayerInteractEvent interact) {
    if (interact.getAction().equals(Action.LEFT_CLICK_AIR)
        || interact.getAction().equals(Action.LEFT_CLICK_BLOCK)) {
      return;
    }

    if (interact.getMaterial() == null) {
      return;
    }

    ProtectionWall wall = new ProtectionWall();
    if (interact.getMaterial() != wall.getItemMaterial()) {
      return;
    }

    Game game = BedwarsRel.getInstance().getGameManager().getGameOfPlayer(interact.getPlayer());
    if (game == null) {
      return;
    }

    if (game.getState() != GameState.RUNNING) {
      return;
    }

    if (game.isSpectator(interact.getPlayer())) {
      return;
    }

    wall.create(interact.getPlayer(), game);
    interact.setCancelled(true);
  }
}
