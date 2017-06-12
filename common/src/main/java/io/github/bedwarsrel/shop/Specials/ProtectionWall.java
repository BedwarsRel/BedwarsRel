package io.github.bedwarsrel.shop.Specials;

import com.google.common.collect.ImmutableMap;
import io.github.bedwarsrel.BedwarsRel;
import io.github.bedwarsrel.game.Game;
import io.github.bedwarsrel.utils.ChatWriter;
import io.github.bedwarsrel.utils.Utils;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

public class ProtectionWall extends SpecialItem {

  private Game game = null;
  private int livingTime = 0;
  private Player owner = null;
  private BukkitTask task = null;
  private List<Block> wallBlocks = null;

  public ProtectionWall() {
    super();
    this.wallBlocks = new ArrayList<Block>();
    this.owner = null;
    this.game = null;
  }

  @SuppressWarnings("deprecation")
  public void create(Player player, Game game) {
    this.owner = player;
    this.game = game;

    int breakTime = BedwarsRel.getInstance().getIntConfig("specials.protection-wall.break-time", 0);
    int waitTime = BedwarsRel.getInstance().getIntConfig("specials.protection-wall.wait-time", 20);
    int width = BedwarsRel.getInstance().getIntConfig("specials.protection-wall.width", 4);
    int height = BedwarsRel.getInstance().getIntConfig("specials.protection-wall.height", 4);
    int distance = BedwarsRel.getInstance().getIntConfig("specials.protection-wall.distance", 2);
    boolean canBreak =
        BedwarsRel.getInstance().getBooleanConfig("specials.protection-wall.can-break", true);
    Material blockMaterial =
        Utils.getMaterialByConfig("specials.protection-wall.block", Material.SANDSTONE);

    if (width % 2 == 0) {
      BedwarsRel.getInstance().getServer().getConsoleSender().sendMessage(ChatWriter.pluginMessage(
          "The width of a protection block has to be odd! " + width + " is not an odd number."));
      width = width + 1;
      if (width % 2 == 0) {
        return;
      }
    }

    if (player.getEyeLocation().getBlock().getType() != Material.AIR) {
      player.sendMessage(
          ChatWriter
              .pluginMessage(
                  BedwarsRel._l(player, "ingame.specials.protection-wall.not-usable-here")));
      return;
    }

    if (waitTime > 0) {
      ArrayList<ProtectionWall> livingWalls = this.getLivingWalls();
      if (!livingWalls.isEmpty()) {
        for (ProtectionWall livingWall : livingWalls) {
          int waitLeft = waitTime - livingWall.getLivingTime();
          if (waitLeft > 0) {
            player.sendMessage(
                ChatWriter.pluginMessage(
                    BedwarsRel._l(player, "ingame.specials.protection-wall.left",
                        ImmutableMap.of("time", String.valueOf(waitLeft)))));
            return;
          }
        }
      }
    }

    Location wallLocation = Utils.getDirectionLocation(player.getLocation(), distance);

    ItemStack usedStack = null;

    if (BedwarsRel.getInstance().getCurrentVersion().startsWith("v1_8")) {
      usedStack = player.getInventory().getItemInHand();
      usedStack.setAmount(usedStack.getAmount() - 1);
      player.getInventory().setItem(player.getInventory().getHeldItemSlot(), usedStack);
    } else {
      if (player.getInventory().getItemInOffHand().getType() == this.getItemMaterial()) {
        usedStack = player.getInventory().getItemInOffHand();
        usedStack.setAmount(usedStack.getAmount() - 1);
        player.getInventory().setItemInOffHand(usedStack);
      } else if (player.getInventory().getItemInMainHand().getType() == this.getItemMaterial()) {
        usedStack = player.getInventory().getItemInMainHand();
        usedStack.setAmount(usedStack.getAmount() - 1);
        player.getInventory().setItemInMainHand(usedStack);
      }
    }
    player.updateInventory();

    BlockFace face = Utils.getCardinalDirection(player.getLocation());
    int widthStart = (int) Math.floor(((double) width) / 2.0);

    for (int w = widthStart * (-1); w < width - widthStart; w++) {
      for (int h = 0; h < height; h++) {
        Location wallBlock = wallLocation.clone();

        switch (face) {
          case SOUTH:
          case NORTH:
          case SELF:
            wallBlock.add(0, h, w);
            break;
          case WEST:
          case EAST:
            wallBlock.add(w, h, 0);
            break;
          case SOUTH_EAST:
            wallBlock.add(w, h, w);
            break;
          case SOUTH_WEST:
            wallBlock.add(w, h, w * (-1));
            break;
          case NORTH_EAST:
            wallBlock.add(w * (-1), h, w);
            break;
          case NORTH_WEST:
            wallBlock.add(w * (-1), h, w * (-1));
            break;
          default:
            wallBlock = null;
            break;
        }

        if (wallBlock == null) {
          continue;
        }

        Block block = wallBlock.getBlock();
        if (!block.getType().equals(Material.AIR)) {
          continue;
        }

        block.setType(blockMaterial);
        if (!canBreak) {
          game.getRegion().addPlacedUnbreakableBlock(wallBlock.getBlock(), null);
        } else {
          game.getRegion().addPlacedBlock(wallBlock.getBlock(), null);
        }
        this.wallBlocks.add(block);
      }
    }

    if (breakTime > 0 || waitTime > 0) {
      this.createTask(breakTime, waitTime);
      game.addSpecialItem(this);
    }
  }

  private void createTask(final int breakTime, final int waitTime) {
    this.task = new BukkitRunnable() {

      @Override
      public void run() {
        ProtectionWall.this.livingTime++;

        if (breakTime > 0 && ProtectionWall.this.livingTime == breakTime) {
          for (Block block : ProtectionWall.this.wallBlocks) {
            block.getChunk().load(true);
            block.setType(Material.AIR);
            ProtectionWall.this.game.getRegion().removePlacedUnbreakableBlock(block);
          }
        }

        if (ProtectionWall.this.livingTime >= waitTime
            && ProtectionWall.this.livingTime >= breakTime) {
          ProtectionWall.this.game.removeRunningTask(this);
          ProtectionWall.this.game.removeSpecialItem(ProtectionWall.this);
          ProtectionWall.this.task = null;
          this.cancel();
          return;
        }
      }
    }.runTaskTimer(BedwarsRel.getInstance(), 20L, 20L);
    this.game.addRunningTask(this.task);
  }

  @Override
  public Material getActivatedMaterial() {
    return null;
  }

  public Game getGame() {
    return this.game;
  }

  @Override
  public Material getItemMaterial() {
    return Utils.getMaterialByConfig("specials.protection-wall.item", Material.BRICK);
  }

  public int getLivingTime() {
    return this.livingTime;
  }

  private ArrayList<ProtectionWall> getLivingWalls() {
    ArrayList<ProtectionWall> livingWalls = new ArrayList<ProtectionWall>();
    for (SpecialItem item : game.getSpecialItems()) {
      if (item instanceof ProtectionWall) {
        ProtectionWall wall = (ProtectionWall) item;
        if (wall.getOwner().equals(this.getOwner())) {
          livingWalls.add(wall);
        }
      }
    }
    return livingWalls;
  }

  public Player getOwner() {
    return this.owner;
  }

  public List<Block> getWallBlocks() {
    return this.wallBlocks;
  }

}
