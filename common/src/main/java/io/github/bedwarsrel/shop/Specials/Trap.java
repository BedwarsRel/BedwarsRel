package io.github.bedwarsrel.shop.Specials;

import io.github.bedwarsrel.BedwarsRel;
import io.github.bedwarsrel.game.Game;
import io.github.bedwarsrel.game.Team;
import io.github.bedwarsrel.utils.ChatWriter;
import io.github.bedwarsrel.utils.SoundMachine;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.scheduler.BukkitRunnable;

public class Trap extends SpecialItem {

  private List<PotionEffect> effects = null;
  private Game game = null;
  private Location location = null;
  private int maxDuration = 5;
  private boolean playSound = true;
  private Team team = null;

  public Trap() {
    this.effects = new ArrayList<PotionEffect>();
  }

  public void activate(final Player player) {
    try {
      ConfigurationSection section =
          BedwarsRel.getInstance().getConfig().getConfigurationSection("specials.trap");

      if (section.contains("play-sound")) {
        this.playSound = section.getBoolean("play-sound");
      }

      for (Object effect : section.getList("effects")) {
        effects.add((PotionEffect) effect);

        if (((PotionEffect) effect).getDuration() / 20 > this.maxDuration) {
          this.maxDuration = ((PotionEffect) effect).getDuration() / 20;
        }
      }

      this.game.addRunningTask(new BukkitRunnable() {

        private int counter = 0;

        @Override
        public void run() {
          if (this.counter >= Trap.this.maxDuration) {
            Trap.this.game.removeRunningTask(this);
            this.cancel();
            return;
          }
          this.counter++;
        }
      }.runTaskTimer(BedwarsRel.getInstance(), 0L, 20L));

      if (effects.size() > 0) {
        for (PotionEffect effect : effects) {
          if (player.hasPotionEffect(effect.getType())) {
            player.removePotionEffect(effect.getType());
          }

          player.addPotionEffect(effect);
        }
      }

      player.playSound(player.getLocation(), SoundMachine.get("FUSE", "ENTITY_TNT_PRIMED"),
          Float.valueOf("1.0"), Float.valueOf("1.0"));

      for (Player aPlayer : this.team.getPlayers()) {
        if (aPlayer.isOnline()) {
          aPlayer.sendMessage(
              ChatWriter.pluginMessage(BedwarsRel._l(aPlayer, "ingame.specials.trap.trapped")));
        }
      }
      if (this.playSound) {
        this.game.broadcastSound(SoundMachine.get("SHEEP_IDLE", "ENTITY_SHEEP_AMBIENT"),
            Float.valueOf("1.0"), Float.valueOf("1.0"), this.team.getPlayers());
      }

      this.game.getRegion().removePlacedUnbreakableBlock(this.location.getBlock());
      this.location.getBlock().setType(Material.AIR);
      this.game.removeSpecialItem(this);
    } catch (Exception ex) {
      BedwarsRel.getInstance().getBugsnag().notify(ex);
      ex.printStackTrace();
    }
  }

  public void create(Game game, Team team, Location location) {
    this.game = game;
    this.team = team;
    this.location = location;

    this.game.addSpecialItem(this);
  }

  @Override
  public Material getActivatedMaterial() {
    return null;
  }

  public Game getGame() {
    return this.game;
  }

  @Override
  public Material getItemMaterial() {
    return Material.TRIPWIRE;
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
