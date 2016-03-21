package io.github.yannici.bedwars.Listener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkUnloadEvent;

import io.github.yannici.bedwars.Main;
import io.github.yannici.bedwars.Game.Game;
import io.github.yannici.bedwars.Game.GameState;

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
		return;
	}

}
