package io.github.yannici.bedwarsreloaded.Game.Events;

import io.github.yannici.bedwarsreloaded.Game.Game;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class BedwarsPlayerLeaveEvent extends Event {
	
	private static final HandlerList handlers = new HandlerList();
	private Game game = null;
	private Player player = null;
	
	public BedwarsPlayerLeaveEvent(Game game, Player player) {
		this.game = game;
		this.player = player;
	}

	@Override
	public HandlerList getHandlers() {
		return BedwarsPlayerLeaveEvent.handlers;
	}
	
	public static HandlerList getHandlerList() {
		return BedwarsPlayerLeaveEvent.handlers;
	}
	
	public Game getGame() {
		return this.game;
	}
	
	public Player getPlayer() {
		return this.player;
	}

}
