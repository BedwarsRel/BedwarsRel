package io.github.bedwarsrel.events;

import io.github.bedwarsrel.game.Game;
import org.bukkit.command.CommandSender;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class BedwarsSaveGameEvent extends Event implements Cancellable {

  private static final HandlerList handlers = new HandlerList();
  private boolean cancelled = false;
  private Game game = null;
  private CommandSender sender = null;

  public BedwarsSaveGameEvent(Game game, CommandSender sender) {
    this.sender = sender;
    this.game = game;
  }

  public static HandlerList getHandlerList() {
    return BedwarsSaveGameEvent.handlers;
  }

  public Game getGame() {
    return this.game;
  }

  @Override
  public HandlerList getHandlers() {
    return BedwarsSaveGameEvent.handlers;
  }

  public CommandSender getSender() {
    return this.sender;
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
