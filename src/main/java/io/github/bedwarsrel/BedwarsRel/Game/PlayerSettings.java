package io.github.bedwarsrel.BedwarsRel.Game;

import org.bukkit.entity.Player;

import io.github.bedwarsrel.BedwarsRel.Main;

public class PlayerSettings {

  private Player player = null;
  private boolean oneStackPerShift = false;
  private Object hologram = null;
  private boolean isTeleporting = false;
  private boolean useOldShop = false;

  public PlayerSettings(Player player) {
    this.player = player;
    this.oneStackPerShift = Main.getInstance().getBooleanConfig("player-settings.one-stack-on-shift", false);
    this.useOldShop = Main.getInstance().getBooleanConfig("player-settings.old-shop-as-default", false);
  }

  public Player getPlayer() {
    return this.player;
  }

  public Object getHologram() {
    return this.hologram;
  }

  public void setHologram(Object holo) {
    this.hologram = holo;
  }

  public boolean oneStackPerShift() {
    return this.oneStackPerShift;
  }

  public void setOneStackPerShift(boolean value) {
    this.oneStackPerShift = value;
  }

  public boolean isTeleporting() {
    return isTeleporting;
  }

  public void setTeleporting(boolean isTeleporting) {
    this.isTeleporting = isTeleporting;
  }

  public boolean useOldShop() {
    return this.useOldShop;
  }

  public void setUseOldShop(boolean value) {
    this.useOldShop = value;
  }

}
