package io.github.yannici.bedwars.Commands;

import io.github.yannici.bedwars.ChatWriter;
import io.github.yannici.bedwars.Main;

import java.util.ArrayList;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

public class ReloadCommand extends BaseCommand {

    public ReloadCommand(Main plugin) {
        super(plugin);
    }

    @Override
    public String getPermission() {
        return "setup";
    }

    @Override
    public String getCommand() {
        return "reload";
    }

    @Override
    public String getName() {
        return Main._l("commands.reload.name");
    }

    @Override
    public String getDescription() {
        return Main._l("commands.reload.desc");
    }

    @Override
    public String[] getArguments() {
        return new String[]{};
    }

    @Override
    public boolean execute(CommandSender sender, ArrayList<String> args) {
        if(!sender.hasPermission(this.getPermission())) {
            return false;
        }
        
        Main.getInstance().saveDefaultConfig();
        Main.getInstance().reloadConfig();
        Main.getInstance().reloadLocalization();
        sender.sendMessage(ChatWriter.pluginMessage(ChatColor.GREEN + Main._l("success.reloadconfig")));
        return true;
    }

}
