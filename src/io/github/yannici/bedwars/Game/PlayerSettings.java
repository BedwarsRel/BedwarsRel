package io.github.yannici.bedwars.Game;

import io.github.yannici.bedwars.Main;

import org.bukkit.entity.Player;

import com.gmail.filoghost.holographicdisplays.api.Hologram;

public class PlayerSettings {
    
    private Player player = null;
    private boolean oneStackPerShift = false;
    private Hologram hologram = null;

    public PlayerSettings(Player player) {
        this.player = player;
        this.oneStackPerShift = Main.getInstance().getBooleanConfig("player-settings.one-stack-on-shift", false);
    }
    
    public Player getPlayer() {
        return this.player;
    }
    
    public Hologram getHologram() {
        return this.hologram;
    }
    
    public void setHologram(Hologram holo) {
        this.hologram = holo;
    }
    
    public void deleteHologram() {
        if(this.hologram != null) this.hologram.delete();
    }
    
    public boolean oneStackPerShift() {
        return this.oneStackPerShift;
    }
    
    public void setOneStackPerShift(boolean value) {
        this.oneStackPerShift = value;
    }

}
