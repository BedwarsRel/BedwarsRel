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
			p.teleport(this.getGame().getLobby());
			this.getGame().getPlayerStorage(p).loadLobbyInventory();
		}
		
		this.getGame().setState(GameState.WAITING);
	}

	@Override
	public void onPlayerLeave(Player player) {
		// teleport to join location
		PlayerStorage storage = this.getGame().getPlayerStorage(player);
		
		player.teleport(storage.getLeft());
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
	
}
