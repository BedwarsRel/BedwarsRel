package io.github.bedwarsrel.BedwarsRel.Shop.Specials;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Material;

import io.github.bedwarsrel.BedwarsRel.Main;

public abstract class SpecialItem {

  private static List<Class<? extends SpecialItem>> availableSpecials =
      new ArrayList<Class<? extends SpecialItem>>();

  public abstract Material getItemMaterial();

  public abstract Material getActivatedMaterial();

  public static void loadSpecials() {
    SpecialItem.availableSpecials.add(RescuePlatform.class);
    SpecialItem.availableSpecials.add(Trap.class);
    SpecialItem.availableSpecials.add(MagnetShoe.class);
    SpecialItem.availableSpecials.add(ProtectionWall.class);
    SpecialItem.availableSpecials.add(WarpPowder.class);
    SpecialItem.availableSpecials.add(TNTSheep.class);
    SpecialItem.availableSpecials.add(Tracker.class);
    SpecialItem.availableSpecials.add(ArrowBlocker.class);
    SpecialItem.availableSpecials.add(AutoBridge.class);
    Main.getInstance().getServer().getPluginManager().registerEvents(new RescuePlatformListener(),
        Main.getInstance());
    Main.getInstance().getServer().getPluginManager().registerEvents(new TrapListener(),
        Main.getInstance());
    Main.getInstance().getServer().getPluginManager().registerEvents(new MagnetShoeListener(),
        Main.getInstance());
    Main.getInstance().getServer().getPluginManager().registerEvents(new ProtectionWallListener(),
        Main.getInstance());
    Main.getInstance().getServer().getPluginManager().registerEvents(new WarpPowderListener(),
        Main.getInstance());
    Main.getInstance().getServer().getPluginManager().registerEvents(new TNTSheepListener(),
        Main.getInstance());
    Main.getInstance().getServer().getPluginManager().registerEvents(new TrackerListener(),
        Main.getInstance());
    Main.getInstance().getServer().getPluginManager().registerEvents(new ArrowBlockerListener(),
        Main.getInstance());
    Main.getInstance().getServer().getPluginManager().registerEvents(new AutoBridgeListener(),
        Main.getInstance());
  }

  public static List<Class<? extends SpecialItem>> getSpecials() {
    return SpecialItem.availableSpecials;
  }

}
