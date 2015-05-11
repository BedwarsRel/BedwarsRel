package io.github.yannici.bedwars.Shop.Specials;

import io.github.yannici.bedwars.Game.Game;
import io.github.yannici.bedwars.Game.GameState;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;

public abstract class SpecialItem {
    
    public SpecialItem() {
        super();
    }
    
    public abstract Material getItemMaterial();
    public abstract void executeEvent(Event event);
    
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
