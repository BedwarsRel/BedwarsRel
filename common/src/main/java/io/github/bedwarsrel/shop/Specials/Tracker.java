package io.github.bedwarsrel.shop.Specials;

import com.google.common.collect.ImmutableMap;
import io.github.bedwarsrel.BedwarsRel;
import io.github.bedwarsrel.game.Game;
import io.github.bedwarsrel.game.Team;
import io.github.bedwarsrel.utils.ChatWriter;
import java.util.ArrayList;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

public class Tracker extends SpecialItem {

  private Game game = null;
  private Player player = null;
  private ItemStack stack = null;

  public void createTask() {
    final Game game = this.game;

    BukkitTask task = new BukkitRunnable() {

      @Override
      public void run() {
        for (Player player : game.getTeamPlayers()) {
          if (player.getInventory().contains(getItemMaterial())) {
            Player target = findTargetPlayer(player);
            if (target != null) {
              player.setCompassTarget(target.getLocation());
              continue;
            }
          }
          player.setCompassTarget(game.getPlayerTeam(player).getSpawnLocation());
        }
      }
    }.runTaskTimer(BedwarsRel.getInstance(), 20L, 20L);
    this.game.addRunningTask(task);
  }

  private Player findTargetPlayer(Player player) {
    Player foundPlayer = null;
    double distance = Double.MAX_VALUE;

    Team playerTeam = this.game.getPlayerTeam(player);

    ArrayList<Player> possibleTargets = new ArrayList<Player>();
    possibleTargets.addAll(this.game.getTeamPlayers());
    possibleTargets.removeAll(playerTeam.getPlayers());

    for (Player p : possibleTargets) {
      if (player.getWorld() != p.getWorld()) {
        continue;
      }
      double dist = player.getLocation().distance(p.getLocation());
      if (dist < distance) {
        foundPlayer = p;
        distance = dist;
      }
    }

    return foundPlayer;
  }

  @Override
  public Material getActivatedMaterial() {
    return null;
  }

  @Override
  public Material getItemMaterial() {
    return Material.COMPASS;
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

  public void setGame(Game game) {
    this.game = game;
  }

  public void trackPlayer() {
    Player target = findTargetPlayer(this.player);

    if (target == null) {
      this.player.sendMessage(ChatWriter
          .pluginMessage(
              ChatColor.RED + BedwarsRel
                  ._l(this.player, "ingame.specials.tracker.no-target-found")));
      this.player.setCompassTarget(this.game.getPlayerTeam(this.player).getSpawnLocation());
      return;
    }

    int blocks = (int) this.player.getLocation().distance(target.getLocation());
    this.player.sendMessage(
        ChatWriter.pluginMessage(BedwarsRel._l(this.player, "ingame.specials.tracker.target-found",
            ImmutableMap.of("player", target.getDisplayName(), "blocks", String.valueOf(blocks)))));
  }
}
