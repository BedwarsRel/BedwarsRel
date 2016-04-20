package io.github.yannici.bedwars.Commands;

import java.util.ArrayList;

import org.bukkit.command.CommandSender;

public interface ICommand {

	public String getCommand();

	public String getPermission();

	public String getName();

	public String getDescription();

	public String[] getArguments();

	public boolean hasPermission(CommandSender sender);

	public boolean execute(CommandSender sender, ArrayList<String> args);

}
