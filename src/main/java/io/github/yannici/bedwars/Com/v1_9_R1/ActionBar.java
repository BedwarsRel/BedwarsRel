package io.github.yannici.bedwars.Com.v1_9_R1;

import org.bukkit.ChatColor;
import org.bukkit.craftbukkit.v1_9_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;

import net.minecraft.server.v1_9_R1.IChatBaseComponent;
import net.minecraft.server.v1_9_R1.PacketPlayOutChat;
import net.minecraft.server.v1_9_R1.IChatBaseComponent.ChatSerializer;

public class ActionBar {

  public static void sendActionBar(Player player, String message) {
    String s = ChatColor.translateAlternateColorCodes('&', message.replace("_", " "));
    IChatBaseComponent icbc = ChatSerializer.a("{\"text\": \"" + s + "\"}");
    PacketPlayOutChat bar = new PacketPlayOutChat(icbc, (byte) 2);
    ((CraftPlayer) player).getHandle().playerConnection.sendPacket(bar);
  }

}
