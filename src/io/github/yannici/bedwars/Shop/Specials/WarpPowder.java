package io.github.yannici.bedwars.Shop.Specials;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import com.google.common.collect.ImmutableMap;

import io.github.yannici.bedwars.ChatWriter;
import io.github.yannici.bedwars.Main;
import io.github.yannici.bedwars.Utils;
import io.github.yannici.bedwars.Game.Game;
import io.github.yannici.bedwars.Game.Team;

public class WarpPowder extends SpecialItem {

	private BukkitTask teleportingTask = null;
	private double teleportingTime = 6.0;
	private Player player = null;
	private Game game = null;
	private int fullTeleportingTime = 6;
	private ItemStack stack = null;

	public WarpPowder() {
		super();

		this.fullTeleportingTime = Main.getInstance().getIntConfig("specials.warp-powder.teleport-time", 6);
	}

	@Override
	public Material getItemMaterial() {
		return Material.SULPHUR;
	}

	@Override
	public Material getActivatedMaterial() {
		return Material.GLOWSTONE_DUST;
	}

	public Player getPlayer() {
		return this.player;
	}

	public void cancelTeleport(boolean removeSpecial, boolean showMessage) {
		try {
			this.teleportingTask.cancel();
		} catch (Exception ex) {
			// already stopped
		}

		this.teleportingTime = (double) Main.getInstance().getIntConfig("specials.warp-powder.teleport-time", 6);
		this.game.removeRunningTask(this.teleportingTask);
		this.player.setLevel(0);

		if (removeSpecial) {
			this.game.removeSpecialItem(this);
		}

		if (showMessage) {
			this.player.sendMessage(ChatWriter.pluginMessage(Main._l("ingame.specials.warp-powder.cancelled")));
		}

		this.player.getInventory().removeItem(this.getCancelItemStack());
		this.player.updateInventory();
	}

	private ItemStack getCancelItemStack() {
		ItemStack glowstone = new ItemStack(this.getActivatedMaterial(), 1);
		ItemMeta meta = glowstone.getItemMeta();
		meta.setDisplayName(Main._l("ingame.specials.warp-powder.cancel"));
		glowstone.setItemMeta(meta);

		return glowstone;
	}

	public void runTask() {
		final int circles = 15;
		final double height = 2.0;

		ItemStack usedStack = this.player.getInventory().getItemInHand();
		this.stack = usedStack.clone();
		this.stack.setAmount(1);

		usedStack.setAmount(usedStack.getAmount() - 1);
		this.player.getInventory().setItem(this.player.getInventory().getHeldItemSlot(), usedStack);
		this.player.getInventory().addItem(this.getCancelItemStack());
		this.player.updateInventory();

		this.teleportingTime = (double) Main.getInstance().getIntConfig("specials.warp-powder.teleport-time", 6);
		this.player.sendMessage(ChatWriter.pluginMessage(Main._l("ingame.specials.warp-powder.start",
				ImmutableMap.of("time", String.valueOf(this.fullTeleportingTime)))));

		this.teleportingTask = new BukkitRunnable() {

			public double through = 0.0;
			public String particle = Main.getInstance().getStringConfig("specials.warp-powder.particle",
					"fireworksSpark");
			public boolean showParticle = Main.getInstance().getBooleanConfig("specials.warp-powder.show-particles",
					true);

			@Override
			public void run() {
				try {
					int circleElements = 20;
					double radius = 1.0;
					double height2 = 1.0;
					double circles = 15.0;
					double fulltime = (double) WarpPowder.this.fullTeleportingTime;
					double teleportingTime = WarpPowder.this.teleportingTime;

					double perThrough = (Math.ceil((height / circles) * ((fulltime * 20) / circles)) / 20);

					WarpPowder.this.teleportingTime = teleportingTime - perThrough;
					Team team = WarpPowder.this.game.getPlayerTeam(WarpPowder.this.player);
					Location tLoc = team.getSpawnLocation();

					if (WarpPowder.this.teleportingTime <= 1.0) {
						WarpPowder.this.player.teleport(team.getSpawnLocation());
						WarpPowder.this.cancelTeleport(true, false);
						return;
					}

					WarpPowder.this.player.setLevel((int) WarpPowder.this.teleportingTime);
					if (!showParticle) {
						return;
					}

					Location loc = WarpPowder.this.player.getLocation();

					double y = (height2 / circles) * through;
					for (int i = 0; i < 20; i++) {
						double alpha = (360.0 / circleElements) * i;
						double x = radius * Math.sin(Math.toRadians(alpha));
						double z = radius * Math.cos(Math.toRadians(alpha));

						Location particleFrom = new Location(loc.getWorld(), loc.getX() + x, loc.getY() + y,
								loc.getZ() + z);
						Utils.createParticleInGame(game, this.particle, particleFrom);

						Location particleTo = new Location(tLoc.getWorld(), tLoc.getX() + x, tLoc.getY() + y,
								tLoc.getZ() + z);
						Utils.createParticleInGame(game, this.particle, particleTo);
					}

					this.through += 1.0;
				} catch (Exception ex) {
					ex.printStackTrace();
					this.cancel();
					WarpPowder.this.cancelTeleport(true, false);
				}
			}
		}.runTaskTimer(Main.getInstance(), 0L,
				(long) Math.ceil((height / circles) * ((this.fullTeleportingTime * 20) / circles)));
		this.game.addRunningTask(this.teleportingTask);
		this.game.addSpecialItem(this);
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
