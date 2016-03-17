package io.github.yannici.bedwars.Events;

import java.util.HashMap;

import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import io.github.yannici.bedwars.Game.Game;
import io.github.yannici.bedwars.Villager.MerchantCategory;

public class BedwarsOpenShopEvent extends Event implements Cancellable {

	private static final HandlerList handlers = new HandlerList();
	private Player player = null;
	private Game game = null;
	private HashMap<Material, MerchantCategory> itemshop = null;
	private Entity clickedEntity = null;
	private boolean cancelled = false;

	public BedwarsOpenShopEvent(Game game, Player player, HashMap<Material, MerchantCategory> itemshop,
			Entity clickedEntity) {
		this.player = player;
		this.game = game;
		this.itemshop = itemshop;
		this.clickedEntity = clickedEntity;
	}

	@Override
	public HandlerList getHandlers() {
		return BedwarsOpenShopEvent.handlers;
	}

	public static HandlerList getHandlerList() {
		return BedwarsOpenShopEvent.handlers;
	}

	public CommandSender getPlayer() {
		return this.player;
	}

	public Game getGame() {
		return this.game;
	}

	public HashMap<Material, MerchantCategory> getItemshop() {
		return this.itemshop;
	}

	public Entity getEntity() {
		return this.clickedEntity;
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
