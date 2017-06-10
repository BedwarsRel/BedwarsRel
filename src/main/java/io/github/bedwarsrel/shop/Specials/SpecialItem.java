package io.github.bedwarsrel.shop.Specials;

import io.github.bedwarsrel.BedwarsRel;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.Material;

public abstract class SpecialItem {

  private static List<Class<? extends SpecialItem>> availableSpecials =
      new ArrayList<Class<? extends SpecialItem>>();

  public static List<Class<? extends SpecialItem>> getSpecials() {
    return SpecialItem.availableSpecials;
  }

  public static void loadSpecials() {
    SpecialItem.availableSpecials.add(RescuePlatform.class);
    SpecialItem.availableSpecials.add(Trap.class);
    SpecialItem.availableSpecials.add(MagnetShoe.class);
    SpecialItem.availableSpecials.add(ProtectionWall.class);
    SpecialItem.availableSpecials.add(WarpPowder.class);
    SpecialItem.availableSpecials.add(TNTSheep.class);
    SpecialItem.availableSpecials.add(Tracker.class);
    SpecialItem.availableSpecials.add(ArrowBlocker.class);
    BedwarsRel.getInstance().getServer().getPluginManager()
        .registerEvents(new RescuePlatformListener(),
            BedwarsRel.getInstance());
    BedwarsRel.getInstance().getServer().getPluginManager().registerEvents(new TrapListener(),
        BedwarsRel.getInstance());
    BedwarsRel.getInstance().getServer().getPluginManager().registerEvents(new MagnetShoeListener(),
        BedwarsRel.getInstance());
    BedwarsRel.getInstance().getServer().getPluginManager()
        .registerEvents(new ProtectionWallListener(),
            BedwarsRel.getInstance());
    BedwarsRel.getInstance().getServer().getPluginManager().registerEvents(new WarpPowderListener(),
        BedwarsRel.getInstance());
    BedwarsRel.getInstance().getServer().getPluginManager().registerEvents(new TNTSheepListener(),
        BedwarsRel.getInstance());
    BedwarsRel.getInstance().getServer().getPluginManager().registerEvents(new TrackerListener(),
        BedwarsRel.getInstance());
    BedwarsRel.getInstance().getServer().getPluginManager()
        .registerEvents(new ArrowBlockerListener(),
            BedwarsRel.getInstance());
  }

  public abstract Material getActivatedMaterial();

  public abstract Material getItemMaterial();

}
