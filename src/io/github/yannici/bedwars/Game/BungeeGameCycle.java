package io.github.yannici.bedwars.Game;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;

import io.github.yannici.bedwars.ChatWriter;
import io.github.yannici.bedwars.Main;
import io.github.yannici.bedwars.Utils;
import io.github.yannici.bedwars.Events.BedwarsGameEndEvent;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

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
		for (Player player : this.getGame().getPlayers()) {
			for (Player freePlayer : this.getGame().getFreePlayers()) {
				player.showPlayer(freePlayer);
			}
			this.getGame().playerLeave(player);
		}
		
		this.getGame().resetRegion();
		Bukkit.shutdown();
	}

	@Override
	public void onPlayerLeave(Player player) {
		if (player.isOnline()) {
			this.bungeeSendToServer(Main.getInstance().getBungeeHub(), player);
		}

		if (this.getGame().getState() == GameState.RUNNING) {
			this.checkGameOver();
		}
	}

	@Override
	public void onGameLoaded() {
		// Reset on game end
	}

	@Override
	public boolean onPlayerJoins(Player player) {
		if (this.getGame().isFull()) {
			if (this.getGame().getState() != GameState.RUNNING
					|| !Main.getInstance().spectationEnabled()) {
				this.bungeeSendToServer(Main.getInstance().getBungeeHub(),
						player);
				new BukkitRunnable() {

					@Override
					public void run() {
						BungeeGameCycle.this.sendBungeeMessage(
								player,
								ChatWriter.pluginMessage(ChatColor.RED
										+ Main._l("lobby.gamefull")));
					}
				}.runTaskLater(Main.getInstance(), 40L);

				return false;
			}
		}

		return true;
	}

	private void sendBungeeMessage(Player player, String message) {
		ByteArrayOutputStream b = new ByteArrayOutputStream();
		DataOutputStream out = new DataOutputStream(b);

		try {
			out.writeUTF("Message");
			out.writeUTF(player.getName());
			out.writeUTF(message);
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}

		if ((b != null) && (Utils.checkBungeePlugin())) {
			player.sendPluginMessage(Main.getInstance(), "BungeeCord",
					b.toByteArray());
		}
	}

	private void bungeeSendToServer(String server, Player player) {
		if (server == null) {
			player.sendMessage(ChatWriter.pluginMessage(ChatColor.RED
					+ Main._l("errors.bungeenoserver")));
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
			player.sendPluginMessage(Main.getInstance(), "BungeeCord",
					b.toByteArray());
		}
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
		} else if (task.getCounter() == task.getStartCount()
				&& task.getWinner() == null) {
			this.getGame().broadcast(ChatColor.GOLD + Main._l("ingame.draw"));
		}

		// game over
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
									"ingame.serverrestart",
									ImmutableMap.of(
											"sec",
											ChatColor.YELLOW.toString()
													+ task.getCounter()
													+ ChatColor.AQUA)));
		}

		task.decCounter();
	}

}
