package io.github.yannici.bedwars.Listener;

import java.util.Iterator;

import io.github.yannici.bedwars.Main;
import io.github.yannici.bedwars.Game.Game;
import io.github.yannici.bedwars.Game.GameState;

import org.bukkit.block.Block;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;

public class EntityListener extends BaseListener {

	public EntityListener() {
		super();
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
				.getGameByWorld(ede.getEntity().getWorld());
		if (game == null) {
			return;
		}

		if (game.getState() != GameState.WAITING
				&& game.getState() != GameState.RUNNING) {
			return;
		}

		ede.setCancelled(true);
	}
	
	@EventHandler
    public void onExplodeDestroy(EntityExplodeEvent eev) {
        if(eev.getEntity() == null) {
            return;
        }
        
        if(eev.getEntity().getWorld() == null) {
            return;
        }
        
        Game game = Main.getInstance().getGameManager().getGameByWorld(eev.getEntity().getWorld());
        
        if(game == null) {
            return;
        }
        
        if(game.getState() == GameState.STOPPED) {
            return;
        }
        
        Iterator<Block> explodeBlocks = eev.blockList().iterator();
        while(explodeBlocks.hasNext()) {
            Block exploding = explodeBlocks.next();
            if(game.getRegion().isInRegion(exploding.getLocation())
                    && !game.getRegion().isPlacedBlock(exploding)) {
                explodeBlocks.remove();
            }
        }
    }

}
