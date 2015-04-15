package io.github.yannici.bedwars.Game;

import io.github.yannici.bedwars.ChatWriter;
import io.github.yannici.bedwars.Main;
import io.github.yannici.bedwars.Events.BedwarsGameEndEvent;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import com.google.common.collect.ImmutableMap;

public class SingleGameCycle extends GameCycle {

	public SingleGameCycle(Game game) {
		super(game);
	}

	@Override
	public void onGameStart() {
		this.getGame().resetRegion();
	}

	@Override
	public void onGameEnds() {
		// All players which are in game, kick to lobby
		for(Player p : this.getGame().getPlayers()) {
			for(Player freePlayer : this.getGame().getFreePlayers()) {
				p.showPlayer(freePlayer);
			}
			
			if(!p.isDead()) {
				p.teleport(this.getGame().getLobby());
			} else {
				this.getGame().playerLeave(p);
			}
			
			PlayerStorage storage = this.getGame().getPlayerStorage(p);
			storage.clean();
			storage.loadLobbyInventory();
			
			this.getGame().resetScoreboard();
			p.setScoreboard(this.getGame().getScoreboard());
		}
		
		this.setEndGameRunning(false);
		this.getGame().setState(GameState.WAITING);
	}

	@Override
	public void onPlayerLeave(Player player) {
		// teleport to join location
		PlayerStorage storage = this.getGame().getPlayerStorage(player);
		
		if(Main.getInstance().toMainLobby()) {
			player.teleport(this.getGame().getMainLobby());
		} else {
			player.teleport(storage.getLeft());
		}
		
		if(this.getGame().getState() == GameState.RUNNING) {
			this.checkGameOver();
		}
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

    @Override
    public void onGameOver(GameOverTask task) {
        if(task.getCounter() == task.getStartCount() && task.getWinner() != null) {
            this.getGame().broadcast(ChatColor.GOLD + Main._l("ingame.teamwon", ImmutableMap.of("team", task.getWinner().getDisplayName() + ChatColor.GOLD)));
        } else if(task.getCounter() == task.getStartCount() && task.getWinner() == null) {
        	this.getGame().broadcast(ChatColor.GOLD + Main._l("ingame.draw"));
        }
        
        if(task.getCounter() == 0) {
        	BedwarsGameEndEvent endEvent = new BedwarsGameEndEvent(this.getGame());
            Main.getInstance().getServer().getPluginManager().callEvent(endEvent);
            
            this.onGameEnds();
            task.cancel();
        } else {
            this.getGame().broadcast(ChatColor.AQUA + Main._l("ingame.backtolobby", ImmutableMap.of("sec", ChatColor.YELLOW.toString() + task.getCounter() + ChatColor.AQUA)));
        }
        
        task.decCounter();
    }
	
}
