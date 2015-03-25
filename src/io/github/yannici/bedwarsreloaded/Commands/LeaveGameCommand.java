package io.github.yannici.bedwarsreloaded.Commands;

import io.github.yannici.bedwarsreloaded.ChatWriter;
import io.github.yannici.bedwarsreloaded.Main;
import io.github.yannici.bedwarsreloaded.Game.Game;

import java.util.ArrayList;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class LeaveGameCommand extends BaseCommand {

    public LeaveGameCommand(Main plugin) {
        super(plugin);
    }

    @Override
    public String getCommand() {
        return "leave";
    }

    @Override
    public String getName() {
        return "Leave Game";
    }

    @Override
    public String getDescription() {
        return "Leave the current game";
    }

    @Override
    public String[] getArguments() {
        return new String[]{};
    }

    @Override
    public boolean execute(CommandSender sender, ArrayList<String> args) {
        if(!super.hasPermission(sender)) {
            return false;
        }

        Player player = (Player) sender;
        Game game = Game.getGameOfPlayer(player);

        if(game == null) {
            sender.sendMessage(ChatWriter.pluginMessage(ChatColor.RED + "You are currently not in a game!"));
            return false;
        }

        if(game.playerLeave(player)) {
            sender.sendMessage(ChatWriter.pluginMessage(ChatColor.GREEN + "You successfully left the game!"));
        }
        return true;
    }

    @Override
    public String getPermission() {
        return "setup";
    }

}
