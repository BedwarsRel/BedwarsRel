package io.github.yannici.bedwars.Localization;

import io.github.yannici.bedwars.ChatWriter;
import io.github.yannici.bedwars.Main;
import io.github.yannici.bedwars.Utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

public class LocalizationConfig extends YamlConfiguration {

	private LocalizationConfig fallback = null;

	@SuppressWarnings("unchecked")
	public String getPlayerLocale(Player player) {
		try {
			Method getHandleMethod = Main.getInstance()
					.getCraftBukkitClass("entity.CraftPlayer")
					.getMethod("getHandle", new Class[] {});
			getHandleMethod.setAccessible(true);
			Object nmsPlayer = getHandleMethod.invoke(player, new Object[] {});

			Field localeField = nmsPlayer.getClass().getDeclaredField("locale");
			localeField.setAccessible(true);
			return localeField.get(nmsPlayer).toString().split("_")[0]
					.toLowerCase();
		} catch (Exception ex) {
			return Main.getInstance().getFallbackLocale();
		}
	}

	public void loadLocale(String locKey, boolean isFallback) {
		File locFile = new File(Main.getInstance().getDataFolder().getPath()
				+ "/locale/" + locKey + ".yml");
		if (!locFile.exists()) {
			locFile = new File(Main.getInstance().getDataFolder().getPath()
					+ "/locale/" + Main.getInstance().getFallbackLocale()
					+ ".yml");
		}
		
		try {
			this.load(locFile);
		} catch (Exception e) {
			// no localization file, no translation :D
			Main.getInstance()
					.getServer()
					.getConsoleSender()
					.sendMessage(
							ChatWriter.pluginMessage(ChatColor.RED
									+ "Failed to load localization language!"));
			return;
		}

		if (!isFallback) {
			// Fallback load
			this.fallback = new LocalizationConfig();
			this.fallback.loadLocale(Main.getInstance().getFallbackLocale(),
					true);
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
		if (super.get(path) == null) {
			if (this.fallback == null) {
				return "LOCALE_NOT_FOUND";
			}

			return this.fallback.getString(path);
		}

		return ChatColor.translateAlternateColorCodes('&',
						super.getString(path));
	}

	public String getFormatString(String path, Map<String, String> params) {
		String str = this.getString(path);
		for (String key : params.keySet()) {
			str = str.replace("$" + key.toLowerCase() + "$", params.get(key));
		}

		return ChatColor.translateAlternateColorCodes('&', str);
	}
	
	private void compareLocale(String filename) throws IOException {
		InputStream stream = Main.getInstance().getResource("locale/" + filename);
		InputStreamReader isr = new InputStreamReader(stream);
		BufferedReader referenceReader = new BufferedReader(isr);
		YamlConfiguration referenceConfig = YamlConfiguration.loadConfiguration(referenceReader);
		File currentFile = new File(Main.getInstance().getDataFolder() + "/locale", filename);
		
		BufferedReader currentReader = new BufferedReader(new InputStreamReader(new FileInputStream(currentFile)));
		YamlConfiguration currentConfig = YamlConfiguration.loadConfiguration(currentReader);
		
		for(String key : referenceConfig.getKeys(true)) {
			if(currentConfig.contains(key)) {
				continue;
			}
			
			currentConfig.set(key, referenceConfig.get(key));
		}
		
		for(String key : currentConfig.getKeys(true)) {
			if(referenceConfig.contains(key)) {
				continue;
			}
			
			currentConfig.set(key, null);
		}
		
		referenceReader.close();
		isr.close();
		stream.close();
		currentReader.close();
		
		currentConfig.save(currentFile);
	}

	public void saveLocales(boolean overwrite) {
		try {
			for (String filename : Utils.getResourceListing(getClass(),
					"locale/")) {
				
				File file = new File(Main.getInstance().getDataFolder() + "/locale", filename);
				if(!file.exists() || overwrite) {
					Main.getInstance()
					.saveResource("locale/" + filename, overwrite);
				}
				
				this.compareLocale(filename);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
