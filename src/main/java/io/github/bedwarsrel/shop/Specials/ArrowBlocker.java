package io.github.bedwarsrel.shop.Specials;

import com.google.common.collect.ImmutableMap;
import io.github.bedwarsrel.BedwarsRel;
import io.github.bedwarsrel.game.Game;
import io.github.bedwarsrel.utils.ChatWriter;
import io.github.bedwarsrel.utils.Utils;
import java.util.ArrayList;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

public class ArrowBlocker extends SpecialItem {

  public boolean isActive = false;
  private Game game = null;
  private int livingTime = 0;
  private Player owner = null;
  private BukkitTask task = null;

  public ArrowBlocker() {
    super();

    this.game = null;
    this.owner = null;
  }

  @SuppressWarnings("deprecation")
  public void create(Player player, Game game) {
    this.game = game;
    this.owner = player;

    int protectionTime = BedwarsRel.getInstance()
        .getIntConfig("specials.arrow-blocker.protection-time", 10);
    int waitTime = BedwarsRel
        .getInstance().getIntConfig("specials.arrow-blocker.using-wait-time", 30);

    if (waitTime > 0) {
      ArrayList<ArrowBlocker> livingBlockers = this.getLivingBlocker();
      if (!livingBlockers.isEmpty()) {
        for (ArrowBlocker livingBlocker : livingBlockers) {
          int waitLeft = waitTime - livingBlocker.getLivingTime();
          if (waitLeft > 0) {
            player.sendMessage(
                ChatWriter.pluginMessage(BedwarsRel._l(player, "ingame.specials.arrow-blocker.left",
                    ImmutableMap.of("time", String.valueOf(waitLeft)))));
            return;
          }
        }
      }
    }

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

    player
        .sendMessage(ChatWriter.pluginMessage(
            BedwarsRel._l(player, "ingame.specials.arrow-blocker.start",
                ImmutableMap.of("time", String.valueOf(protectionTime)))));

    if (protectionTime > 0 || waitTime > 0) {
      isActive = true;
      this.runTask(protectionTime, waitTime, player);
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
    return Utils.getMaterialByConfig("specials.arrow-blocker.item", Material.EYE_OF_ENDER);
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

  public int getLivingTime() {
    return this.livingTime;
  }

  public Player getOwner() {
    return this.owner;
  }

  public void runTask(final int protectionTime, final int waitTime, final Player player) {
    this.task = new BukkitRunnable() {

      @Override
      public void run() {
        ArrowBlocker.this.livingTime++;

        if (protectionTime > 0 && ArrowBlocker.this.livingTime == protectionTime) {
          player.sendMessage(
              ChatWriter.pluginMessage(BedwarsRel._l(player, "ingame.specials.arrow-blocker.end")));
          isActive = false;
        }

        if (ArrowBlocker.this.livingTime >= waitTime
            && ArrowBlocker.this.livingTime >= protectionTime) {
          ArrowBlocker.this.game.removeRunningTask(this);
          ArrowBlocker.this.game.removeSpecialItem(ArrowBlocker.this);
          ArrowBlocker.this.task = null;
          isActive = false;
          this.cancel();
          return;
        }
      }
    }.runTaskTimer(BedwarsRel.getInstance(), 0, 20L);
    this.game.addRunningTask(this.task);
  }

}
