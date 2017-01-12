package io.github.bedwarsrel.BedwarsRel.Events;

import org.bukkit.Location;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;

import io.github.bedwarsrel.BedwarsRel.Game.Game;

public class BedwarsResourceSpawnEvent extends Event implements Cancellable {

  private static final HandlerList handlers = new HandlerList();
  private Game game = null;
  private Location location = null;
  private ItemStack resource = null;
  private boolean cancelled = false;

  public BedwarsResourceSpawnEvent(Game game, Location location, ItemStack resource) {
    this.game = game;
    this.location = location;
    this.resource = resource;
  }

  @Override
  public HandlerList getHandlers() {
    return BedwarsResourceSpawnEvent.handlers;
  }

  public static HandlerList getHandlerList() {
    return BedwarsResourceSpawnEvent.handlers;
  }

  public Game getGame() {
    return this.game;
  }

  public Location getLocation() {
    return this.location;
  }

  public void setResource(ItemStack resource) {
    this.resource = resource;
  }

  public ItemStack getResource() {
    return this.resource;
  }

  @Override
  public boolean isCancelled() {
    return this.cancelled;
  }

  @Override
  public void setCancelled(boolean cancel) {
    this.cancelled = cancel;
  }

}
