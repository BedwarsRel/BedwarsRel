package io.github.bedwarsrel.listener;

import io.github.bedwarsrel.BedwarsRel;
import io.github.bedwarsrel.game.Game;
import io.github.bedwarsrel.game.GameState;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkUnloadEvent;

public class ChunkListener implements Listener {

  @EventHandler
  public void onUnload(ChunkUnloadEvent unload) {
    Game game = BedwarsRel.getInstance().getGameManager()
        .getGameByChunkLocation(unload.getChunk().getX(),
            unload.getChunk().getZ());
    if (game == null) {
      return;
    }

    if (game.getState() != GameState.RUNNING) {
      return;
    }

    unload.setCancelled(true);
  }

}
