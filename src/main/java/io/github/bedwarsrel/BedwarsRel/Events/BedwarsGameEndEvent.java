package io.github.bedwarsrel.BedwarsRel.Events;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import io.github.bedwarsrel.BedwarsRel.Game.Game;

public class BedwarsGameEndEvent extends Event {

  private static final HandlerList handlers = new HandlerList();
  private Game game = null;

  public BedwarsGameEndEvent(Game game) {
    this.game = game;
  }

  @Override
  public HandlerList getHandlers() {
    return BedwarsGameEndEvent.handlers;
  }

  public static HandlerList getHandlerList() {
    return BedwarsGameEndEvent.handlers;
  }

  public Game getGame() {
    return this.game;
  }

}
