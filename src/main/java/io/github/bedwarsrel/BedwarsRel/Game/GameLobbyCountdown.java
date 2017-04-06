package io.github.bedwarsrel.BedwarsRel.Game;

import com.google.common.collect.ImmutableMap;
import io.github.bedwarsrel.BedwarsRel.Main;
import io.github.bedwarsrel.BedwarsRel.Utils.ChatWriter;
import io.github.bedwarsrel.BedwarsRel.Utils.SoundMachine;
import java.lang.reflect.Method;
import java.util.ArrayList;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class GameLobbyCountdown extends BukkitRunnable {

  @Getter
  @Setter
  private int counter = 0;
  private Game game = null;
  @Getter
  private int lobbytime;
  @Getter
  private int lobbytimeWhenFull;

  public GameLobbyCountdown(Game game) {
    this.game = game;
    this.counter = Main.getInstance().getConfig().getInt("lobbytime");
    this.lobbytime = this.counter;
    this.lobbytimeWhenFull = Main.getInstance().getConfig().getInt("lobbytime-full");
  }

  @Override
  public void run() {
    ArrayList<Player> players = this.game.getPlayers();
    float xpPerLevel = 1.0F / this.lobbytime;

    if (this.game.getState() != GameState.WAITING) {
      this.game.setGameLobbyCountdown(null);
      this.cancel();
      return;
    }

    if (this.counter > this.lobbytimeWhenFull
        && this.game.getPlayerAmount() == this.game.getMaxPlayers()) {
      this.counter = this.lobbytimeWhenFull;
      for (Player aPlayer : players) {
        if (aPlayer.isOnline()) {
          aPlayer.sendMessage(ChatWriter.pluginMessage(ChatColor.YELLOW
              + Main
              ._l(aPlayer, "lobby.countdown",
                  ImmutableMap.of("sec",
                      ChatColor.RED.toString() + this.counter + ChatColor.YELLOW))));
        }
      }
    }

    if (this.counter == this.lobbytimeWhenFull) {
      for (Player p : players) {
        if (p.getInventory().contains(Material.EMERALD)) {
          p.getInventory().remove(Material.EMERALD);
        }
      }
    }

    for (Player p : players) {
      p.setLevel(this.counter);
      if (this.counter == this.lobbytime) {
        p.setExp(1.0F);
      } else {
        p.setExp(1.0F - (xpPerLevel * (this.lobbytime - this.counter)));
      }

    }

    if (this.counter == this.lobbytime) {
      for (Player aPlayer : players) {
        if (aPlayer.isOnline()) {
          aPlayer.sendMessage(ChatWriter.pluginMessage(ChatColor.YELLOW
              + Main
              ._l(aPlayer, "lobby.countdown",
                  ImmutableMap.of("sec",
                      ChatColor.RED.toString() + this.counter + ChatColor.YELLOW))));
        }
      }

      for (Player p : players) {
        if (!p.getInventory().contains(Material.DIAMOND) && p.hasPermission("bw.vip.forcestart")) {
          this.game.getPlayerStorage(p).addGameStartItem();
        }

        if (!p.getInventory().contains(Material.EMERALD) && (p.isOp() || p.hasPermission("bw.setup")
            || p.hasPermission("bw.vip.reducecountdown"))) {
          this.game.getPlayerStorage(p).addReduceCountdownItem();
        }
      }
    }

    if (!this.game.isStartable()) {
      if (!this.game.hasEnoughPlayers()) {
        for (Player aPlayer : players) {
          if (aPlayer.isOnline()) {
            aPlayer.sendMessage(ChatWriter.pluginMessage(
                ChatColor.RED + Main._l(aPlayer, "lobby.cancelcountdown.not_enough_players")));
          }
        }
      } else if (!this.game.hasEnoughTeams()) {
        for (Player aPlayer : players) {
          if (aPlayer.isOnline()) {
            aPlayer.sendMessage(ChatWriter.pluginMessage(
                ChatColor.RED + Main._l(aPlayer, "lobby.cancelcountdown.not_enough_teams")));
          }
        }
      }

      this.counter = this.lobbytime;
      for (Player p : players) {
        p.setLevel(0);
        p.setExp(0.0F);
        if (p.getInventory().contains(Material.EMERALD)) {
          p.getInventory().remove(Material.EMERALD);
        }
      }

      this.game.setGameLobbyCountdown(null);
      this.cancel();
    }

    if (this.counter <= 10 && this.counter > 0) {
      for (Player aPlayer : players) {
        if (aPlayer.isOnline()) {
          aPlayer.sendMessage(ChatWriter.pluginMessage(ChatColor.YELLOW
              + Main
              ._l(aPlayer, "lobby.countdown",
                  ImmutableMap.of("sec",
                      ChatColor.RED.toString() + this.counter + ChatColor.YELLOW))));
        }
      }

      Class<?> titleClass = null;
      Method showTitle = null;
      String title = ChatColor.translateAlternateColorCodes('&',
          Main.getInstance().getStringConfig("titles.countdown.format", "&3{countdown}"));
      title = title.replace("{countdown}", String.valueOf(this.counter));

      if (Main.getInstance().getBooleanConfig("titles.countdown.enabled", true)) {
        try {
          titleClass = Main.getInstance().getVersionRelatedClass("Title");
          showTitle = titleClass.getMethod("showTitle", Player.class, String.class, double.class,
              double.class, double.class);
        } catch (Exception ex) {
          Main.getInstance().getBugsnag().notify(ex);
          ex.printStackTrace();
        }
      }

      for (Player player : players) {
        player.playSound(player.getLocation(), SoundMachine.get("CLICK", "UI_BUTTON_CLICK"),
            Float.valueOf("1.0"), Float.valueOf("1.0"));

        if (titleClass == null) {
          continue;
        }

        try {
          showTitle.invoke(null, player, title, 0.2, 0.6, 0.2);
        } catch (Exception ex) {
          Main.getInstance().getBugsnag().notify(ex);
          ex.printStackTrace();
        }
      }
    }

    if (this.counter == 0) {
      this.game.setGameLobbyCountdown(null);
      this.cancel();
      for (Player player : players) {
        player.playSound(player.getLocation(),
            SoundMachine.get("LEVEL_UP", "ENTITY_PLAYER_LEVELUP"), Float.valueOf("1.0"),
            Float.valueOf("1.0"));
        player.setLevel(0);
        player.setExp(0.0F);
      }

      this.game.start(Main.getInstance().getServer().getConsoleSender());
      return;
    }

    this.counter--;
  }
}
