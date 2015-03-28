package io.github.yannici.bedwarsreloaded.Commands;

import io.github.yannici.bedwarsreloaded.ChatWriter;
import io.github.yannici.bedwarsreloaded.Main;
import io.github.yannici.bedwarsreloaded.Game.Game;

import java.util.ArrayList;
import java.util.HashSet;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SetBedCommand extends BaseCommand implements ICommand {

    public SetBedCommand(Main plugin) {
        super(plugin);
    }

    @Override
    public String getCommand() {
        return "setbed";
    }

    @Override
    public String getName() {
        return "Set bed";
    }

    @Override
    public String getDescription() {
        return "Sets the location of a team's bed";
    }

    @Override
    public String[] getArguments() {
        return new String[]{"game", "team"};
    }

    @SuppressWarnings("serial")
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
        
        Block targetBlock = player.getTargetBlock(new HashSet<Material>(){}, 15);
        Block standingBlock = player.getLocation().getBlock().getRelative(BlockFace.DOWN);
        
        if(targetBlock == null || standingBlock == null) {
            player.sendMessage(ChatWriter.pluginMessage(ChatColor.RED + "You have to targeting or stand on a Bed!"));
            return false;
        }
        
        if(targetBlock.getType() != Material.BED && standingBlock.getType() != Material.BED) {
            player.sendMessage(ChatWriter.pluginMessage(ChatColor.RED + "You have to targeting or stand on a Bed!"));
            return false;
        }

        game.setLobby(player);
        return true;
    }

    @Override
    public String getPermission() {
        return "setup";
    }

}
