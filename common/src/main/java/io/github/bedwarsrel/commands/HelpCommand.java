package io.github.bedwarsrel.commands;

import com.google.common.collect.ImmutableMap;
import io.github.bedwarsrel.BedwarsRel;
import io.github.bedwarsrel.utils.Utils;
import java.util.ArrayList;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.util.ChatPaginator;
import org.bukkit.util.ChatPaginator.ChatPage;

public class HelpCommand extends BaseCommand {

  public HelpCommand(BedwarsRel plugin) {
    super(plugin);
  }

  private void appendCommand(BaseCommand command, StringBuilder sb) {
    String arg = "";
    for (String argument : command.getArguments()) {
      arg = arg + " {" + argument + "}";
    }

    if (command.getCommand().equalsIgnoreCase("help")) {
      arg = " {page?}";
    } else if (command.getCommand().equalsIgnoreCase("list")) {
      arg = " {page?}";
    } else if (command.getCommand().equalsIgnoreCase("stats")) {
      arg = " {player?}";
    } else if (command.getCommand().equalsIgnoreCase("reload")) {
      arg = " {config;locale;shop;games;all?}";
    } else if (command.getCommand().equalsIgnoreCase("stop")) {
      arg = " {game?}";
    }

    sb.append(ChatColor.YELLOW + "/" + "bw"
        + " " + command.getCommand() + arg + " - " + command.getDescription() + "\n");
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

    ArrayList<BaseCommand> baseCommands = BedwarsRel.getInstance().getBaseCommands();
    ArrayList<BaseCommand> setupCommands = BedwarsRel.getInstance().getSetupCommands();
    ArrayList<BaseCommand> kickCommands = BedwarsRel.getInstance().getCommandsByPermission("kick");

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
    sender.sendMessage(ChatColor.GREEN + "---------- "
        + BedwarsRel._l(sender, "default.pages",
        ImmutableMap.of("current", String.valueOf(chatPage.getPageNumber()), "max",
            String.valueOf(chatPage.getTotalPages())))
        + " ----------");

    return true;
  }

  @Override
  public String[] getArguments() {
    return new String[]{};
  }

  @Override
  public String getCommand() {
    return "help";
  }

  @Override
  public String getDescription() {
    return BedwarsRel._l("commands.help.desc");
  }

  @Override
  public String getName() {
    return BedwarsRel._l("commands.help.name");
  }

  @Override
  public String getPermission() {
    return "base";
  }

}
