package io.github.yannici.bedwars.Shop.Specials;

import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;

import io.github.yannici.bedwars.ChatWriter;
import io.github.yannici.bedwars.Main;
import io.github.yannici.bedwars.Game.Game;
import io.github.yannici.bedwars.Game.GameState;

public class WarpPowderListener implements Listener {

	public WarpPowderListener() {
		super();
	}

	@EventHandler
	public void onInteract(PlayerInteractEvent ev) {
		Player player = ev.getPlayer();
		Game game = Main.getInstance().getGameManager().getGameOfPlayer(player);

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

		if (ev.getMaterial().equals(warpPowder.getActivatedMaterial())) {
			if (ev.getItem().getItemMeta().getDisplayName() == null) {
				return;
			}

			if (!ev.getItem().getItemMeta().getDisplayName().equals(Main._l("ingame.specials.warp-powder.cancel"))) {
				return;
			}

			if (powder != null) {
				player.getInventory().addItem(powder.getStack());
				player.updateInventory();
				powder.cancelTeleport(true, true);
				ev.setCancelled(true);
			}

			return;
		}

		if (powder != null) {
			player.sendMessage(ChatWriter.pluginMessage(Main._l("ingame.specials.warp-powder.multiuse")));
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
		Game game = Main.getInstance().getGameManager().getGameOfPlayer(player);

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
			if (!powder.getPlayer().equals(player)) {
				powder = null;
				continue;
			}
			break;
		}

		if (powder != null) {
			player.getInventory().addItem(powder.getStack());
			player.updateInventory();
			powder.cancelTeleport(true, true);
			return;
		}
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
		Game game = Main.getInstance().getGameManager().getGameOfPlayer(player);

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

}
