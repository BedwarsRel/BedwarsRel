package io.github.bedwarsrel.BedwarsRel.Localization;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import io.github.bedwarsrel.BedwarsRel.ChatWriter;
import io.github.bedwarsrel.BedwarsRel.Main;
import io.github.bedwarsrel.BedwarsRel.Utils;

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
    String localeKey = locKey;
    if (!locFile.exists()) {

      File folder = new File(Main.getInstance().getDataFolder().getPath() + "/locale/");
      File[] listOfFiles = folder.listFiles();
      for (int i = 0; i < listOfFiles.length; i++) {
        if (listOfFiles[i].isFile() && listOfFiles[i].getName().startsWith(localeKey)
            && listOfFiles[i].getName().endsWith(".yml")) {
          locFile = listOfFiles[i];
          localeKey = listOfFiles[i].getName().substring(0, listOfFiles[i].getName().length() - 4);
          break;
        }
      }
      if (!locFile.exists()) {
        locFile = new File(Main.getInstance().getDataFolder().getPath() + "/locale/"
            + Main.getInstance().getFallbackLocale() + ".yml");
      }
    }

    BufferedReader reader = null;
    try {
      reader = new BufferedReader(new InputStreamReader(new FileInputStream(locFile), "UTF-8"));
      this.load(reader);
    } catch (Exception e) {
      Main.getInstance().getBugsnag().notify(e);
      // no localization file, no translation :D
      Main.getInstance().getServer().getConsoleSender().sendMessage(
          ChatWriter.pluginMessage(ChatColor.RED + "Failed to load localization language!"));
      return;
    } finally {
      if (reader != null) {
        try {
          reader.close();
        } catch (IOException e) {
          Main.getInstance().getBugsnag().notify(e);
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

  public void saveLocales(boolean overwrite) {
    try {
      for (String filename : Utils.getResourceListing(getClass(), "locale/")) {

        File file = new File(Main.getInstance().getDataFolder() + "/locale", filename);
        if (!file.exists() || overwrite) {
          Main.getInstance().saveResource("locale/" + filename, overwrite);
        }
      }
    } catch (Exception e) {
      Main.getInstance().getBugsnag().notify(e);
      e.printStackTrace();
    }
  }
}
