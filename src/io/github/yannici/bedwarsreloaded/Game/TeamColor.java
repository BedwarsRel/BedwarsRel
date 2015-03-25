package io.github.yannici.bedwarsreloaded.Game;

import org.bukkit.ChatColor;

public enum TeamColor {
    GREEN(ChatColor.GREEN),
    RED(ChatColor.RED),
    BLUE(ChatColor.BLUE),
    YELLOW(ChatColor.YELLOW);

    @SuppressWarnings("unused")
    private ChatColor color;

    private TeamColor(ChatColor color) {
        this.color = color;
    }
}
