package io.github.yannici.bedwars.Events;

import java.util.ArrayList;

import org.bukkit.command.CommandSender;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import io.github.yannici.bedwars.Commands.BaseCommand;

public class BedwarsExecuteCommandEvent extends Event implements Cancellable {

	private static final HandlerList handlers = new HandlerList();
	private CommandSender sender = null;
	private BaseCommand command = null;
	private ArrayList<String> params = null;
	private boolean cancelled = false;

	public BedwarsExecuteCommandEvent(CommandSender sender, BaseCommand command, ArrayList<String> params) {
		this.sender = sender;
		this.command = command;
		this.params = params;
	}

	@Override
	public HandlerList getHandlers() {
		return BedwarsExecuteCommandEvent.handlers;
	}

	public static HandlerList getHandlerList() {
		return BedwarsExecuteCommandEvent.handlers;
	}

	public CommandSender getSender() {
		return this.sender;
	}

	public BaseCommand getCommand() {
		return this.command;
	}

	public ArrayList<String> getParameter() {
		return this.params;
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
