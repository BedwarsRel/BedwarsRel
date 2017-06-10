package io.github.bedwarsrel.events;

import io.github.bedwarsrel.game.Game;
import io.github.bedwarsrel.game.Team;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class BedwarsTargetBlockDestroyedEvent extends Event {

  private static final HandlerList handlers = new HandlerList();
  private Game game = null;
  private Player player = null;
  private Team team = null;

  public BedwarsTargetBlockDestroyedEvent(Game game, Player player, Team team) {
    this.player = player;
    this.team = team;
    this.game = game;
  }

  public static HandlerList getHandlerList() {
    return BedwarsTargetBlockDestroyedEvent.handlers;
  }

  public Game getGame() {
    return this.game;
  }

  @Override
  public HandlerList getHandlers() {
    return BedwarsTargetBlockDestroyedEvent.handlers;
  }

  public Player getPlayer() {
    return this.player;
  }

  public Team getTeam() {
    return this.team;
  }

}
