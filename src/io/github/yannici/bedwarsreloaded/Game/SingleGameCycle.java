package io.github.yannici.bedwarsreloaded.Game;

import io.github.yannici.bedwarsreloaded.ChatWriter;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

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
			if(!p.isDead()) {
				p.teleport(this.getGame().getLobby());
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
		player.teleport(storage.getLeft());
		
		this.checkGameOver();
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

    @Override
    public void onGameOver(GameOverTask task) {
        if(task.getCounter() == task.getStartCount()) {
            this.getGame().broadcast(ChatColor.GOLD + "Congratulations! Team " + task.getWinner().getDisplayName() + ChatColor.GOLD + " wins!");
        }
        
        if(task.getCounter() == 0) {
            this.onGameEnds();
            task.cancel();
        } else {
            this.getGame().broadcast(ChatColor.AQUA + "Back to lobby in " + ChatColor.YELLOW + task.getCounter() + ChatColor.AQUA + " second(s)!");
        }
        
        task.decCounter();
    }
	
}
