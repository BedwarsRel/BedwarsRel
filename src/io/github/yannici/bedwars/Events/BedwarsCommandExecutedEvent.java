package io.github.yannici.bedwars.Events;

import java.util.ArrayList;

import org.bukkit.command.CommandSender;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import io.github.yannici.bedwars.Commands.BaseCommand;

public class BedwarsCommandExecutedEvent extends Event {

	private static final HandlerList handlers = new HandlerList();
	private CommandSender sender = null;
	private BaseCommand command = null;
	private boolean result = false;
	private ArrayList<String> params = null;

	public BedwarsCommandExecutedEvent(CommandSender sender, BaseCommand command, ArrayList<String> params,
			boolean result) {
		this.sender = sender;
		this.command = command;
		this.params = params;
		this.result = result;
	}

	@Override
	public HandlerList getHandlers() {
		return BedwarsCommandExecutedEvent.handlers;
	}

	public static HandlerList getHandlerList() {
		return BedwarsCommandExecutedEvent.handlers;
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

	public boolean isSuccess() {
		return this.result;
	}

}
