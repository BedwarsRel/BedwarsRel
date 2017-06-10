package io.github.bedwarsrel.events;

import io.github.bedwarsrel.game.Game;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class BedwarsPlayerKilledEvent extends Event {

  private static final HandlerList handlers = new HandlerList();
  private Game game = null;
  private Player killer = null;
  private Player player = null;

  public BedwarsPlayerKilledEvent(Game game, Player player, Player killer) {
    this.player = player;
    this.killer = killer;
    this.game = game;
  }

  public static HandlerList getHandlerList() {
    return BedwarsPlayerKilledEvent.handlers;
  }

  public Game getGame() {
    return this.game;
  }

  @Override
  public HandlerList getHandlers() {
    return BedwarsPlayerKilledEvent.handlers;
  }

  public Player getKiller() {
    return this.killer;
  }

  public Player getPlayer() {
    return this.player;
  }

}
