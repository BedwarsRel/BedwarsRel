package io.github.bedwarsrel.BedwarsRel.Reflection;

import java.util.List;

import org.bukkit.entity.Player;
import org.inventivetalent.reflection.minecraft.Minecraft;
import org.inventivetalent.reflection.resolver.ClassResolver;
import org.inventivetalent.reflection.resolver.ConstructorResolver;
import org.inventivetalent.reflection.resolver.FieldResolver;
import org.inventivetalent.reflection.resolver.MethodResolver;
import org.inventivetalent.reflection.resolver.minecraft.NMSClassResolver;

public class ParticleSpawner {

  static ClassResolver classResolver = new ClassResolver();
  static NMSClassResolver nmsClassResolver = new NMSClassResolver();
  static Class<?> PlayerConnection = nmsClassResolver.resolveSilent("PlayerConnection");
  static Class<?> EntityPlayer = nmsClassResolver.resolveSilent("EntityPlayer");
  static Class<?> NetworkManager = nmsClassResolver.resolveSilent("NetworkManager");
  static Class<?> Channel = classResolver
      .resolveSilent("net.minecraft.util.io.netty.channel.Channel", "io.netty.channel.Channel");
  static FieldResolver EntityPlayerFieldResolver = new FieldResolver(EntityPlayer);
  static FieldResolver PlayerConnectionFieldResolver = new FieldResolver(PlayerConnection);
  static FieldResolver NetworkManagerFieldResolver = new FieldResolver(NetworkManager);

  static MethodResolver PlayerConnectionMethodResolver = new MethodResolver(PlayerConnection);
  static MethodResolver NetworkManagerMethodResolver = new MethodResolver(NetworkManager);


  static Class<?> PacketPlayOutWorldParticles = classResolver.resolveSilent(
      "net.minecraft.server." + Minecraft.getVersion() + "PacketPlayOutWorldParticles");
  static Class<?> EnumParticle = classResolver.resolveSilent(
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
        sendPacket(player, packetRespawn);
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  static void sendPacket(Player receiver, Object packet) throws ReflectiveOperationException {
    Object handle = Minecraft.getHandle(receiver);
    Object connection = EntityPlayerFieldResolver.resolve("playerConnection").get(handle);
    PlayerConnectionMethodResolver.resolve("sendPacket").invoke(connection, packet);
  }
}
