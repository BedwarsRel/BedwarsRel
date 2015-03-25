package io.github.yannici.bedwarsreloaded.Listener;

import io.github.yannici.bedwarsreloaded.Game.Game;
import io.github.yannici.bedwarsreloaded.Game.GameState;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockBreakEvent;

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

}
