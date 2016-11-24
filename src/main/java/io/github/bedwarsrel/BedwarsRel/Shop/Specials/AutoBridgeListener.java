package io.github.bedwarsrel.BedwarsRel.Shop.Specials;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

import io.github.bedwarsrel.BedwarsRel.Main;
import io.github.bedwarsrel.BedwarsRel.Game.Game;
import io.github.bedwarsrel.BedwarsRel.Game.GameState;

public class AutoBridgeListener implements Listener {

  @EventHandler(priority = EventPriority.HIGHEST)
  public void onInteract(PlayerInteractEvent interact) {
    if (interact.getAction().equals(Action.LEFT_CLICK_AIR)
        || interact.getAction().equals(Action.LEFT_CLICK_BLOCK)) {
      return;
    }

    if (interact.getMaterial() == null) {
      return;
    }

    AutoBridge bridge = new AutoBridge();
    if (interact.getMaterial() != bridge.getItemMaterial()) {
      return;
    }

    Game game = Main.getInstance().getGameManager().getGameOfPlayer(interact.getPlayer());
    if (game == null) {
      return;
    }

    if (game.getState() != GameState.RUNNING) {
      return;
    }

    if (game.isSpectator(interact.getPlayer())) {
      return;
    }

    interact.setCancelled(true);
    bridge.create(interact.getPlayer(), game);
  }
}
