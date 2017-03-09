package io.github.bedwarsrel.BedwarsRel.Utils;

import io.github.bedwarsrel.BedwarsRel.Game.Game;
import io.github.bedwarsrel.BedwarsRel.Game.PlayerStorage;
import io.github.bedwarsrel.BedwarsRel.Main;
import java.util.Collection;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class ChatWriter {

  public static String pluginMessage(String str) {
    return ChatColor.translateAlternateColorCodes('&',
        Main.getInstance().getConfig().getString("chat-prefix",
            ChatColor.GRAY + "[" + ChatColor.AQUA + "BedWars" + ChatColor.GRAY + "]"))
        + " " + ChatColor.WHITE + str;
  }

}
