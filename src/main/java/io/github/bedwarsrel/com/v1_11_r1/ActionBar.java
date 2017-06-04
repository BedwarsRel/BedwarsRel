package io.github.bedwarsrel.com.v1_11_r1;

import net.minecraft.server.v1_11_R1.IChatBaseComponent;
import net.minecraft.server.v1_11_R1.IChatBaseComponent.ChatSerializer;
import net.minecraft.server.v1_11_R1.PacketPlayOutChat;
import org.bukkit.ChatColor;
import org.bukkit.craftbukkit.v1_11_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;

public class ActionBar {

  public static void sendActionBar(Player player, String message) {
    String s = ChatColor.translateAlternateColorCodes('&', message.replace("_", " "));
    IChatBaseComponent icbc = ChatSerializer.a("{\"text\": \"" + s + "\"}");
    PacketPlayOutChat bar = new PacketPlayOutChat(icbc, (byte) 2);
    ((CraftPlayer) player).getHandle().playerConnection.sendPacket(bar);
  }

}
