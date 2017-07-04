package io.github.bedwarsrel.game;

import io.github.bedwarsrel.BedwarsRel;
import io.github.bedwarsrel.events.BedwarsResourceSpawnEvent;
import io.github.bedwarsrel.utils.Utils;
import io.github.bedwarsrel.villager.ItemStackParser;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Location;
import org.bukkit.block.BlockState;
import org.bukkit.block.Chest;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.SerializableAs;
import org.bukkit.entity.Item;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

@Getter
@Setter
@SerializableAs("RessourceSpawner")
public class ResourceSpawner implements Runnable, ConfigurationSerializable {

  private Game game = null;
  private int interval = 1000;
  private List<ItemStack> resources = new ArrayList<>();
  private Location location = null;
  private double spread = 1.0;
  private String name = null;

  public ResourceSpawner(Map<String, Object> deserialize) {
    this.location = Utils.locationDeserialize(deserialize.get("location"));

    if (deserialize.containsKey("name")) {
      String name = deserialize.get("name").toString();

      if (BedwarsRel.getInstance().getConfig().contains("resource." + name)) {
        List<Object> resourceList = (List<Object>) BedwarsRel.getInstance().getConfig()
            .getList("resource." + name + ".item");
        for (Object resource : resourceList) {
          ItemStack itemStack = ItemStack.deserialize((Map<String, Object>) resource);
          if (itemStack != null) {
            this.resources.add(itemStack);
          }
        }
        this.interval =
            BedwarsRel.getInstance().getIntConfig("resource." + name + ".spawn-interval", 1000);
        this.spread =
            BedwarsRel.getInstance().getConfig().getDouble("resource." + name + ".spread", 1.0);
        this.name = name;
      } else {
        List<Object> resourceList = (List<Object>) BedwarsRel.getInstance().getConfig()
            .getList("resource." + name + ".item");
        for (Object resource : resourceList) {
          ItemStack itemStack = ItemStack.deserialize((Map<String, Object>) resource);
          if (itemStack != null) {
            this.resources.add(itemStack);
          }
        }
        this.interval = Integer.parseInt(deserialize.get("interval").toString());
        if (deserialize.containsKey("spread")) {
          this.spread = Double.parseDouble(deserialize.get("spread").toString());
        }
      }
    } else {
      List<Object> resourceList = (List<Object>) BedwarsRel.getInstance().getConfig()
          .getList("resource." + name + ".item");
      for (Object resource : resourceList) {
        ItemStack itemStack = ItemStack.deserialize((Map<String, Object>) resource);
        if (itemStack != null) {
          this.resources.add(itemStack);
        }
      }
      this.interval = Integer.parseInt(deserialize.get("interval").toString());
      if (deserialize.containsKey("spread")) {
        this.spread = Double.parseDouble(deserialize.get("spread").toString());
      }
    }
  }

  public ResourceSpawner(Game game, String name, Location location) {
    this.game = game;
    this.name = name;
    this.interval =
        BedwarsRel.getInstance().getIntConfig("resource." + name + ".spawn-interval", 1000);
    this.location = location;
    List<Object> resourceList = (List<Object>) BedwarsRel.getInstance().getConfig()
        .getList("resource." + name + ".item");
    for (Object resource : resourceList) {
      ItemStack itemStack = ItemStack.deserialize((Map<String, Object>) resource);
      if (itemStack != null) {
        this.resources.add(itemStack);
      }
    }
    this.spread =
        BedwarsRel.getInstance().getConfig().getDouble("resource." + name + ".spread", 1.0);
  }

  public static ItemStack createSpawnerStackByConfig(Object section) {
    ItemStackParser parser = new ItemStackParser(section);
    return parser.parse();
  }

  public boolean canContainItem(Inventory inv, ItemStack item) {
    int space = 0;
    for (ItemStack stack : inv.getContents()) {
      if (stack == null) {
        space += item.getMaxStackSize();
      } else if (stack.getType() == item.getType()
          && stack.getDurability() == item.getDurability()) {
        space += item.getMaxStackSize() - stack.getAmount();
      }
    }
    return space >= item.getAmount();
  }

  public void dropItem(Location dropLocation, ItemStack itemStack) {
    Item item = this.game.getRegion().getWorld().dropItemNaturally(dropLocation, itemStack);
    item.setPickupDelay(0);
    if (this.spread != 1.0) {
      item.setVelocity(item.getVelocity().multiply(this.spread));
    }
  }

  @Override
  public void run() {
    Location dropLocation = this.location.clone();
    for (ItemStack itemStack : this.resources) {
      ItemStack item = itemStack.clone();

      BedwarsResourceSpawnEvent resourceSpawnEvent =
          new BedwarsResourceSpawnEvent(this.game, this.location, item);
      BedwarsRel.getInstance().getServer().getPluginManager().callEvent(resourceSpawnEvent);

      if (resourceSpawnEvent.isCancelled()) {
        return;
      }

      item = resourceSpawnEvent.getResource();

      if (BedwarsRel.getInstance().getBooleanConfig("spawn-resources-in-chest", true)) {
        BlockState blockState = dropLocation.getBlock().getState();
        if (blockState instanceof Chest) {
          Chest chest = (Chest) blockState;
          if (canContainItem(chest.getInventory(), item)) {
            chest.getInventory().addItem(item);
            continue;
          } else {
            dropLocation.setY(dropLocation.getY() + 1);
          }
        }
      }
      dropItem(dropLocation, item);
    }
  }

  @Override
  public Map<String, Object> serialize() {
    HashMap<String, Object> rs = new HashMap<>();

    rs.put("location", Utils.locationSerialize(this.location));
    rs.put("name", this.name);
    return rs;
  }

}
