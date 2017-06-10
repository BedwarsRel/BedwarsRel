package io.github.bedwarsrel.listener;

import io.github.bedwarsrel.BedwarsRel;
import io.github.bedwarsrel.game.Game;
import io.github.bedwarsrel.game.GameState;
import java.util.List;
import org.bukkit.event.EventHandler;
import org.bukkit.event.weather.WeatherChangeEvent;

public class WeatherListener extends BaseListener {

  @EventHandler
  public void onWeatherEvent(WeatherChangeEvent we) {
    if (we.isCancelled()) {
      return;
    }

    List<Game> games = BedwarsRel.getInstance().getGameManager().getGamesByWorld(we.getWorld());

    if (games.size() == 0) {
      return;
    }

    for (Game game : games) {
      if (game.getState() != GameState.STOPPED) {
        we.setCancelled(true);
        break;
      }
    }
  }

}
