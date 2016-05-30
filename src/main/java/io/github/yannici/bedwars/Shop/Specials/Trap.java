package io.github.yannici.bedwars.Shop.Specials;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.scheduler.BukkitRunnable;

import io.github.yannici.bedwars.Main;
import io.github.yannici.bedwars.SoundMachine;
import io.github.yannici.bedwars.Game.Game;
import io.github.yannici.bedwars.Game.Team;

public class Trap extends SpecialItem {

  List<PotionEffect> effects = new ArrayList<PotionEffect>();
  FileConfiguration cfg = null;
  private Game game = null;
  private Team team = null;
  private int duration = 10;
  private boolean playSound = true;
  private Location location = null;

  @SuppressWarnings("unchecked")
  public Trap() {
    ConfigurationSection section = Main.getInstance().getConfig().getConfigurationSection("spacials").getConfigurationSection("trap");
    
    for (Object effect : section.getList("effects")) {
      if (effect instanceof Boolean) {
        if (effect.toString().equalsIgnoreCase("play-sound")){
          this.playSound = section.getBoolean("play-sound");
        }

        continue;
      }

      Map<String, Object> map = (Map<String, Object>) effect;
      PotionEffect pe = new PotionEffect(map);
      effects.add(pe);
    }
  }

  @Override
  public Material getItemMaterial() {
    return Material.TRIPWIRE;
  }

  @Override
  public Material getActivatedMaterial() {
    return null;
  }

  public void activate(final Player player) {
    try {
      this.game.addRunningTask(new BukkitRunnable() {

        private int counter = 0;

        @Override
        public void run() {
          if (this.counter >= Trap.this.duration) {
            Trap.this.game.removeRunningTask(this);
            this.cancel();
            return;
          }

          player.playSound(player.getLocation(), SoundMachine.get("FUSE", "ENTITY_TNT_PRIMED"),
              2.0F, 1.0F);
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

      this.game.broadcast(Main._l("ingame.specials.trap.trapped"),
          new ArrayList<Player>(this.team.getPlayers()));
      if (this.playSound) {
        this.game.broadcastSound(SoundMachine.get("SHEEP_IDLE", "ENTITY_SHEEP_AMBIENT"), 4.0F,
            1.0F, this.team.getPlayers());
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
