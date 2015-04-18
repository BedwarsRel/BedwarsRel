package io.github.yannici.bedwars;

import org.bukkit.ChatColor;

public class ChatWriter {

	public ChatWriter() {
		super();
	}

	public static String pluginMessage(String str) {
		return ChatColor.GOLD + "[Bedwars] "
				+ ChatColor.WHITE + str;
	}

}
