package io.github.bedwarsrel.BedwarsRel.Shop.Specials;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import com.google.common.collect.ImmutableMap;

import io.github.bedwarsrel.BedwarsRel.Main;
import io.github.bedwarsrel.BedwarsRel.Game.Game;
import io.github.bedwarsrel.BedwarsRel.Utils.ChatWriter;
import io.github.bedwarsrel.BedwarsRel.Utils.Utils;

public class AutoBridge extends SpecialItem {

  private int livingTime = 0;
  private Player owner = null;
  private BukkitTask task = null;
  private BukkitTask bridgeTask = null;
  private Game game = null;
  private List<Block> bridgeBlocks = null;
  Set<Material> fillMaterial = null;
  private int count = 0;

  public AutoBridge() {
    super();
    this.bridgeBlocks = new ArrayList<Block>();
    this.fillMaterial = new HashSet<Material>();
    this.game = null;
    this.owner = null;
  }

  @Override
  public Material getItemMaterial() {
    return Utils.getMaterialByConfig("specials.auto-bridge.item", Material.EGG);
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

  @SuppressWarnings("deprecation")
  public void create(Player player, Game game) {
    this.game = game;
    this.owner = player;

    int breakTime = Main.getInstance().getIntConfig("specials.auto-bridge.break-time", 20);
    int waitTime = Main.getInstance().getIntConfig("specials.auto-bridge.using-wait-time", 120);
    int distance = Main.getInstance().getIntConfig("specials.auto-bridge.distance", 10);
    boolean canBreak = Main.getInstance().getBooleanConfig("specials.auto-bridge.can-break", true);
    Material blockMaterial =
        Utils.getMaterialByConfig("specials.auto-bridge.block", Material.SANDSTONE);

    if (player.getEyeLocation().getBlock().getType() != Material.AIR) {
      player.sendMessage(
          ChatWriter.pluginMessage(Main._l("ingame.specials.auto-bridge.not-usable-here")));
      return;
    }


    if (waitTime > 0) {
      ArrayList<AutoBridge> livingBridges = this.getLivingBridge();
      if (!livingBridges.isEmpty()) {
        for (AutoBridge livingBridge : livingBridges) {
          int waitLeft = waitTime - livingBridge.getLivingTime();
          if (waitLeft > 0) {
            player.sendMessage(ChatWriter.pluginMessage(Main._l("ingame.specials.auto-bridge.left",
                ImmutableMap.of("time", String.valueOf(waitLeft)))));
            return;
          }
        }
      }
    }

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

    fillMaterial.add(Material.AIR);
    bridgeBlocks = (player.getLineOfSight(fillMaterial, distance));
    // bridgeBlocks.remove(0);

    buildBridge(player, blockMaterial, canBreak, bridgeBlocks.size());

    if (breakTime > 0 || waitTime > 0) {
      this.runTask(breakTime, waitTime, player);
      game.addSpecialItem(this);
    }
  }

  public void buildBridge(final Player player, final Material blockMaterial, final boolean canBreak,
      final int max) {
    this.bridgeTask = new BukkitRunnable() {
      @Override
      public void run() {
        count++;
        if (count < max) {
          Block block = bridgeBlocks.get(count);
          Location loc =
              new Location(player.getWorld(), block.getX(), block.getY() - 2, block.getZ());
          block = loc.getBlock();
          if (!block.getType().equals(Material.AIR)) {
            return;
          }

          block.setType(blockMaterial);
          if (!canBreak) {
            game.getRegion().addPlacedUnbreakableBlock(block, null);
          } else {
            game.getRegion().addPlacedBlock(block, null);
          }
        } else {
          this.cancel();
          return;
        }
      }
    }.runTaskTimer(Main.getInstance(), 0, 1L);
    this.game.addRunningTask(this.bridgeTask);
  }

  public void runTask(final int breakTime, final int waitTime, final Player player) {
    this.task = new BukkitRunnable() {

      @Override
      public void run() {
        AutoBridge.this.livingTime++;

        if (breakTime > 0 && AutoBridge.this.livingTime == breakTime) {
          AutoBridge.this.game.removeRunningTask(AutoBridge.this.bridgeTask);
          AutoBridge.this.bridgeTask.cancel();
          AutoBridge.this.bridgeTask = null;

          for (Block block : AutoBridge.this.bridgeBlocks) {
            block.getChunk().load(true);
            block.setType(Material.AIR);
            AutoBridge.this.game.getRegion().removePlacedUnbreakableBlock(block);
          }
        }

        if (AutoBridge.this.livingTime >= waitTime && AutoBridge.this.livingTime >= breakTime) {
          AutoBridge.this.game.removeRunningTask(this);
          AutoBridge.this.game.removeSpecialItem(AutoBridge.this);
          AutoBridge.this.task = null;
          this.cancel();
          return;
        }
      }
    }.runTaskTimer(Main.getInstance(), 0, 20L);
    this.game.addRunningTask(this.task);
  }

  private ArrayList<AutoBridge> getLivingBridge() {
    ArrayList<AutoBridge> livingBridge = new ArrayList<AutoBridge>();
    for (SpecialItem item : game.getSpecialItems()) {
      if (item instanceof AutoBridge) {
        AutoBridge bridge = (AutoBridge) item;
        if (bridge.getOwner().equals(this.getOwner())) {
          livingBridge.add(bridge);
        }
      }
    }
    return livingBridge;
  }

  @Override
  public Material getActivatedMaterial() {
    // not needed
    return null;
  }

}
