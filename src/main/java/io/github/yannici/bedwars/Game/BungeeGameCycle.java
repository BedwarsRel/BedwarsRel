package io.github.yannici.bedwars.Game;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

import com.google.common.collect.ImmutableMap;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;

import io.github.yannici.bedwars.ChatWriter;
import io.github.yannici.bedwars.Main;
import io.github.yannici.bedwars.Utils;
import io.github.yannici.bedwars.Events.BedwarsGameEndEvent;

public class BungeeGameCycle extends GameCycle {

	public BungeeGameCycle(Game game) {
		super(game);
	}

	@Override
	public void onGameStart() {
		// do nothing, world will be reseted on restarting
	}

	private void kickAllPlayers() {
		for (Player player : this.getGame().getTeamPlayers()) {
			for (Player freePlayer : this.getGame().getFreePlayers()) {
				player.showPlayer(freePlayer);
			}
			this.getGame().playerLeave(player, false);
		}

		for (Player freePlayer : this.getGame().getFreePlayersClone()) {
			this.getGame().playerLeave(freePlayer, false);
		}
	}

	@Override
	public void onGameEnds() {
		if (Main.getInstance().getBooleanConfig("bungeecord.full-restart", true)) {
			this.kickAllPlayers();

			this.getGame().resetRegion();
			new BukkitRunnable() {

				@Override
				public void run() {
					if (Main.getInstance().isSpigot()
							&& Main.getInstance().getBooleanConfig("bungeecord.spigot-restart", true)) {
						Main.getInstance().getServer()
								.dispatchCommand(Main.getInstance().getServer().getConsoleSender(), "restart");
					} else {
						Bukkit.shutdown();
					}
				}
			}.runTaskLater(Main.getInstance(), 70L);
		} else {
			// Reset scoreboard first
			this.getGame().resetScoreboard();

			// Kick all players
			this.kickAllPlayers();

			// reset countdown prevention breaks
			this.setEndGameRunning(false);

			// Reset team chests
			for (Team team : this.getGame().getTeams().values()) {
				team.setInventory(null);
				team.getChests().clear();
			}

			// clear protections
			this.getGame().clearProtections();

			// set state and with that, the sign
			this.getGame().setState(GameState.WAITING);
			this.getGame().updateScoreboard();

			// reset region
			this.getGame().resetRegion();
		}
	}

	@Override
	public void onPlayerLeave(Player player) {
		if (player.isOnline() || player.isDead()) {
			this.bungeeSendToServer(Main.getInstance().getBungeeHub(), player, true);
		}

		if (this.getGame().getState() == GameState.RUNNING && !this.getGame().isStopping()) {
			this.checkGameOver();
		}
	}

	@Override
	public void onGameLoaded() {
		// Reset on game end
	}

	@Override
	public boolean onPlayerJoins(Player player) {
		final Player p = player;

		if (this.getGame().isFull() && !player.hasPermission("bw.vip.joinfull")) {
			if (this.getGame().getState() != GameState.RUNNING || !Main.getInstance().spectationEnabled()) {
				this.bungeeSendToServer(Main.getInstance().getBungeeHub(), p, false);
				new BukkitRunnable() {

					@Override
					public void run() {
						BungeeGameCycle.this.sendBungeeMessage(p,
								ChatWriter.pluginMessage(ChatColor.RED + Main._l("lobby.gamefull")));
					}
				}.runTaskLater(Main.getInstance(), 60L);

				return false;
			}
		} else if (this.getGame().isFull() && player.hasPermission("bw.vip.joinfull")) {
			if (this.getGame().getState() == GameState.WAITING) {
				List<Player> players = this.getGame().getNonVipPlayers();

				if (players.size() == 0) {
					this.bungeeSendToServer(Main.getInstance().getBungeeHub(), p, false);
					new BukkitRunnable() {

						@Override
						public void run() {
							BungeeGameCycle.this.sendBungeeMessage(p,
									ChatWriter.pluginMessage(ChatColor.RED + Main._l("lobby.gamefullpremium")));
						}
					}.runTaskLater(Main.getInstance(), 60L);
					return false;
				}

				Player kickPlayer = null;
				if (players.size() == 1) {
					kickPlayer = players.get(0);
				} else {
					kickPlayer = players.get(Utils.randInt(0, players.size() - 1));
				}

				final Player kickedPlayer = kickPlayer;

				this.getGame().playerLeave(kickedPlayer, false);
				new BukkitRunnable() {

					@Override
					public void run() {
						BungeeGameCycle.this.sendBungeeMessage(kickedPlayer,
								ChatWriter.pluginMessage(ChatColor.RED + Main._l("lobby.kickedbyvip")));
					}
				}.runTaskLater(Main.getInstance(), 60L);
			} else {
				if (this.getGame().getState() == GameState.RUNNING && !Main.getInstance().spectationEnabled()) {

					new BukkitRunnable() {

						@Override
						public void run() {
							BungeeGameCycle.this.bungeeSendToServer(Main.getInstance().getBungeeHub(), p, false);
						}

					}.runTaskLater(Main.getInstance(), 5L);

					new BukkitRunnable() {

						@Override
						public void run() {
							BungeeGameCycle.this.sendBungeeMessage(p,
									ChatWriter.pluginMessage(ChatColor.RED + Main._l("lobby.gamefull")));
						}
					}.runTaskLater(Main.getInstance(), 60L);
					return false;
				}
			}
		}

		return true;
	}

