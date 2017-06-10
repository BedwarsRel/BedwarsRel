package io.github.bedwarsrel.listener;

import io.github.bedwarsrel.BedwarsRel;
import org.bukkit.event.Listener;

public abstract class BaseListener implements Listener {

  public BaseListener() {
    this.registerEvents();
  }

  private void registerEvents() {
    BedwarsRel.getInstance().getServer().getPluginManager()
        .registerEvents(this, BedwarsRel.getInstance());
  }

}
