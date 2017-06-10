package io.github.bedwarsrel.commands;

import java.util.ArrayList;
import org.bukkit.command.CommandSender;

public interface ICommand {

  public boolean execute(CommandSender sender, ArrayList<String> args);

  public String[] getArguments();

  public String getCommand();

  public String getDescription();

  public String getName();

  public String getPermission();

  public boolean hasPermission(CommandSender sender);

}
