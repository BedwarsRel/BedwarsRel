package io.github.yannici.bedwars;

import org.bukkit.ChatColor;

public class ChatWriter {

	public ChatWriter() {
		super();
	}

	public static String pluginMessage(String str) {
		return ChatColor.translateAlternateColorCodes('&',
				Main.getInstance().getConfig().getString("chat-prefix",
						ChatColor.GRAY + "[" + ChatColor.AQUA + "BedWars" + ChatColor.GRAY + "]"))
				+ " " + ChatColor.WHITE + str;
	}

}
