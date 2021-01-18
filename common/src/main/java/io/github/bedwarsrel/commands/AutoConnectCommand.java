package io.github.bedwarsrel.commands;

import com.google.common.collect.ImmutableMap;
import io.github.bedwarsrel.BedwarsRel;
import io.github.bedwarsrel.game.Game;
import io.github.bedwarsrel.game.GameCheckCode;
import io.github.bedwarsrel.game.GameManager;
import io.github.bedwarsrel.game.GameState;
import io.github.bedwarsrel.utils.ChatWriter;
import io.github.bedwarsrel.utils.Utils;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.ChatPaginator;
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
import java.util.ArrayList;

import static org.bukkit.Bukkit.getServer;

public class AutoConnectCommand extends BaseCommand {

  private BedwarsRel plugin = null;

  public AutoConnectCommand(BedwarsRel plugin) {
    super(plugin);
  }

    @Override
    public boolean execute(CommandSender sender, ArrayList<String> args) {


        ArrayList<Game> showedGames = new ArrayList<Game>();
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
String status = game.getState().toString().toLowerCase();
            if(game.getState() == GameState.WAITING){
                sender.sendMessage(ChatColor.GREEN + "Connecting to the game lobby.");
                Player player = getServer().getPlayer(sender.getName());
                String command = "bw join " + game.getName();
                player.performCommand(command);
            }
        }

        if (showedGames.size() == 0) {
            sender.sendMessage(ChatColor.RED + "No Games :(");
        }

        return true;
    }

    @Override
    public String[] getArguments() {
      return new String[]{};
    }

    @Override
    public String getCommand() {
      return "autojoin";
    }

    @Override
    public String getDescription() {
      return "Auto connect to first lobby";
    }

    @Override
    public String getName() {
      return "autojoin";
    }

    @Override
    public String getPermission() {
      return "base";
    }
    @Override
    public boolean hasPermission(CommandSender sender){
    return true;
    }
  }
