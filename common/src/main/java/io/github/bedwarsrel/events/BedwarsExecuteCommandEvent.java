package io.github.bedwarsrel.events;

import io.github.bedwarsrel.commands.BaseCommand;
import java.util.ArrayList;
import org.bukkit.command.CommandSender;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class BedwarsExecuteCommandEvent extends Event implements Cancellable {

  private static final HandlerList handlers = new HandlerList();
  private boolean cancelled = false;
  private BaseCommand command = null;
  private ArrayList<String> params = null;
  private CommandSender sender = null;

  public BedwarsExecuteCommandEvent(CommandSender sender, BaseCommand command,
      ArrayList<String> params) {
    this.sender = sender;
    this.command = command;
    this.params = params;
  }

  public static HandlerList getHandlerList() {
    return BedwarsExecuteCommandEvent.handlers;
  }

  public BaseCommand getCommand() {
    return this.command;
  }

  @Override
  public HandlerList getHandlers() {
    return BedwarsExecuteCommandEvent.handlers;
  }

  public ArrayList<String> getParameter() {
    return this.params;
  }

  public CommandSender getSender() {
    return this.sender;
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
