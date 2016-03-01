package io.github.yannici.bedwars.Commands;

import java.util.ArrayList;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.util.ChatPaginator;
import org.bukkit.util.ChatPaginator.ChatPage;

import com.google.common.collect.ImmutableMap;

import io.github.yannici.bedwars.Main;
import io.github.yannici.bedwars.Utils;

public class HelpCommand extends BaseCommand {

	public HelpCommand(Main plugin) {
		super(plugin);
	}

	@Override
	public String getCommand() {
		return "help";
	}

	@Override
	public String getName() {
		return Main._l("commands.help.name");
	}

	@Override
	public String getDescription() {
		return Main._l("commands.help.desc");
	}

	@Override
	public String[] getArguments() {
		return new String[] {};
	}

	@Override
	public boolean execute(CommandSender sender, ArrayList<String> args) {
		if (!sender.hasPermission("bw." + this.getPermission())) {
			return false;
		}

		String paginate;
		int page = 1;

		if (args.size() == 0 || args.size() > 1) {
			paginate = "1";
		} else {
			paginate = args.get(0);
			if (paginate.isEmpty()) {
				paginate = "1";
			}

			if (!Utils.isNumber(paginate)) {
				paginate = "1";
			}
		}

		page = Integer.parseInt(paginate);
		StringBuilder sb = new StringBuilder();
		sender.sendMessage(ChatColor.GREEN + "---------- Bedwars Help ----------");

		ArrayList<BaseCommand> baseCommands = Main.getInstance().getBaseCommands();
		ArrayList<BaseCommand> setupCommands = Main.getInstance().getSetupCommands();
		ArrayList<BaseCommand> kickCommands = Main.getInstance().getCommandsByPermission("kick");

		for (BaseCommand command : baseCommands) {
			this.appendCommand(command, sb);
		}

		if (sender.hasPermission("bw.kick")) {
			for (BaseCommand command : kickCommands) {
				this.appendCommand(command, sb);
			}
		}

		if (sender.hasPermission("bw.setup")) {
			sb.append(ChatColor.BLUE + "------- Bedwars Admin Help -------\n");

			for (BaseCommand command : setupCommands) {
				this.appendCommand(command, sb);
			}
		}

		ChatPage chatPage = ChatPaginator.paginate(sb.toString(), page);
		for (String line : chatPage.getLines()) {
			sender.sendMessage(line);
		}
		sender.sendMessage(ChatColor.GREEN
				+ "---------- " + Main._l("default.pages", ImmutableMap.of("current",
						String.valueOf(chatPage.getPageNumber()), "max", String.valueOf(chatPage.getTotalPages())))
				+ " ----------");

		return true;
	}

	private void appendCommand(BaseCommand command, StringBuilder sb) {
		String arg = "";
		for (String argument : command.getArguments()) {
			arg = arg + " {" + argument + "}";
		}

		if (command.getCommand().equals("help")) {
			arg = " {page?}";
		} else if (command.getCommand().equalsIgnoreCase("list")) {
			arg = " {page?}";
		} else if (command.getCommand().equalsIgnoreCase("stats")) {
			arg = " {player?}";
		} else if (command.getCommand().equalsIgnoreCase("reload")) {
			arg = " {config;locale;shop;games;all?}";
		}

		sb.append(ChatColor.YELLOW + "/" + Main.getInstance().getStringConfig("command-prefix", "bw") + " "
				+ command.getCommand() + arg + " - " + command.getDescription() + "\n");
	}

	@Override
	public String getPermission() {
		return "base";
	}

}
