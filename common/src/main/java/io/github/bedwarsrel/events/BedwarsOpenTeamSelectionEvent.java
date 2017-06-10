package io.github.bedwarsrel.events;

import io.github.bedwarsrel.game.Game;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class BedwarsOpenTeamSelectionEvent extends Event implements Cancellable {

  private static final HandlerList handlers = new HandlerList();
  private boolean cancelled = false;
  private Game game = null;
  private Player player = null;

  public BedwarsOpenTeamSelectionEvent(Game game, Player player) {
    this.player = player;
    this.game = game;
  }

  public static HandlerList getHandlerList() {
    return BedwarsOpenTeamSelectionEvent.handlers;
  }

  public Game getGame() {
    return this.game;
  }

  @Override
  public HandlerList getHandlers() {
    return BedwarsOpenTeamSelectionEvent.handlers;
  }

  public CommandSender getPlayer() {
    return this.player;
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
