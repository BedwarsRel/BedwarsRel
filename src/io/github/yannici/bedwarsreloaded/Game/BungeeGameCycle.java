package io.github.yannici.bedwarsreloaded.Game;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;

import io.github.yannici.bedwarsreloaded.ChatWriter;
import io.github.yannici.bedwarsreloaded.Main;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

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
	}

	@Override
	public void onPlayerLeave(Player player) {
		if(player.isOnline()) {
			this.bungeeSendToServer(Main.getInstance().getBungeeHub(), player);
		}
	}

	@Override
	public void onGameLoaded() {
		this.getGame().resetRegion();
	}

	@Override
	public boolean onPlayerJoins(Player player) {
		if(this.getGame().isFull()) {
			player.sendMessage(ChatWriter.pluginMessage(ChatColor.RED + "Game is full!"));
			return false;
		}
		
		return true;
	}
	
	private void bungeeSendToServer(String server, Player player) {
		if(server == null) {
			player.sendMessage(ChatWriter.pluginMessage("Bungeecord Servers wasn't set properly! Talk to the server administrator!"));
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
	    
	    if ((b != null) && (this.checkBungeePlugin())) {
	      player.sendPluginMessage(Main.getInstance(), "BungeeCord", b.toByteArray());
	    }
	}
	
	private boolean checkBungeePlugin()
	  {
	    try
	    {
	      Class.forName("net.md_5.bungee.BungeeCord");
	      return true;
	    }
	    catch (Exception e) {}
	    
	    return false;
	  }

}
