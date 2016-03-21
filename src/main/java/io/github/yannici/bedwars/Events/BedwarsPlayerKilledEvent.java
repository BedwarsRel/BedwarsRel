package io.github.yannici.bedwars.Events;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import io.github.yannici.bedwars.Game.Game;

public class BedwarsPlayerKilledEvent extends Event {

	private static final HandlerList handlers = new HandlerList();
	private Player player = null;
	private Player killer = null;
	private Game game = null;

	public BedwarsPlayerKilledEvent(Game game, Player player, Player killer) {
		this.player = player;
		this.killer = killer;
		this.game = game;
	}

	@Override
	public HandlerList getHandlers() {
		return BedwarsPlayerKilledEvent.handlers;
	}

	public static HandlerList getHandlerList() {
		return BedwarsPlayerKilledEvent.handlers;
	}

	public Player getPlayer() {
		return this.player;
	}

	public Player getKiller() {
		return this.killer;
	}

	public Game getGame() {
		return this.game;
	}

}
