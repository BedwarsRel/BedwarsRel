package io.github.yannici.bedwars.Shop.Specials;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.TNTPrimed;

public interface ITNTSheep {

	public Location getLocation();

	public void setTNT(TNTPrimed tnt);

	public TNTPrimed getTNT();

	public void setPassenger(TNTPrimed tnt);

	public void remove();

	public void setTNTSource(Entity source);

}
