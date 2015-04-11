package io.github.yannici.bedwarsreloaded.Game;

import io.github.yannici.bedwarsreloaded.Main;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerRespawnEvent;

import com.google.common.collect.ImmutableMap;

public abstract class GameCycle {
    
    private Game game = null;
    private boolean endGameRunning = false;
    
    public GameCycle(Game game) {
        this.game = game;
    }

    public Game getGame() {
        return game;
    }
    
    public abstract void onGameStart();
    
    public abstract void onGameEnds();
    
    public abstract void onPlayerLeave(Player player);
    
    public abstract void onGameLoaded();
    
    public abstract boolean onPlayerJoins(Player player);
    
    public abstract void onGameOver(GameOverTask task);
    
    public void checkGameOver() {
        if(!Main.getInstance().isEnabled()) {
            return;
        }
        
        Team winner = this.getGame().isOver();
        if(winner != null) {
            if(this.isEndGameRunning() == false) {
                this.setEndGameRunning(true);
                int delay = Main.getInstance().getConfig().getInt("gameoverdelay"); // configurable delay
                GameOverTask gameOver = new GameOverTask(this, delay, winner);
                gameOver.runTaskTimer(Main.getInstance(), 0L, 20L);
            }
        }
    }
    
    public void onPlayerRespawn(PlayerRespawnEvent pre, Player player) {
        Team team = Game.getPlayerTeam(player, this.getGame());
        if(team == null) {
            return;
        }
        
        if(team.isDead()) {
            // TODO: Player to spectator
            
            PlayerStorage storage = this.getGame().getPlayerStorage(player);
            if(storage != null) {
                if(storage.getLeft() != null) {
                    pre.setRespawnLocation(storage.getLeft());
                }
            }
            
            this.getGame().playerLeave(player);
        } else {
            pre.setRespawnLocation(team.getSpawnLocation());
        }
    }
    
    public void onPlayerDies(Player player, Player killer) {
        
        Team deathTeam = Game.getPlayerTeam(player, this.getGame());
        if(killer == null) {
            this.getGame().broadcast(ChatColor.GOLD + Main._l("ingame.player.died", ImmutableMap.of("player", Game.getPlayerWithTeamString(player, deathTeam, ChatColor.GOLD))));
            this.checkGameOver();
            return;
        }
        
        Team killerTeam = Game.getPlayerTeam(killer, this.getGame());
        if(killerTeam == null) {
            this.getGame().broadcast(ChatColor.GOLD + Main._l("ingame.player.died", ImmutableMap.of("player", Game.getPlayerWithTeamString(player, deathTeam, ChatColor.GOLD))));
            this.checkGameOver();
            return;
        }
        
        this.getGame().broadcast(ChatColor.GOLD + Main._l("ingame.player.killed", ImmutableMap.of("killer", Game.getPlayerWithTeamString(killer, killerTeam, ChatColor.GOLD), "player", Game.getPlayerWithTeamString(player, deathTeam, ChatColor.GOLD))));
        
        this.checkGameOver();
    }
    
    public void setEndGameRunning(boolean running) {
        this.endGameRunning = running;
    }
    
    public boolean isEndGameRunning() {
        return this.endGameRunning;
    }
    
}
