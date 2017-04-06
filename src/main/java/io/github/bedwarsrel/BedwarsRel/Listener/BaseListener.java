package io.github.bedwarsrel.BedwarsRel.Listener;

import io.github.bedwarsrel.BedwarsRel.Main;
import org.bukkit.event.Listener;

public abstract class BaseListener implements Listener {

  public BaseListener() {
    this.registerEvents();
  }

  private void registerEvents() {
    Main.getInstance().getServer().getPluginManager().registerEvents(this, Main.getInstance());
  }

}
