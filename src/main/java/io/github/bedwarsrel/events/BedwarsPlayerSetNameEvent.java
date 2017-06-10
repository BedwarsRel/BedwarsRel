package io.github.bedwarsrel.events;

import io.github.bedwarsrel.game.Team;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class BedwarsPlayerSetNameEvent extends Event implements Cancellable {

  private static final HandlerList handlers = new HandlerList();
  private boolean cancelled = false;
  private String displayName = null;
  private Player player = null;
  private String playerListName = null;
  private Team team = null;

  public BedwarsPlayerSetNameEvent(Team team, String displayName, String playerListName,
      Player player) {
    this.team = team;
    this.player = player;
    this.displayName = displayName;
    this.playerListName = playerListName;
  }

  public static HandlerList getHandlerList() {
    return BedwarsPlayerSetNameEvent.handlers;
  }

  public String getDisplayName() {
    return this.displayName;
  }

  public void setDisplayName(String displayName) {
    this.displayName = displayName;
  }

  @Override
  public HandlerList getHandlers() {
    return BedwarsPlayerSetNameEvent.handlers;
  }

  public Player getPlayer() {
    return this.player;
  }

  public String getPlayerListName() {
    return this.playerListName;
  }

  public void setPlayerListName(String playerListName) {
    this.playerListName = playerListName;
  }

  public Team getTeam() {
    return this.team;
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
