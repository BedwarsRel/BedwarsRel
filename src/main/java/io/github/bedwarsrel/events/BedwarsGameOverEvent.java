package io.github.bedwarsrel.events;

import io.github.bedwarsrel.game.Game;
import io.github.bedwarsrel.game.Team;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class BedwarsGameOverEvent extends Event implements Cancellable {

  private static final HandlerList handlers = new HandlerList();
  private boolean cancelled = false;
  private Game game = null;
  private Team winner = null;

  public BedwarsGameOverEvent(Game game, Team winner) {
    this.game = game;
    this.winner = winner;
  }

  public static HandlerList getHandlerList() {
    return BedwarsGameOverEvent.handlers;
  }

  public Game getGame() {
    return this.game;
  }

  @Override
  public HandlerList getHandlers() {
    return BedwarsGameOverEvent.handlers;
  }

  public Team getWinner() {
    return this.winner;
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
