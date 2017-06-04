package io.github.bedwarsrel;

import com.gmail.filoghost.holographicdisplays.api.Hologram;
import com.gmail.filoghost.holographicdisplays.api.HologramsAPI;
import com.gmail.filoghost.holographicdisplays.api.handler.TouchHandler;
import com.gmail.filoghost.holographicdisplays.api.line.TextLine;
import io.github.bedwarsrel.statistics.PlayerStatistic;
import io.github.bedwarsrel.utils.ChatWriter;
import io.github.bedwarsrel.utils.Utils;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import lombok.Getter;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

public class HolographicDisplaysInteraction implements IHologramInteraction {

  @Getter
  private ArrayList<Location> hologramLocations = null;
  private Map<Player, List<Hologram>> holograms = null;

  public void addHologramLocation(Location eyeLocation) {
    this.hologramLocations.add(eyeLocation);
    this.updateHologramDatabase();
  }

  private Hologram createPlayerStatisticHologram(Player player, Location holoLocation) {
    final Hologram holo = HologramsAPI.createHologram(BedwarsRel.getInstance(), holoLocation);
    holo.getVisibilityManager().setVisibleByDefault(false);
    holo.getVisibilityManager().showTo(player);

    this.updatePlayerStatisticHologram(player, holo);
    return holo;
  }

  private Hologram getHologramByLocation(List<Hologram> holograms, Location holoLocation) {
    for (Hologram holo : holograms) {
      if (holo.getLocation().getX() == holoLocation.getX()
          && holo.getLocation().getY() == holoLocation.getY()
          && holo.getLocation().getZ() == holoLocation.getZ()) {
        return holo;
      }
    }

    return null;
  }

  private Location getHologramLocationByLocation(Location holoLocation) {
    for (Location loc : this.hologramLocations) {
      if (loc.getX() == holoLocation.getX() && loc.getY() == holoLocation.getY()
          && loc.getZ() == holoLocation.getZ()) {
        return loc;
      }
    }

    return null;
  }

  public List<Hologram> getHolograms(Player player) {
    return this.holograms.get(player);
  }

  public Map<Player, List<Hologram>> getHolograms() {
    return this.holograms;
  }

  @Override
  public String getType() {
    return "HolographicDisplays";
  }

  @SuppressWarnings("unchecked")
  public void loadHolograms() {
    if (!BedwarsRel.getInstance().isHologramsEnabled()) {
      return;
    }

    if (this.holograms != null && this.hologramLocations != null) {
      // first unload all holograms
      this.unloadHolograms();
    }

    this.holograms = new HashMap<Player, List<Hologram>>();
    this.hologramLocations = new ArrayList<Location>();

    File file = new File(BedwarsRel.getInstance().getDataFolder(), "holodb.yml");
    if (file.exists()) {
      YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
      List<Object> locations = (List<Object>) config.get("locations");
      for (Object location : locations) {
        Location loc = Utils.locationDeserialize(location);
        if (loc == null) {
          continue;
        }

        this.hologramLocations.add(loc);
      }
    }

    if (this.hologramLocations.size() == 0) {
      return;
    }

    this.updateHolograms();
  }

  public void onHologramTouch(final Player player, final Hologram holo) {
    if (!player.hasMetadata("bw-remove-holo")
        || (!player.isOp() && !player.hasPermission("bw.setup"))) {
      return;
    }

    player.removeMetadata("bw-remove-holo", BedwarsRel.getInstance());
    BedwarsRel.getInstance().getServer().getScheduler().runTask(BedwarsRel.getInstance(), new Runnable() {

      @Override
      public void run() {
        // remove all player holograms on this location
        for (Entry<Player, List<Hologram>> entry : HolographicDisplaysInteraction.this
            .getHolograms().entrySet()) {
          Iterator<Hologram> iterator = entry.getValue().iterator();
          while (iterator.hasNext()) {
            Hologram hologram = iterator.next();
            if (hologram.getX() == holo.getX() && hologram.getY() == holo.getY()
                && hologram.getZ() == holo.getZ()) {
              hologram.delete();
              iterator.remove();
            }
          }
        }

        Location holoLocation =
            HolographicDisplaysInteraction.this.getHologramLocationByLocation(holo.getLocation());
        if (holoLocation != null) {
          HolographicDisplaysInteraction.this.hologramLocations.remove(holoLocation);
          HolographicDisplaysInteraction.this.updateHologramDatabase();
        }
        player.sendMessage(
            ChatWriter.pluginMessage(ChatColor.GREEN + BedwarsRel._l("success.holoremoved")));
      }

    });
  }

  @Override
  public void onHologramTouch(Player player, Location holoLocation) {
    // NOT NEEDED HERE
  }

  public void removeHologramPlayer(Player player) {
    this.holograms.remove(player);
  }

