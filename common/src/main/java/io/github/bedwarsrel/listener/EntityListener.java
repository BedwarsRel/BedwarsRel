package io.github.bedwarsrel.listener;

import com.google.common.collect.ImmutableMap;
import io.github.bedwarsrel.BedwarsRel;
import io.github.bedwarsrel.game.Game;
import io.github.bedwarsrel.game.GameState;
import io.github.bedwarsrel.game.Team;
import io.github.bedwarsrel.game.TeamJoinMetaDataValue;
import io.github.bedwarsrel.utils.ChatWriter;
import io.github.bedwarsrel.utils.Utils;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.EntityInteractEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.metadata.MetadataValue;

public class EntityListener extends BaseListener {

  @EventHandler(priority = EventPriority.HIGHEST)
  public void onEntityDamage(EntityDamageEvent ede) {
    List<EntityType> canDamageTypes = new ArrayList<EntityType>();
    canDamageTypes.add(EntityType.PLAYER);

    if (BedwarsRel.getInstance().getServer().getPluginManager().isPluginEnabled("AntiAura")
        || BedwarsRel.getInstance().getServer().getPluginManager().isPluginEnabled("AAC")) {
      canDamageTypes.add(EntityType.SQUID);
    }

    if (canDamageTypes.contains(ede.getEntityType())) {
      return;
    }

    Game game =
        BedwarsRel.getInstance().getGameManager().getGameByLocation(ede.getEntity().getLocation());
    if (game == null) {
      return;
    }

    if (game.getState() == GameState.STOPPED) {
      return;
    }

    ede.setCancelled(true);
  }

  @EventHandler(priority = EventPriority.HIGHEST)
  public void onEntityDamageByEntity(EntityDamageByEntityEvent ede) {
    List<EntityType> canDamageTypes = new ArrayList<EntityType>();
    canDamageTypes.add(EntityType.PLAYER);

    if (BedwarsRel.getInstance().getServer().getPluginManager().isPluginEnabled("AntiAura")
        || BedwarsRel.getInstance().getServer().getPluginManager().isPluginEnabled("AAC")) {
      canDamageTypes.add(EntityType.SQUID);
    }

    if (canDamageTypes.contains(ede.getEntityType())) {
      return;
    }

    Game game =
        BedwarsRel.getInstance().getGameManager().getGameByLocation(ede.getEntity().getLocation());
    if (game == null) {
      return;
    }

    if (game.getState() == GameState.STOPPED) {
      return;
    }

    ede.setCancelled(true);
  }

  @EventHandler
  public void onEntityInteract(EntityInteractEvent event) {
    if (!(event.getEntity() instanceof Player)) {
      return;
    }

    if (event.getBlock().getType() != Material.SOIL
        && event.getBlock().getType() != Material.WHEAT) {
      return;
    }

    Player player = (Player) event.getEntity();
    Game game = BedwarsRel.getInstance().getGameManager().getGameOfPlayer(player);

    if (game == null) {
      return;
    }

    if (game.getState() == GameState.WAITING) {
      event.setCancelled(true);
    }
  }

  @EventHandler
  public void onEntitySpawn(CreatureSpawnEvent ese) {
    if (BedwarsRel.getInstance().getGameManager() == null) {
      return;
    }

    if (ese.getLocation() == null) {
      return;
    }

    if (ese.getLocation().getWorld() == null) {
      return;
    }

    Game game = BedwarsRel.getInstance().getGameManager().getGameByLocation(ese.getLocation());
    if (game == null) {
      return;
    }

    if (game.getState() == GameState.STOPPED) {
      return;
    }

    if (ese.getEntityType().equals(EntityType.CREEPER)
        || ese.getEntityType().equals(EntityType.CAVE_SPIDER)
        || ese.getEntityType().equals(EntityType.SPIDER)
        || ese.getEntityType().equals(EntityType.ZOMBIE)
        || ese.getEntityType().equals(EntityType.SKELETON)
        || ese.getEntityType().equals(EntityType.SILVERFISH)) {
      ese.setCancelled(true);
    }
  }

