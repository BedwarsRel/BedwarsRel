package io.github.bedwarsrel.BedwarsRel.Reflection;

import org.bukkit.entity.Player;
import org.inventivetalent.reflection.resolver.FieldResolver;
import org.inventivetalent.reflection.resolver.MethodResolver;

public class CraftPlayer {

  private static Class<?> CraftPlayer =
      ReflectionHelper.obcClassResolver.resolveSilent("entity.CraftPlayer");
  private static MethodResolver CraftPlayerMethodResolver = new MethodResolver(CraftPlayer);
  public static FieldResolver CraftPlayerFieldResolver = new FieldResolver(CraftPlayer);
  private Player player;

  public CraftPlayer(Player player) {
    this.player = player;
  }

  public Object getHandle() {
    try {
      return CraftPlayerMethodResolver.resolve("getHandle").invoke(this.player);
    } catch (Exception e) {
      e.printStackTrace();
    }
    return null;
  }

}
