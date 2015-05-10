package io.github.yannici.bedwars.Shop.Specials;

import io.github.yannici.bedwars.ChatWriter;
import io.github.yannici.bedwars.Main;
import io.github.yannici.bedwars.Game.Game;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerInteractEvent;

public class RescuePlatform extends SpecialItem {
    
    public RescuePlatform() {
        super();
    }

    @Override
    public Material getItemMaterial() {
        return Material.BLAZE_ROD;
    }
    
    @EventHandler(priority = EventPriority.HIGH)
    public void onInteract(PlayerInteractEvent ev) {
        if(super.returnPlayerEvent(ev.getPlayer())) {
            return;
        }
        
        Player player = ev.getPlayer();
        Game game = Game.getGameOfPlayer(player);
        
        if(player.getLocation().getBlock().getRelative(BlockFace.DOWN).getType() != Material.AIR) {
            player.sendMessage(ChatWriter.pluginMessage(ChatColor.RED + Main._l("errors.notinair")));
            return;
        }
        
        Location mid = player.getLocation().clone();
        mid.setY(mid.getY()-1.0D);
        
        for(BlockFace face : BlockFace.values()) {
            if(face.equals(BlockFace.DOWN) || face.equals(BlockFace.UP)) {
                continue;
            }
            
            Block placed = mid.getBlock().getRelative(face);
            if(placed.getType() != Material.AIR) {
                continue;
            }
            
            placed.setType(Material.GLASS);
            
            game.getRegion().addPlacedBlock(placed, null);
        }
    }

}
