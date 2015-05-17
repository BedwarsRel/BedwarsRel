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
    public abstract boolean executeEventActivated(Event event);
    public abstract Material getActivatedMaterial();
    public abstract List<Class<? extends Event>> getUsedEvents();

    public boolean returnPlayerEvent(Player player) {
        if(!player.getItemInHand().getType().equals(this.getItemMaterial())
                && (!player.getItemInHand().getType().equals(this.getActivatedMaterial()) 
                        && this.getActivatedMaterial() != null)) {
            return true;
        }
        
        Game game = Game.getGameOfPlayer(player);
        
        if(game == null) {
            return true;
        }
        
        if(game.getState() != GameState.RUNNING) {
            return true;
        }
        
        if(game.isSpectator(player)) {
            return true;
        }
        
        return false;
    }
    
    public static void loadSpecials() {
        SpecialItem.availableSpecials.add(RescuePlatform.class);
    }
    
    public static List<Class<? extends SpecialItem>> getSpecials() {
    	return SpecialItem.availableSpecials;
    }
    
}
