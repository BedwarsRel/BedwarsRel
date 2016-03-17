package io.github.yannici.bedwars.Events;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import io.github.yannici.bedwars.Game.Game;

public class BedwarsPlayerJoinEvent extends Event implements Cancellable {

	private static final HandlerList handlers = new HandlerList();
	private Game game = null;
	private Player player = null;
	private boolean cancelled = false;

	public BedwarsPlayerJoinEvent(Game game, Player player) {
		this.game = game;
		this.player = player;
	}

	@Override
	public HandlerList getHandlers() {
		return BedwarsPlayerJoinEvent.handlers;
	}

	public static HandlerList getHandlerList() {
		return BedwarsPlayerJoinEvent.handlers;
	}

	public Game getGame() {
		return this.game;
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
