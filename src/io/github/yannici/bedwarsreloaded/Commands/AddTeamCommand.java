package io.github.yannici.bedwarsreloaded.Commands;

import io.github.yannici.bedwarsreloaded.ChatWriter;
import io.github.yannici.bedwarsreloaded.Main;
import io.github.yannici.bedwarsreloaded.Game.Game;
import io.github.yannici.bedwarsreloaded.Game.TeamColor;

import java.util.ArrayList;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

public class AddTeamCommand extends BaseCommand {

    public AddTeamCommand(Main plugin) {
        super(plugin);
    }

    @Override
    public String getCommand() {
        return "addteam";
    }

    @Override
    public String getName() {
        return "Add Team";
    }

    @Override
    public String getDescription() {
        return "Adds a team to a specific game";
    }

    @Override
    public String[] getArguments() {
        return new String[]{"game", "name", "color", "maxplayers"};
    }

    @Override
    public boolean execute(CommandSender sender, ArrayList<String> args) {
        if(!sender.hasPermission("bw." + this.getPermission())) {
            return false;
        }

        Game game = this.getPlugin().getGameManager().getGame(args.get(0));
        String name = args.get(1);
        String color = args.get(2);
        String maxPlayers = args.get(3);

        TeamColor tColor = TeamColor.valueOf(color);

        if(game == null) {
            sender.sendMessage(ChatWriter.pluginMessage(ChatColor.RED + "The given game wasn't found!"));
            return false;
        }

        int playerMax = Integer.parseInt(maxPlayers);

        if(playerMax < 1 || playerMax > 16) {
            sender.sendMessage(ChatWriter.pluginMessage(ChatColor.RED + "The maxium of players can't be lower than 1 or higher than 8!"));
            return false;
        }

        if(tColor == null) {
            sender.sendMessage(ChatWriter.pluginMessage(ChatColor.RED + "The given Team color isn't a allowed color!"));
            return false;
        }

        if(name.length() < 3) {
            sender.sendMessage(ChatWriter.pluginMessage(ChatColor.RED + "Team name have to be more than 2 characters!"));
            return false;
        }

        game.addTeam(name, tColor, playerMax);
        sender.sendMessage(ChatWriter.pluginMessage(ChatColor.GREEN + "Team \"" + name + "\" successfully added!"));
        return true;
    }

    @Override
    public String getPermission() {
        return "setup";
    }

}
