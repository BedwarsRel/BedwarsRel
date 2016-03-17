package io.github.yannici.bedwars.Listener;

import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.server.ServerListPingEvent;

import io.github.yannici.bedwars.Main;
import io.github.yannici.bedwars.Game.Game;
import io.github.yannici.bedwars.Game.GameState;

public class ServerListener extends BaseListener {

	@EventHandler
	public void onServerListPing(ServerListPingEvent slpe) {
		// Only enabled on bungeecord
		if (!Main.getInstance().isBungee()) {
			return;
		}

		if (Main.getInstance().getGameManager().getGames().size() == 0) {
			return;
		}

		Game game = Main.getInstance().getGameManager().getGames().get(0);
		switch (game.getState()) {
		case STOPPED:
			slpe.setMotd(replacePlaceholder(game, ChatColor.translateAlternateColorCodes('&',
					Main.getInstance().getConfig().getString("bungeecord.motds.stopped"))));
			break;
		case WAITING:
			if (game.isFull()) {
				slpe.setMotd(replacePlaceholder(game, ChatColor.translateAlternateColorCodes('&',
						Main.getInstance().getConfig().getString("bungeecord.motds.full"))));
			} else {
				slpe.setMotd(replacePlaceholder(game, ChatColor.translateAlternateColorCodes('&',
						Main.getInstance().getConfig().getString("bungeecord.motds.lobby"))));
			}

			break;
		case RUNNING:
			slpe.setMotd(replacePlaceholder(game, ChatColor.translateAlternateColorCodes('&',
					Main.getInstance().getConfig().getString("bungeecord.motds.running"))));
			break;
		}
	}

	private String replacePlaceholder(Game game, String line) {
		line = line.replace("$title$", Main._l("sign.firstline"));
		line = line.replace("$gamename$", game.getName());
		line = line.replace("$regionname$", game.getRegion().getName());
		line = line.replace("$maxplayers$", getMaxPlayersString(game));
		line = line.replace("$currentplayers$", getCurrentPlayersString(game));
		line = line.replace("$status$", getStatus(game));

		return line;
	}

	private String getMaxPlayersString(Game game) {
		int maxPlayers = game.getMaxPlayers();
		return String.valueOf(maxPlayers);
	}

	private String getCurrentPlayersString(Game game) {
		int currentPlayers = 0;
		if (game.getState() == GameState.RUNNING) {
			currentPlayers = game.getTeamPlayers().size();
		} else if (game.getState() == GameState.WAITING) {
			currentPlayers = game.getPlayers().size();
		} else {
			currentPlayers = 0;
		}

		return String.valueOf(currentPlayers);
	}

	private String getStatus(Game game) {
		String status = null;
		if (game.getState() == GameState.WAITING && game.isFull()) {
			status = ChatColor.RED + Main._l("sign.gamestate.full");
		} else {
			status = Main._l("sign.gamestate." + game.getState().toString().toLowerCase());
		}

		return status;
	}

}
