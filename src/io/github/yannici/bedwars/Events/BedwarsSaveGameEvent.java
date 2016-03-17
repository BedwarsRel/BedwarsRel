package io.github.yannici.bedwars.Events;

import org.bukkit.command.CommandSender;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import io.github.yannici.bedwars.Game.Game;

public class BedwarsSaveGameEvent extends Event implements Cancellable {

	private static final HandlerList handlers = new HandlerList();
	private CommandSender sender = null;
	private Game game = null;
	private boolean cancelled = false;

	public BedwarsSaveGameEvent(Game game, CommandSender sender) {
		this.sender = sender;
		this.game = game;
	}

	@Override
	public HandlerList getHandlers() {
		return BedwarsSaveGameEvent.handlers;
	}

	public static HandlerList getHandlerList() {
		return BedwarsSaveGameEvent.handlers;
	}

	public CommandSender getSender() {
		return this.sender;
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
