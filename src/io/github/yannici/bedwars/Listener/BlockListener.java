package io.github.yannici.bedwars.Listener;

import java.util.List;

import io.github.yannici.bedwars.ChatWriter;
import io.github.yannici.bedwars.Main;
import io.github.yannici.bedwars.Game.Game;
import io.github.yannici.bedwars.Game.GameState;
import io.github.yannici.bedwars.Game.Team;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.material.Bed;

import com.google.common.collect.ImmutableMap;

public class BlockListener extends BaseListener {

	public BlockListener() {
		super();
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onBreak(BlockBreakEvent e) {
		Player p = e.getPlayer();

		Game g = Game.getGameOfPlayer(p);
		if (g == null) {
			Block breaked = e.getBlock();
			Material type = e.getBlock().getType();
			if (type != Material.SIGN && type != Material.SIGN_POST
					&& type != Material.WALL_SIGN) {
				return;
			}

			Game game = Main.getInstance().getGameManager()
					.getGameBySignLocation(breaked.getLocation());
			if (game == null) {
				return;
			}

			game.removeJoinSign(breaked.getLocation());
			return;
		}

		if (g.getState() != GameState.RUNNING
				&& g.getState() != GameState.WAITING) {
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

		if (e.getBlock().getType() == Material.BED_BLOCK) {
			e.setCancelled(true);

			Team team = Game.getPlayerTeam(p, g);
			if (team == null) {
				return;
			}

			Block bedBlock = team.getBed();
			Block breakBlock = e.getBlock();
			Bed breakBed = (Bed) e.getBlock().getState().getData();

			if (!breakBed.isHeadOfBed()) {
				breakBlock = breakBlock.getRelative(breakBed.getFacing());
				breakBed = (Bed) breakBlock.getState().getData();
			}

			if (bedBlock.equals(breakBlock)) {
				p.sendMessage(ChatWriter.pluginMessage(ChatColor.RED
						+ Main._l("ingame.blocks.ownbeddestroy")));
				return;
			}

			Team bedDestroyTeam = Game.getTeamOfBed(g, breakBlock);
			if (bedDestroyTeam == null) {
				return;
			}

			Block neighbor = breakBlock.getRelative(breakBed.getFacing()
					.getOppositeFace());
			neighbor.getDrops().clear();
			neighbor.setType(Material.AIR);
			breakBlock.getDrops().clear();
			breakBlock.setType(Material.AIR);

			g.broadcast(ChatColor.RED
					+ Main._l(
							"ingame.blocks.beddestroyed",
							ImmutableMap.of("team",
									bedDestroyTeam.getChatColor()
											+ bedDestroyTeam.getName()
											+ ChatColor.RED)));
			g.broadcastSound(Sound.ENDERDRAGON_GROWL, 30.0F, 10.0F);
			g.setPlayersScoreboard();
			return;
		}

		if (e.getBlock().getType() == Material.ENDER_CHEST) {
			Team playerTeam = Game.getPlayerTeam(p, g);
			Block breakedBlock = e.getBlock();

			if (playerTeam.getInventory() == null) {
				playerTeam.createTeamInventory();
			}

			for (Team team : g.getTeams().values()) {
				List<Block> teamChests = team.getChests();
				if (teamChests.contains(breakedBlock)) {
					team.removeChest(breakedBlock);
				}
			}
		}

		if (g.getRegion().getBlocks(false).contains(e.getBlock())) {
			e.setCancelled(true);
		}
	}

	@EventHandler
	public void onPlace(BlockPlaceEvent bpe) {
		Player player = bpe.getPlayer();
		Game game = Game.getGameOfPlayer(player);

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

			if (placeBlock.getType() == Material.BED
					|| placeBlock.getType() == Material.BED_BLOCK) {
				bpe.setCancelled(true);
				bpe.setBuild(false);
			}

			if (!game.getRegion().isInRegion(placeBlock.getLocation())) {
				bpe.setCancelled(true);
				bpe.setBuild(false);
			}

			if (placeBlock.getType() == Material.ENDER_CHEST) {
				Team playerTeam = Game.getPlayerTeam(player, game);
				if (playerTeam.getInventory() == null) {
					playerTeam.createTeamInventory();
				}

				playerTeam.addChest(placeBlock);
			}
		}
	}

}
