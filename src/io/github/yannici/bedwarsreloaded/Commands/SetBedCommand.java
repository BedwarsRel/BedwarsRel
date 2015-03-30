package io.github.yannici.bedwarsreloaded.Commands;

import io.github.yannici.bedwarsreloaded.ChatWriter;
import io.github.yannici.bedwarsreloaded.Main;
import io.github.yannici.bedwarsreloaded.Game.Game;
import io.github.yannici.bedwarsreloaded.Game.Team;

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
    
    @Override
    public boolean execute(CommandSender sender, ArrayList<String> args) {
        if(!super.hasPermission(sender)) {
            return false;
        }

        Player player = (Player) sender;
        String team = args.get(1);

        Game game = this.getPlugin().getGameManager().getGame(args.get(0));
        if(game == null) {
            player.sendMessage(ChatWriter.pluginMessage(ChatColor.RED + "The given game wasn't found!"));
            return false;
        }
        
        Team gameTeam = game.getTeam(team);
        
        if(team == null) {
            player.sendMessage(ChatWriter.pluginMessage(ChatColor.RED + "The given team wasn't found in this game!"));
            return false;
        }
        
        HashSet<Material> transparent = new HashSet<Material>();
        transparent.add(Material.AIR);
        
        Block targetBlock = player.getTargetBlock(transparent, 15);
        Block standingBlock = player.getLocation().getBlock().getRelative(BlockFace.DOWN);
        
        if(targetBlock == null || standingBlock == null) {
            player.sendMessage(ChatWriter.pluginMessage(ChatColor.RED + "You have to targeting or stand on a Bed!"));
            return false;
        }
        
        if(targetBlock.getType() != Material.BED_BLOCK && standingBlock.getType() != Material.BED_BLOCK) {
            player.sendMessage(ChatWriter.pluginMessage(ChatColor.RED + "You have to targeting or stand on a Bed!"));
            return false;
        }
        
        Block theBlock = null;
        if(targetBlock.getType() == Material.BED_BLOCK) {
            theBlock = targetBlock;
        } else {
            theBlock = standingBlock;
        }
        
        gameTeam.setBed(theBlock);
        player.sendMessage(ChatWriter.pluginMessage(ChatColor.GREEN + "You set the bed for team " + gameTeam.getChatColor() + gameTeam.getName() + ChatColor.GREEN + " successfully!"));
        return true;
    }

    @Override
    public String getPermission() {
        return "setup";
    }

}
