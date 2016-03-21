package io.github.yannici.bedwars.Shop.Specials;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.SpawnEgg;

import io.github.yannici.bedwars.Main;
import io.github.yannici.bedwars.Utils;
import io.github.yannici.bedwars.Game.Game;
import io.github.yannici.bedwars.Game.GameState;
import io.github.yannici.bedwars.Game.Team;

public class TNTSheepListener implements Listener {

	public TNTSheepListener() {
		try {
			// register entities
			Class<?> tntRegisterClass = Main.getInstance().getVersionRelatedClass("TNTSheepRegister");
			ITNTSheepRegister register = (ITNTSheepRegister) tntRegisterClass.newInstance();
			register.registerEntities(Main.getInstance().getIntConfig("specials.tntsheep.entity-id", 91));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onInteract(PlayerInteractEvent event) {
		if (event.getPlayer() == null) {
			return;
		}

		Player player = event.getPlayer();

		Game game = Main.getInstance().getGameManager().getGameOfPlayer(player);

		if (game == null) {
			return;
		}

		if (game.getState() != GameState.RUNNING && !game.isStopping()) {
			return;
		}

		if (event.getPlayer().getItemInHand() == null) {
			return;
		}

		TNTSheep creature = new TNTSheep();
		ItemStack inHand = player.getItemInHand();
		if (inHand.getType() != creature.getItemMaterial()) {
			return;
		}

		if (!(inHand.getData() instanceof SpawnEgg)) {
			return;
		}

		if (((SpawnEgg) inHand.getData()).getSpawnedType() != EntityType.SHEEP) {
			return;
		}

		if (game.isSpectator(player)) {
			return;
		}

		Location startLocation = null;
		if (event.getClickedBlock() == null
				|| event.getClickedBlock().getRelative(BlockFace.UP).getType() != Material.AIR) {
			startLocation = player.getLocation().getBlock()
					.getRelative(Utils.getCardinalDirection(player.getLocation())).getLocation();
		} else {
			startLocation = event.getClickedBlock().getRelative(BlockFace.UP).getLocation();
		}

		creature.setPlayer(player);
		creature.setGame(game);
		creature.run(startLocation);
		event.setCancelled(true);
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onInteractOtherUser(PlayerInteractEntityEvent event) {
		if (event.getPlayer() == null) {
			return;
		}

		Player player = event.getPlayer();
		Game game = Main.getInstance().getGameManager().getGameOfPlayer(player);

		if (game == null) {
			return;
		}

		if (game.getState() != GameState.RUNNING) {
			return;
		}

		if (event.getRightClicked() == null) {
			return;
		}

		if (event.getRightClicked() instanceof ITNTSheep) {
			event.setCancelled(true);
			return;
		}

		if (event.getRightClicked().getVehicle() != null) {
			if (event.getRightClicked().getVehicle() instanceof ITNTSheep) {
				event.setCancelled(true);
				return;
			}
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onDamageEntity(EntityDamageByEntityEvent event) {
		if (event.getCause().equals(DamageCause.CUSTOM) || event.getCause().equals(DamageCause.VOID)
				|| event.getCause().equals(DamageCause.FALL)) {
			return;
		}

		if (event.getEntity() instanceof ITNTSheep) {
			event.setDamage(0.0);
			return;
		}

		if (!(event.getEntity() instanceof Player)) {
			return;
		}

		if (!(event.getDamager() instanceof TNTPrimed)) {
			return;
		}

		TNTPrimed damager = (TNTPrimed) event.getDamager();

		if (!(damager.getSource() instanceof Player)) {
			return;
		}

		Player damagerPlayer = (Player) damager.getSource();
		Player player = (Player) event.getEntity();
		Game game = Main.getInstance().getGameManager().getGameOfPlayer(player);

		if (game == null) {
			return;
		}

		if (game.getState() != GameState.RUNNING) {
			return;
		}

		if (game.isSpectator(damagerPlayer) || game.isSpectator(player)) {
			event.setCancelled(true);
			return;
		}

		Team damagerTeam = game.getPlayerTeam(damagerPlayer);
		Team team = game.getPlayerTeam(player);

		if (damagerTeam.equals(team) && !damagerTeam.getScoreboardTeam().allowFriendlyFire()) {
			event.setCancelled(true);
			return;
		}
	}

}
