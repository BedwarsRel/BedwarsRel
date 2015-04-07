package io.github.yannici.bedwarsreloaded.Localization;

import io.github.yannici.bedwarsreloaded.ChatWriter;
import io.github.yannici.bedwarsreloaded.Main;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.Map;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

public class LocalizationConfig extends YamlConfiguration {
	
	private LocalizationConfig fallback = null;
	
	@SuppressWarnings("unchecked")
	public String getPlayerLocale(Player player) {
    	try {
    		Method getHandleMethod = Main.getInstance().getCraftBukkitClass("entity.CraftPlayer").getMethod("getHandle", new Class[]{});
        	getHandleMethod.setAccessible(true);
        	Object nmsPlayer = getHandleMethod.invoke(player, new Object[]{});
        	
        	Field localeField = nmsPlayer.getClass().getDeclaredField("locale");
        	localeField.setAccessible(true);
        	return localeField.get(nmsPlayer).toString().split("_")[0].toLowerCase();
    	} catch(Exception ex) {
    		return Main.getInstance().getFallbackLocale();
    	}
    }
	
	public void loadLocale(String locKey, boolean isFallback) {
		URL url = getClass().getResource("/locale/de.yml");
		if(url == null) {
			url = getClass().getResource("/locale/" + Main.getInstance().getFallbackLocale() + ".yml");
		}
		
		File file = new File(url.toString());
		try {
			this.load(file);
		} catch (Exception e) {
			Main.getInstance().getServer().getConsoleSender().sendMessage(ChatWriter.pluginMessage(ChatColor.RED + "Failed to load localization language!"));
			return;
		}
		
		if(!isFallback) {
			// Fallback load
			this.fallback = new LocalizationConfig();
			this.fallback.loadLocale(locKey, true);
		}
	}
	
	@Override
	public Object get(String path) {
		return this.getString(path);
	}
	
	public Object get(String path, Map<String, String> params) {
		return this.getFormatString(path, params);
	}
	
	@Override
	public String getString(String path) {
		if(!this.contains(path)) {
			if(this.fallback == null) {
				return "LOCALE_NOT_FOUND";
			}
			
			return this.fallback.getString(path);
		}
		
		return ChatColor.translateAlternateColorCodes('&', super.getString(path));
	} 
	
	public String getFormatString(String path, Map<String, String> params) {
		String str = this.getString(path);
		for(String key : params.keySet()) {
			str.replace("%" + key.toLowerCase() + "%", params.get(key));
		}
		
		return ChatColor.translateAlternateColorCodes('&', str);
	}
}
