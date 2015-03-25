package io.github.yannici.bedwarsreloaded.Commands;

import io.github.yannici.bedwarsreloaded.ChatWriter;
import io.github.yannici.bedwarsreloaded.Main;
import io.github.yannici.bedwarsreloaded.Game.Game;
import io.github.yannici.bedwarsreloaded.Game.Team;

import java.util.ArrayList;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SetSpawnCommand extends BaseCommand implements ICommand {

    public SetSpawnCommand(Main plugin) {
        super(plugin);
    }

    @Override
    public String getCommand() {
        return "setspawn";
    }

    @Override
    public String getName() {
        return "Set Team Spawn";
    }

    @Override
    public String getDescription() {
        return "Sets the spawn of the given team";
    }

    @Override
    public String[] getArguments() {
        return new String[]{"game", "team"};
    }

    @Override
    public boolean execute(CommandSender sender, ArrayList<String> args) {
        if(!super.hasPermission(sender)) {
            return false;
        }

        Player player = (Player) sender;

        Game game = this.getPlugin().getGameManager().getGame(args.get(0));
        if(game == null) {
            player.sendMessage(ChatWriter.pluginMessage(ChatColor.RED + "The given game wasn't found!"));
            return false;
        }

        Team team = game.getTeam(args.get(1));
        if(team == null) {
            player.sendMessage(ChatWriter.pluginMessage(ChatColor.RED + "The given team wasn't found!"));
            return false;
        }

        team.setSpawnLocation(player.getLocation());
        player.sendMessage(ChatWriter.pluginMessage(ChatColor.GREEN + "Spawn location for Team " + team.getName() + " was set successfully!"));
        return true;
    }

    @Override
    public String getPermission() {
        return "setup";
    }

}
