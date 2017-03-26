package io.github.bedwarsrel.BedwarsRel.Events;

import io.github.bedwarsrel.BedwarsRel.Game.Game;
import io.github.bedwarsrel.BedwarsRel.Game.Team;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class BedwarsTargetBlockDestroyedEvent extends Event {

  private static final HandlerList handlers = new HandlerList();
  private Player player = null;
  private Team team = null;
  private Game game = null;

  public BedwarsTargetBlockDestroyedEvent(Game game, Player player, Team team) {
    this.player = player;
    this.team = team;
    this.game = game;
  }

  @Override
  public HandlerList getHandlers() {
    return BedwarsTargetBlockDestroyedEvent.handlers;
  }

  public static HandlerList getHandlerList() {
    return BedwarsTargetBlockDestroyedEvent.handlers;
  }

  public Player getPlayer() {
    return this.player;
  }

  public Team getTeam() {
    return this.team;
  }

  public Game getGame() {
    return this.game;
  }

}
