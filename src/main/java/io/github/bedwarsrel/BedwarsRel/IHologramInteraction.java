package io.github.bedwarsrel.BedwarsRel;

import java.util.ArrayList;

import org.bukkit.Location;
import org.bukkit.entity.Player;

public interface IHologramInteraction {

  public String getType();

  public void loadHolograms();

  public void unloadHolograms();

  public void updateHolograms(Player p);

  public void updateHolograms(Player player, long l);

  public void unloadAllHolograms(Player player);

  public void updateHolograms();

  public void addHologramLocation(Location eyeLocation);

  public ArrayList<Location> getHologramLocations();

  public void onHologramTouch(Player player, Location holoLocation);

}
