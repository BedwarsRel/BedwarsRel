package io.github.bedwarsrel.game;

import io.github.bedwarsrel.BedwarsRel;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;

public class GameJoinSign {

  private Game game = null;
  private Location signLocation = null;

  public GameJoinSign(Game game, Location sign) {
    this.game = game;
    this.signLocation = sign;
  }

  private String getCurrentPlayersString() {
    int maxPlayers = this.game.getMaxPlayers();
    int currentPlayers = 0;
    if (this.game.getState() == GameState.RUNNING) {
      currentPlayers = this.game.getTeamPlayers().size();
    } else if (this.game.getState() == GameState.WAITING) {
      currentPlayers = this.game.getPlayers().size();
    } else {
      currentPlayers = 0;
    }

    String current = "0";
    if (currentPlayers >= maxPlayers) {
      current = ChatColor.RED + String.valueOf(currentPlayers) + ChatColor.WHITE;
    } else {
      current = String.valueOf(currentPlayers);
    }

    return current;
  }

  private String getMaxPlayersString() {
    int maxPlayers = this.game.getMaxPlayers();
    int currentPlayers = 0;
    if (this.game.getState() == GameState.RUNNING) {
      currentPlayers = this.game.getTeamPlayers().size();
    } else if (this.game.getState() == GameState.WAITING) {
      currentPlayers = this.game.getPlayers().size();
    } else {
      currentPlayers = 0;
    }

    String max = String.valueOf(maxPlayers);

    if (currentPlayers >= maxPlayers) {
      max = ChatColor.RED + max + ChatColor.WHITE;
    }

    return max;
  }

  public Sign getSign() {
    BlockState state = this.signLocation.getBlock().getState();

    if (!(state instanceof Sign)) {
      return null;
    }

    return (Sign) state;
  }

  private String[] getSignLines() {
    String[] sign = new String[4];
    sign[0] = this.replacePlaceholder(ChatColor.translateAlternateColorCodes('&',
        BedwarsRel.getInstance().getConfig().getString("sign.first-line")));
    sign[1] = this.replacePlaceholder(ChatColor.translateAlternateColorCodes('&',
        BedwarsRel.getInstance().getConfig().getString("sign.second-line")));
    sign[2] = this.replacePlaceholder(ChatColor.translateAlternateColorCodes('&',
        BedwarsRel.getInstance().getConfig().getString("sign.third-line")));
    sign[3] = this.replacePlaceholder(ChatColor.translateAlternateColorCodes('&',
        BedwarsRel.getInstance().getConfig().getString("sign.fourth-line")));

    return sign;
  }

  private String getStatus() {
    String status = null;
    if (this.game.getState() == GameState.WAITING && this.game.isFull()) {
      status = ChatColor.RED + BedwarsRel._l("sign.gamestate.full");
    } else {
      status = BedwarsRel._l("sign.gamestate." + this.game.getState().toString().toLowerCase());
    }

    return status;
  }

  private String replacePlaceholder(String line) {
    String finalLine = line;

    finalLine = finalLine.replace("$title$", BedwarsRel._l("sign.firstline"));
    finalLine = finalLine.replace("$gamename$", this.game.getName());
    finalLine = finalLine.replace("$regionname$", this.game.getRegion().getName());
    finalLine = finalLine.replace("$maxplayers$", this.getMaxPlayersString());
    finalLine = finalLine.replace("$currentplayers$", this.getCurrentPlayersString());
    finalLine = finalLine.replace("$status$", this.getStatus());

    return finalLine;
  }

  public void updateSign() {
    Sign sign = (Sign) this.signLocation.getBlock().getState();

    String[] signLines = this.getSignLines();
    for (int i = 0; i < signLines.length; i++) {
      sign.setLine(i, signLines[i]);
    }

    sign.update(true, true);
  }

}
