package io.github.yannici.bedwars.Commands;

import io.github.yannici.bedwars.ChatWriter;
import io.github.yannici.bedwars.Main;
import io.github.yannici.bedwars.Game.Game;

import java.util.ArrayList;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.google.common.collect.ImmutableMap;

public class JoinGameCommand extends BaseCommand {

    public JoinGameCommand(Main plugin) {
        super(plugin);
    }

    @Override
    public String getCommand() {
        return "join";
    }

    @Override
    public String getName() {
        return Main._l("commands.join.name");
    }

    @Override
    public String getDescription() {
        return Main._l("commands.join.desc");
    }

    @Override
    public String[] getArguments() {
        return new String[]{"game"};
    }

    @Override
    public boolean execute(CommandSender sender, ArrayList<String> args) {
        if(!super.hasPermission(sender)) {
            return false;
        }

        Player player = (Player) sender;
        Game game = this.getPlugin().getGameManager().getGame(args.get(0));

        if(game == null) {
            sender.sendMessage(ChatWriter.pluginMessage(ChatColor.RED + Main._l("errors.gamenotfound", ImmutableMap.of("game", args.get(0).toString()))));
            return false;
        }

        if(game.playerJoins(player)) {
            sender.sendMessage(ChatWriter.pluginMessage(ChatColor.GREEN + Main._l("success.joined")));
        }
        return true;
    }

    @Override
    public String getPermission() {
        return "base";
    }

}
