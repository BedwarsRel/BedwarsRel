package io.github.yannici.bedwars.Listener;

import io.github.yannici.bedwars.Main;
import io.github.yannici.bedwars.Utils;
import io.github.yannici.bedwars.Game.Game;

import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.server.ServerListPingEvent;

public class ServerListener extends BaseListener {

    @EventHandler
    public void onServerListPing(ServerListPingEvent slpe) {
        // Only enabled on bungeecord
        if(!Main.getInstance().isBungee() || !Utils.checkBungeePlugin()) {
            return;
        }
        
        if(Main.getInstance().getGameManager().getGames().size() == 0) {
            return;
        }
        
        Game game = Main.getInstance().getGameManager().getGames().get(0);
        switch(game.getState()) {
            case STOPPED:
                slpe.setMotd(ChatColor.RED + ChatColor.translateAlternateColorCodes('§', Main.getInstance().getConfig().getString("bungeecord.motds.stopped")));
                break;
            case WAITING:
                slpe.setMotd(ChatColor.GREEN + ChatColor.translateAlternateColorCodes('§', Main.getInstance().getConfig().getString("bungeecord.motds.lobby")));
                break;
            case RUNNING:
                slpe.setMotd(ChatColor.YELLOW + ChatColor.translateAlternateColorCodes('§', Main.getInstance().getConfig().getString("bungeecord.motds.running")));
                break;
        }
    }

}
