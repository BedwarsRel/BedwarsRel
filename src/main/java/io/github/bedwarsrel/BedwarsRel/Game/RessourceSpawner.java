package io.github.bedwarsrel.BedwarsRel.Game;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.Chest;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.SerializableAs;
import org.bukkit.entity.Item;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import io.github.bedwarsrel.BedwarsRel.Main;
import io.github.bedwarsrel.BedwarsRel.Utils.Utils;
import io.github.bedwarsrel.BedwarsRel.Villager.ItemStackParser;

@SerializableAs("RessourceSpawner")
public class RessourceSpawner implements Runnable, ConfigurationSerializable {

  private Game game = null;
  private Location location = null;
  private int interval = 1000;
  private ItemStack itemstack = null;
  private String name = null;
  private double spread = 1.0;

  public RessourceSpawner(Map<String, Object> deserialize) {
    this.location = Utils.locationDeserialize(deserialize.get("location"));

    if (deserialize.containsKey("name")) {
      this.name = deserialize.get("name").toString();

      if (!Main.getInstance().getConfig().contains("ressource." + this.name)) {
        this.itemstack = (ItemStack) deserialize.get("itemstack");
        this.interval = Integer.parseInt(deserialize.get("interval").toString());
        if (deserialize.containsKey("spread")) {
          this.spread = Double.parseDouble(deserialize.get("spread").toString());
        }
      } else {
        this.itemstack = RessourceSpawner.createSpawnerStackByConfig(
            Main.getInstance().getConfig().get("ressource." + this.name));
        this.interval =
            Main.getInstance().getIntConfig("ressource." + this.name + ".spawn-interval", 1000);
        this.spread =
            Main.getInstance().getConfig().getDouble("ressource." + this.name + ".spread", 1.0);
      }
    } else {
      ItemStack stack = (ItemStack) deserialize.get("itemstack");
      this.name = this.getNameByMaterial(stack.getType());

      if (this.name == null) {
        this.itemstack = stack;
        this.interval = Integer.parseInt(deserialize.get("interval").toString());
        if (deserialize.containsKey("spread")) {
          this.spread = Double.parseDouble(deserialize.get("spread").toString());
        }
      } else {
        this.itemstack = RessourceSpawner.createSpawnerStackByConfig(
            Main.getInstance().getConfig().get("ressource." + this.name));
        this.interval =
            Main.getInstance().getIntConfig("ressource." + this.name + ".spawn-interval", 1000);
        this.spread =
            Main.getInstance().getConfig().getDouble("ressource." + this.name + ".spread", 1.0);
      }
    }
  }

  public RessourceSpawner(Game game, String name, Location location) {
    this.game = game;
    this.name = name;
    this.interval =
        Main.getInstance().getIntConfig("ressource." + this.name + ".spawn-interval", 1000);
    this.location = location;
    this.itemstack = RessourceSpawner
        .createSpawnerStackByConfig(Main.getInstance().getConfig().get("ressource." + this.name));
    this.spread =
        Main.getInstance().getConfig().getDouble("ressource." + this.name + ".spread", 1.0);
  }

  private String getNameByMaterial(Material material) {
    for (String key : Main.getInstance().getConfig().getConfigurationSection("ressource")
        .getKeys(true)) {
      ConfigurationSection keySection =
          Main.getInstance().getConfig().getConfigurationSection("ressource." + key);
      if (keySection == null) {
        continue;
      }

      if (!keySection.contains("item")) {
        continue;
      }

      Material mat = Utils.parseMaterial(keySection.getString("item"));
      if (mat.equals(material)) {
        return key;
      }
    }

    return null;
  }

  public int getInterval() {
    return this.interval;
  }

  public void setGame(Game game) {
    this.game = game;
  }

  @Override
  public void run() {
    Location dropLocation = this.location;
    ItemStack item = this.itemstack.clone();

    if (Main.getInstance().getBooleanConfig("spawnRessourcesInChest", true)) {
      BlockState blockState = dropLocation.getBlock().getState();
      if (blockState instanceof Chest) {
        Chest chest = (Chest) blockState;
        if (canContainItem(chest.getInventory(), item)) {
          chest.getInventory().addItem(item);
        } else {
          dropItem(chest.getBlock().getRelative(BlockFace.UP).getLocation());
        }
        return;
      }
    }
    dropItem(dropLocation);
  }

  public boolean canContainItem(Inventory inv, ItemStack item) {
    int space = 0;
    for (ItemStack stack : inv.getContents()) {
      if (stack == null) {
        space += this.itemstack.getMaxStackSize();
      } else if (stack.getType() == this.itemstack.getType()
          && stack.getDurability() == this.itemstack.getDurability()) {
        space += this.itemstack.getMaxStackSize() - stack.getAmount();
      }
    }
    return space >= this.itemstack.getAmount();
  }

  public void dropItem(Location dropLocation) {
    Item item = this.game.getRegion().getWorld().dropItemNaturally(dropLocation, this.itemstack);
    item.setPickupDelay(0);

    if (this.spread != 1.0) {
      item.setVelocity(item.getVelocity().multiply(this.spread));
    }
  }

  @Override
  public Map<String, Object> serialize() {
    HashMap<String, Object> rs = new HashMap<>();

    rs.put("location", Utils.locationSerialize(this.location));
    rs.put("name", this.name);
    return rs;
  }

  public ItemStack getItemStack() {
    return this.itemstack;
  }

  public Location getLocation() {
    return this.location;
  }

  public static ItemStack createSpawnerStackByConfig(Object section) {
    ItemStackParser parser = new ItemStackParser(section);
    return parser.parse();
  }

}
