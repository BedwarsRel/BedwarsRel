package io.github.bedwarsrel.BedwarsRel.Reflection;

import java.util.List;

import org.bukkit.entity.Player;
import org.inventivetalent.reflection.minecraft.Minecraft;
import org.inventivetalent.reflection.resolver.ConstructorResolver;

public class ParticleSpawner {

  static Class<?> PacketPlayOutWorldParticles = ReflectionHelper.classResolver.resolveSilent(
      "net.minecraft.server." + Minecraft.getVersion() + "PacketPlayOutWorldParticles");
  static Class<?> EnumParticle = ReflectionHelper.classResolver.resolveSilent(
      "net.minecraft.server." + Minecraft.getVersion() + "PacketPlayOutWorldParticles$EnumParticle",
      "net.minecraft.server." + Minecraft.getVersion() + "EnumParticle");
  static ConstructorResolver PacketPlayOutWorldParticlesConstructorResolver =
      new ConstructorResolver(PacketPlayOutWorldParticles);


  public static void spawnParticle(List<Player> players, String particle, float x, float y,
      float z) {
    try {
      Object packetRespawn = PacketPlayOutWorldParticlesConstructorResolver
          .resolve(new Class[] {EnumParticle, boolean.class, float.class, float.class, float.class,
              float.class, float.class, float.class, float.class, int.class, int[].class})
          .newInstance(EnumParticle.getEnumConstants()[3], false, x, y, z, 0.0F, 0.0F, 0.0F, 0.0F,
              1, null);
      for (Player player : players) {
        ReflectionHelper.sendPacket(player, packetRespawn);
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }


}
