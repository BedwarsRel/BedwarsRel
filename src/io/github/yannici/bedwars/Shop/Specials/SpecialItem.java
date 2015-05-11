package io.github.yannici.bedwars.Shop.Specials;

import java.util.ArrayList;
import java.util.List;

import io.github.yannici.bedwars.Game.Game;
import io.github.yannici.bedwars.Game.GameState;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;

public abstract class SpecialItem {

    private static List<Class<? extends SpecialItem>> availableSpecials = new ArrayList<Class<? extends SpecialItem>>();
    
    public SpecialItem() {
        super();
    }
    
    public abstract Material getItemMaterial();
    public abstract boolean executeEvent(Event event);
    public abstract void cycle();
    
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
        SpecialItem.availableSpecials.add(RescuePlatform.class);
    }
    
}
