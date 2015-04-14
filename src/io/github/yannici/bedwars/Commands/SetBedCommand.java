package io.github.yannici.bedwars.Commands;

import io.github.yannici.bedwars.ChatWriter;
import io.github.yannici.bedwars.Main;
import io.github.yannici.bedwars.Game.Game;
import io.github.yannici.bedwars.Game.Team;

import java.util.ArrayList;
import java.util.HashSet;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.material.Bed;

import com.google.common.collect.ImmutableMap;

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
        return Main._l("commands.setbed.name");
    }

    @Override
    public String getDescription() {
        return Main._l("commands.setbed.desc");
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
            player.sendMessage(ChatWriter.pluginMessage(ChatColor.RED + Main._l("errors.gamenotfound", ImmutableMap.of("game", args.get(0).toString()))));
            return false;
        }
        
        Team gameTeam = game.getTeam(team);
        
        if(team == null) {
            player.sendMessage(ChatWriter.pluginMessage(ChatColor.RED + Main._l("errors.teamnotfound")));
            return false;
        }
        
        HashSet<Material> transparent = new HashSet<Material>();
        transparent.add(Material.AIR);
        
        Block targetBlock = player.getTargetBlock(transparent, 15);
        Block standingBlock = player.getLocation().getBlock().getRelative(BlockFace.DOWN);
        
        if(targetBlock == null || standingBlock == null) {
            player.sendMessage(ChatWriter.pluginMessage(ChatColor.RED + Main._l("errors.bedtargeting")));
            return false;
        }
        
        if(targetBlock.getType() != Material.BED_BLOCK && standingBlock.getType() != Material.BED_BLOCK) {
            player.sendMessage(ChatWriter.pluginMessage(ChatColor.RED + Main._l("errors.bedtargeting")));
            return false;
        }
        
        Block theBlock = null;
        if(targetBlock.getType() == Material.BED_BLOCK) {
            theBlock = targetBlock;
        } else {
            theBlock = standingBlock;
        }
        
        Bed theBed = (Bed)theBlock.getState().getData();
        
        if(!theBed.isHeadOfBed()) {
            theBlock = theBlock.getRelative(theBed.getFacing());
        }
        
        gameTeam.setBed(theBlock);
        player.sendMessage(ChatWriter.pluginMessage(ChatColor.GREEN + Main._l("success.bedset", ImmutableMap.of("team", gameTeam.getChatColor() + gameTeam.getName() + ChatColor.GREEN))));
        return true;
    }

    @Override
    public String getPermission() {
        return "setup";
    }

}
