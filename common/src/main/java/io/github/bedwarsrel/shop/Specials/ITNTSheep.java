package io.github.bedwarsrel.shop.Specials;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.TNTPrimed;

public interface ITNTSheep {

  public Location getLocation();

  public TNTPrimed getTNT();

  public void setTNT(TNTPrimed tnt);

  public void remove();

  public void setPassenger(TNTPrimed tnt);

  public void setTNTSource(Entity source);

}
