package io.github.bedwarsrel;

import de.inventivegames.hologram.Hologram;
import de.inventivegames.hologram.HologramAPI;
import de.inventivegames.hologram.view.ViewHandler;
import io.github.bedwarsrel.statistics.PlayerStatistic;
import io.github.bedwarsrel.utils.ChatWriter;
import io.github.bedwarsrel.utils.Utils;
import java.io.File;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import lombok.Getter;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

public class HologramAPIInteraction implements IHologramInteraction {

  @Getter
  private ArrayList<Location> hologramLocations = null;

  @Getter
  private HashMap<Location, List<Hologram>> hologramSets = null;

  public void addHologramLocation(Location eyeLocation) {
    this.hologramLocations.add(eyeLocation);
    this.updateHologramDatabase();
    this.createStatisticHologram(eyeLocation);
  }

  private void createStatisticHologram(Location holoLocation) {

    List<String> lines = new ArrayList<String>();
    List<Hologram> holograms = new ArrayList<Hologram>();
    final PlayerStatistic statistic = new PlayerStatistic();

    lines.add(ChatColor.translateAlternateColorCodes('&', BedwarsRel.getInstance()
        .getStringConfig("holographic-stats.head-line", "Your &eBEDWARS&f stats")));

    for (Entry<String, Object> entry : statistic.serialize().entrySet()) {
      lines.add(ChatColor.GRAY + BedwarsRel._l("stats." + entry.getKey()) + ": " + ChatColor.YELLOW
          + "%%" + entry.getValue() + "%%");
    }

    int currentLine = 0;
    while (currentLine < lines.size()) {
      Hologram holo = HologramAPI.createHologram(
          new Location(holoLocation.getWorld(), holoLocation.getX(),
              holoLocation.getY() - (currentLine * 0.3), holoLocation.getZ()),
          lines.get(currentLine));
      holo.addViewHandler(new ViewHandler() {
        @Override
        public String onView(Hologram hologram, Player player, String line) {
          PlayerStatistic playerStatistic =
              BedwarsRel.getInstance().getPlayerStatisticManager().getStatistic(player);
          for (Entry<String, Object> entry : statistic.serialize().entrySet()) {
            String value = entry.getValue().toString();
            if (entry.getKey().equals("kd")) {
              value =
                  (BigDecimal.valueOf(Double.valueOf(value)).setScale(2, BigDecimal.ROUND_HALF_UP))
                      .toPlainString();
            }
            line = line.replace("%%" + entry.getKey() + "%%", value);
          }
          return line;
        }
      });
      /*
       * holo.setTouchable(true); holo.addTouchHandler(new TouchHandler() {
       *
       * @Override public void onTouch(Hologram hologram, Player player, TouchAction action) {
       * HologramAPIInteraction.this.onHologramTouch(player, hologram); }
       *
       * });
       */
      holo.spawn();
      holograms.add(holo);
      currentLine++;
    }
    this.hologramSets.put(holoLocation, holograms);
  }

  @Override
  public String getType() {
    return "HologramAPI";
  }

  @SuppressWarnings("unchecked")
  public void loadHolograms() {
    if (!BedwarsRel.getInstance().isHologramsEnabled()) {
      return;
    }

    if (this.hologramLocations != null) {
      this.unloadHolograms();
    }

    this.hologramLocations = new ArrayList<Location>();
    this.hologramSets = new HashMap<Location, List<Hologram>>();

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

    for (Location holoLocation : this.hologramLocations) {
      this.createStatisticHologram(holoLocation);
    }
  }

  @Override
  public void onHologramTouch(Player player, Location holoLocation) {
    onHologramTouch(player, this.hologramSets.get(holoLocation).get(0));
  }

  public void onHologramTouch(final Player player, final Hologram touchedHologram) {
    if (!player.hasMetadata("bw-remove-holo")
        || (!player.isOp() && !player.hasPermission("bw.setup"))) {
      return;
    }

    player.removeMetadata("bw-remove-holo", BedwarsRel.getInstance());
    BedwarsRel.getInstance().getServer().getScheduler().runTask(BedwarsRel.getInstance(), new Runnable() {

      @Override
      public void run() {
        // remove all player holograms on this location
        Location touchedSetLocation = null;
        for (Entry<Location, List<Hologram>> hologramSet : HologramAPIInteraction.this
            .getHologramSets().entrySet()) {
          for (Hologram hologram : hologramSet.getValue()) {
            if (hologram.equals(touchedHologram)) {
              touchedSetLocation = hologramSet.getKey();
              break;
            }
          }

          if (touchedSetLocation != null) {
            break;
          }
        }
        if (touchedSetLocation != null) {
          List<Hologram> touchedSetHolograms =
              HologramAPIInteraction.this.getHologramSets().get(touchedSetLocation);
          for (Hologram hologram : touchedSetHolograms) {
            hologram.despawn();
          }
          HologramAPIInteraction.this.getHologramSets().remove(touchedSetLocation);

          HologramAPIInteraction.this.hologramLocations.remove(touchedSetLocation);
          HologramAPIInteraction.this.updateHologramDatabase();
        }
        player.sendMessage(
            ChatWriter.pluginMessage(ChatColor.GREEN + BedwarsRel._l("success.holoremoved")));
      }

    });

  }

  @Override
  public void unloadAllHolograms(Player player) {
    // NOT NEEDED HERE
  }

  public void unloadHolograms() {
    if (BedwarsRel.getInstance().isHologramsEnabled()) {
      for (Hologram hologram : HologramAPI.getHolograms()) {
        if (hologram.isSpawned()) {
          hologram.despawn();
        }
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

  @Override
  public void updateHolograms(Player p) {
    // NOT NEEDED HERE
  }

  @Override
  public void updateHolograms(Player player, long l) {
    // NOT NEEDED HERE
  }

  @Override
  public void updateHolograms() {
    // NOT NEEDED HERE
  }

}
