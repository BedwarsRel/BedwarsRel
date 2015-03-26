package io.github.yannici.bedwarsreloaded.Game;

import java.util.ArrayList;

import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import io.github.yannici.bedwarsreloaded.Main;

public class GameLobbyCountdown extends BukkitRunnable {

    private Game game = null;
    private int counter = 0;
    private int lobbytime;

    public GameLobbyCountdown(Game game) {
        this.game = game;
        this.counter = Main.getInstance().getConfig().getInt("lobbytime");
        this.lobbytime = this.counter;
    }

    @Override
    public void run() {
        ArrayList<Player> players = this.game.getPlayers();
        
        if(this.game.getState() != GameState.WAITING) {
            this.cancel();
        }

        if(this.counter == this.lobbytime) {
            this.game.broadcast(ChatColor.YELLOW + "Game will start in " + this.counter + " seconds!", players);
        }

        if(players.size() < this.game.getMinPlayers()) {
            this.game.broadcast(ChatColor.RED + "More players needed: Countdown was cancelled!", players);
            this.counter = this.lobbytime;
            this.cancel();
        }

        if(this.counter <= 10 && this.counter > 0) {
            this.game.broadcast(ChatColor.YELLOW + "Game will start in " + ChatColor.RED + this.counter + ChatColor.YELLOW + " seconds!", players);
            
            for(Player player : players) {
                player.playSound(player.getLocation(), Sound.CLICK, 20.0F, 20.0F);
            }
        }

        if(this.counter == 0) {
            this.cancel();
            for(Player player : players) {
                player.playSound(player.getLocation(), Sound.LEVEL_UP, 20.0F, 20.0F);
            }
            
            this.game.start(Main.getInstance().getServer().getConsoleSender());
        }

        this.counter--;
    }

}
