package io.github.bedwarsrel.shop.Specials;

import com.google.common.collect.ImmutableMap;
import io.github.bedwarsrel.BedwarsRel;
import io.github.bedwarsrel.game.Game;
import io.github.bedwarsrel.game.Team;
import io.github.bedwarsrel.utils.ChatWriter;
import io.github.bedwarsrel.utils.Utils;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

public class WarpPowder extends SpecialItem {

  private int fullTeleportingTime = 6;
  private Game game = null;
  private Player player = null;
  private ItemStack stack = null;
  private BukkitTask teleportingTask = null;
  private double teleportingTime = 6.0;

  public WarpPowder() {
    super();

    this.fullTeleportingTime =
        BedwarsRel.getInstance().getIntConfig("specials.warp-powder.teleport-time", 6);
  }

  public void cancelTeleport(boolean removeSpecial, boolean showMessage) {
    try {
      this.teleportingTask.cancel();
    } catch (Exception ex) {
      BedwarsRel.getInstance().getBugsnag().notify(ex);
      // already stopped
    }

    this.teleportingTime =
        (double) BedwarsRel.getInstance().getIntConfig("specials.warp-powder.teleport-time", 6);
    this.game.removeRunningTask(this.teleportingTask);
    this.player.setLevel(0);

    if (removeSpecial) {
      this.game.removeSpecialItem(this);
    }

    if (showMessage) {
      this.player
          .sendMessage(ChatWriter
              .pluginMessage(BedwarsRel._l(this.player, "ingame.specials.warp-powder.cancelled")));
    }

    this.setStackAmount(this.getStack().getAmount() - 1);

    if (player.getInventory().first(this.getCancelItemStack()) != -1) {
      this.player.getInventory().setItem(player.getInventory().first(this.getCancelItemStack()),
          this.stack);
    } else {
      this.player.getInventory().setItemInOffHand(this.stack);
    }

    this.player.updateInventory();
  }

  @Override
  public Material getActivatedMaterial() {
    return Material.GLOWSTONE_DUST;
  }

  private ItemStack getCancelItemStack() {
    ItemStack glowstone = new ItemStack(this.getActivatedMaterial(), 1);
    ItemMeta meta = glowstone.getItemMeta();
    meta.setDisplayName(BedwarsRel._l("ingame.specials.warp-powder.cancel"));
    glowstone.setItemMeta(meta);

    return glowstone;
  }

  @Override
  public Material getItemMaterial() {
    return Material.SULPHUR;
  }

  public Player getPlayer() {
    return this.player;
  }

  public void setPlayer(Player player) {
    this.player = player;
  }

  public ItemStack getStack() {
    return this.stack;
  }

  @SuppressWarnings("deprecation")
  public void runTask() {
    final int circles = 15;
    final double height = 2.0;

    if (BedwarsRel.getInstance().getCurrentVersion().startsWith("v1_8")) {
      this.stack = player.getInventory().getItemInHand();
      this.player.getInventory().setItem(player.getInventory().getHeldItemSlot(),
          this.getCancelItemStack());
    } else {
      if (player.getInventory().getItemInOffHand().getType() == this.getItemMaterial()) {
        this.stack = player.getInventory().getItemInOffHand();
        this.player.getInventory().setItemInOffHand(this.getCancelItemStack());
      } else if (player.getInventory().getItemInMainHand().getType() == this.getItemMaterial()) {
        this.stack = player.getInventory().getItemInMainHand();
        this.player.getInventory().setItemInMainHand(this.getCancelItemStack());
      }
    }
    this.player.updateInventory();

    this.teleportingTime =
        (double) BedwarsRel.getInstance().getIntConfig("specials.warp-powder.teleport-time", 6);
    this.player.sendMessage(
        ChatWriter.pluginMessage(BedwarsRel._l(this.player, "ingame.specials.warp-powder.start",
            ImmutableMap.of("time", String.valueOf(this.fullTeleportingTime)))));

    this.teleportingTask = new BukkitRunnable() {

      public String particle =
          BedwarsRel.getInstance()
              .getStringConfig("specials.warp-powder.particle", "fireworksSpark");
      public boolean showParticle =
          BedwarsRel.getInstance().getBooleanConfig("specials.warp-powder.show-particles", true);
      public double through = 0.0;

      @Override
      public void run() {
        try {
          int circleElements = 20;
          double radius = 1.0;
          double height2 = 1.0;
          double circles = 15.0;
          double fulltime = (double) WarpPowder.this.fullTeleportingTime;
          double teleportingTime = WarpPowder.this.teleportingTime;

          double perThrough = (Math.ceil((height / circles) * ((fulltime * 20) / circles)) / 20);

          WarpPowder.this.teleportingTime = teleportingTime - perThrough;
          Team team = WarpPowder.this.game.getPlayerTeam(WarpPowder.this.player);
          Location tLoc = team.getSpawnLocation();

          if (WarpPowder.this.teleportingTime <= 1.0) {
            WarpPowder.this.player.teleport(team.getSpawnLocation());
            WarpPowder.this.cancelTeleport(true, false);
            return;
          }

          WarpPowder.this.player.setLevel((int) WarpPowder.this.teleportingTime);
          if (!showParticle) {
            return;
          }

          Location loc = WarpPowder.this.player.getLocation();

          double y = (height2 / circles) * through;
          for (int i = 0; i < 20; i++) {
            double alpha = (360.0 / circleElements) * i;
            double x = radius * Math.sin(Math.toRadians(alpha));
            double z = radius * Math.cos(Math.toRadians(alpha));

            Location particleFrom =
                new Location(loc.getWorld(), loc.getX() + x, loc.getY() + y, loc.getZ() + z);
            Utils.createParticleInGame(game, this.particle, particleFrom);

            Location particleTo =
                new Location(tLoc.getWorld(), tLoc.getX() + x, tLoc.getY() + y, tLoc.getZ() + z);
            Utils.createParticleInGame(game, this.particle, particleTo);
          }

          this.through += 1.0;
        } catch (Exception ex) {
          BedwarsRel.getInstance().getBugsnag().notify(ex);
          ex.printStackTrace();
          this.cancel();
          WarpPowder.this.cancelTeleport(true, false);
        }
      }
    }.runTaskTimer(BedwarsRel.getInstance(), 0L,
        (long) Math.ceil((height / circles) * ((this.fullTeleportingTime * 20) / circles)));
    this.game.addRunningTask(this.teleportingTask);
    this.game.addSpecialItem(this);
  }

  public void setGame(Game game) {
    this.game = game;
  }

  public void setStackAmount(int amount) {
    this.stack.setAmount(amount);
  }
}
