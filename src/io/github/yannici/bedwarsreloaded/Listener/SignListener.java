package io.github.yannici.bedwarsreloaded.Listener;

import io.github.yannici.bedwarsreloaded.Main;
import io.github.yannici.bedwarsreloaded.Game.Game;

import org.bukkit.ChatColor;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.SignChangeEvent;

public class SignListener extends BaseListener {

    @EventHandler
    public void onSignChange(SignChangeEvent sce) {
        String firstLine = sce.getLine(0).trim();
        if(!firstLine.startsWith("bw:")) {
            return;
        }
        
        Player player = sce.getPlayer();
        if(!player.hasPermission("bw.setup")) {
            return;
        }
        
        String gameName = firstLine.split(":")[1];
        Game game = Main.getInstance().getGameManager().getGame(gameName);
        
        if(game == null) {
            sce.setLine(0, ChatColor.RED + Main._l("errors.gamenotfoundsimple"));
            sce.setLine(1, "");
            sce.setLine(2, "");
            sce.setLine(3, "");
            return;
        }
        
        sce.setCancelled(true);
        game.addJoinSign((Sign)sce.getBlock().getState());
        game.updateSigns();
    }

}
