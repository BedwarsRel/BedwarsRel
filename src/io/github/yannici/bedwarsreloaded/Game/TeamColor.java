package io.github.yannici.bedwarsreloaded.Game;

import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.DyeColor;

public enum TeamColor {
    GREEN(Color.fromRGB(85, 255, 85), ChatColor.GREEN),
    RED(Color.fromRGB(255, 85, 85), ChatColor.RED),
    BLUE(Color.fromRGB(85, 85, 255), ChatColor.BLUE),
    YELLOW(Color.fromRGB(255, 255, 85), ChatColor.YELLOW),
    AQUA(Color.fromRGB(85, 255, 255), ChatColor.AQUA),
    BLACK(Color.BLACK, ChatColor.BLACK),
    GOLD(Color.fromRGB(255, 170, 0), ChatColor.GOLD),
    DARK_BLUE(Color.fromRGB(0, 0, 170), ChatColor.DARK_BLUE),
    DARK_GREEN(Color.fromRGB(0, 170, 0), ChatColor.GREEN),
    DARK_AQUA(Color.fromRGB(0, 170, 170), ChatColor.DARK_AQUA),
    DARK_RED(Color.fromRGB(170, 0, 0), ChatColor.DARK_RED),
    DARK_PURPLE(Color.fromRGB(170, 0, 170), ChatColor.DARK_PURPLE),
    GRAY(Color.fromRGB(170, 170, 170), ChatColor.GRAY),
    DARK_GRAY(Color.fromRGB(85, 85, 85), ChatColor.DARK_GRAY),
    LIGHT_PURPLE(Color.fromRGB(255, 85, 255), ChatColor.LIGHT_PURPLE),
    WHITE(Color.WHITE, ChatColor.WHITE);
    
    private Color color;
    private ChatColor chatColor;
    private DyeColor dyeColor;

    private TeamColor(Color color, ChatColor chatColor) {
        this.chatColor = chatColor;
        this.color = color;
        this.dyeColor = DyeColor.getByColor(this.color);
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
