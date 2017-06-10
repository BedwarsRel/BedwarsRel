package io.github.bedwarsrel.events;

import io.github.bedwarsrel.game.Game;
import io.github.bedwarsrel.villager.MerchantCategory;
import java.util.HashMap;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class BedwarsOpenShopEvent extends Event implements Cancellable {

  private static final HandlerList handlers = new HandlerList();
  private boolean cancelled = false;
  private Entity clickedEntity = null;
  private Game game = null;
  private HashMap<Material, MerchantCategory> itemshop = null;
  private Player player = null;

  public BedwarsOpenShopEvent(Game game, Player player,
      HashMap<Material, MerchantCategory> itemshop, Entity clickedEntity) {
    this.player = player;
    this.game = game;
    this.itemshop = itemshop;
    this.clickedEntity = clickedEntity;
  }

  public static HandlerList getHandlerList() {
    return BedwarsOpenShopEvent.handlers;
  }

  public Entity getEntity() {
    return this.clickedEntity;
  }

  public Game getGame() {
    return this.game;
  }

  @Override
  public HandlerList getHandlers() {
    return BedwarsOpenShopEvent.handlers;
  }

  public HashMap<Material, MerchantCategory> getItemshop() {
    return this.itemshop;
  }

  public CommandSender getPlayer() {
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
