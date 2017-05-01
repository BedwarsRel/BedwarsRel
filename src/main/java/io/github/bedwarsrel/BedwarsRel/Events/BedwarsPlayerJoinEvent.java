package io.github.bedwarsrel.BedwarsRel.Events;

import io.github.bedwarsrel.BedwarsRel.Game.Game;
import io.github.bedwarsrel.BedwarsRel.Main;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class BedwarsPlayerJoinEvent extends Event implements Cancellable {

  private static final HandlerList handlers = new HandlerList();
  private boolean cancelled = false;
  private Game game = null;
  private Player player = null;
  private Boolean kickOnCancel = true;

  public BedwarsPlayerJoinEvent(Game game, Player player) {
    this.game = game;
    this.player = player;
  }

  public static HandlerList getHandlerList() {
    return BedwarsPlayerJoinEvent.handlers;
  }

  public Game getGame() {
    return this.game;
  }

  @Override
  public HandlerList getHandlers() {
    return BedwarsPlayerJoinEvent.handlers;
  }

  public Player getPlayer() {
    return this.player;
  }

  public void setKickOnCancel(Boolean kick){
    this.kickOnCancel = kick;
  }

  public Boolean getKickOnCancel() {
    return kickOnCancel;
  }

  @Override
  public boolean isCancelled() {
    return this.cancelled;
  }

  @Override
  public void setCancelled(boolean cancel) {
    this.cancelled = cancel;
  }

  public void setPlayerLocale(String locale) {
    if (Main.getInstance().getPlayerLocales().containsKey(this.player.getUniqueId()) && !locale
        .equals(Main.getInstance().getPlayerLocales().get(this.player.getUniqueId()))) {
      Main.getInstance().getPlayerLocales().remove(this.player.getUniqueId());
    }
    Main.getInstance().getPlayerLocales().put(this.player.getUniqueId(), locale);
  }

}
