package io.github.bedwarsrel.listener;

import io.github.bedwarsrel.BedwarsRel;
import io.github.bedwarsrel.game.Game;
import io.github.bedwarsrel.game.GameState;
import io.github.bedwarsrel.game.Team;
import io.github.bedwarsrel.utils.ChatWriter;
import java.util.List;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockFadeEvent;
import org.bukkit.event.block.BlockFormEvent;
import org.bukkit.event.block.BlockGrowEvent;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.BlockIgniteEvent.IgniteCause;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.BlockSpreadEvent;
import org.bukkit.event.world.StructureGrowEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class BlockListener extends BaseListener {

  @EventHandler(ignoreCancelled = true)
  public void onBlockGrow(BlockGrowEvent grow) {

    Game game =
        BedwarsRel.getInstance().getGameManager().getGameByLocation(grow.getBlock().getLocation());
    if (game == null) {
      return;
    }

    grow.setCancelled(true);
  }

  @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
  public void onBreak(BlockBreakEvent e) {

    Player p = e.getPlayer();
    if (p == null) {
      Block block = e.getBlock();
      if (block == null) {
        return;
      }

      Game game = BedwarsRel.getInstance().getGameManager().getGameByLocation(block.getLocation());
      if (game == null) {
        return;
      }

      if (game.getState() != GameState.RUNNING) {
        return;
      }

      e.setCancelled(true);
      return;
    }

    Game g = BedwarsRel.getInstance().getGameManager().getGameOfPlayer(p);
    if (g == null) {
      Block breaked = e.getBlock();
      if (!(breaked.getState() instanceof Sign)) {
        return;
      }

      if (!p.hasPermission("bw.setup") || e.isCancelled()) {
        return;
      }

      Game game = BedwarsRel.getInstance().getGameManager()
          .getGameBySignLocation(breaked.getLocation());
      if (game == null) {
        return;
      }

      game.removeJoinSign(breaked.getLocation());
      return;
    }

    if (g.getState() != GameState.RUNNING && g.getState() != GameState.WAITING) {
      return;
    }

    if (g.getState() == GameState.WAITING) {
      e.setCancelled(true);
      return;
    }

    if (g.isSpectator(p)) {
      e.setCancelled(true);
      return;
    }

    Material targetMaterial = g.getTargetMaterial();
    if (e.getBlock().getType() == targetMaterial) {
      e.setCancelled(true);

      g.handleDestroyTargetMaterial(p, e.getBlock());
      return;
    }

    Block breakedBlock = e.getBlock();

    if (!g.getRegion().isPlacedBlock(breakedBlock)) {
      if (breakedBlock == null) {
        e.setCancelled(true);
        return;
      }

      if (BedwarsRel.getInstance().isBreakableType(breakedBlock.getType())) {
        g.getRegion().addBreakedBlock(breakedBlock);
        e.setCancelled(false);
        return;
      }

      e.setCancelled(true);
    } else {
      if (!BedwarsRel.getInstance().getBooleanConfig("friendlybreak", true)) {
        Team playerTeam = g.getPlayerTeam(p);
        for (Player player : playerTeam.getPlayers()) {
          if (player.equals(p)) {
            continue;
          }

          if (player.getLocation().getBlock().getRelative(BlockFace.DOWN).equals(e.getBlock())) {
            p.sendMessage(
                ChatWriter
                    .pluginMessage(ChatColor.RED + BedwarsRel._l(p, "ingame.no-friendlybreak")));
            e.setCancelled(true);
            return;
          }
        }
      }

      if (e.getBlock().getType() == Material.ENDER_CHEST) {
        for (Team team : g.getTeams().values()) {
          List<Block> teamChests = team.getChests();
          if (teamChests.contains(breakedBlock)) {
            team.removeChest(breakedBlock);
            for (Player aPlayer : team.getPlayers()) {
              if (aPlayer.isOnline()) {
                aPlayer.sendMessage(
                    ChatWriter.pluginMessage(BedwarsRel._l(aPlayer, "ingame.teamchestdestroy")));
              }
            }
            break;
          }
        }

        // Drop ender chest
        ItemStack enderChest = new ItemStack(Material.ENDER_CHEST, 1);
        ItemMeta meta = enderChest.getItemMeta();
        meta.setDisplayName(BedwarsRel._l("ingame.teamchest"));
        enderChest.setItemMeta(meta);

        e.setCancelled(true);
        breakedBlock.getDrops().clear();
        breakedBlock.setType(Material.AIR);
        breakedBlock.getWorld().dropItemNaturally(breakedBlock.getLocation(), enderChest);
      }

      for (ItemStack drop : breakedBlock.getDrops()) {
        if (!drop.getType().equals(breakedBlock.getType())) {
          breakedBlock.getDrops().remove(drop);
          breakedBlock.setType(Material.AIR);
          break;
        }
      }

      g.getRegion().removePlacedBlock(breakedBlock);
    }
  }

  @EventHandler(priority = EventPriority.HIGH)
  public void onBurn(BlockBurnEvent bbe) {
    Block block = bbe.getBlock();
    if (block == null) {
      return;
    }

    Game game = BedwarsRel.getInstance().getGameManager().getGameByLocation(block.getLocation());
    if (game == null) {
      return;
    }

    if (game.getState() == GameState.STOPPED) {
      return;
    }

    bbe.setCancelled(true);
  }

  @EventHandler(ignoreCancelled = true)
  public void onFade(BlockFadeEvent e) {

    Game game = BedwarsRel.getInstance().getGameManager()
        .getGameByLocation(e.getBlock().getLocation());
    if (game == null) {
      return;
    }

    if (game.getState() == GameState.STOPPED) {
      return;
    }

    if (!game.getRegion().isPlacedBlock(e.getBlock())) {
      e.setCancelled(true);
    }
  }

  @EventHandler(ignoreCancelled = true)
  public void onForm(BlockFormEvent form) {

    if (form.getNewState().getType() != Material.SNOW) {
      return;
    }

    Game game =
        BedwarsRel.getInstance().getGameManager().getGameByLocation(form.getBlock().getLocation());
    if (game == null) {
      return;
    }

    if (game.getState() == GameState.STOPPED) {
      return;
    }

    form.setCancelled(true);
  }

  @EventHandler(ignoreCancelled = true)
  public void onIgnite(BlockIgniteEvent ignite) {

    if (ignite.getIgnitingBlock() == null && ignite.getIgnitingEntity() == null) {
      return;
    }

    Game game = null;
    if (ignite.getIgnitingBlock() == null) {
      if (ignite.getIgnitingEntity() instanceof Player) {
        game = BedwarsRel.getInstance().getGameManager()
            .getGameOfPlayer((Player) ignite.getIgnitingEntity());
      } else {
        game = BedwarsRel.getInstance().getGameManager()
            .getGameByLocation(ignite.getIgnitingEntity().getLocation());
      }
    } else {
      game = BedwarsRel.getInstance().getGameManager()
          .getGameByLocation(ignite.getIgnitingBlock().getLocation());
    }

    if (game == null) {
      return;
    }

    if (game.getState() == GameState.STOPPED) {
      return;
    }

    if (ignite.getCause() == IgniteCause.ENDER_CRYSTAL || ignite.getCause() == IgniteCause.LIGHTNING
        || ignite.getCause() == IgniteCause.SPREAD) {
      ignite.setCancelled(true);
      return;
    }

    if (ignite.getIgnitingEntity() == null) {
      ignite.setCancelled(true);
      return;
    }

    if (game.getState() == GameState.WAITING) {
      return;
    }

    if (!game.getRegion().isPlacedBlock(ignite.getIgnitingBlock())
        && ignite.getIgnitingBlock() != null) {
      game.getRegion().addPlacedBlock(ignite.getIgnitingBlock(),
          ignite.getIgnitingBlock().getState());
    }
  }

  @EventHandler(priority = EventPriority.NORMAL)
  public void onPlace(BlockPlaceEvent bpe) {
    Player player = bpe.getPlayer();
    Game game = BedwarsRel.getInstance().getGameManager().getGameOfPlayer(player);

    if (game == null) {
      return;
    }

    if (game.getState() == GameState.STOPPED) {
      return;
    }

    if (game.getState() == GameState.WAITING) {
      bpe.setCancelled(true);
      bpe.setBuild(false);
      return;
    }

    if (game.getState() == GameState.RUNNING) {
      if (game.isSpectator(player)) {
        bpe.setCancelled(true);
        bpe.setBuild(false);
        return;
      }

      Block placeBlock = bpe.getBlockPlaced();
      BlockState replacedBlock = bpe.getBlockReplacedState();

      if (placeBlock.getType() == game.getTargetMaterial()) {
        bpe.setCancelled(true);
        bpe.setBuild(false);
        return;
      }

      if (!game.getRegion().isInRegion(placeBlock.getLocation())) {
        bpe.setCancelled(true);
        bpe.setBuild(false);
        return;
      }

      if (replacedBlock != null && !BedwarsRel
          .getInstance().getBooleanConfig("place-in-liquid", true)
          && (replacedBlock.getType().equals(Material.WATER)
          || replacedBlock.getType().equals(Material.STATIONARY_WATER)
          || replacedBlock.getType().equals(Material.LAVA)
          || replacedBlock.getType().equals(Material.STATIONARY_LAVA))) {
        bpe.setCancelled(true);
        bpe.setBuild(false);
        return;
      }

      if (replacedBlock != null && placeBlock.getType().equals(Material.WEB)
          && (replacedBlock.getType().equals(Material.WATER)
          || replacedBlock.getType().equals(Material.STATIONARY_WATER)
          || replacedBlock.getType().equals(Material.LAVA)
          || replacedBlock.getType().equals(Material.STATIONARY_LAVA))) {
        bpe.setCancelled(true);
        bpe.setBuild(false);
        return;
      }

      if (placeBlock.getType() == Material.ENDER_CHEST) {
        Team playerTeam = game.getPlayerTeam(player);
        if (playerTeam.getInventory() == null) {
          playerTeam.createTeamInventory();
        }

        playerTeam.addChest(placeBlock);
      }

      if (!bpe.isCancelled()) {
        game.getRegion().addPlacedBlock(placeBlock,
            (replacedBlock.getType().equals(Material.AIR) ? null : replacedBlock));
      }
    }
  }

  @EventHandler(ignoreCancelled = true)
  public void onSpread(BlockSpreadEvent spread) {

    if (spread.getBlock() == null) {
      return;
    }

    Game game =
        BedwarsRel.getInstance().getGameManager()
            .getGameByLocation(spread.getBlock().getLocation());
    if (game == null) {
      return;
    }

    if (game.getState() != GameState.RUNNING) {
      return;
    }

    if (spread.getNewState() == null || spread.getSource() == null) {
      return;
    }

    if (spread.getNewState().getType().equals(Material.FIRE)) {
      spread.setCancelled(true);
      return;
    }

    if (game.getRegion().isPlacedBlock(spread.getSource())) {
      game.getRegion().addPlacedBlock(spread.getBlock(), spread.getBlock().getState());
    } else {
      game.getRegion().addPlacedUnbreakableBlock(spread.getBlock(), spread.getBlock().getState());
    }
  }

  @EventHandler(ignoreCancelled = true)
  public void onStructureGrow(StructureGrowEvent grow) {

    Game game = BedwarsRel.getInstance().getGameManager().getGameByLocation(grow.getLocation());
    if (game == null) {
      return;
    }

    grow.setCancelled(true);
  }

}
