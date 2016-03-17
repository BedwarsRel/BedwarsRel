package io.github.yannici.bedwars.Events;

import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import io.github.yannici.bedwars.Game.Game;

public class BedwarsGameStartEvent extends Event implements Cancellable {

	private static final HandlerList handlers = new HandlerList();
	private boolean cancelled = false;
	private Game game = null;

	public BedwarsGameStartEvent(Game game) {
		this.game = game;
	}

	@Override
	public HandlerList getHandlers() {
		return BedwarsGameStartEvent.handlers;
	}

	public static HandlerList getHandlerList() {
		return BedwarsGameStartEvent.handlers;
	}

	public Game getGame() {
		return this.game;
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
