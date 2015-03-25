package io.github.yannici.bedwarsreloaded.Commands;

import io.github.yannici.bedwarsreloaded.ChatWriter;
import io.github.yannici.bedwarsreloaded.Main;

import java.util.ArrayList;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class InfoCommand extends BaseCommand {

    public InfoCommand(Main plugin) {
        super(plugin);
    }

    @Override
    public String getCommand() {
        return "help";
    }

    @Override
    public String getName() {
        return "Info";
    }

    @Override
    public String getDescription() {
        return "Shows info and help about the plugin";
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

        player.sendMessage(ChatWriter.pluginMessage("The bedwars plugin with new and better code!"));
        return true;
    }

    @Override
    public String getPermission() {
        return "base";
    }

}
