package io.github.yannici.bedwars.Listener;

import java.util.Arrays;
import java.util.Iterator;
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
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.material.Bed;

import com.google.common.collect.ImmutableMap;

public class EntityListener extends BaseListener {

	public EntityListener() {
		super();
	}
	
	@EventHandler
	public void onEntitySpawn(EntitySpawnEvent ese) {
	    if(Main.getInstance().getGameManager() == null) {
	        return;
	    }
	    
	    if(ese.getLocation() == null) {
	        return;
	    }
	    
	    if(ese.getLocation().getWorld() == null) {
	        return;
	    }
	    
	    Game game = Main.getInstance().getGameManager().getGameByLocation(ese.getLocation());
	    if(game == null) {
	        return;
	    }
	    
	    if(game.getState() == GameState.STOPPED) {
	       return; 
	    }
	    
	    if(ese.getEntityType().equals(EntityType.CREEPER)
                || ese.getEntityType().equals(EntityType.CAVE_SPIDER)
                || ese.getEntityType().equals(EntityType.SPIDER)
                || ese.getEntityType().equals(EntityType.ZOMBIE)
                || ese.getEntityType().equals(EntityType.SKELETON)
                || ese.getEntityType().equals(EntityType.SILVERFISH)) {
	        ese.setCancelled(true);
	    }
	}
	
	@EventHandler(priority = EventPriority.HIGH)
	public void onRegainHealth(EntityRegainHealthEvent rhe) {
		if(rhe.getEntityType() != EntityType.PLAYER) {
			return;
		}
		
		Player player = (Player)rhe.getEntity();
		Game game = Game.getGameOfPlayer(player);
		
		if(game == null) {
			return;
		}
		
		if(game.getState() != GameState.RUNNING) {
			return;
		}
		
		if(player.getHealth() >= player.getMaxHealth()) {
			game.setPlayerDamager(player, null);
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onEntityDamage(EntityDamageEvent ede) {
		if (ede.getEntityType() != EntityType.VILLAGER) {
			return;
		}

		Game game = Main.getInstance().getGameManager()
				.getGameByLocation(ede.getEntity().getLocation());
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
	    List<EntityType> notAllowedTypes = Arrays.asList(
	            EntityType.PLAYER // important lol
        );
	    
		if (notAllowedTypes.contains(ede.getEntityType())) {
			return;
		}
		
		Game game = Main.getInstance().getGameManager()
				.getGameByLocation(ede.getEntity().getLocation());
		if (game == null) {
			return;
		}

		if (game.getState() == GameState.STOPPED) {
			return;
		}
		
		ede.setCancelled(true);
	}
	
	@EventHandler
    public void onExplodeDestroy(EntityExplodeEvent eev) {
	    if(eev.isCancelled()) {
	        return;
	    }
	    
        if(eev.getEntity() == null) {
            return;
        }
        
        if(eev.getEntity().getWorld() == null) {
            return;
        }
        
        Game game = Main.getInstance().getGameManager().getGameByLocation(eev.getEntity().getLocation());
        
        if(game == null) {
            return;
        }
        
        if(game.getState() == GameState.STOPPED) {
            return;
        }
        
        Iterator<Block> explodeBlocks = eev.blockList().iterator();
        boolean tntDestroyEnabled = Main.getInstance().getBooleanConfig("explodes.destroy-worldblocks", false);
        boolean tntDestroyBeds = Main.getInstance().getBooleanConfig("explodes.destroy-beds", false);
        
        if(!Main.getInstance().getBooleanConfig("explodes.drop-blocks", false)) {
        	eev.setYield(0F);
        }
        while(explodeBlocks.hasNext()) {
            Block exploding = explodeBlocks.next();
            if(!game.getRegion().isInRegion(exploding.getLocation())) {
            	explodeBlocks.remove();
            	continue;
            }
            
            if(!tntDestroyEnabled) {
            	if(!game.getRegion().isPlacedBlock(exploding)) {
                    explodeBlocks.remove();
                } else {
                	game.getRegion().removePlacedBlock(exploding);
                }
            } else {
            	if(game.getRegion().isPlacedBlock(exploding)) {
            		game.getRegion().removePlacedBlock(exploding);
            	} else {
            		if(exploding.getType().equals(Material.BED)
            				|| exploding.getType().equals(Material.BED_BLOCK)) {
            			if(!tntDestroyBeds) {
            				explodeBlocks.remove();
            			} else {
            			    // only destroyable by tnt
            			    if(!eev.getEntityType().equals(EntityType.PRIMED_TNT)
            			    		&& !eev.getEntityType().equals(EntityType.MINECART_TNT)) {
            			        explodeBlocks.remove();
            			        continue;
            			    }
            			    
            			    // when it wasn't player who ignited the tnt
            			    TNTPrimed primedTnt = (TNTPrimed)eev.getEntity();
            			    if(!(primedTnt.getSource() instanceof Player)) {
            			        explodeBlocks.remove();
                                continue;
            			    }
            			    
            			    Player p = (Player)primedTnt.getSource();
        		            Team team = Game.getPlayerTeam(p, game);
        		            if (team == null) {
        		                explodeBlocks.remove();
        		                continue;
        		            }

        		            Block bedBlock = team.getHeadBed();
        		            Block breakBlock = exploding;
        		            Block neighbor = null;
        		            Bed breakBed = (Bed) breakBlock.getState().getData();
        		            
        		            if (!breakBed.isHeadOfBed()) {
        		                explodeBlocks.remove();
                                continue;
        		            } else {
        		                neighbor = Utils.getBedNeighbor(breakBlock);
        		            }
        		            
        		            if (bedBlock.equals(breakBlock)) {
        		                p.sendMessage(ChatWriter.pluginMessage(ChatColor.RED
        		                        + Main._l("ingame.blocks.ownbeddestroy")));
        		                explodeBlocks.remove();
        		                continue;
        		            }

        		            Team bedDestroyTeam = Game.getTeamOfBed(game, breakBlock);
        		            if (bedDestroyTeam == null) {
        		                explodeBlocks.remove();
                                continue;
        		            }
        		            
        		            // set statistics
        		            if(Main.getInstance().statisticsEnabled()) {
        		                PlayerStatistic statistic = Main.getInstance().getPlayerStatisticManager().getStatistic(p);
        		                statistic.setDestroyedBeds(statistic.getDestroyedBeds()+1);
        		                statistic.addCurrentScore(Main.getInstance().getIntConfig("statistics.scores.bed-destroy", 25));
        		            }
        		            
        		            neighbor.getDrops().clear();
        		            neighbor.setType(Material.AIR);
        		            breakBlock.getDrops().clear();
        		            breakBlock.setType(Material.AIR);

        		            game.broadcast(ChatColor.RED
        		                    + Main._l(
        		                            "ingame.blocks.beddestroyed",
        		                            ImmutableMap.of("team",
        		                                    bedDestroyTeam.getChatColor()
        		                                            + bedDestroyTeam.getName()
        		                                            + ChatColor.RED,
        		                                            "player",
        		                                            Game.getPlayerWithTeamString(p, team, ChatColor.RED))));
        		            game.broadcastSound(Sound.ENDERDRAGON_GROWL, 30.0F, 10.0F);
        		            game.setPlayersScoreboard();
            			}
            		} else {
            			game.getRegion().addBreakedBlock(exploding);
            		}
            	}
            }
        }
    }

}
