package io.github.yannici.bedwars.Shop.Specials;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

import io.github.yannici.bedwars.Main;
import io.github.yannici.bedwars.Events.BedwarsGameStartEvent;
import io.github.yannici.bedwars.Game.Game;
import io.github.yannici.bedwars.Game.GameState;

public class TrackerListener implements Listener {

	public TrackerListener() {
		super();
	}

	@EventHandler
	public void onInteract(PlayerInteractEvent ev) {
		Player player = ev.getPlayer();
		Game game = Main.getInstance().getGameManager().getGameOfPlayer(player);

		if (game == null) {
			return;
		}

		if (game.getState() != GameState.RUNNING) {
			return;
		}

		if (game.isSpectator(player)) {
			return;
		}

		Tracker tracker = new Tracker();
		if (!ev.getMaterial().equals(tracker.getItemMaterial())) {
			return;
		}

		if (ev.getAction().equals(Action.LEFT_CLICK_AIR) || ev.getAction().equals(Action.LEFT_CLICK_BLOCK)) {
			return;
		}

		tracker.setPlayer(player);
		tracker.setGame(game);
		tracker.trackPlayer();
		ev.setCancelled(true);
	}

	@EventHandler
	public void onStart(BedwarsGameStartEvent ev) {
		final Game game = ev.getGame();

		if (game == null) {
			return;
		}

		Tracker tracker = new Tracker();
		tracker.setGame(game);
		tracker.createTask();

	}

}
