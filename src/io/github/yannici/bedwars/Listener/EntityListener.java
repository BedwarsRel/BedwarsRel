package io.github.yannici.bedwars.Listener;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import io.github.yannici.bedwars.Main;
import io.github.yannici.bedwars.Utils;
import io.github.yannici.bedwars.Game.Game;
import io.github.yannici.bedwars.Game.GameState;

import org.bukkit.Material;
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
		Game game = Main.getInstance().getGameManager().getGameOfPlayer(player);
		
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
	    List<EntityType> canDamageTypes = Arrays.asList(
                EntityType.PLAYER // important lol
        );
        
        if (canDamageTypes.contains(ede.getEntityType())) {
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
	    List<EntityType> canDamageTypes = Arrays.asList(
	            EntityType.PLAYER // important lol
        );
	    
		if (canDamageTypes.contains(ede.getEntityType())) {
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
        
        Material targetMaterial = Utils.getMaterialByConfig("game-block", Material.BED_BLOCK);
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
            	
            	continue;
            }
            
        	if(game.getRegion().isPlacedBlock(exploding)) {
        		game.getRegion().removePlacedBlock(exploding);
        		continue;
        	}
        	
    		if(exploding.getType().equals(targetMaterial)) {
    			if(!tntDestroyBeds) {
    				explodeBlocks.remove();
    				continue;
    			}
    			
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
			    if(!game.handleDestroyTargetMaterial(p, exploding)) {
			    	explodeBlocks.remove();
			    	continue;
			    }
    		} else {
    			game.getRegion().addBreakedBlock(exploding);
    		}
        }
    }
}
