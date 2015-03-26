package io.github.yannici.bedwarsreloaded.Listener;

import io.github.yannici.bedwarsreloaded.Game.Game;
import io.github.yannici.bedwarsreloaded.Game.GameState;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;

public class BlockListener extends BaseListener {

    public BlockListener() {
        super();
    }

    @EventHandler
    public void onBreak(BlockBreakEvent e) {
        Player p = e.getPlayer();

        Game g = Game.getGameOfPlayer(p);
        if(g == null) {
            return;
        }

        if(g.getState() != GameState.RUNNING && g.getState() != GameState.WAITING) {
            return;
        }

        if(g.getState() == GameState.WAITING) {
            e.setCancelled(true);
            return;
        }

        if(e.getBlock().getType() == Material.BED) {
            // TODO: Implement check if user is allowed to damage bed
            e.setCancelled(true);
            return;
        }

        if(g.getRegion().getBlocks(false).contains(e.getBlock())) {
            e.setCancelled(true);
        }
    }
    
    @EventHandler
    public void onPlace(BlockPlaceEvent bpe) {
    	Player player = bpe.getPlayer();
    	Game game = Game.getGameOfPlayer(player);
    	
    	if(game == null) {
    		return;
    	}
    	
    	if(game.getState() == GameState.STOPPED) {
    		return;
    	}
    	
    	if(game.getState() == GameState.WAITING) {
    		bpe.setCancelled(false);
    		bpe.setBuild(false);
    		return;
    	}
    	
    	if(game.getState() == GameState.RUNNING) {
    		Block placeBlock = bpe.getBlockPlaced();
        	
        	if(placeBlock.getType() == Material.BED || placeBlock.getType() == Material.BED_BLOCK) {
        		bpe.setCancelled(true);
        		bpe.setBuild(false);
        	}
    	}
    }

}
