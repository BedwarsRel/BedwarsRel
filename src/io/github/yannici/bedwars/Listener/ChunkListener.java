package io.github.yannici.bedwars.Listener;

import io.github.yannici.bedwars.Main;
import io.github.yannici.bedwars.Game.Game;
import io.github.yannici.bedwars.Game.GameState;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkUnloadEvent;

public class ChunkListener implements Listener {

    @EventHandler
    public void onUnload(ChunkUnloadEvent unload) {
        Game game = Main.getInstance().getGameManager().getGameByWorld(unload.getWorld());
        if(game == null) {
            return;
        }
        
        if(game.getState() == GameState.STOPPED) {
            return;
        }
        
        if(game.getRegion().chunkIsInRegion(unload.getChunk())) {
            unload.setCancelled(true);
        }
    }

}