  @EventHandler(priority = EventPriority.HIGHEST)
  public void onExplodeDestroy(EntityExplodeEvent eev) {
    if (eev.isCancelled()) {
      return;
    }

    if (eev.getEntity() == null) {
      return;
    }

    if (eev.getEntity().getWorld() == null) {
      return;
    }

    Game game =
        BedwarsRel.getInstance().getGameManager().getGameByLocation(eev.getEntity().getLocation());

    if (game == null) {
      return;
    }

    if (game.getState() == GameState.STOPPED) {
      return;
    }

    Iterator<Block> explodeBlocks = eev.blockList().iterator();
    boolean tntDestroyEnabled =
        BedwarsRel.getInstance().getBooleanConfig("explodes.destroy-worldblocks", false);
    boolean tntDestroyBeds = BedwarsRel
        .getInstance().getBooleanConfig("explodes.destroy-beds", false);

    if (!BedwarsRel.getInstance().getBooleanConfig("explodes.drop-blocks", false)) {
      eev.setYield(0F);
    }

    Material targetMaterial = game.getTargetMaterial();
    while (explodeBlocks.hasNext()) {
      Block exploding = explodeBlocks.next();
      if (!game.getRegion().isInRegion(exploding.getLocation())) {
        explodeBlocks.remove();
        continue;
      }

      if ((!tntDestroyEnabled && !tntDestroyBeds) || (!tntDestroyEnabled && tntDestroyBeds
          && exploding.getType() != Material.BED_BLOCK && exploding.getType() != Material.BED)) {
        if (!game.getRegion().isPlacedBlock(exploding)) {
          if (BedwarsRel.getInstance().isBreakableType(exploding.getType())) {
            game.getRegion().addBreakedBlock(exploding);
            continue;
          }

          explodeBlocks.remove();
        } else {
          game.getRegion().removePlacedBlock(exploding);
        }

        continue;
      }

      if (game.getRegion().isPlacedBlock(exploding)) {
        game.getRegion().removePlacedBlock(exploding);
        continue;
      }

      if (exploding.getType().equals(targetMaterial)) {
        if (!tntDestroyBeds) {
          explodeBlocks.remove();
          continue;
        }

        // only destroyable by tnt
        if (!eev.getEntityType().equals(EntityType.PRIMED_TNT)
            && !eev.getEntityType().equals(EntityType.MINECART_TNT)) {
          explodeBlocks.remove();
          continue;
        }

        // when it wasn't player who ignited the tnt
        TNTPrimed primedTnt = (TNTPrimed) eev.getEntity();
        if (!(primedTnt.getSource() instanceof Player)) {
          explodeBlocks.remove();
          continue;
        }

        Player p = (Player) primedTnt.getSource();
        if (!game.handleDestroyTargetMaterial(p, exploding)) {
          explodeBlocks.remove();
          continue;
        }
      } else {
        game.getRegion().addBreakedBlock(exploding);
      }
    }
  }

  @EventHandler(priority = EventPriority.HIGH)
  public void onInteractEntity(PlayerInteractAtEntityEvent event) {
    if (event.getRightClicked() == null) {
      return;
    }

    Entity entity = event.getRightClicked();
    Player player = event.getPlayer();
    if (!player.hasMetadata("bw-addteamjoin")) {
      if (!(entity instanceof LivingEntity)) {
        return;
      }

      LivingEntity livEntity = (LivingEntity) entity;
      Game game = BedwarsRel.getInstance().getGameManager().getGameOfPlayer(player);
      if (game == null) {
        return;
      }

      if (game.getState() != GameState.WAITING) {
        return;
      }

      Team team = game.getTeam(ChatColor.stripColor(livEntity.getCustomName()));
      if (team == null) {
        return;
      }

      game.playerJoinTeam(player, team);
      event.setCancelled(true);
      return;
    }

    List<MetadataValue> values = player.getMetadata("bw-addteamjoin");
    if (values == null || values.size() == 0) {
      return;
    }

    event.setCancelled(true);
    TeamJoinMetaDataValue value = (TeamJoinMetaDataValue) values.get(0);
    if (!((boolean) value.value())) {
      return;
    }

    if (!(entity instanceof LivingEntity)) {
      player.sendMessage(
          ChatWriter.pluginMessage(ChatColor.RED + BedwarsRel
              ._l(player, "errors.entitynotcompatible")));
      return;
    }

    LivingEntity living = (LivingEntity) entity;
    living.setRemoveWhenFarAway(false);
    living.setCanPickupItems(false);
    living.setCustomName(value.getTeam().getChatColor() + value.getTeam().getDisplayName());
    living.setCustomNameVisible(
        BedwarsRel.getInstance().getBooleanConfig("jointeam-entity.show-name", true));

    if (living.getType().equals(EntityType.valueOf("ARMOR_STAND"))) {
      Utils.equipArmorStand(living, value.getTeam());
    }

    player.removeMetadata("bw-addteamjoin", BedwarsRel.getInstance());
    player.sendMessage(ChatWriter
        .pluginMessage(
            ChatColor.GREEN + BedwarsRel._l(player, "success.teamjoinadded", ImmutableMap.of("team",
                value.getTeam().getChatColor() + value.getTeam().getDisplayName()
                    + ChatColor.GREEN))));
  }

  @EventHandler(priority = EventPriority.HIGH)
  public void onRegainHealth(EntityRegainHealthEvent rhe) {
    if (rhe.getEntityType() != EntityType.PLAYER) {
      return;
    }

    Player player = (Player) rhe.getEntity();
    Game game = BedwarsRel.getInstance().getGameManager().getGameOfPlayer(player);

    if (game == null) {
      return;
    }

    if (game.getState() != GameState.RUNNING) {
      return;
    }

    if (player.getHealth() >= player.getMaxHealth()) {
      game.setPlayerDamager(player, null);
    }
  }
}
