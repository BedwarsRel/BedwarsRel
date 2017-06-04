package io.github.bedwarsrel.commands;

import com.google.common.collect.ImmutableMap;
import io.github.bedwarsrel.BedwarsRel;
import io.github.bedwarsrel.game.Game;
import io.github.bedwarsrel.game.GameCheckCode;
import io.github.bedwarsrel.game.GameState;
import io.github.bedwarsrel.utils.Utils;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.util.ChatPaginator;
import org.bukkit.util.ChatPaginator.ChatPage;

public class ListGamesCommand extends BaseCommand {

  public ListGamesCommand(BedwarsRel plugin) {
    super(plugin);
  }

  @Override
  public boolean execute(CommandSender sender, ArrayList<String> args) {
    if (!sender.hasPermission("bw." + this.getPermission())) {
      return false;
    }

    String paginate;
    int page = 1;
    ArrayList<Game> showedGames = new ArrayList<Game>();

    if (args != null) {
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
    } else {
      paginate = "1";
    }

    page = Integer.parseInt(paginate);
    StringBuilder sb = new StringBuilder();
    sender.sendMessage(ChatColor.GREEN + "---------- Bedwars Games ----------");

    List<Game> games = BedwarsRel.getInstance().getGameManager().getGames();
    for (Game game : games) {
      GameCheckCode code = game.checkGame();
      if (code != GameCheckCode.OK && !sender.hasPermission("bw.setup")) {
        continue;
      }

      showedGames.add(game);
      int players = 0;
      if (game.getState() == GameState.RUNNING) {
        players = game.getCurrentPlayerAmount();
      } else {
        players = game.getPlayers().size();
      }

      sb.append(ChatColor.YELLOW
          + ((code != GameCheckCode.OK) ? ChatColor.RED + game.getName() + ChatColor.YELLOW
          : game.getName())
          + " - " + game.getRegion().getName() + " - "
          + BedwarsRel._l(sender, "sign.gamestate." + game.getState().toString().toLowerCase())
          + ChatColor.YELLOW
          + " - " + BedwarsRel._l(sender, "sign.players") + ": " + ChatColor.WHITE + "["
          + ChatColor.YELLOW
          + players + ChatColor.WHITE + "/" + ChatColor.YELLOW + game.getMaxPlayers()
          + ChatColor.WHITE + "]\n");
    }

    if (showedGames.size() == 0) {
      sb.append(ChatColor.RED + BedwarsRel._l(sender, "errors.nogames"));
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
    return "list";
  }

  @Override
  public String getDescription() {
    return BedwarsRel._l("commands.list.desc");
  }

  @Override
  public String getName() {
    return BedwarsRel._l("commands.list.name");
  }

  @Override
  public String getPermission() {
    return "base";
  }

}
