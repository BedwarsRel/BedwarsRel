package io.github.yannici.bedwars.Com.Fallback;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import io.github.yannici.bedwars.ChatWriter;
import io.github.yannici.bedwars.Main;
import io.github.yannici.bedwars.Utils;

public class PerformRespawnRunnable extends BukkitRunnable {

	private Player player = null;

	public PerformRespawnRunnable(Player player) {
		this.player = player;
	}

	@Override
	public void run() {
		try {
			Class<?> enumClientCommand = Main.getInstance().getMinecraftServerClass("EnumClientCommand");
			Class<?> packetClass = Main.getInstance().getMinecraftServerClass("PacketPlayInClientCommand");
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
					new Class[] { enumClientCommand }, new Object[] { respawnObject });
			Object craftPlayer = Utils.getCraftPlayer(this.player);
			Class<?> craftPlayerClass = Main.getInstance().getCraftBukkitClass("entity.CraftPlayer");
			Field playerConnectionField = craftPlayerClass.getField("playerConnection");
			playerConnectionField.setAccessible(true);

			// player connection method
			Object playerConnection = playerConnectionField.get(craftPlayer);
			Class<?> playerConnectionClass = Main.getInstance().getMinecraftServerClass("PlayerConnection");
			Method aMethod = playerConnectionClass.getMethod("a", new Class[] { packetClass });
			aMethod.setAccessible(true);

			// invoke respawn
			aMethod.invoke(playerConnection, new Object[] { packetPlayInClientCommand });
		} catch (Exception ex) {
			Main.getInstance().getServer().getConsoleSender().sendMessage(
					ChatWriter.pluginMessage(ChatColor.RED + "Plugin not compatible with your server version!"));
		}
	}

	private Object getPacketObject(String packetName, Class<?>[] constructorClasses, Object[] constructorParams) {
		try {
			Class<?> clazz = Main.getInstance().getMinecraftServerClass(packetName);
			if (clazz == null) {
				throw new Exception("packet not found");
			}

			Constructor<?> constr = clazz.getDeclaredConstructor(constructorClasses);
			if (constr == null) {
				throw new Exception("constructor not found");
			}

			constr.setAccessible(true);
			return constr.newInstance(constructorParams);
		} catch (Exception ex) {
			Main.getInstance().getServer().getConsoleSender().sendMessage(ChatWriter
					.pluginMessage(ChatColor.RED + "Couldn't catch packet class " + ChatColor.YELLOW + packetName));
		}

		return null;
	}

}
