package io.github.yannici.bedwars.Shop.Specials;

import java.util.ArrayList;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import io.github.yannici.bedwars.ChatWriter;
import io.github.yannici.bedwars.Main;
import io.github.yannici.bedwars.Events.BedwarsUseTNTSheepEvent;
import io.github.yannici.bedwars.Game.Game;
import io.github.yannici.bedwars.Game.GameState;
import io.github.yannici.bedwars.Game.Team;

public class TNTSheep extends SpecialItem {

	private Player player = null;
	private Game game = null;
	private ITNTSheep sheep = null;

	@Override
	public Material getItemMaterial() {
		return Material.MONSTER_EGG;
	}

	public int getEntityTypeId() {
		return 91;
	}

	@Override
	public Material getActivatedMaterial() {
		return null;
	}

	public void setPlayer(Player player) {
		this.player = player;
	}

	public void setGame(Game game) {
		this.game = game;
	}

	public Game getGame() {
		return this.game;
	}

	public Player getPlayer() {
		return this.player;
	}

	public ITNTSheep getSheep() {
		return this.sheep;
	}

	public void run(Location startLocation) {
		ItemStack usedStack = this.player.getItemInHand().clone();
		usedStack.setAmount(1);
		this.player.getInventory().removeItem(usedStack);

		final Team playerTeam = this.game.getPlayerTeam(this.player);
		Player targetPlayer = this.findTargetPlayer(this.player);
		if (targetPlayer == null) {
			this.player.sendMessage(
					ChatWriter.pluginMessage(ChatColor.RED + Main._l("ingame.specials.tntsheep.no-target-found")));
			return;
		}

		BedwarsUseTNTSheepEvent event = new BedwarsUseTNTSheepEvent(this.game, this.player, targetPlayer,
				startLocation);
		Main.getInstance().getServer().getPluginManager().callEvent(event);

		if (event.isCancelled()) {
			return;
		}

		final Player target = event.getTargetPlayer();
		final Location start = event.getStartLocation();

		// as task
		new BukkitRunnable() {

			@Override
			public void run() {
				final TNTSheep that = TNTSheep.this;

				try {
					// register entity
					Class<?> tntRegisterClass = Main.getInstance().getVersionRelatedClass("TNTSheepRegister");
					ITNTSheepRegister register = (ITNTSheepRegister) tntRegisterClass.newInstance();
					TNTSheep.this.sheep = register.spawnCreature(that, start, TNTSheep.this.player, target,
							playerTeam.getColor().getDyeColor());

					new BukkitRunnable() {

						@Override
						public void run() {
							that.getGame().getRegion().removeRemovingEntity(that.getSheep().getTNT().getVehicle());
							that.getGame().getRegion().removeRemovingEntity(that.getSheep().getTNT());
						}
					}.runTaskLater(Main.getInstance(),
							(long) ((Main.getInstance().getConfig().getDouble("specials.tntsheep.fuse-time", 8.0) * 20)
									- 5));

					new BukkitRunnable() {

						@Override
						public void run() {
							that.getSheep().getTNT().remove();
							that.getSheep().remove();
							that.getGame().removeSpecialItem(that);
						}
					}.runTaskLater(Main.getInstance(),
							(long) ((Main.getInstance().getConfig().getDouble("specials.tntsheep.fuse-time", 8.0) * 20)
									+ 13));

					TNTSheep.this.game.addSpecialItem(that);
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		}.runTask(Main.getInstance());
	}

	public void updateTNT() {
		new BukkitRunnable() {

			@Override
			public void run() {
				final TNTSheep that = TNTSheep.this;

				if (that.game.isStopping() || that.game.getState() != GameState.RUNNING) {
					return;
				}

				if (that.sheep == null) {
					return;
				}

				if (that.sheep.getTNT() == null) {
					return;
				}

				TNTPrimed old = that.sheep.getTNT();
				final int fuse = old.getFuseTicks();

				if (fuse <= 0) {
					return;
				}

				final Entity source = old.getSource();
				final Location oldLoc = old.getLocation();
				final float yield = old.getYield();
				old.leaveVehicle();
				old.remove();

				new BukkitRunnable() {

					@Override
					public void run() {
						TNTPrimed primed = (TNTPrimed) that.game.getRegion().getWorld().spawnEntity(oldLoc,
								EntityType.PRIMED_TNT);
						primed.setFuseTicks(fuse);
						primed.setYield(yield);
						primed.setIsIncendiary(false);
						that.sheep.setPassenger(primed);
						that.sheep.setTNT(primed);
						that.sheep.setTNTSource(source);

						if (primed.getFuseTicks() >= 60) {
							that.updateTNT();
						}
					}
				}.runTaskLater(Main.getInstance(), 3L);
			}

		}.runTaskLater(Main.getInstance(), 60L);
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

}
