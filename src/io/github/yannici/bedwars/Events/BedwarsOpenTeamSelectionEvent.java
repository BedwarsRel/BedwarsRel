package io.github.yannici.bedwars.Events;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import io.github.yannici.bedwars.Game.Game;

public class BedwarsOpenTeamSelectionEvent extends Event implements Cancellable {

	private static final HandlerList handlers = new HandlerList();
	private Player player = null;
	private Game game = null;
	private boolean cancelled = false;

	public BedwarsOpenTeamSelectionEvent(Game game, Player player) {
		this.player = player;
		this.game = game;
	}

	@Override
	public HandlerList getHandlers() {
		return BedwarsOpenTeamSelectionEvent.handlers;
	}

	public static HandlerList getHandlerList() {
		return BedwarsOpenTeamSelectionEvent.handlers;
	}

	public CommandSender getPlayer() {
		return this.player;
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
