package io.github.yannici.bedwarsreloaded.Game;

import org.bukkit.ChatColor;
import org.bukkit.block.Sign;

import io.github.yannici.bedwarsreloaded.Main;

public class GameJoinSign {
    
    private Game game = null;
    private Sign sign = null;

    public GameJoinSign(Game game, Sign sign) {
        this.game = game;
        this.sign = sign;
    }
    
    public void updateSign() {
        String[] signLines = this.getSignLines();
        for(int i = 0; i < signLines.length; i++) {
            this.sign.setLine(i, signLines[i]);
        }
        
        this.sign.update(true);
    }
    
    private String[] getSignLines() {
        String[] sign = new String[4];
        sign[0] = Main._l("sign.firstline");
        sign[1] = this.game.getRegion().getWorld().getName();
        
        int maxPlayers = this.game.getMaxPlayers();
        int currentPlayers = 0;
        if(this.game.getState() == GameState.RUNNING) {
            currentPlayers = this.game.getTeamPlayers().size();
        } else if(this.game.getState() == GameState.WAITING) {
            currentPlayers = this.game.getPlayers().size();
        }
        
        String current = "0";
        if (currentPlayers >= maxPlayers) {
            current = ChatColor.RED + String.valueOf(currentPlayers);
        } else {
            current = ChatColor.AQUA + String.valueOf(currentPlayers);
        }
        
        String playerString =  ChatColor.GRAY + "[" + current + ChatColor.GRAY + "/" + ChatColor.AQUA + String.valueOf(maxPlayers) + ChatColor.GRAY + "]";
        sign[2] = Main._l("sign.players") + " " + playerString;
        sign[3] = Main._l("sign.gamestate." + this.game.getState().toString().toLowerCase());
        
        return sign;
    }
    
    public Sign getSign() {
        return this.sign;
    }

}
