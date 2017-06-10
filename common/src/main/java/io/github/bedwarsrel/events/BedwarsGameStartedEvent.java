package io.github.bedwarsrel.events;

import io.github.bedwarsrel.game.Game;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class BedwarsGameStartedEvent extends Event {

  private static final HandlerList handlers = new HandlerList();
  private Game game = null;

  public BedwarsGameStartedEvent(Game game) {
    this.game = game;
  }

  public static HandlerList getHandlerList() {
    return BedwarsGameStartedEvent.handlers;
  }

  public Game getGame() {
    return this.game;
  }

  @Override
  public HandlerList getHandlers() {
    return BedwarsGameStartedEvent.handlers;
  }

}
