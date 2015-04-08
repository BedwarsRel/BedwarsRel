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
        return Main._l("commands.leave.name");
    }

    @Override
    public String getDescription() {
        return Main._l("commands.leave.desc");
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
            sender.sendMessage(ChatWriter.pluginMessage(ChatColor.RED + Main._l("errors.notingame")));
            return false;
        }

        if(game.playerLeave(player)) {
            sender.sendMessage(ChatWriter.pluginMessage(ChatColor.GREEN + Main._l("success.left")));
        }
        return true;
    }

    @Override
    public String getPermission() {
        return "base";
    }

}
