package io.github.yannici.bedwars.Shop.Specials;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import io.github.yannici.bedwars.Main;
import io.github.yannici.bedwars.SoundMachine;
import io.github.yannici.bedwars.Game.Game;
import io.github.yannici.bedwars.Game.Team;

public class Trap extends SpecialItem {

	private Game game = null;
	private Team team = null;
	private int duration = 10;
	private int amplifierBlindness = 1;
	private int amplifierSlowness = 1;
	private int amplifierWeakness = 1;
	private boolean activateBlindness = true;
	private boolean activateSlowness = true;
	private boolean activateWeakness = true;
	private boolean particles = true;
	private boolean playSound = true;
	private Location location = null;

	public Trap() {
		this.duration = Main.getInstance().getIntConfig("specials.trap.duration", 10);
		this.amplifierBlindness = Main.getInstance().getIntConfig("specials.trap.blindness.amplifier", 2);
		this.amplifierSlowness = Main.getInstance().getIntConfig("specials.trap.slowness.amplifier", 2);
		this.amplifierWeakness = Main.getInstance().getIntConfig("specials.trap.weakness.amplifier", 1);
		this.particles = Main.getInstance().getBooleanConfig("specials.trap.show-particles", true);
		this.playSound = Main.getInstance().getBooleanConfig("specials.trap.play-sound", true);
		this.activateBlindness = Main.getInstance().getBooleanConfig("specials.trap.blindness.enabled", true);
		this.activateSlowness = Main.getInstance().getBooleanConfig("specials.trap.slowness.enabled", true);
		this.activateWeakness = Main.getInstance().getBooleanConfig("specials.trap.weakness.enabled", true);
	}

	@Override
	public Material getItemMaterial() {
		return Material.TRIPWIRE;
	}

	@Override
	public Material getActivatedMaterial() {
		return null;
	}

	private Constructor<PotionEffect> getPotionConstructor() {
		Constructor<PotionEffect> effectConstructor = null;
		try {
			effectConstructor = PotionEffect.class.getConstructor(PotionEffectType.class, int.class, int.class,
					boolean.class, boolean.class);
			return effectConstructor;
		} catch (Exception ex) {
			// no constr
		}

		try {
			effectConstructor = PotionEffect.class.getConstructor(PotionEffectType.class, int.class, int.class,
					boolean.class);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return effectConstructor;
	}

	public void activate(final Player player) {
		try {
			List<PotionEffect> effects = new ArrayList<PotionEffect>();
			Constructor<PotionEffect> potionEffectConstr = this.getPotionConstructor();

			if (this.activateBlindness) {
				PotionEffect blind = null;

				if (potionEffectConstr.getParameterTypes().length == 5) {
					blind = potionEffectConstr.newInstance(PotionEffectType.BLINDNESS, this.duration * 20,
							this.amplifierBlindness, true, this.particles);
				} else {
					blind = potionEffectConstr.newInstance(PotionEffectType.BLINDNESS, this.duration * 20,
							this.amplifierBlindness, true);
				}

				effects.add(blind);
			}

			if (this.activateWeakness) {
				PotionEffect weak = null;
				if (potionEffectConstr.getParameterTypes().length == 5) {
					weak = potionEffectConstr.newInstance(PotionEffectType.WEAKNESS, this.duration * 20,
							this.amplifierWeakness, true, this.particles);
				} else {
					weak = potionEffectConstr.newInstance(PotionEffectType.WEAKNESS, this.duration * 20,
							this.amplifierWeakness, true);
				}
				effects.add(weak);
			}

			if (this.activateSlowness) {
				PotionEffect slow = null;
				if (potionEffectConstr.getParameterTypes().length == 5) {
					slow = potionEffectConstr.newInstance(PotionEffectType.SLOW, this.duration * 20,
							this.amplifierSlowness, true, this.particles);
				} else {
					slow = potionEffectConstr.newInstance(PotionEffectType.SLOW, this.duration * 20,
							this.amplifierSlowness, true);
				}

				effects.add(slow);
			}

			this.game.addRunningTask(new BukkitRunnable() {

				private int counter = 0;

				@Override
				public void run() {
					if (this.counter >= Trap.this.duration) {
						Trap.this.game.removeRunningTask(this);
						this.cancel();
						return;
					}

					player.playSound(player.getLocation(), SoundMachine.get("FUSE", "ENTITY_TNT_PRIMED"), 2.0F, 1.0F);
					this.counter++;
				}
			}.runTaskTimer(Main.getInstance(), 0L, 20L));

			if (effects.size() > 0) {
				for (PotionEffect effect : effects) {
					if (player.hasPotionEffect(effect.getType())) {
						player.removePotionEffect(effect.getType());
					}

					player.addPotionEffect(effect);
				}
			}

			this.game.broadcast(Main._l("ingame.specials.trap.trapped"), new ArrayList<Player>(this.team.getPlayers()));
			if (this.playSound) {
				this.game.broadcastSound(SoundMachine.get("SHEEP_IDLE", "ENTITY_SHEEP_AMBIENT"), 4.0F, 1.0F, this.team.getPlayers());
			}

			this.game.getRegion().removePlacedUnbreakableBlock(this.location.getBlock());
			this.location.getBlock().setType(Material.AIR);
			this.game.removeSpecialItem(this);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public void create(Game game, Team team, Location location) {
		this.game = game;
		this.team = team;
		this.location = location;

		this.game.addSpecialItem(this);
	}

	public Game getGame() {
		return this.game;
	}

	public Location getLocation() {
		return this.location;
	}

	public void setLocation(Location location) {
		this.location = location;
	}

	public Team getPlacedTeam() {
		return this.team;
	}
}
