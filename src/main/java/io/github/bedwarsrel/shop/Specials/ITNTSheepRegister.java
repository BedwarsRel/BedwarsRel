package io.github.bedwarsrel.shop.Specials;

import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public interface ITNTSheepRegister {

  public void registerEntities(int entityId);

  public ITNTSheep spawnCreature(TNTSheep specialItem, Location location, Player owner,
      Player target, DyeColor color);

}
