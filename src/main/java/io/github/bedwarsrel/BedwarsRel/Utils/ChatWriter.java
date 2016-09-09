package io.github.bedwarsrel.BedwarsRel.Utils;

import org.bukkit.ChatColor;

import io.github.bedwarsrel.BedwarsRel.Main;

public class ChatWriter {

  public static String pluginMessage(String str) {
    return ChatColor.translateAlternateColorCodes('&',
        Main.getInstance().getConfig().getString("chat-prefix",
            ChatColor.GRAY + "[" + ChatColor.AQUA + "BedWars" + ChatColor.GRAY + "]"))
        + " " + ChatColor.WHITE + str;
  }

}
