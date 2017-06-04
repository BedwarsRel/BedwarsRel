package io.github.bedwarsrel.events;

import io.github.bedwarsrel.game.Game;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class BedwarsUseTNTSheepEvent extends Event implements Cancellable {

  private static final HandlerList handlers = new HandlerList();
  private boolean cancelled = false;
  private Game game = null;
  private Player player = null;
  private Location startLocation = null;
  private Player targetPlayer = null;

  public BedwarsUseTNTSheepEvent(Game game, Player player, Player targetPlayer,
      Location startLocation) {
    this.player = player;
    this.game = game;
    this.startLocation = startLocation;
    this.targetPlayer = targetPlayer;
  }

  public static HandlerList getHandlerList() {
    return BedwarsUseTNTSheepEvent.handlers;
  }

  public Game getGame() {
    return this.game;
  }

  @Override
  public HandlerList getHandlers() {
    return BedwarsUseTNTSheepEvent.handlers;
  }

  public CommandSender getPlayer() {
    return this.player;
  }

  public Location getStartLocation() {
    return this.startLocation;
  }

  public void setStartLocation(Location loc) {
    this.startLocation = loc;
  }

  public Player getTargetPlayer() {
    return this.targetPlayer;
  }

  public void setTargetPlayer(Player target) {
    this.targetPlayer = target;
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
