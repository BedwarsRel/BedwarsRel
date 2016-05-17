package io.github.yannici.bedwars.Events;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import io.github.yannici.bedwars.Game.Game;
import io.github.yannici.bedwars.Game.Team;

public class BedwarsPlayerLeaveEvent extends Event {

	private static final HandlerList handlers = new HandlerList();
	private Game game = null;
	private Player player = null;
	private Team team = null;

	public BedwarsPlayerLeaveEvent(Game game, Player player, Team team) {
		this.game = game;
		this.player = player;
		this.team = team;
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
	
	public Team getTeam() {
		return this.team;
	}

}
