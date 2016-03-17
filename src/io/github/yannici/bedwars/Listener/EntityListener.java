package io.github.yannici.bedwars.Listener;

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

import com.google.common.collect.ImmutableMap;

import io.github.yannici.bedwars.ChatWriter;
import io.github.yannici.bedwars.Main;
import io.github.yannici.bedwars.Utils;
import io.github.yannici.bedwars.Game.Game;
import io.github.yannici.bedwars.Game.GameState;
import io.github.yannici.bedwars.Game.Team;
import io.github.yannici.bedwars.Game.TeamJoinMetaDataValue;

public class EntityListener extends BaseListener {

	public EntityListener() {
		super();
	}

	@EventHandler
	public void onEntitySpawn(CreatureSpawnEvent ese) {
		if (Main.getInstance().getGameManager() == null) {
			return;
		}

		if (ese.getLocation() == null) {
			return;
		}

		if (ese.getLocation().getWorld() == null) {
			return;
		}

		Game game = Main.getInstance().getGameManager().getGameByLocation(ese.getLocation());
		if (game == null) {
			return;
		}

		if (game.getState() == GameState.STOPPED) {
			return;
		}

		if (ese.getEntityType().equals(EntityType.CREEPER) || ese.getEntityType().equals(EntityType.CAVE_SPIDER)
				|| ese.getEntityType().equals(EntityType.SPIDER) || ese.getEntityType().equals(EntityType.ZOMBIE)
				|| ese.getEntityType().equals(EntityType.SKELETON)
				|| ese.getEntityType().equals(EntityType.SILVERFISH)) {
			ese.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void onRegainHealth(EntityRegainHealthEvent rhe) {
		if (rhe.getEntityType() != EntityType.PLAYER) {
			return;
		}

		Player player = (Player) rhe.getEntity();
		Game game = Main.getInstance().getGameManager().getGameOfPlayer(player);

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
			Game game = Main.getInstance().getGameManager().getGameOfPlayer(player);
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
			player.sendMessage(ChatWriter.pluginMessage(ChatColor.RED + Main._l("errors.entitynotcompatible")));
			return;
		}

		LivingEntity living = (LivingEntity) entity;
		living.setRemoveWhenFarAway(false);
		living.setCanPickupItems(false);
		living.setCustomName(value.getTeam().getChatColor() + value.getTeam().getDisplayName());
		living.setCustomNameVisible(Main.getInstance().getBooleanConfig("jointeam-entity.show-name", true));

		if (living.getType().equals(EntityType.valueOf("ARMOR_STAND"))) {
			Utils.equipArmorStand(living, value.getTeam());
		}

		player.removeMetadata("bw-addteamjoin", Main.getInstance());
		player.sendMessage(ChatWriter.pluginMessage(ChatColor.GREEN + Main._l("success.teamjoinadded", ImmutableMap
				.of("team", value.getTeam().getChatColor() + value.getTeam().getDisplayName() + ChatColor.GREEN))));
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onEntityDamage(EntityDamageEvent ede) {
		List<EntityType> canDamageTypes = new ArrayList<EntityType>();
		canDamageTypes.add(EntityType.PLAYER);

		if (Main.getInstance().getServer().getPluginManager().isPluginEnabled("AntiAura")
				|| Main.getInstance().getServer().getPluginManager().isPluginEnabled("AAC")) {
			canDamageTypes.add(EntityType.SQUID);
		}

		if (canDamageTypes.contains(ede.getEntityType())) {
			return;
		}

		Game game = Main.getInstance().getGameManager().getGameByLocation(ede.getEntity().getLocation());
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

		if (Main.getInstance().getServer().getPluginManager().isPluginEnabled("AntiAura")
				|| Main.getInstance().getServer().getPluginManager().isPluginEnabled("AAC")) {
			canDamageTypes.add(EntityType.SQUID);
		}

		if (canDamageTypes.contains(ede.getEntityType())) {
			return;
		}

		Game game = Main.getInstance().getGameManager().getGameByLocation(ede.getEntity().getLocation());
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

		if (event.getBlock().getType() != Material.SOIL && event.getBlock().getType() != Material.WHEAT) {
			return;
		}

		Player player = (Player) event.getEntity();
		Game game = Main.getInstance().getGameManager().getGameOfPlayer(player);

		if (game == null) {
			return;
		}

		if (game.getState() == GameState.WAITING) {
			event.setCancelled(true);
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

		Game game = Main.getInstance().getGameManager().getGameByLocation(eev.getEntity().getLocation());

		if (game == null) {
			return;
		}

		if (game.getState() == GameState.STOPPED) {
			return;
		}

		Iterator<Block> explodeBlocks = eev.blockList().iterator();
		boolean tntDestroyEnabled = Main.getInstance().getBooleanConfig("explodes.destroy-worldblocks", false);
		boolean tntDestroyBeds = Main.getInstance().getBooleanConfig("explodes.destroy-beds", false);

		if (!Main.getInstance().getBooleanConfig("explodes.drop-blocks", false)) {
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
					if (Main.getInstance().isBreakableType(exploding.getType())) {
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
}
