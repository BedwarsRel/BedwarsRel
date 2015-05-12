package io.github.yannici.bedwars.Listener;

import java.util.List;

import io.github.yannici.bedwars.ChatWriter;
import io.github.yannici.bedwars.Main;
import io.github.yannici.bedwars.Utils;
import io.github.yannici.bedwars.Game.Game;
import io.github.yannici.bedwars.Game.GameState;
import io.github.yannici.bedwars.Game.Team;
import io.github.yannici.bedwars.Statistics.PlayerStatistic;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.material.Bed;

import com.google.common.collect.ImmutableMap;

public class BlockListener extends BaseListener {

	public BlockListener() {
		super();
	}
	
	@EventHandler(priority = EventPriority.HIGH)
	public void onBurn(BlockBurnEvent bbe) {
		Block block = bbe.getBlock();
		if(block == null) {
			return;
		}
		
		Game game = Main.getInstance().getGameManager().getGameByWorld(block.getWorld());
		if(game == null) {
			return;
		}
		
		if(game.getState() != GameState.RUNNING) {
			return;
		}
		
		if(!game.getRegion().isInRegion(block.getLocation())) {
			return;
		}
		
		bbe.setCancelled(true);
		return;
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onBreak(BlockBreakEvent e) {
		if(e.isCancelled()) {
			return;
		}
		
		Player p = e.getPlayer();
		if(p == null) {
			Block block = e.getBlock();
			if(block == null) {
				return;
			}
			
			Game game = Main.getInstance().getGameManager().getGameByWorld(block.getWorld());
			if(game == null) {
				return;
			}
			
			if(game.getState() != GameState.RUNNING) {
				return;
			}
			
			e.setCancelled(true);
			return;
		}
		
		Game g = Game.getGameOfPlayer(p);
		if (g == null) {
			Block breaked = e.getBlock();
			if (!(breaked.getState() instanceof Sign)) {
				return;
			}
			
			if(!p.hasPermission("bw.setup") || e.isCancelled()) {
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

			Block bedBlock = team.getHeadBed();
			Block breakBlock = e.getBlock();
			Block neighbor = null;
			Bed breakBed = (Bed) breakBlock.getState().getData();

			if (!breakBed.isHeadOfBed()) {
				neighbor = breakBlock;
				breakBlock = Utils.getBedNeighbor(neighbor);
			} else {
				neighbor = Utils.getBedNeighbor(breakBlock);
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
			
			// set statistics
			if(Main.getInstance().statisticsEnabled()) {
				PlayerStatistic statistic = Main.getInstance().getPlayerStatisticManager().getStatistic(p);
				statistic.setDestroyedBeds(statistic.getDestroyedBeds()+1);
				statistic.addCurrentScore(Main.getInstance().getIntConfig("statistics.scores.bed-destroy", 25));
			}
			
			// not used anymore
			//g.getRegion().addBreakedBlock(neighbor);
            //g.getRegion().addBreakedBlock(breakBlock);
            
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
											+ ChatColor.RED,
											"player",
                                            Game.getPlayerWithTeamString(p, team, ChatColor.RED))));
			g.broadcastSound(Sound.ENDERDRAGON_GROWL, 30.0F, 10.0F);
			g.setPlayersScoreboard();
			return;
		}
		
		Block breakedBlock = e.getBlock();

		if (!g.getRegion().isPlacedBlock(breakedBlock)) {
			e.setCancelled(true);
		} else {
		    
		    if (e.getBlock().getType() == Material.ENDER_CHEST) {
	            for (Team team : g.getTeams().values()) {
	                List<Block> teamChests = team.getChests();
	                if (teamChests.contains(breakedBlock)) {
	                    team.removeChest(breakedBlock);
	                }
	            }
	            
	            // Drop ender chest
	            ItemStack enderChest = new ItemStack(Material.ENDER_CHEST, 1);
	            ItemMeta meta = enderChest.getItemMeta();
	            meta.setDisplayName(Main._l("ingame.teamchest"));
	            enderChest.setItemMeta(meta);
	            
	            e.setCancelled(true);
	            breakedBlock.getDrops().clear();
	            breakedBlock.setType(Material.AIR);
	            breakedBlock.getWorld().dropItemNaturally(breakedBlock.getLocation(), enderChest);
	        }
		    
		    g.getRegion().removePlacedBlock(breakedBlock);
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
				return;
			}

			if (!game.getRegion().isInRegion(placeBlock.getLocation())) {
				bpe.setCancelled(true);
				bpe.setBuild(false);
				return;
			}
			
			if (placeBlock.getType() == Material.ENDER_CHEST) {
				Team playerTeam = Game.getPlayerTeam(player, game);
				if (playerTeam.getInventory() == null) {
					playerTeam.createTeamInventory();
				}

				playerTeam.addChest(placeBlock);
			}
			
			if(!bpe.isCancelled()) {
			    game.getRegion().addPlacedBlock(placeBlock, bpe.getBlockReplacedState());
			}
		}
	}

}
