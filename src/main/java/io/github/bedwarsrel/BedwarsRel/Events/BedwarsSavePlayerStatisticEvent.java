package io.github.bedwarsrel.BedwarsRel.Events;

import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import io.github.bedwarsrel.BedwarsRel.Statistics.PlayerStatistic;

public class BedwarsSavePlayerStatisticEvent extends Event implements Cancellable {

  private static final HandlerList handlers = new HandlerList();
  private PlayerStatistic playerStatistic = null;
  private boolean cancelled = false;

  public BedwarsSavePlayerStatisticEvent(PlayerStatistic playerStatistic) {
    this.playerStatistic = playerStatistic;
  }

  @Override
  public HandlerList getHandlers() {
    return BedwarsSavePlayerStatisticEvent.handlers;
  }

  public static HandlerList getHandlerList() {
    return BedwarsSavePlayerStatisticEvent.handlers;
  }

  public PlayerStatistic getPlayerStatistic() {
    return this.playerStatistic;
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
