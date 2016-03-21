package io.github.yannici.bedwars.Listener;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;

import io.github.yannici.bedwars.Main;
import io.github.yannici.bedwars.Game.BungeeGameCycle;
import io.github.yannici.bedwars.Game.Game;
import io.github.yannici.bedwars.Game.GameState;

public class Player19Listener extends BaseListener {

	public Player19Listener() {
		super();
	}

	/*
	 * GLOBAL
	 */


	@EventHandler
	public void onPlayerSwapHandItems(PlayerSwapHandItemsEvent event) {
		Player player = event.getPlayer();
		Game game = Main.getInstance().getGameManager().getGameOfPlayer(player);

		if (game == null) {
			return;
		}

		if (game.getState() == GameState.WAITING
				|| (game.getCycle() instanceof BungeeGameCycle && game.getCycle().isEndGameRunning()
						&& Main.getInstance().getBooleanConfig("bungeecord.endgame-in-lobby", true))) {
			event.setCancelled(true);
			return;
		}
	}

}
