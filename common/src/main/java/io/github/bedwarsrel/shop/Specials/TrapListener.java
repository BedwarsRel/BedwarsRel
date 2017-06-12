package io.github.bedwarsrel.shop.Specials;

import io.github.bedwarsrel.BedwarsRel;
import io.github.bedwarsrel.game.Game;
import io.github.bedwarsrel.game.GameState;
import io.github.bedwarsrel.game.Team;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerMoveEvent;

public class TrapListener implements Listener {

  @EventHandler(priority = EventPriority.HIGH)
  public void onBreak(BlockBreakEvent br) {
    if (br.isCancelled()) {
      return;
    }

    Block toDestroy = br.getBlock();
    if (br.getBlock().getType() != Material.TRIPWIRE) {
      Block relative = br.getBlock().getRelative(BlockFace.UP);
      // check above
      if (!relative.getType().equals(Material.TRIPWIRE)) {
        return;
      }

      toDestroy = relative;
    }

    Player player = br.getPlayer();
    Game game = BedwarsRel.getInstance().getGameManager().getGameOfPlayer(player);

    if (game == null) {
      return;
    }

    if (game.getState() != GameState.RUNNING) {
      return;
    }

    if (br.getBlock().equals(toDestroy)) {
      br.setCancelled(true);
      return;
    }

    toDestroy.setType(Material.AIR);
  }

  @EventHandler
  public void onMove(PlayerMoveEvent move) {
    if (move.isCancelled()) {
      return;
    }

    double difX = Math.abs(move.getFrom().getX() - move.getTo().getX());
    double difZ = Math.abs(move.getFrom().getZ() - move.getTo().getZ());

    if (difX == 0.0 && difZ == 0.0) {
      return;
    }

    Player player = move.getPlayer();
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

    Trap tmpTrap = new Trap();
    if (!move.getTo().getBlock().getType().equals(tmpTrap.getItemMaterial())) {
      return;
    }

    Team team = game.getPlayerTeam(player);
    if (team == null || game.isSpectator(player)) {
      return;
    }

    // get trapped trap ;)
    for (SpecialItem item : game.getSpecialItems()) {
      if (!(item instanceof Trap)) {
        continue;
      }

      Trap trap = (Trap) item;
      if (!trap.getLocation().equals(player.getLocation().getBlock().getLocation())) {
        continue;
      }

      if (trap.getPlacedTeam() == null) {
        continue;
      }

      if (!trap.getPlacedTeam().equals(team)) {
        trap.activate(player);
        return;
      }
    }
  }

  @EventHandler(priority = EventPriority.HIGH)
  public void onPlace(BlockPlaceEvent place) {
    if (place.isCancelled()) {
      return;
    }

    Player player = place.getPlayer();
    Game game = BedwarsRel.getInstance().getGameManager().getGameOfPlayer(player);

    if (game == null) {
      return;
    }

    if (game.getState() != GameState.RUNNING) {
      return;
    }

    Team team = game.getPlayerTeam(player);
    if (team == null) {
      place.setCancelled(true);
      place.setBuild(false);
      return;
    }

    Trap trap = new Trap();
    trap.create(game, team, place.getBlockPlaced().getLocation());
    game.getRegion().addPlacedUnbreakableBlock(place.getBlockPlaced(), null);
  }

}
