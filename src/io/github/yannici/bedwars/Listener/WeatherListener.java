package io.github.yannici.bedwars.Listener;

import io.github.yannici.bedwars.Main;
import io.github.yannici.bedwars.Game.Game;
import io.github.yannici.bedwars.Game.GameState;

import org.bukkit.event.EventHandler;
import org.bukkit.event.weather.WeatherChangeEvent;

public class WeatherListener extends BaseListener {

	public WeatherListener() {
		super();
	}

	@EventHandler
	public void onWeatherEvent(WeatherChangeEvent we) {
	    if(we.isCancelled()) {
	        return;
	    }
	    
	    Game game = Main.getInstance().getGameManager().getGameByWorld(we.getWorld());
	    
	    if(game == null) {
	        return;
	    }
	    
	    if(game.getState() == GameState.STOPPED) {
	        return;
	    }
	    
		we.setCancelled(true);
	}

}
