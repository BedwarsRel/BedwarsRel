package io.github.yannici.bedwars.Shop.Specials;

import java.util.ArrayList;
import java.util.List;

import io.github.yannici.bedwars.Main;
import io.github.yannici.bedwars.Game.Game;
import io.github.yannici.bedwars.Game.Team;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

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
		this.amplifierBlindness = Main.getInstance().getIntConfig("specials.trap.blindness.amplifier", 1);
		this.amplifierSlowness = Main.getInstance().getIntConfig("specials.trap.slowness.amplifier", 1);
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
	
	public void activate(Player player) {
		List<PotionEffect> effects = new ArrayList<PotionEffect>();
		
		if(this.activateBlindness) {
			PotionEffect blind = new PotionEffect(PotionEffectType.BLINDNESS, this.duration*20, this.amplifierBlindness, true, this.particles);
			effects.add(blind);
		}
		
		if(this.activateSlowness) {
			PotionEffect slow = new PotionEffect(PotionEffectType.SLOW, this.duration*20, this.amplifierSlowness, true, this.particles);
			effects.add(slow);
		}
		
		if(this.activateWeakness) {
			PotionEffect weakness = new PotionEffect(PotionEffectType.WEAKNESS, this.duration*20, this.amplifierWeakness, true, this.particles);
			effects.add(weakness);
		}
		
		if(effects.size() > 0) {
			player.addPotionEffects(effects);
		}
		
		this.game.broadcast(Main._l("ingame.specials.trap.trapped"), new ArrayList<Player>(this.team.getPlayers()));
		if(this.playSound) {
			this.game.broadcastSound(Sound.SHEEP_IDLE, 3.0F, 1.0F, this.team.getPlayers());
		}
		this.game.removeSpecialItem(this);
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
