package io.github.yannici.bedwars.Shop.Specials;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import com.google.common.collect.ImmutableMap;

import io.github.yannici.bedwars.ChatWriter;
import io.github.yannici.bedwars.Main;
import io.github.yannici.bedwars.Utils;
import io.github.yannici.bedwars.Game.Game;
import io.github.yannici.bedwars.Game.GameState;
import io.github.yannici.bedwars.Game.Team;

public class RescuePlatformListener implements Listener {

	public RescuePlatformListener() {
		super();
	}

	@SuppressWarnings("deprecation")
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

		if (ev.getAction().equals(Action.LEFT_CLICK_AIR) || ev.getAction().equals(Action.LEFT_CLICK_BLOCK)) {
			return;
		}

		RescuePlatform platform = new RescuePlatform();
		if (!ev.getMaterial().equals(platform.getItemMaterial())) {
			return;
		}

		for (SpecialItem item : game.getSpecialItems()) {
			if (!(item instanceof RescuePlatform)) {
				continue;
			}

			RescuePlatform rescuePlatform = (RescuePlatform) item;
			if (!rescuePlatform.getPlayer().equals(player)) {
				continue;
			}

			int waitleft = Main.getInstance().getConfig().getInt("specials.rescue-platform.using-wait-time", 20)
					- rescuePlatform.getLivingTime();
			player.sendMessage(ChatWriter.pluginMessage(Main._l("ingame.specials.rescue-platform.left",
					ImmutableMap.of("time", String.valueOf(waitleft)))));
			return;
		}

		boolean canBreak = Main.getInstance().getBooleanConfig("specials.rescue-platform.can-break", false);

		if (player.getLocation().getBlock().getRelative(BlockFace.DOWN).getType() != Material.AIR) {
			player.sendMessage(ChatWriter.pluginMessage(ChatColor.RED + Main._l("errors.notinair")));
			return;
		}

		Location mid = player.getLocation().clone();
		mid.setY(mid.getY() - 1.0D);

		Team team = game.getPlayerTeam(player);
		ItemStack usedStack = player.getInventory().getItemInHand();
		usedStack.setAmount(usedStack.getAmount() - 1);
		player.getInventory().setItem(player.getInventory().getHeldItemSlot(), usedStack);
		player.updateInventory();
		for (BlockFace face : BlockFace.values()) {
			if (face.equals(BlockFace.DOWN) || face.equals(BlockFace.UP)) {
				continue;
			}

			Block placed = mid.getBlock().getRelative(face);
			if (placed.getType() != Material.AIR) {
				continue;
			}

			Material configMaterial = Utils.getMaterialByConfig("specials.rescue-platform.block",
					Material.STAINED_GLASS);
			placed.setType(configMaterial);
			if (configMaterial.equals(Material.STAINED_GLASS) || configMaterial.equals(Material.WOOL)) {
				placed.setData(team.getColor().getDyeColor().getData());
			}

			if (!canBreak) {
				game.getRegion().addPlacedUnbreakableBlock(placed, null);
			} else {
				game.getRegion().addPlacedBlock(placed, null);
			}

			platform.addPlatformBlock(placed);
		}

		platform.setActivatedPlayer(player);
		platform.setGame(game);
		platform.runTask();

		game.addSpecialItem(platform);
	}

}
