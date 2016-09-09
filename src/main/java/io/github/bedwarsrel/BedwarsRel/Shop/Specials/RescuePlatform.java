package io.github.bedwarsrel.BedwarsRel.Shop.Specials;

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

import com.google.common.collect.ImmutableMap;

import io.github.bedwarsrel.BedwarsRel.Main;
import io.github.bedwarsrel.BedwarsRel.Game.Game;
import io.github.bedwarsrel.BedwarsRel.Game.Team;
import io.github.bedwarsrel.BedwarsRel.Utils.ChatWriter;
import io.github.bedwarsrel.BedwarsRel.Utils.Utils;

public class RescuePlatform extends SpecialItem {

  private int livingTime = 0;
  private Player owner = null;
  private List<Block> platformBlocks = null;
  private BukkitTask task = null;
  private Game game = null;

  public RescuePlatform() {
    super();

    this.platformBlocks = new ArrayList<Block>();
    this.game = null;
    this.owner = null;
  }

  @Override
  public Material getItemMaterial() {
    return Utils.getMaterialByConfig("specials.rescue-platform.item", Material.BLAZE_ROD);
  }

  public int getLivingTime() {
    return this.livingTime;
  }

  public Game getGame() {
    return this.game;
  }

  public Player getOwner() {
    return this.owner;
  }

  public void addPlatformBlock(Block block) {
    this.platformBlocks.add(block);
  }

  @SuppressWarnings("deprecation")
  public void create(Player player, Game game) {
    this.game = game;
    this.owner = player;

    int breakTime = Main.getInstance().getIntConfig("specials.rescue-platform.break-time", 10);
    int waitTime = Main.getInstance().getIntConfig("specials.rescue-platform.using-wait-time", 20);
    boolean canBreak =
        Main.getInstance().getBooleanConfig("specials.rescue-platform.can-break", false);
    Material configMaterial =
        Utils.getMaterialByConfig("specials.rescue-platform.block", Material.STAINED_GLASS);

    if (waitTime > 0) {
      ArrayList<RescuePlatform> livingPlatforms = this.getLivingPlatforms();
      if (!livingPlatforms.isEmpty()) {
        for (RescuePlatform livingPlatform : livingPlatforms) {
          int waitLeft = waitTime - livingPlatform.getLivingTime();
          if (waitLeft > 0) {
            player.sendMessage(
                ChatWriter.pluginMessage(Main._l("ingame.specials.rescue-platform.left",
                    ImmutableMap.of("time", String.valueOf(waitLeft)))));
            return;
          }
        }
      }
    }

    if (player.getLocation().getBlock().getRelative(BlockFace.DOWN).getType() != Material.AIR) {
      player.sendMessage(ChatWriter.pluginMessage(ChatColor.RED + Main._l("errors.notinair")));
      return;
    }

    Location mid = player.getLocation().clone();
    mid.setY(mid.getY() - 1.0D);

    Team team = game.getPlayerTeam(player);

    ItemStack usedStack = null;

    if (Main.getInstance().getCurrentVersion().startsWith("v1_8")) {
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
        placed.setData(team.getColor().getDyeColor().getData());
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
    }.runTaskTimer(Main.getInstance(), 20L, 20L);
    this.game.addRunningTask(this.task);
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

  @Override
  public Material getActivatedMaterial() {
    // not needed
    return null;
  }

}
