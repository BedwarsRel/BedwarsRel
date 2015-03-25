package io.github.yannici.bedwarsreloaded.Listener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.weather.WeatherChangeEvent;

public class WeatherListener extends BaseListener {

    public WeatherListener() {
        super();
    }

    @EventHandler
    public void onWeatherEvent(WeatherChangeEvent we) {
        we.setCancelled(true);
    }

}
