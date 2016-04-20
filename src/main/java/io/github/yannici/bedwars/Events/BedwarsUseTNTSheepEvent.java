package io.github.yannici.bedwars.Events;

import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import io.github.yannici.bedwars.Game.Game;

public class BedwarsUseTNTSheepEvent extends Event implements Cancellable {

	private static final HandlerList handlers = new HandlerList();
	private Player player = null;
	private Game game = null;
	private Location startLocation = null;
	private Player targetPlayer = null;
	private boolean cancelled = false;

	public BedwarsUseTNTSheepEvent(Game game, Player player, Player targetPlayer, Location startLocation) {
		this.player = player;
		this.game = game;
		this.startLocation = startLocation;
		this.targetPlayer = targetPlayer;
	}

	@Override
	public HandlerList getHandlers() {
		return BedwarsUseTNTSheepEvent.handlers;
	}

	public static HandlerList getHandlerList() {
		return BedwarsUseTNTSheepEvent.handlers;
	}

	public CommandSender getPlayer() {
		return this.player;
	}

	public Game getGame() {
		return this.game;
	}

	public Location getStartLocation() {
		return this.startLocation;
	}

	public Player getTargetPlayer() {
		return this.targetPlayer;
	}

	public void setStartLocation(Location loc) {
		this.startLocation = loc;
	}

	public void setTargetPlayer(Player target) {
		this.targetPlayer = target;
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
