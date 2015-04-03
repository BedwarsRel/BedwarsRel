package io.github.yannici.bedwarsreloaded.Listener;

import io.github.yannici.bedwarsreloaded.Main;
import io.github.yannici.bedwarsreloaded.Game.Game;

import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.server.ServerListPingEvent;

public class ServerListener extends BaseListener {

    @EventHandler
    public void onServerListPing(ServerListPingEvent slpe) {
        // Only enabled on bungeecord
        /*if(!Main.getInstance().isBungee()) {
            return;
        }*/
        
        if(Main.getInstance().getGameManager().getGames().size() == 0) {
            return;
        }
        
        Game game = Main.getInstance().getGameManager().getGames().get(0);
        switch(game.getState()) {
            case STOPPED:
                slpe.setMotd(ChatColor.translateAlternateColorCodes('§', Main.getInstance().getConfig().getString("bungeecord.motd.stopped")));
                break;
            case WAITING:
                slpe.setMotd(ChatColor.translateAlternateColorCodes('§', Main.getInstance().getConfig().getString("bungeecord.motd.waiting")));
                break;
            case RUNNING:
                slpe.setMotd(ChatColor.translateAlternateColorCodes('§', Main.getInstance().getConfig().getString("bungeecord.motd.running")));
                break;
        }
    }

}
