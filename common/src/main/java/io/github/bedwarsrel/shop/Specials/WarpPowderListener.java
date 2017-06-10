package io.github.bedwarsrel.shop.Specials;

import io.github.bedwarsrel.BedwarsRel;
import io.github.bedwarsrel.game.Game;
import io.github.bedwarsrel.game.GameState;
import io.github.bedwarsrel.utils.ChatWriter;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;

public class WarpPowderListener implements Listener {

  private WarpPowder getActiveWarpPowder(Game game, Player player) {
    for (SpecialItem item : game.getSpecialItems()) {
      if (item instanceof WarpPowder) {
        WarpPowder powder = (WarpPowder) item;
        if (powder.getPlayer().equals(player)) {
          return powder;
        }
      }
    }

    return null;
  }

  @EventHandler
  public void onDamage(EntityDamageEvent dmg) {
    if (dmg.isCancelled()) {
      return;
    }

    if (!(dmg.getEntity() instanceof Player)) {
      return;
    }

    Player player = (Player) dmg.getEntity();
    Game game = BedwarsRel.getInstance().getGameManager().getGameOfPlayer(player);

    if (game == null) {
      return;
    }

    if (game.getState() != GameState.RUNNING) {
      return;
    }

    if (game.isSpectator(player)) {
      return;
    }

    WarpPowder powder = null;
    for (SpecialItem item : game.getSpecialItems()) {
      if (!(item instanceof WarpPowder)) {
        continue;
      }

      powder = (WarpPowder) item;
      if (!powder.getPlayer().equals(player)) {
        powder = null;
        continue;
      }
      break;
    }

    if (powder != null) {
      powder.cancelTeleport(true, true);
      return;
    }
  }

  @EventHandler
  public void onDrop(PlayerDropItemEvent event) {
    Player p = event.getPlayer();
    Game g = BedwarsRel.getInstance().getGameManager().getGameOfPlayer(p);
    if (g == null) {
      return;
    }

    if (g.getState() == GameState.RUNNING
        && event.getItemDrop().getItemStack().getItemMeta().getDisplayName() != null
        && event.getItemDrop().getItemStack().getItemMeta().getDisplayName()
        .equals(BedwarsRel._l("ingame.specials.warp-powder.cancel"))) {
      event.setCancelled(true);
    }

  }

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

    WarpPowder warpPowder = new WarpPowder();
    if (!ev.getMaterial().equals(warpPowder.getItemMaterial())
        && !ev.getMaterial().equals(warpPowder.getActivatedMaterial())) {
      return;
    }

    WarpPowder powder = this.getActiveWarpPowder(game, player);

    if (ev.getMaterial().equals(warpPowder.getActivatedMaterial())) {
      if (ev.getItem().getItemMeta().getDisplayName() != null && !ev.getItem().getItemMeta()
          .getDisplayName().equals(BedwarsRel._l("ingame.specials.warp-powder.cancel"))) {
        return;
      }

      if (powder != null) {
        powder.setStackAmount(powder.getStack().getAmount() + 1);

        player.updateInventory();
        powder.cancelTeleport(true, true);
        ev.setCancelled(true);
      }

      return;
    }

    if (powder != null) {
      player.sendMessage(
          ChatWriter.pluginMessage(BedwarsRel._l(player, "ingame.specials.warp-powder.multiuse")));
      return;
    }

    if (player.getLocation().getBlock().getRelative(BlockFace.DOWN).getType() == Material.AIR) {
      return;
    }

    warpPowder.setPlayer(player);
    warpPowder.setGame(game);
    warpPowder.runTask();
    ev.setCancelled(true);
  }

  @EventHandler
  public void onMove(PlayerMoveEvent mv) {
    if (mv.isCancelled()) {
      return;
    }

    if (mv.getFrom().getBlock().equals(mv.getTo().getBlock())) {
      return;
    }

    Player player = mv.getPlayer();
    Game game = BedwarsRel.getInstance().getGameManager().getGameOfPlayer(player);

    if (game == null) {
      return;
    }

    if (game.getState() != GameState.RUNNING) {
      return;
    }

    WarpPowder powder = null;
    for (SpecialItem item : game.getSpecialItems()) {
      if (!(item instanceof WarpPowder)) {
        continue;
      }

      powder = (WarpPowder) item;
      if (powder.getPlayer().equals(player)) {
        break;
      }

      powder = null;
    }

    if (powder != null) {
      powder.setStackAmount(powder.getStack().getAmount() + 1);
      player.updateInventory();
      powder.cancelTeleport(true, true);
      return;
    }
  }

}
