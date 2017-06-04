package io.github.bedwarsrel.game;

import com.google.common.collect.ImmutableMap;
import io.github.bedwarsrel.BedwarsRel;
import io.github.bedwarsrel.utils.ChatWriter;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class RespawnProtectionRunnable extends BukkitRunnable {

  private Game game = null;
  private int length = 0;
  private Player player = null;

  public RespawnProtectionRunnable(Game game, Player player, int seconds) {
    this.game = game;
    this.player = player;
    this.length = seconds;
  }

  @Override
  public void run() {
    if (this.length > 0) {
      this.player
          .sendMessage(ChatWriter.pluginMessage(BedwarsRel._l(player, "ingame.protectionleft",
              ImmutableMap.of("length", String.valueOf(this.length)))));
    }

    if (this.length <= 0) {
      this.player
          .sendMessage(
              ChatWriter.pluginMessage(BedwarsRel._l(this.player, "ingame.protectionend")));
      this.game.removeProtection(this.player);
    }

    this.length--;
  }

  public void runProtection() {
    this.runTaskTimerAsynchronously(BedwarsRel.getInstance(), 5L, 20L);
  }

}
