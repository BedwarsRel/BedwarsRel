package io.github.bedwarsrel.BedwarsRel.Events;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import io.github.bedwarsrel.BedwarsRel.Game.Team;

public class BedwarsPlayerJoinTeamEvent extends Event implements Cancellable {

  private static final HandlerList handlers = new HandlerList();
  private Player player = null;
  private Team team = null;
  private boolean cancelled = false;

  public BedwarsPlayerJoinTeamEvent(Team team, Player player) {
    this.player = player;
    this.team = team;
  }

  @Override
  public HandlerList getHandlers() {
    return BedwarsPlayerJoinTeamEvent.handlers;
  }

  public static HandlerList getHandlerList() {
    return BedwarsPlayerJoinTeamEvent.handlers;
  }

  public Team getTeam() {
    return this.team;
  }

  public void setTeam(Team team) {
    this.team = team;
  }

  public Player getPlayer() {
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
