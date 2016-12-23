package io.github.bedwarsrel.BedwarsRel.Events;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import io.github.bedwarsrel.BedwarsRel.Game.Team;

public class BedwarsPlayerSetNameEvent extends Event implements Cancellable {

  private static final HandlerList handlers = new HandlerList();
  private Team team = null;
  private Player player = null;
  private String displayName = null;
  private String playerListName = null;
  private boolean cancelled = false;

  public BedwarsPlayerSetNameEvent(Team team, String displayName, String playerListName,
      Player player) {
    this.team = team;
    this.player = player;
    this.displayName = displayName;
    this.playerListName = playerListName;
  }

  @Override
  public HandlerList getHandlers() {
    return BedwarsPlayerSetNameEvent.handlers;
  }

  public static HandlerList getHandlerList() {
    return BedwarsPlayerSetNameEvent.handlers;
  }

  public Team getTeam() {
    return this.team;
  }

  public String getDisplayName() {
    return this.displayName;
  }

  public void setDisplayName(String displayName) {
    this.displayName = displayName;
  }

  public String getPlayerListName() {
    return this.playerListName;
  }

  public void getPlayerListName(String playerListName) {
    this.playerListName = playerListName;
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
