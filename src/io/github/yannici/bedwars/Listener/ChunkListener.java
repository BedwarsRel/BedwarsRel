package io.github.yannici.bedwars.Listener;

import java.util.List;

import io.github.yannici.bedwars.Main;
import io.github.yannici.bedwars.Game.Game;
import io.github.yannici.bedwars.Game.GameState;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkUnloadEvent;

public class ChunkListener implements Listener {

    @EventHandler
    public void onUnload(ChunkUnloadEvent unload) {
        List<Game> games = Main.getInstance().getGameManager().getGamesByWorld(unload.getWorld());
        if(games.size() == 0) {
            return;
        }
        
        for(Game game : games) {
        	if(game.getState() == GameState.STOPPED) {
                continue;
            }
            
            if(game.getRegion().chunkIsInRegion(unload.getChunk())) {
                unload.setCancelled(true);
                return;
            }
        }
    }

}
