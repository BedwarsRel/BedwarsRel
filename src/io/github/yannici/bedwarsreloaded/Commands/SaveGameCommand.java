package io.github.yannici.bedwarsreloaded.Commands;

import io.github.yannici.bedwarsreloaded.ChatWriter;
import io.github.yannici.bedwarsreloaded.Main;
import io.github.yannici.bedwarsreloaded.Game.Game;

import java.util.ArrayList;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

public class SaveGameCommand extends BaseCommand implements ICommand {

    public SaveGameCommand(Main plugin) {
        super(plugin);
    }

    @Override
    public String getCommand() {
        return "savegame";
    }

    @Override
    public String getName() {
        return "Save Game";
    }

    @Override
    public String getDescription() {
        return "Saves a game to config file(s)";
    }

    @Override
    public String[] getArguments() {
        return new String[]{"game"};
    }

    @Override
    public boolean execute(CommandSender sender, ArrayList<String> args) {
        if(!sender.hasPermission("bw" + this.getPermission())) {
            return false;
        }

        Game game = this.getPlugin().getGameManager().getGame(args.get(0));
        if(game == null) {
            sender.sendMessage(ChatWriter.pluginMessage(ChatColor.RED + "The given game wasn't found!"));
            return false;
        }

        if(!game.saveGame(sender, true)) {
            return false;
        }

        sender.sendMessage(ChatWriter.pluginMessage(ChatColor.GREEN + "Game was saved successfully!"));
        return true;
    }

    @Override
    public String getPermission() {
        return "setup";
    }

}
