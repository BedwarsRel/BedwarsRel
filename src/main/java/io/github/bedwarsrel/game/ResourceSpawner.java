package io.github.bedwarsrel.game;

import io.github.bedwarsrel.BedwarsRel;
import io.github.bedwarsrel.events.BedwarsResourceSpawnEvent;
import io.github.bedwarsrel.utils.Utils;
import io.github.bedwarsrel.villager.ItemStackParser;
import java.util.HashMap;
import java.util.Map;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockState;
import org.bukkit.block.Chest;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.SerializableAs;
import org.bukkit.entity.Item;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

@SerializableAs("RessourceSpawner")
public class ResourceSpawner implements Runnable, ConfigurationSerializable {

  private Game game = null;
  private int interval = 1000;
  private ItemStack itemstack = null;
  private Location location = null;
  private String name = null;
  private double spread = 1.0;

  public ResourceSpawner(Map<String, Object> deserialize) {
    this.location = Utils.locationDeserialize(deserialize.get("location"));

    if (deserialize.containsKey("name")) {
      this.name = deserialize.get("name").toString();

      if (!BedwarsRel.getInstance().getConfig().contains("ressource." + this.name)) {
        this.itemstack = (ItemStack) deserialize.get("itemstack");
        this.interval = Integer.parseInt(deserialize.get("interval").toString());
        if (deserialize.containsKey("spread")) {
          this.spread = Double.parseDouble(deserialize.get("spread").toString());
        }
      } else {
        this.itemstack = ResourceSpawner.createSpawnerStackByConfig(
            BedwarsRel.getInstance().getConfig().get("ressource." + this.name));
        this.interval =
            BedwarsRel.getInstance().getIntConfig("ressource." + this.name + ".spawn-interval", 1000);
        this.spread =
            BedwarsRel.getInstance().getConfig().getDouble("ressource." + this.name + ".spread", 1.0);
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
        this.itemstack = ResourceSpawner.createSpawnerStackByConfig(
            BedwarsRel.getInstance().getConfig().get("ressource." + this.name));
        this.interval =
            BedwarsRel.getInstance().getIntConfig("ressource." + this.name + ".spawn-interval", 1000);
        this.spread =
            BedwarsRel.getInstance().getConfig().getDouble("ressource." + this.name + ".spread", 1.0);
      }
    }
  }

  public ResourceSpawner(Game game, String name, Location location) {
    this.game = game;
    this.name = name;
    this.interval =
        BedwarsRel.getInstance().getIntConfig("ressource." + this.name + ".spawn-interval", 1000);
    this.location = location;
    this.itemstack = ResourceSpawner
        .createSpawnerStackByConfig(BedwarsRel.getInstance().getConfig().get("ressource." + this.name));
    this.spread =
        BedwarsRel.getInstance().getConfig().getDouble("ressource." + this.name + ".spread", 1.0);
  }

  public static ItemStack createSpawnerStackByConfig(Object section) {
    ItemStackParser parser = new ItemStackParser(section);
    return parser.parse();
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

  public int getInterval() {
    return this.interval;
  }

  public ItemStack getItemStack() {
    return this.itemstack;
  }

  public Location getLocation() {
    return this.location;
  }

  private String getNameByMaterial(Material material) {
    for (String key : BedwarsRel.getInstance().getConfig().getConfigurationSection("ressource")
        .getKeys(true)) {
      ConfigurationSection keySection =
          BedwarsRel.getInstance().getConfig().getConfigurationSection("ressource." + key);
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

  @Override
  public void run() {
    Location dropLocation = this.location.clone();
    ItemStack item = this.itemstack.clone();

    BedwarsResourceSpawnEvent resourceSpawnEvent =
        new BedwarsResourceSpawnEvent(this.game, this.location, item);
    BedwarsRel.getInstance().getServer().getPluginManager().callEvent(resourceSpawnEvent);

    if (resourceSpawnEvent.isCancelled()) {
      return;
    }

    item = resourceSpawnEvent.getResource();

    if (BedwarsRel.getInstance().getBooleanConfig("spawn-ressources-in-chest", true)) {
      BlockState blockState = dropLocation.getBlock().getState();
      if (blockState instanceof Chest) {
        Chest chest = (Chest) blockState;
        if (canContainItem(chest.getInventory(), item)) {
          chest.getInventory().addItem(item);
          return;
        } else {
          dropLocation.setY(dropLocation.getY() + 1);
        }
      }
    }
    dropItem(dropLocation);
  }

  @Override
  public Map<String, Object> serialize() {
    HashMap<String, Object> rs = new HashMap<>();

    rs.put("location", Utils.locationSerialize(this.location));
    rs.put("name", this.name);
    return rs;
  }

  public void setGame(Game game) {
    this.game = game;
  }

}
