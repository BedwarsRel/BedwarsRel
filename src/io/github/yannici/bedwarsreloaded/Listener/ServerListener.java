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
                slpe.setMotd(ChatColor.RED + "[Stopped]");
                break;
            case WAITING:
                slpe.setMotd(ChatColor.GREEN + "[Lobby]");
                break;
            case RUNNING:
                slpe.setMotd(ChatColor.YELLOW + "[Running]");
                break;
        }
    }

}
