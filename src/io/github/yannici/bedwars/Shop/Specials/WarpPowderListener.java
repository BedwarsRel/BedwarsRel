package io.github.yannici.bedwars.Shop.Specials;

import io.github.yannici.bedwars.Game.Game;
import io.github.yannici.bedwars.Game.GameState;

import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;

public class WarpPowderListener implements Listener {

    public WarpPowderListener() {
        super();
    }
    
    @EventHandler
    public void onInteract(PlayerInteractEvent ev) {
        final Player player = ev.getPlayer();
        final Game game = Game.getGameOfPlayer(player);    
        
        if(game == null) {
            return;
        }
        
        if(game.getState() != GameState.RUNNING) {
            return;
        }
        
        WarpPowder warpPowder = new WarpPowder();
        if(!ev.getMaterial().equals(warpPowder.getItemMaterial())) {
            return;
        }
        
        if(player.getLocation().getBlock().getRelative(BlockFace.DOWN).getType() == Material.AIR) {
            return;
        }
        
        warpPowder.setPlayer(player);
        warpPowder.setGame(game);
        warpPowder.runTask();
    }

}
