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
		// Reset scoreboard first
		this.getGame().resetScoreboard();

		// First team players, they get a reserved slot in lobby
		for (Player p : this.getGame().getTeamPlayers()) {
			this.kickPlayer(p, false);
		}

		// and now the spectators
		for (Player p : this.getGame().getFreePlayers()) {
			this.kickPlayer(p, true);
		}

		// reset countdown prevention breaks
		this.setEndGameRunning(false);

		// Reset team chests
		for (Team team : this.getGame().getTeams().values()) {
			team.setInventory(null);
			team.getChests().clear();
		}

		// set state and with that, the sign
		this.getGame().setState(GameState.WAITING);
	}

	private void kickPlayer(Player player, boolean wasSpectator) {
		for (Player freePlayer : this.getGame().getFreePlayers()) {
			player.showPlayer(freePlayer);
		}

		if (wasSpectator && this.getGame().isFull()) {
			this.getGame().playerLeave(player);
			return;
		}
		
		if(Main.getInstance().toMainLobby()) {
		    if(Main.getInstance().allPlayersBackToMainLobby()) {
		        this.getGame().playerLeave(player);
		        return;
		    } else {
		        player.teleport(this.getGame().getLobby());
		    }
		} else {
		    player.teleport(this.getGame().getLobby());
		}
		
		PlayerStorage storage = this.getGame().getPlayerStorage(player);
        storage.clean();
        storage.loadLobbyInventory();
        
        player.setScoreboard(this.getGame().getScoreboard());
	}

	@Override
	public void onPlayerLeave(Player player) {
		// teleport to join location
		PlayerStorage storage = this.getGame().getPlayerStorage(player);

		if (Main.getInstance().toMainLobby()) {
			player.teleport(this.getGame().getMainLobby());
		} else {
			player.teleport(storage.getLeft());
		}

		if (this.getGame().getState() == GameState.RUNNING && !this.getGame().isStopping()) {
			this.checkGameOver();
		}
	}

	@Override
	public void onGameLoaded() {
		this.getGame().resetRegion();
	}

	@Override
	public boolean onPlayerJoins(Player player) {
		if (this.getGame().isFull()) {
			if (this.getGame().getState() != GameState.RUNNING
					|| !Main.getInstance().spectationEnabled()) {
				player.sendMessage(ChatWriter.pluginMessage(ChatColor.RED
						+ Main._l("lobby.gamefull")));
				return false;
			}
		}

		return true;
	}

	@Override
	public void onGameOver(GameOverTask task) {
		if (task.getCounter() == task.getStartCount()
				&& task.getWinner() != null) {
			this.getGame()
					.broadcast(
							ChatColor.GOLD
									+ Main._l("ingame.teamwon", ImmutableMap
											.of("team", task.getWinner()
													.getDisplayName()
													+ ChatColor.GOLD)));
			this.getGame().stopWorkers();
		} else if (task.getCounter() == task.getStartCount()
				&& task.getWinner() == null) {
			this.getGame().broadcast(ChatColor.GOLD + Main._l("ingame.draw"));
		}

		if (task.getCounter() == 0) {
			BedwarsGameEndEvent endEvent = new BedwarsGameEndEvent(
					this.getGame());
			Main.getInstance().getServer().getPluginManager()
					.callEvent(endEvent);

			this.onGameEnds();
			task.cancel();
		} else {
			this.getGame().broadcast(
					ChatColor.AQUA
							+ Main._l(
									"ingame.backtolobby",
									ImmutableMap.of(
											"sec",
											ChatColor.YELLOW.toString()
													+ task.getCounter()
													+ ChatColor.AQUA)));
		}

		task.decCounter();
	}

}
