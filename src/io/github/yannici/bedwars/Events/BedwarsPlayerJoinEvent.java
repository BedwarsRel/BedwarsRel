package io.github.yannici.bedwars.Events;

import io.github.yannici.bedwars.Game.Game;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class BedwarsPlayerJoinEvent extends Event {

	private static final HandlerList handlers = new HandlerList();
	private Game game = null;
	private Player player = null;

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

}
