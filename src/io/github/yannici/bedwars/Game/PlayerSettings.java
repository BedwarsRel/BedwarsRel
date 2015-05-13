package io.github.yannici.bedwars.Game;

import io.github.yannici.bedwars.Main;

import org.bukkit.entity.Player;

public class PlayerSettings {
    
    private Player player = null;
    private boolean oneStackPerShift = false;

    public PlayerSettings(Player player) {
        this.player = player;
        this.oneStackPerShift = Main.getInstance().getBooleanConfig("player-settings.one-stack-on-shift", false);
    }
    
    public Player getPlayer() {
        return this.player;
    }
    
    public boolean oneStackPerShift() {
        return this.oneStackPerShift;
    }
    
    public void setOneStackPerShift(boolean value) {
        this.oneStackPerShift = value;
    }

}
