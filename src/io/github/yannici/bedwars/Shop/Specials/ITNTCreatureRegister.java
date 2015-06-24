package io.github.yannici.bedwars.Shop.Specials;

import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public interface ITNTCreatureRegister {
	
	public void registerEntities(int entityId);
	public ITNTCreature spawnCreature(Location location, Player owner, Player target, DyeColor color);

}