  public void unloadAllHolograms(Player player) {
    if (!this.holograms.containsKey(player)) {
      return;
    }

    for (Hologram holo : this.holograms.get(player)) {
      holo.delete();
    }

    this.holograms.remove(player);
  }

  public void unloadHolograms() {
    if (BedwarsRel.getInstance().isHologramsEnabled()) {
      Iterator<Hologram> iterator = HologramsAPI.getHolograms(BedwarsRel.getInstance()).iterator();
      while (iterator.hasNext()) {
        iterator.next().delete();
      }
    }
  }

  private void updateHologramDatabase() {
    try {
      // update hologram-database file
      File file = new File(BedwarsRel.getInstance().getDataFolder(), "holodb.yml");
      YamlConfiguration config = new YamlConfiguration();
      List<Map<String, Object>> serializedLocations = new ArrayList<Map<String, Object>>();

      for (Location holoLocation : this.hologramLocations) {
        serializedLocations.add(Utils.locationSerialize(holoLocation));
      }

      if (!file.exists()) {
        file.createNewFile();
      }

      config.set("locations", serializedLocations);
      config.save(file);
    } catch (Exception ex) {
      BedwarsRel.getInstance().getBugsnag().notify(ex);
      ex.printStackTrace();
    }
  }

  public void updateHolograms() {
    for (final Player player : BedwarsRel.getInstance().getServer().getOnlinePlayers()) {
      BedwarsRel.getInstance().getServer().getScheduler().runTask(BedwarsRel.getInstance(), new Runnable() {

        @Override
        public void run() {
          for (Location holoLocation : HolographicDisplaysInteraction.this.hologramLocations) {
            HolographicDisplaysInteraction.this.updatePlayerHologram(player, holoLocation);
          }
        }
      });
    }
  }

  public void updateHolograms(final Player player) {
    BedwarsRel.getInstance().getServer().getScheduler().runTask(BedwarsRel.getInstance(), new Runnable() {

      @Override
      public void run() {
        for (Location holoLocation : HolographicDisplaysInteraction.this.hologramLocations) {
          HolographicDisplaysInteraction.this.updatePlayerHologram(player, holoLocation);
        }
      }
    });
  }

  public void updateHolograms(final Player player, long delay) {
    BedwarsRel.getInstance().getServer().getScheduler().runTaskLater(BedwarsRel.getInstance(), new Runnable() {

      @Override
      public void run() {
        HolographicDisplaysInteraction.this.updateHolograms(player);
      }
    }, delay);
  }

  private void updatePlayerHologram(Player player, Location holoLocation) {
    List<Hologram> holograms = null;
    if (!this.holograms.containsKey(player)) {
      this.holograms.put(player, new ArrayList<Hologram>());
    }

    holograms = this.holograms.get(player);
    Hologram holo = this.getHologramByLocation(holograms, holoLocation);
    if (holo == null && player.getWorld() == holoLocation.getWorld()) {
      holograms.add(this.createPlayerStatisticHologram(player, holoLocation));
    } else if (holo != null) {
      if (holo.getLocation().getWorld() == player.getWorld()) {
        this.updatePlayerStatisticHologram(player, holo);
      } else {
        holograms.remove(holo);
        holo.delete();
      }
    }
  }

  private void updatePlayerStatisticHologram(Player player, final Hologram holo) {
    PlayerStatistic statistic = BedwarsRel.getInstance().getPlayerStatisticManager().getStatistic(player);
    holo.clearLines();

    String nameColor = ChatColor.GRAY.toString();
    String valueColor = ChatColor.YELLOW.toString();

    try {
      nameColor = ChatColor.translateAlternateColorCodes('&',
          BedwarsRel.getInstance().getStringConfig("holographic-stats.name-color", "&7"));

      valueColor = ChatColor.translateAlternateColorCodes('&',
          BedwarsRel.getInstance().getStringConfig("holographic-stats.value-color", "&e"));
    } catch (Exception ex) {
      BedwarsRel.getInstance().getBugsnag().notify(ex);
      // nothing to do
    }

    List<String> lines = BedwarsRel.getInstance().getPlayerStatisticManager().createStatisticLines(statistic,
        BedwarsRel.getInstance().getBooleanConfig("holographic-stats.show-prefix", false), nameColor,
        valueColor);

    String headline =
        BedwarsRel
            .getInstance().getStringConfig("holographic-stats.head-line", "Your &eBEDWARS&f stats");
    if (!headline.trim().isEmpty()) {
      lines.add(0, ChatColor.translateAlternateColorCodes('&', headline));
    }

    for (String line : lines) {
      TextLine textLine = holo.appendTextLine(line);
      textLine.setTouchHandler(new TouchHandler() {

        @Override
        public void onTouch(Player player) {
          HolographicDisplaysInteraction.this.onHologramTouch(player, holo);
        }
      });
    }
  }

}
