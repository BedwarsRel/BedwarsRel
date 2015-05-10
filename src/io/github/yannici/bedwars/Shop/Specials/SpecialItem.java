package io.github.yannici.bedwars.Shop.Specials;

import io.github.yannici.bedwars.Main;
import io.github.yannici.bedwars.Game.Game;
import io.github.yannici.bedwars.Game.GameState;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

public abstract class SpecialItem implements Listener {
    
    public SpecialItem() {
        Main.getInstance().getServer().getPluginManager()
        .registerEvents(this, Main.getInstance());
    }
    
    public abstract Material getItemMaterial();
    
    public boolean returnPlayerEvent(Player player) {
        if(!player.getItemInHand().getType().equals(this.getItemMaterial())) {
            return true;
        }
        
        Game game = Game.getGameOfPlayer(player);
        
        if(game == null) {
            return true;
        }
        
        if(game.getState() != GameState.RUNNING) {
            return true;
        }
        
        return false;
    }
    
    public static void loadSpecials() {
        new RescuePlatform();
    }
    
}
