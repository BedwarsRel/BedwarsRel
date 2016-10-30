package io.github.bedwarsrel.BedwarsRel.Shop.Specials;

import java.util.ArrayList;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import com.google.common.collect.ImmutableMap;

import io.github.bedwarsrel.BedwarsRel.Main;
import io.github.bedwarsrel.BedwarsRel.Game.Game;
import io.github.bedwarsrel.BedwarsRel.Utils.ChatWriter;
import io.github.bedwarsrel.BedwarsRel.Utils.Utils;

public class ArrowBlocker extends SpecialItem {

  private int livingTime = 0;
  private Player owner = null;
  private BukkitTask task = null;
  private Game game = null;
  public boolean isActive = false;

  public ArrowBlocker() {
    super();

    this.game = null;
    this.owner = null;
  }

  @Override
  public Material getItemMaterial() {
    return Utils.getMaterialByConfig("specials.Arrow-Blocker.item", Material.BARRIER);
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

    int breakTime = Main.getInstance().getIntConfig("specials.arrow-blocker.break-time", 10);
    int waitTime = Main.getInstance().getIntConfig("specials.arrow-blocker.using-wait-time", 30);

    if (waitTime > 0) {
      ArrayList<ArrowBlocker> livingBlockers = this.getLivingBlocker();
      if (!livingBlockers.isEmpty()) {
        for (ArrowBlocker livingBlocker : livingBlockers) {
          int waitLeft = waitTime - livingBlocker.getLivingTime();
          if (waitLeft > 0) {
            player.sendMessage(
                ChatWriter.pluginMessage(Main._l("ingame.specials.arrow-blocker.left",
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
    
    player.sendMessage(ChatWriter.pluginMessage(Main._l("ingame.specials.arrow-blocker.start",
        ImmutableMap.of("time", String.valueOf(breakTime)))));
    
    if (breakTime > 0 || waitTime > 0) {
      this.runTask(breakTime, waitTime);
      game.addSpecialItem(this);
    }
  }
  
  public void runTask(final int breakTime, final int waitTime) {
    this.task = new BukkitRunnable() {

      @Override
      public void run() {
        ArrowBlocker.this.livingTime++;
        isActive = true;

        if (breakTime > 0 && ArrowBlocker.this.livingTime == breakTime) {
          isActive = false;
        }

        if (ArrowBlocker.this.livingTime >= waitTime
            && ArrowBlocker.this.livingTime >= breakTime) {
          ArrowBlocker.this.game.removeRunningTask(this);
          ArrowBlocker.this.game.removeSpecialItem(ArrowBlocker.this);
          ArrowBlocker.this.task = null;
          isActive = false;
          this.cancel();
          return;
        }
      }
    }.runTaskTimer(Main.getInstance(), 20L, 20L);
    this.game.addRunningTask(this.task);
  }

  private ArrayList<ArrowBlocker> getLivingBlocker() {
    ArrayList<ArrowBlocker> livingBlocker = new ArrayList<ArrowBlocker>();
    for (SpecialItem item : game.getSpecialItems()) {
      if (item instanceof ArrowBlocker) {
        ArrowBlocker blocker = (ArrowBlocker) item;
        if (blocker.getOwner().equals(this.getOwner())) {
          livingBlocker.add(blocker);
        }
      }
    }
    return livingBlocker;
  }

  @Override
  public Material getActivatedMaterial() {
    // not needed
    return null;
  }

}
