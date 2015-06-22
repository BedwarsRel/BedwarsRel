package io.github.yannici.bedwars.Shop.Specials;

import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public interface ITNTSheepRegister {
	
	public void registerEntities();
	public ITNTSheep spawnSheep(Location location, Player owner, Player target, DyeColor color);

}
