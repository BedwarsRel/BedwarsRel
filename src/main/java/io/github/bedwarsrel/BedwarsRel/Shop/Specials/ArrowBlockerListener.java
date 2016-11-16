package io.github.bedwarsrel.BedwarsRel.Shop.Specials;

import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.player.PlayerInteractEvent;
import io.github.bedwarsrel.BedwarsRel.Main;
import io.github.bedwarsrel.BedwarsRel.Game.Game;
import io.github.bedwarsrel.BedwarsRel.Game.GameState;

public class ArrowBlockerListener implements Listener {

  @EventHandler
  public void onInteract(PlayerInteractEvent ev) {
    if (ev.getAction().equals(Action.LEFT_CLICK_AIR)
        || ev.getAction().equals(Action.LEFT_CLICK_BLOCK)) {
      return;
    }

    Player player = ev.getPlayer();
    Game game = Main.getInstance().getGameManager().getGameOfPlayer(player);

    if (game == null) {
      return;
    }

    if (game.getState() != GameState.RUNNING) {
      return;
    }

    ArrowBlocker blocker = new ArrowBlocker();
    if (!ev.getMaterial().equals(blocker.getItemMaterial())) {
      return;
    }

    blocker.create(player, game);
    ev.setCancelled(true);
    player.updateInventory();
  }

  @EventHandler(priority = EventPriority.HIGH)
  public void onDamage(EntityDamageEvent ev) {
    if (!ev.getCause().equals(DamageCause.PROJECTILE)) {
      return;
    }
    
    if(!ev.getEntityType().equals(EntityType.PLAYER)){
      return;
    }
    Player player = (Player) ev.getEntity();
    
    Game game = Main.getInstance().getGameManager().getGameOfPlayer(player);

    if (game == null) {
      return;
    }

    if (game.getState() != GameState.RUNNING) {
      return;
    }
    
    if (!isBlockerActive(player, game)){
      return;
    }
    
    ev.setCancelled(true);
  }

  private boolean isBlockerActive(Player player, Game game) {
    for (SpecialItem item : game.getSpecialItems()) {
      if (item instanceof ArrowBlocker) {
        ArrowBlocker blocker = (ArrowBlocker) item;
        if (blocker.getOwner().equals(player)) {
          if (blocker.isActive){
            return true;
          }
        }
      }
    }
    return false;
  }

}
