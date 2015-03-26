package io.github.yannici.bedwarsreloaded.Game;

import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.DyeColor;

public enum TeamColor {
    GREEN(Color.GREEN, ChatColor.GREEN, DyeColor.GREEN),
    RED(Color.RED, ChatColor.RED, DyeColor.RED),
    BLUE(Color.BLUE, ChatColor.BLUE, DyeColor.BLUE),
    YELLOW(Color.YELLOW, ChatColor.YELLOW, DyeColor.YELLOW),
    AQUA(Color.AQUA, ChatColor.AQUA, DyeColor.getByColor(Color.AQUA)),
    BLACK(Color.BLACK, ChatColor.BLACK, DyeColor.BLACK),
    GOLD(Color.ORANGE, ChatColor.GOLD, DyeColor.ORANGE);
    
    private Color color;
    private ChatColor chatColor;
    private DyeColor dyeColor;

    private TeamColor(Color color, ChatColor chatColor, DyeColor dyeColor) {
        this.chatColor = chatColor;
        this.color = color;
        this.dyeColor = dyeColor;
    }
    
    public Color getColor() {
        return this.color;
    }
    
    public DyeColor getDyeColor() {
        return this.dyeColor;
    }
    
    public ChatColor getChatColor() {
        return this.chatColor;
    }
}
