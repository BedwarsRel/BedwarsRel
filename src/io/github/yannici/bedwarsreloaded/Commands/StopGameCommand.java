package io.github.yannici.bedwarsreloaded.Commands;

import io.github.yannici.bedwarsreloaded.ChatWriter;
import io.github.yannici.bedwarsreloaded.Main;
import io.github.yannici.bedwarsreloaded.Game.Game;

import java.util.ArrayList;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

public class StopGameCommand extends BaseCommand implements ICommand {

    public StopGameCommand(Main plugin) {
        super(plugin);
    }

    @Override
    public String getCommand() {
        return "stop";
    }

    @Override
    public String getName() {
        return "Stop Game";
    }

    @Override
    public String getDescription() {
        return "Stops a game";
    }

    @Override
    public String[] getArguments() {
        return new String[]{"game"};
    }

    @Override
    public boolean execute(CommandSender sender, ArrayList<String> args) {
        if(!sender.hasPermission("bw." + this.getPermission())) {
            return false;
        }

        if(args.size() == 0) {
            sender.sendMessage(ChatWriter.pluginMessage(ChatColor.RED + "Arguments length does not match the expected amount!"));
            return false;
        }

        Game game = this.getPlugin().getGameManager().getGame(args.get(0));
        if(game == null) {
            sender.sendMessage(ChatWriter.pluginMessage(ChatColor.RED + "The given game wasn't found!"));
            return false;
        }

        if(!game.stop()) {
            sender.sendMessage(ChatWriter.pluginMessage(ChatColor.RED + "Game isn't running!"));
            return false;
        }

        sender.sendMessage(ChatWriter.pluginMessage(ChatColor.GREEN + "Game successfully stopped!"));
        return true;
    }

    @Override
    public String getPermission() {
        return "setup";
    }

}
