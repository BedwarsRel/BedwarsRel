package io.github.bedwarsrel.utils;

import io.github.bedwarsrel.BedwarsRel;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Collections;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

public class SupportData {

  public static String getBukkitVersion() {
    return BedwarsRel.getInstance().getServer().getBukkitVersion();
  }

  public static File getConfigFile() {
    File configFile = new File(BedwarsRel.getInstance().getDataFolder(), "config.yml");
    File tmp = null;

    YamlConfiguration config = new YamlConfiguration();
    try {
      BufferedReader reader =
          new BufferedReader(new InputStreamReader(new FileInputStream(configFile), "UTF-8"));
      config.load(reader);
    } catch (Exception e) {
      e.printStackTrace();
    }

    config.set("database.user", "***");
    config.set("database.password", "***");

    try {
      tmp = File.createTempFile("bedwarsrel-supportdata-config", null);
      config.save(tmp);
    } catch (IOException e) {
      e.printStackTrace();
    }

    return tmp;
  }

  public static String getIdentifier() {
    String identifier = "";
    try {
      for (NetworkInterface ni : Collections.list(NetworkInterface.getNetworkInterfaces())) {
        byte[] adr = ni.getHardwareAddress();
        if (adr == null || adr.length != 6) {
          continue;
        }
        String mac = String.format("%02X:%02X:%02X:%02X:%02X:%02X", adr[0], adr[1], adr[2], adr[3],
            adr[4], adr[5]);
        identifier = mac;
      }
    } catch (SocketException e) {
      e.printStackTrace();
    }
    return identifier;
  }

  public static ArrayList<String> getJavaInformation() {
    ArrayList<String> javaInformation = new ArrayList<String>();
    Runtime runtime = Runtime.getRuntime();
    javaInformation.add("memory.free: " + runtime.freeMemory());
    javaInformation.add("memory.max: " + runtime.maxMemory());
    javaInformation
        .add("java.specification.version: " + System.getProperty("java.specification.version"));
    javaInformation.add("java.vendor: " + System.getProperty("java.vendor"));
    javaInformation.add("java.version: " + System.getProperty("java.version"));
    javaInformation.add("os.arch: " + System.getProperty("os.arch"));
    javaInformation.add("os.name: " + System.getProperty("os.name"));
    javaInformation.add("os.version:  " + System.getProperty("os.version"));
    javaInformation.add("os.name: " + System.getProperty("os.name"));
    javaInformation.add("os.name: " + System.getProperty("os.name"));
    return javaInformation;
  }

  public static String getPluginVersion() {
    if (getPluginVersionArray().length > 1) {
      return getPluginVersionArray()[0];
    }
    return BedwarsRel.getInstance().getDescription().getVersion();
  }

  public static String[] getPluginVersionArray() {
    return BedwarsRel.getInstance().getDescription().getVersion().split("-");
  }

  public static String getPluginVersionBuild() {
    if (getPluginVersionArray().length == 3) {
      return getPluginVersionArray()[1];
    }
    return "unknown";
  }

  public static String getPluginVersionType() {
    if (BedwarsRel.getInstance().getDescription().getVersion().contains("SNAPSHOT")) {
      return "SNAPSHOT";
    }
    return "RELEASE";
  }

  public static ArrayList<String> getPlugins() {
    ArrayList<String> pluginList = new ArrayList<String>();
    Plugin[] plugins = BedwarsRel.getInstance().getServer().getPluginManager().getPlugins();
    for (Plugin plugin : plugins) {
      pluginList.add(plugin.getName() + " (" + plugin.getDescription().getVersion() + ")");
    }
    return pluginList;
  }

  public static String getServerMode() {
    if (BedwarsRel.getInstance().isBungee()) {
      return "BungeeCord";
    }
    return "Single Instance";
  }

  public static String getServerVersion() {
    return BedwarsRel.getInstance().getServer().getVersion();
  }

  public static File getShopConfigFile() {
    return new File(BedwarsRel.getInstance().getDataFolder(), "shop.yml");
  }
}
