package io.github.bedwarsrel.game;

import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.DyeColor;

public enum TeamColor {
  GREEN(Color.fromRGB(85, 255, 85), ChatColor.GREEN, DyeColor.LIME),
  RED(Color.fromRGB(255, 85, 85), ChatColor.RED, DyeColor.RED),
  BLUE(Color.fromRGB(85, 85, 255), ChatColor.BLUE, DyeColor.LIGHT_BLUE),
  YELLOW(Color.fromRGB(255, 255, 85), ChatColor.YELLOW, DyeColor.YELLOW),
  AQUA(Color.fromRGB(85, 255, 255), ChatColor.AQUA, DyeColor.CYAN),
  BLACK(Color.BLACK, ChatColor.BLACK, DyeColor.BLACK),
  GOLD(Color.fromRGB(255, 170, 0), ChatColor.GOLD, DyeColor.ORANGE),
  DARK_BLUE(Color.fromRGB(0, 0, 170), ChatColor.DARK_BLUE, DyeColor.BLUE),
  DARK_GREEN(Color.fromRGB(0, 170, 0), ChatColor.DARK_GREEN, DyeColor.GREEN),
  DARK_RED(Color.fromRGB(170, 0, 0), ChatColor.DARK_RED, DyeColor.BROWN),
  DARK_PURPLE(Color.fromRGB(170, 0, 170), ChatColor.DARK_PURPLE, DyeColor.MAGENTA),
  GRAY(Color.fromRGB(170, 170, 170), ChatColor.GRAY, DyeColor.SILVER),
  DARK_GRAY(Color.fromRGB(85, 85, 85), ChatColor.DARK_GRAY, DyeColor.GRAY),
  LIGHT_PURPLE(Color.fromRGB(255, 85, 255), ChatColor.LIGHT_PURPLE, DyeColor.PINK),
  WHITE(Color.WHITE, ChatColor.WHITE, DyeColor.WHITE);

  private ChatColor chatColor;
  private Color color;
  private DyeColor dyeColor;

  private TeamColor(Color color, ChatColor chatColor, DyeColor dye) {
    this.chatColor = chatColor;
    this.color = color;
    this.dyeColor = dye;
  }

  public ChatColor getChatColor() {
    return this.chatColor;
  }

  public Color getColor() {
    return this.color;
  }

  public DyeColor getDyeColor() {
    return this.dyeColor;
  }
}
