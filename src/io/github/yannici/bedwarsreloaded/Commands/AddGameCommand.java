package io.github.yannici.bedwarsreloaded.Commands;

import io.github.yannici.bedwarsreloaded.ChatWriter;
import io.github.yannici.bedwarsreloaded.Main;

import java.util.ArrayList;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import com.google.common.collect.ImmutableMap;

public class AddGameCommand extends BaseCommand {

    public AddGameCommand(Main plugin) {
        super(plugin);
    }

    @Override
    public String getCommand() {
        return "addgame";
    }

    @Override
    public String getName() {
        return Main._l("commands.addgame.name");
    }

    @Override
    public String getDescription() {
        return Main._l("commands.addgame.desc");
    }

    @Override
    public String[] getArguments() {
        return new String[]{"name"};
    }

    @Override
    public boolean execute(CommandSender sender, ArrayList<String> args) {
        if(!sender.hasPermission("bw." + this.getPermission())) {
            return false;
        }

        boolean addGame = this.getPlugin().getGameManager().addGame(args.get(0));

        if(addGame == false) {
            sender.sendMessage(ChatWriter.pluginMessage(ChatColor.RED + Main._l("errors.gameexists")));
            return false;
        }

        sender.sendMessage(ChatWriter.pluginMessage(ChatColor.GREEN + Main._l("success.gameadded", ImmutableMap.of("game", args.get(0).toString()))));
        return true;
    }

    @Override
    public String getPermission() {
        return "setup";
    }

}
