package io.github.bedwarsrel.BedwarsRel.Localization;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import io.github.bedwarsrel.BedwarsRel.ChatWriter;
import io.github.bedwarsrel.BedwarsRel.Main;

public class LocalizationConfig extends YamlConfiguration {

  @SuppressWarnings("unchecked")
  public String getPlayerLocale(Player player) {
    try {
      Method getHandleMethod = Main.getInstance().getCraftBukkitClass("entity.CraftPlayer")
          .getMethod("getHandle", new Class[] {});
      getHandleMethod.setAccessible(true);
      Object nmsPlayer = getHandleMethod.invoke(player, new Object[] {});

      Field localeField = nmsPlayer.getClass().getDeclaredField("locale");
      localeField.setAccessible(true);
      return localeField.get(nmsPlayer).toString().split("_")[0].toLowerCase();
    } catch (Exception ex) {
      Main.getInstance().getBugsnag().notify(ex);
      return Main.getInstance().getFallbackLocale();
    }
  }

  public void loadLocale(String locKey, boolean isFallback) {
    File locFile =
        new File(Main.getInstance().getDataFolder().getPath() + "/locale/" + locKey + ".yml");
    BufferedReader reader = null;
    InputStream inputStream = null;
    if (locFile.exists()) {
      try {
        inputStream = new FileInputStream(locFile);
      } catch (FileNotFoundException e) {
        // NO ERROR
      }
      Main.getInstance().getServer().getConsoleSender().sendMessage(ChatWriter
          .pluginMessage(ChatColor.GOLD + "Using your custom locale \"" + locKey + "\"."));
    } else {
      if (inputStream == null) {
        inputStream = Main.getInstance().getResource("locale/" + locKey + ".yml");
      }
      if (inputStream == null) {
        Main.getInstance().getServer().getConsoleSender()
            .sendMessage(ChatWriter.pluginMessage(ChatColor.GOLD + "The locale \"" + locKey
                + "\" defined in your config is not available. Using fallback locale: "
                + Main.getInstance().getFallbackLocale()));
        inputStream = Main.getInstance()
            .getResource("locale/" + Main.getInstance().getFallbackLocale() + ".yml");
      }
    }
    try {
      reader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
      this.load(reader);
    } catch (Exception e) {
      Main.getInstance().getServer().getConsoleSender().sendMessage(
          ChatWriter.pluginMessage(ChatColor.RED + "Failed to load localization language!"));
      return;
    } finally {
      if (reader != null) {
        try {
          reader.close();
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
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
      return "LOCALE_NOT_FOUND";
    }

    return ChatColor.translateAlternateColorCodes('&', super.getString(path));
  }

  public String getFormatString(String path, Map<String, String> params) {
    String str = this.getString(path);
    for (String key : params.keySet()) {
      str = str.replace("$" + key.toLowerCase() + "$", params.get(key));
    }

    return ChatColor.translateAlternateColorCodes('&', str);
  }

}
