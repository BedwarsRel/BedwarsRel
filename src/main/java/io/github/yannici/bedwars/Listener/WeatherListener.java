package io.github.yannici.bedwars.Listener;

import java.util.List;

import org.bukkit.event.EventHandler;
import org.bukkit.event.weather.WeatherChangeEvent;

import io.github.yannici.bedwars.Main;
import io.github.yannici.bedwars.Game.Game;
import io.github.yannici.bedwars.Game.GameState;

public class WeatherListener extends BaseListener {

	public WeatherListener() {
		super();
	}

	@EventHandler
	public void onWeatherEvent(WeatherChangeEvent we) {
		if (we.isCancelled()) {
			return;
		}

		List<Game> games = Main.getInstance().getGameManager().getGamesByWorld(we.getWorld());

		if (games.size() == 0) {
			return;
		}

		for (Game game : games) {
			if (game.getState() == GameState.STOPPED) {
				continue;
			}

			we.setCancelled(true);
			return;
		}
	}

}
