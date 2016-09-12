package io.github.bedwarsrel.BedwarsRel.Reflection;

import org.bukkit.entity.Player;
import org.inventivetalent.reflection.minecraft.Minecraft;
import org.inventivetalent.reflection.resolver.ClassResolver;
import org.inventivetalent.reflection.resolver.ConstructorResolver;
import org.inventivetalent.reflection.resolver.FieldResolver;
import org.inventivetalent.reflection.resolver.MethodResolver;
import org.inventivetalent.reflection.resolver.ResolverQuery;
import org.inventivetalent.reflection.resolver.minecraft.NMSClassResolver;

public class PlayerPacketSender {


  protected static Object EMPTY_COMPONENT;

  static ClassResolver classResolver = new ClassResolver();
  static NMSClassResolver nmsClassResolver = new NMSClassResolver();

  static Class<?> IChatBaseComponent = nmsClassResolver.resolveSilent("IChatBaseComponent");
  static Class<?> ChatSerializer =
      nmsClassResolver.resolveSilent("ChatSerializer", "IChatBaseComponent$ChatSerializer");
  static Class<?> nmsPacketPlayOutChat = nmsClassResolver.resolveSilent("PacketPlayOutChat");

  static Class<?> PacketPlayOutTitle = classResolver
      .resolveSilent("net.minecraft.server." + Minecraft.getVersion() + "PacketPlayOutTitle");
  static Class<?> EnumTitleAction = classResolver.resolveSilent(
      "net.minecraft.server." + Minecraft.getVersion() + "PacketPlayOutTitle$EnumTitleAction",
      "net.minecraft.server." + Minecraft.getVersion() + "EnumTitleAction");

  static Class<?> PacketPlayInClientCommand = classResolver.resolveSilent(
      "net.minecraft.server." + Minecraft.getVersion() + "PacketPlayInClientCommand");
  static Class<?> EnumClientCommand = classResolver.resolveSilent(
      "net.minecraft.server." + Minecraft.getVersion()
          + "PacketPlayInClientCommand$EnumClientCommand",
      "net.minecraft.server." + Minecraft.getVersion() + "EnumClientCommand");

  static Class<?> PlayerConnection = nmsClassResolver.resolveSilent("PlayerConnection");
  static Class<?> EntityPlayer = nmsClassResolver.resolveSilent("EntityPlayer");
  static Class<?> NetworkManager = nmsClassResolver.resolveSilent("NetworkManager");
  static Class<?> Channel = classResolver
      .resolveSilent("net.minecraft.util.io.netty.channel.Channel", "io.netty.channel.Channel");

  static ConstructorResolver PacketTitleConstructorResolver =
      new ConstructorResolver(PacketPlayOutTitle);

  static ConstructorResolver PacketClientCommandConstructorResolver =
      new ConstructorResolver(PacketPlayInClientCommand);

  static FieldResolver EntityPlayerFieldResolver = new FieldResolver(EntityPlayer);
  static FieldResolver PlayerConnectionFieldResolver = new FieldResolver(PlayerConnection);
  static FieldResolver NetworkManagerFieldResolver = new FieldResolver(NetworkManager);

  static MethodResolver PlayerConnectionMethodResolver = new MethodResolver(PlayerConnection);
  static MethodResolver ChatSerializerMethodResolver = new MethodResolver(ChatSerializer);
  static MethodResolver NetworkManagerMethodResolver = new MethodResolver(NetworkManager);

  public static void sendRawMessage(Player player, String json, int position) {
    try {
      Object serialized = serialize(json);
      Object packet =
          nmsPacketPlayOutChat.getConstructor(new Class[] {IChatBaseComponent, byte.class})
              .newInstance(new Object[] {serialized, (byte) position});
      if (packet != null) {
        sendPacket(player, packet);
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public static void sendTitle(Player player, String json) {
    try {
      Object serialized = serialize(json);
      Object packetTitle =
          PacketTitleConstructorResolver.resolve(new Class[] {EnumTitleAction, IChatBaseComponent})
              .newInstance(EnumTitleAction.getEnumConstants()[0], serialized);
      sendPacket(player, packetTitle);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public static void sendTitle(Player player, String json, double fadeIn, double stay,
      double fadeOut) {
    sendTitleTimings(player, fadeIn, stay, fadeOut);
    sendTitle(player, json);
  }

  public static void sendSubTitle(Player player, String json) {
    try {
      Object serialized = serialize(json);
      Object packetTitle =
          PacketTitleConstructorResolver.resolve(new Class[] {EnumTitleAction, IChatBaseComponent})
              .newInstance(EnumTitleAction.getEnumConstants()[1], serialized);
      sendPacket(player, packetTitle);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public static void sendSubTitle(Player player, String json, double fadeIn, double stay,
      double fadeOut) {
    sendTitleTimings(player, fadeIn, stay, fadeOut);
    sendSubTitle(player, json);
  }

  public static void sendTitleTimings(Player player, double fadeIn, double stay, double fadeOut) {
    try {
      Object packetTitle =
          PacketTitleConstructorResolver.resolve(new Class[] {int.class, int.class, int.class})
              .newInstance((int) Math.round(fadeIn * 20), (int) Math.round(stay * 20),
                  (int) Math.round(fadeOut * 20));
      sendPacket(player, packetTitle);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public static void clearTitle(Player player) {
    try {
      Object packetTitle =
          PacketTitleConstructorResolver.resolve(new Class[] {EnumTitleAction, IChatBaseComponent})
              .newInstance(EnumTitleAction.getEnumConstants()[3], null);
      sendPacket(player, packetTitle);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public static void resetTitle(Player player) {
    try {
      Object packetTitle =
          PacketTitleConstructorResolver.resolve(new Class[] {EnumTitleAction, IChatBaseComponent})
              .newInstance(EnumTitleAction.getEnumConstants()[4], null);
      sendPacket(player, packetTitle);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public static void respawnPlayer(Player player) {
    try {
      Object packetRespawn =
          PacketClientCommandConstructorResolver.resolve(new Class[] {EnumClientCommand})
              .newInstance(EnumClientCommand.getEnumConstants()[0]);
      a(player, packetRespawn);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  static void a(Player receiver, Object packet) throws ReflectiveOperationException {
    Object handle = Minecraft.getHandle(receiver);
    Object connection = EntityPlayerFieldResolver.resolve("playerConnection").get(handle);
    PlayerConnectionMethodResolver.resolve(new ResolverQuery("a", packet.getClass()))
        .invoke(connection, packet);
  }

  static void sendPacket(Player receiver, Object packet) throws ReflectiveOperationException {
    Object handle = Minecraft.getHandle(receiver);
    Object connection = EntityPlayerFieldResolver.resolve("playerConnection").get(handle);
    PlayerConnectionMethodResolver.resolve("sendPacket").invoke(connection, packet);
  }

  static Object serialize(String json) throws ReflectiveOperationException {
    return ChatSerializerMethodResolver.resolve(new ResolverQuery("a", String.class)).invoke(null,
        json);
  }

  public static String toJson(String str) {
    if (str.startsWith("{") && str.endsWith("}"))
      return str;
    return "{\"text\":\"" + str + "\"}";
  }

}
