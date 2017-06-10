package io.github.bedwarsrel.com.fallback;

import io.github.bedwarsrel.BedwarsRel;
import io.github.bedwarsrel.utils.ChatWriter;
import io.github.bedwarsrel.utils.Utils;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class PerformRespawnRunnable extends BukkitRunnable {

  private Player player = null;

  public PerformRespawnRunnable(Player player) {
    this.player = player;
  }

  private Object getPacketObject(String packetName, Class<?>[] constructorClasses,
      Object[] constructorParams) {
    try {
      Class<?> clazz = BedwarsRel.getInstance().getMinecraftServerClass(packetName);
      if (clazz == null) {
        // TODO
      }

      Constructor<?> constr = clazz.getDeclaredConstructor(constructorClasses);
      if (constr == null) {
        // TODO
      }

      constr.setAccessible(true);
      return constr.newInstance(constructorParams);
    } catch (Exception ex) {
      BedwarsRel.getInstance().getBugsnag().notify(ex);
      BedwarsRel.getInstance().getServer().getConsoleSender().sendMessage(ChatWriter.pluginMessage(
          ChatColor.RED + "Couldn't catch packet class " + ChatColor.YELLOW + packetName));
    }

    return null;
  }

  @Override
  public void run() {
    try {
      Class<?> enumClientCommand = BedwarsRel.getInstance()
          .getMinecraftServerClass("EnumClientCommand");
      Class<?> packetClass =
          BedwarsRel.getInstance().getMinecraftServerClass("PacketPlayInClientCommand");
      if (enumClientCommand == null) {
        for (Class<?> clazz : packetClass.getDeclaredClasses()) {
          if (clazz.getSimpleName().equals("EnumClientCommand")) {
            enumClientCommand = clazz;
            break;
          }
        }
      }

      @SuppressWarnings("unchecked")
      List<Object> constants = (List<Object>) Arrays.asList(enumClientCommand.getEnumConstants());
      Object respawnObject = null;

      for (Object constant : constants) {
        if (constant.toString().equals("PERFORM_RESPAWN")) {
          respawnObject = constant;
          break;
        }
      }

      // Create packet instance
      Object packetPlayInClientCommand = this.getPacketObject("PacketPlayInClientCommand",
          new Class[]{enumClientCommand}, new Object[]{respawnObject});
      Object craftPlayer = Utils.getCraftPlayer(this.player);
      Class<?> craftPlayerClass = BedwarsRel.getInstance()
          .getCraftBukkitClass("entity.CraftPlayer");
      Field playerConnectionField = craftPlayerClass.getField("playerConnection");
      playerConnectionField.setAccessible(true);

      // player connection method
      Object playerConnection = playerConnectionField.get(craftPlayer);
      Class<?> playerConnectionClass =
          BedwarsRel.getInstance().getMinecraftServerClass("PlayerConnection");
      Method aMethod = playerConnectionClass.getMethod("a", new Class[]{packetClass});
      aMethod.setAccessible(true);

      // invoke respawn
      aMethod.invoke(playerConnection, new Object[]{packetPlayInClientCommand});
    } catch (Exception ex) {
      BedwarsRel.getInstance().getBugsnag().notify(ex);
      BedwarsRel.getInstance().getServer().getConsoleSender().sendMessage(ChatWriter
          .pluginMessage(ChatColor.RED + "Plugin not compatible with your server version!"));
    }
  }

}
