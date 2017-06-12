package io.github.bedwarsrel.events;

import io.github.bedwarsrel.BedwarsRel;
import io.github.bedwarsrel.game.Game;
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

  public Boolean getKickOnCancel() {
    return kickOnCancel;
  }

  public void setKickOnCancel(Boolean kick) {
    this.kickOnCancel = kick;
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
    if (BedwarsRel.getInstance().getPlayerLocales().containsKey(this.player.getUniqueId())
        && !locale
        .equals(BedwarsRel.getInstance().getPlayerLocales().get(this.player.getUniqueId()))) {
      BedwarsRel.getInstance().getPlayerLocales().remove(this.player.getUniqueId());
    }
    BedwarsRel.getInstance().getPlayerLocales().put(this.player.getUniqueId(), locale);
  }

}
