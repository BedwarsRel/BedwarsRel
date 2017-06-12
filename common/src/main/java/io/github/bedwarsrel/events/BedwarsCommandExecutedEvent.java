package io.github.bedwarsrel.events;

import io.github.bedwarsrel.commands.BaseCommand;
import java.util.ArrayList;
import org.bukkit.command.CommandSender;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class BedwarsCommandExecutedEvent extends Event {

  private static final HandlerList handlers = new HandlerList();
  private BaseCommand command = null;
  private ArrayList<String> params = null;
  private boolean result = false;
  private CommandSender sender = null;

  public BedwarsCommandExecutedEvent(CommandSender sender, BaseCommand command,
      ArrayList<String> params, boolean result) {
    this.sender = sender;
    this.command = command;
    this.params = params;
    this.result = result;
  }

  public static HandlerList getHandlerList() {
    return BedwarsCommandExecutedEvent.handlers;
  }

  public BaseCommand getCommand() {
    return this.command;
  }

  @Override
  public HandlerList getHandlers() {
    return BedwarsCommandExecutedEvent.handlers;
  }

  public ArrayList<String> getParameter() {
    return this.params;
  }

  public CommandSender getSender() {
    return this.sender;
  }

  public boolean isSuccess() {
    return this.result;
  }

}
