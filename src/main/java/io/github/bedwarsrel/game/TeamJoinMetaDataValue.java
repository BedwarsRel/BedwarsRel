package io.github.bedwarsrel.game;

import io.github.bedwarsrel.BedwarsRel;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.plugin.Plugin;

public class TeamJoinMetaDataValue implements MetadataValue {

  private Team team = null;
  private boolean teamjoin = true;

  public TeamJoinMetaDataValue(Team team) {
    this.team = team;
  }

  @Override
  public boolean asBoolean() {
    return true;
  }

  @Override
  public byte asByte() {
    return this.asBoolean() ? (byte) 1 : (byte) 0;
  }

  @Override
  public double asDouble() {
    return this.asBoolean() ? 1 : 0;
  }

  @Override
  public float asFloat() {
    return this.asBoolean() ? 1F : 0F;
  }

  @Override
  public int asInt() {
    return this.asBoolean() ? 1 : 0;
  }

  @Override
  public long asLong() {
    return this.asBoolean() ? 1 : 0;
  }

  @Override
  public short asShort() {
    return this.asBoolean() ? (short) 1 : (short) 0;
  }

  @Override
  public String asString() {
    return this.asBoolean() ? "true" : "false";
  }

  @Override
  public Plugin getOwningPlugin() {
    return BedwarsRel.getInstance();
  }

  public Team getTeam() {
    return this.team;
  }

  @Override
  public void invalidate() {
    this.teamjoin = false;
  }

  @Override
  public Object value() {
    return this.teamjoin;
  }

}
