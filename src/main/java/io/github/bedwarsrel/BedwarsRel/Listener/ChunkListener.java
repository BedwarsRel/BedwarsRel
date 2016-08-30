package io.github.bedwarsrel.BedwarsRel.Listener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkUnloadEvent;

import io.github.bedwarsrel.BedwarsRel.Main;
import io.github.bedwarsrel.BedwarsRel.Game.Game;
import io.github.bedwarsrel.BedwarsRel.Game.GameState;

public class ChunkListener implements Listener {

  @EventHandler
  public void onUnload(ChunkUnloadEvent unload) {
    Game game = Main.getInstance().getGameManager().getGameByChunkLocation(unload.getChunk().getX(),
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
