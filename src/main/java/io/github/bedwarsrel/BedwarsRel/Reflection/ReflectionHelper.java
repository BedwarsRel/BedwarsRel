package io.github.bedwarsrel.BedwarsRel.Reflection;

import org.bukkit.entity.Player;
import org.inventivetalent.reflection.minecraft.Minecraft;
import org.inventivetalent.reflection.resolver.ClassResolver;
import org.inventivetalent.reflection.resolver.FieldResolver;
import org.inventivetalent.reflection.resolver.MethodResolver;
import org.inventivetalent.reflection.resolver.ResolverQuery;
import org.inventivetalent.reflection.resolver.minecraft.NMSClassResolver;
import org.inventivetalent.reflection.resolver.minecraft.OBCClassResolver;

public class ReflectionHelper {

  public static ClassResolver classResolver = new ClassResolver();
  public static NMSClassResolver nmsClassResolver = new NMSClassResolver();
  public static OBCClassResolver obcClassResolver = new OBCClassResolver();
  public static Class<?> PlayerConnection = nmsClassResolver.resolveSilent("PlayerConnection");
  public static Class<?> EntityPlayer = nmsClassResolver.resolveSilent("EntityPlayer");
  public static Class<?> NetworkManager = nmsClassResolver.resolveSilent("NetworkManager");
  public static Class<?> Channel = classResolver
      .resolveSilent("net.minecraft.util.io.netty.channel.Channel", "io.netty.channel.Channel");
  public static FieldResolver EntityPlayerFieldResolver = new FieldResolver(EntityPlayer);
  public static FieldResolver PlayerConnectionFieldResolver = new FieldResolver(PlayerConnection);
  public static FieldResolver NetworkManagerFieldResolver = new FieldResolver(NetworkManager);

  public static MethodResolver PlayerConnectionMethodResolver =
      new MethodResolver(PlayerConnection);
  public static MethodResolver NetworkManagerMethodResolver = new MethodResolver(NetworkManager);

  public static void sendPacket(Player receiver, Object packet) throws ReflectiveOperationException {
    Object handle = Minecraft.getHandle(receiver);
    Object connection = EntityPlayerFieldResolver.resolve("playerConnection").get(handle);
    PlayerConnectionMethodResolver.resolve("sendPacket").invoke(connection, packet);
  }

  public static void a(Player receiver, Object packet) throws ReflectiveOperationException {
    Object handle = Minecraft.getHandle(receiver);
    Object connection = EntityPlayerFieldResolver.resolve("playerConnection").get(handle);
    PlayerConnectionMethodResolver.resolve(new ResolverQuery("a", packet.getClass()))
        .invoke(connection, packet);
  }

}
