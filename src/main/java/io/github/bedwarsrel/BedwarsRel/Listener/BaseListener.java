package io.github.bedwarsrel.BedwarsRel.Listener;

import org.bukkit.event.Listener;

import io.github.bedwarsrel.BedwarsRel.Main;

public abstract class BaseListener implements Listener {

  public BaseListener() {
    this.registerEvents();
  }

  private void registerEvents() {
    Main.getInstance().getServer().getPluginManager().registerEvents(this, Main.getInstance());
  }

}
