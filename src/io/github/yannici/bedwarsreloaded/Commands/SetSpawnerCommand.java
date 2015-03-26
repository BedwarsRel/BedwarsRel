package io.github.yannici.bedwarsreloaded.Commands;

import io.github.yannici.bedwarsreloaded.ChatWriter;
import io.github.yannici.bedwarsreloaded.Main;
import io.github.yannici.bedwarsreloaded.Game.Game;

import java.util.ArrayList;
import java.util.Arrays;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class SetSpawnerCommand extends BaseCommand {

    public SetSpawnerCommand(Main plugin) {
        super(plugin);
    }

    @Override
    public String getCommand() {
        return "setspawner";
    }

    @Override
    public String getName() {
        return "Set Spawner";
    }

    @Override
    public String getDescription() {
        return "Sets the spawner location of a specific element";
    }

    @Override
    public String[] getArguments() {
        return new String[]{"type"};
    }

    private String[] getRessources() {
        return new String[]{"gold", "iron", "bronze"};
    }

    @Override
    public boolean execute(CommandSender sender, ArrayList<String> args) {
        if(!super.hasPermission(sender)) {
            return false;
        }

        Player player = (Player) sender;
        ArrayList<String> arguments = new ArrayList<String>(Arrays.asList(this.getRessources()));

        if(args.size() < 2) {
            player.sendMessage(ChatWriter.pluginMessage(ChatColor.RED + "The amount of arguments doesn't match! See \"/help TestPlugin\" for more information"));
            return false;
        }

        String material = args.get(1).toLowerCase();
        Game game = this.getPlugin().getGameManager().getGame(args.get(0));

        if(game == null) {
            player.sendMessage(ChatWriter.pluginMessage(ChatColor.RED + "The given game wasn't found!"));
            return false;
        }

        if(!arguments.contains(material)) {
            player.sendMessage(ChatWriter.pluginMessage(ChatColor.RED + "Your argument have to be one of the following: (gold;iron;bronze)"));
            return false;
        }

        Material droppingMaterial = null;
        String name = "Ress";
        switch(material) {
        case "gold":
            droppingMaterial = Material.GOLD_INGOT;
            name = ChatColor.GOLD + "Gold";
            break;
        case "iron":
            droppingMaterial = Material.IRON_INGOT;
            name = ChatColor.GRAY + "Silver";
            break;
        case "bronze":
            droppingMaterial = Material.CLAY_BRICK;
            name = ChatColor.DARK_RED + "Bronze";
            break;
        }
        
        ItemStack stack2 = new ItemStack(Material.POTION, 1);
        

        ItemStack stack = new ItemStack(droppingMaterial, this.getPlugin().getConfig().getInt("ressource." + material + ".amount"));
        ItemMeta meta = stack.getItemMeta();
        meta.setDisplayName(name);
        stack.setItemMeta(meta);
        
        int interval = this.getPlugin().getConfig().getInt("ressource." + material + ".spawninterval");
        Block downBlock = player.getLocation().getBlock();

        if(downBlock == null) {
            player.sendMessage(ChatWriter.pluginMessage(ChatWriter.pluginMessage(ChatColor.RED + "Block right under you wasn't found!")));
            return false;
        }

        game.addRessourceSpawner(interval, downBlock.getLocation(), stack);
        player.sendMessage(ChatWriter.pluginMessage(ChatColor.GREEN + "Ressource spawn location successfully set!"));
        return true;
    }

    @Override
    public String getPermission() {
        return "setup";
    }

}