	public void sendBungeeMessage(Player player, String message) {
		ByteArrayDataOutput out = ByteStreams.newDataOutput();

		out.writeUTF("Message");
		out.writeUTF(player.getName());
		out.writeUTF(message);

		player.sendPluginMessage(Main.getInstance(), "BungeeCord", out.toByteArray());
	}

	public void bungeeSendToServer(final String server, final Player player, boolean preventDelay) {
		if (server == null) {
			player.sendMessage(ChatWriter.pluginMessage(ChatColor.RED + Main._l("errors.bungeenoserver")));
			return;
		}

		new BukkitRunnable() {

			@Override
			public void run() {
				ByteArrayOutputStream b = new ByteArrayOutputStream();
				DataOutputStream out = new DataOutputStream(b);

				try {
					out.writeUTF("Connect");
					out.writeUTF(server);
				} catch (Exception e) {
					e.printStackTrace();
					return;
				}

				if (b != null) {
					player.sendPluginMessage(Main.getInstance(), "BungeeCord", b.toByteArray());
				}
			}
		}.runTaskLater(Main.getInstance(), (preventDelay) ? 0L : 20L);
	}

	@Override
	public void onGameOver(GameOverTask task) {
		if (Main.getInstance().getBooleanConfig("bungeecord.endgame-in-lobby", true)) {
			final ArrayList<Player> players = new ArrayList<Player>();
			final Game game = this.getGame();
			players.addAll(this.getGame().getTeamPlayers());
			players.addAll(this.getGame().getFreePlayers());
			for (Player player : players) {

				if (!player.getWorld().equals(this.getGame().getLobby().getWorld())) {
					game.getPlayerSettings(player).setTeleporting(true);
					player.teleport(this.getGame().getLobby());
					game.getPlayerStorage(player).clean();
				}
			}

			new BukkitRunnable() {
				@Override
				public void run() {
					for (Player player : players) {
						game.setPlayerGameMode(player);
						game.setPlayerVisibility(player);

						if (!player.getInventory().contains(Material.SLIME_BALL)) {
							// Leave Game (Slimeball)
							ItemStack leaveGame = new ItemStack(Material.SLIME_BALL, 1);
							ItemMeta im = leaveGame.getItemMeta();
							im.setDisplayName(Main._l("lobby.leavegame"));
							leaveGame.setItemMeta(im);
							player.getInventory().setItem(8, leaveGame);
							player.updateInventory();
						}
					}
				}
			}.runTaskLater(Main.getInstance(), 20L);
		}
		if (task.getCounter() == task.getStartCount() && task.getWinner() != null) {
			this.getGame().broadcast(ChatColor.GOLD + Main._l("ingame.teamwon",
					ImmutableMap.of("team", task.getWinner().getDisplayName() + ChatColor.GOLD)));
		} else if (task.getCounter() == task.getStartCount() && task.getWinner() == null) {
			this.getGame().broadcast(ChatColor.GOLD + Main._l("ingame.draw"));
		}

		// game over
		if (task.getCounter() == 0) {
			BedwarsGameEndEvent endEvent = new BedwarsGameEndEvent(this.getGame());
			Main.getInstance().getServer().getPluginManager().callEvent(endEvent);

			this.onGameEnds();
			task.cancel();
		} else if ((task.getCounter() == task.getStartCount()) || (task.getCounter() % 10 == 0)
				|| (task.getCounter() <= 5 && (task.getCounter() > 0))) {
			this.getGame().broadcast(ChatColor.AQUA + Main._l("ingame.serverrestart",
					ImmutableMap.of("sec", ChatColor.YELLOW.toString() + task.getCounter() + ChatColor.AQUA)));
		}

		task.decCounter();
	}

}
