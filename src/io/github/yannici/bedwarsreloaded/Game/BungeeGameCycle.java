package io.github.yannici.bedwarsreloaded.Game;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;

import io.github.yannici.bedwarsreloaded.ChatWriter;
import io.github.yannici.bedwarsreloaded.Main;
import io.github.yannici.bedwarsreloaded.Utils;
import io.github.yannici.bedwarsreloaded.Game.Events.BedwarsGameEndEvent;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import com.google.common.collect.ImmutableMap;

public class BungeeGameCycle extends GameCycle {

	public BungeeGameCycle(Game game) {
		super(game);
	}

	@Override
	public void onGameStart() {
		// do nothing, world will be reseted on restarting
	}

	@Override
	public void onGameEnds() {
		this.getGame().kickAllPlayers();
        Bukkit.shutdown();
	}

	@Override
	public void onPlayerLeave(Player player) {
		if(player.isOnline()) {
			this.bungeeSendToServer(Main.getInstance().getBungeeHub(), player);
		}
		
		this.checkGameOver();
	}

	@Override
	public void onGameLoaded() {
		this.getGame().resetRegion();
	}

	@Override
	public boolean onPlayerJoins(Player player) {
		if(this.getGame().isFull()) {
			player.sendMessage(ChatWriter.pluginMessage(ChatColor.RED + Main._l("lobby.gamefull")));
			return false;
		}
		
		return true;
	}
	
	private void bungeeSendToServer(String server, Player player) {
		if(server == null) {
			player.sendMessage(ChatWriter.pluginMessage(ChatColor.RED + Main._l("errors.bungeenoserver")));
			return;
		}
		
		ByteArrayOutputStream b = new ByteArrayOutputStream();
	    DataOutputStream out = new DataOutputStream(b);
	    
	    try {
	      out.writeUTF("Connect");
	      out.writeUTF(server);
	    } catch (Exception e) {
	      e.printStackTrace();
	      return;
	    }
	    
	    if ((b != null) && (Utils.checkBungeePlugin())) {
	      player.sendPluginMessage(Main.getInstance(), "BungeeCord", b.toByteArray());
	    }
	}

    @Override
    public void onGameOver(GameOverTask task) {
        if(task.getCounter() == task.getStartCount()) {
            this.getGame().broadcast(ChatColor.GOLD + Main._l("ingame.teamwon", ImmutableMap.of("team", task.getWinner().getDisplayName() + ChatColor.GOLD)));
        }
        
        // game over
        if(task.getCounter() == 0) {
            this.onGameEnds();
            
            BedwarsGameEndEvent endEvent = new BedwarsGameEndEvent(this.getGame());
            Main.getInstance().getServer().getPluginManager().callEvent(endEvent);
            
            task.cancel();
        } else {
            this.getGame().broadcast(ChatColor.AQUA + Main._l("ingame.serverrestart", ImmutableMap.of("sec", ChatColor.YELLOW.toString() + task.getCounter() + ChatColor.AQUA)));
        }
        
        task.decCounter();
    }

}
