package io.github.yannici.bedwars.Game;

import io.github.yannici.bedwars.Main;

import org.bukkit.entity.Player;

public class PlayerSettings {
    
    private Player player = null;
    private boolean oneStackPerShift = false;
    private Object hologram = null;

    public PlayerSettings(Player player) {
        this.player = player;
        this.oneStackPerShift = Main.getInstance().getBooleanConfig("player-settings.one-stack-on-shift", false);
    }
    
    public Player getPlayer() {
        return this.player;
    }
    
    public Object getHologram() {
        return this.hologram;
    }
    
    public void setHologram(Object holo) {
        this.hologram = holo;
    }
    
    public boolean oneStackPerShift() {
        return this.oneStackPerShift;
    }
    
    public void setOneStackPerShift(boolean value) {
        this.oneStackPerShift = value;
    }

}
