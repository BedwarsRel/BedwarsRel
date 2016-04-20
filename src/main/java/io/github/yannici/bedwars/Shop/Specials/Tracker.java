package io.github.yannici.bedwars.Shop.Specials;

import java.util.ArrayList;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import com.google.common.collect.ImmutableMap;

import io.github.yannici.bedwars.ChatWriter;
import io.github.yannici.bedwars.Main;
import io.github.yannici.bedwars.Game.Game;
import io.github.yannici.bedwars.Game.Team;

public class Tracker extends SpecialItem {

	private Player player = null;
	private Game game = null;
	private ItemStack stack = null;

	public Tracker() {
		super();
	}

	@Override
	public Material getItemMaterial() {
		return Material.COMPASS;
	}

	@Override
	public Material getActivatedMaterial() {
		return null;
	}

	public void trackPlayer() {
		Player target = findTargetPlayer(this.player);

		if (target == null) {
			this.player.sendMessage(
					ChatWriter.pluginMessage(ChatColor.RED + Main._l("ingame.specials.tracker.no-target-found")));
			this.player.setCompassTarget(this.game.getPlayerTeam(this.player).getSpawnLocation());
			return;
		}

		int blocks = (int) this.player.getLocation().distance(target.getLocation());
		this.player.sendMessage(ChatWriter.pluginMessage(Main._l("ingame.specials.tracker.target-found",
				ImmutableMap.of("player", target.getDisplayName(), "blocks", String.valueOf(blocks)))));
	}

	public void createTask() {
		final Game game = this.game;

		BukkitTask task = new BukkitRunnable() {

			@Override
			public void run() {
				for (Player player : game.getTeamPlayers()) {
					if (player.getInventory().contains(getItemMaterial())) {
						Player target = findTargetPlayer(player);
						if (target != null) {
							player.setCompassTarget(target.getLocation());
							continue;
						}
					}
					player.setCompassTarget(game.getPlayerTeam(player).getSpawnLocation());
					continue;
				}
			}
		}.runTaskTimer(Main.getInstance(), 20L, 20L);
		this.game.addRunningTask(task);
	}

	private Player findTargetPlayer(Player player) {
		Player foundPlayer = null;
		double distance = Double.MAX_VALUE;

		Team playerTeam = this.game.getPlayerTeam(player);

		ArrayList<Player> possibleTargets = new ArrayList<Player>();
		possibleTargets.addAll(this.game.getTeamPlayers());
		possibleTargets.removeAll(playerTeam.getPlayers());

		for (Player p : possibleTargets) {
			double dist = player.getLocation().distance(p.getLocation());
			if (dist < distance) {
				foundPlayer = p;
				distance = dist;
			}
		}

		return foundPlayer;
	}

	public Player getPlayer() {
		return this.player;
	}

	public void setPlayer(Player player) {
		this.player = player;
	}

	public void setGame(Game game) {
		this.game = game;
	}

	public ItemStack getStack() {
		return this.stack;
	}
}
