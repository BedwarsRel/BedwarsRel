package io.github.bedwarsrel.shop.Specials;

import com.google.common.collect.ImmutableMap;
import io.github.bedwarsrel.BedwarsRel;
import io.github.bedwarsrel.game.Game;
import io.github.bedwarsrel.game.Team;
import io.github.bedwarsrel.utils.ChatWriter;
import io.github.bedwarsrel.utils.Utils;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

public class RescuePlatform extends SpecialItem {

  private Game game = null;
  private int livingTime = 0;
  private Player owner = null;
  private List<Block> platformBlocks = null;
  private BukkitTask task = null;

  public RescuePlatform() {
    super();

    this.platformBlocks = new ArrayList<Block>();
    this.game = null;
    this.owner = null;
  }

  public void addPlatformBlock(Block block) {
    this.platformBlocks.add(block);
  }

  @SuppressWarnings("deprecation")
  public void create(Player player, Game game) {
    this.game = game;
    this.owner = player;

    int breakTime = BedwarsRel.getInstance()
        .getIntConfig("specials.rescue-platform.break-time", 10);
    int waitTime = BedwarsRel
        .getInstance().getIntConfig("specials.rescue-platform.using-wait-time", 20);
    boolean canBreak =
        BedwarsRel.getInstance().getBooleanConfig("specials.rescue-platform.can-break", false);
    Material configMaterial =
        Utils.getMaterialByConfig("specials.rescue-platform.block", Material.STAINED_GLASS);

    if (waitTime > 0) {
      ArrayList<RescuePlatform> livingPlatforms = this.getLivingPlatforms();
      if (!livingPlatforms.isEmpty()) {
        for (RescuePlatform livingPlatform : livingPlatforms) {
          int waitLeft = waitTime - livingPlatform.getLivingTime();
          if (waitLeft > 0) {
            player.sendMessage(
                ChatWriter.pluginMessage(
                    BedwarsRel._l(player, "ingame.specials.rescue-platform.left",
                        ImmutableMap.of("time", String.valueOf(waitLeft)))));
            return;
          }
        }
      }
    }

    if (player.getLocation().getBlock().getRelative(BlockFace.DOWN).getType() != Material.AIR) {
      player.sendMessage(
          ChatWriter.pluginMessage(ChatColor.RED + BedwarsRel._l(player, "errors.notinair")));
      return;
    }

    Location mid = player.getLocation().clone();
    mid.setY(mid.getY() - 1.0D);

    Team team = game.getPlayerTeam(player);

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

    for (BlockFace face : BlockFace.values()) {
      if (face.equals(BlockFace.DOWN) || face.equals(BlockFace.UP)) {
        continue;
      }

      Block placed = mid.getBlock().getRelative(face);
      if (placed.getType() != Material.AIR) {
        continue;
      }

      placed.setType(configMaterial);
      if (configMaterial.equals(Material.STAINED_GLASS) || configMaterial.equals(Material.WOOL)
          || configMaterial.equals(Material.STAINED_CLAY)) {
        placed.setData(team.getColor().getDyeColor().getWoolData());
      }

      if (!canBreak) {
        game.getRegion().addPlacedUnbreakableBlock(placed, null);
      } else {
        game.getRegion().addPlacedBlock(placed, null);
      }

      this.addPlatformBlock(placed);
    }
    if (breakTime > 0 || waitTime > 0) {
      this.runTask(breakTime, waitTime);
      game.addSpecialItem(this);
    }
  }

  @Override
  public Material getActivatedMaterial() {
    // not needed
    return null;
  }

  public Game getGame() {
    return this.game;
  }

  @Override
  public Material getItemMaterial() {
    return Utils.getMaterialByConfig("specials.rescue-platform.item", Material.BLAZE_ROD);
  }

  private ArrayList<RescuePlatform> getLivingPlatforms() {
    ArrayList<RescuePlatform> livingPlatforms = new ArrayList<RescuePlatform>();
    for (SpecialItem item : game.getSpecialItems()) {
      if (item instanceof RescuePlatform) {
        RescuePlatform rescuePlatform = (RescuePlatform) item;
        if (rescuePlatform.getOwner().equals(this.getOwner())) {
          livingPlatforms.add(rescuePlatform);
        }
      }
    }
    return livingPlatforms;
  }

  public int getLivingTime() {
    return this.livingTime;
  }

  public Player getOwner() {
    return this.owner;
  }

  public void runTask(final int breakTime, final int waitTime) {
    this.task = new BukkitRunnable() {

      @Override
      public void run() {
        RescuePlatform.this.livingTime++;

        if (breakTime > 0 && RescuePlatform.this.livingTime == breakTime) {
          for (Block block : RescuePlatform.this.platformBlocks) {
            block.getChunk().load(true);
            block.setType(Material.AIR);
            RescuePlatform.this.game.getRegion().removePlacedUnbreakableBlock(block);
          }
        }

        if (RescuePlatform.this.livingTime >= waitTime
            && RescuePlatform.this.livingTime >= breakTime) {
          RescuePlatform.this.game.removeRunningTask(this);
          RescuePlatform.this.game.removeSpecialItem(RescuePlatform.this);
          RescuePlatform.this.task = null;
          this.cancel();
          return;
        }
      }
    }.runTaskTimer(BedwarsRel.getInstance(), 20L, 20L);
    this.game.addRunningTask(this.task);
  }

}
