package io.github.yannici.bedwars.Shop.Specials;

import org.bukkit.Location;
import org.bukkit.entity.TNTPrimed;

public interface ITNTCreature {
	
	public Location getLocation();
	public void setTNT(TNTPrimed tnt);
	public TNTPrimed getTNT();

}
