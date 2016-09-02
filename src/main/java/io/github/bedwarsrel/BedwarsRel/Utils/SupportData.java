package io.github.bedwarsrel.BedwarsRel.Utils;

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

import io.github.bedwarsrel.BedwarsRel.Main;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SupportData {
  private String pluginVersion;
  private String serverVersion;
  private String bukkitVersion;
  private String serverMode;
  private String identifier;
  private ArrayList<String> plugins;
  private ArrayList<String> javaInformation;

  public SupportData() {
    this.pluginVersion = Main.getInstance().getDescription().getVersion();
    this.serverVersion = Main.getInstance().getServer().getVersion();
    this.bukkitVersion = Main.getInstance().getServer().getBukkitVersion();
    if (Main.getInstance().isBungee()) {
      this.serverMode = "BungeeCord";
    } else {
      this.serverMode = "Single Instance";
    }

    try {
      for (NetworkInterface ni : Collections.list(NetworkInterface.getNetworkInterfaces())) {
        byte[] adr = ni.getHardwareAddress();
        if (adr == null || adr.length != 6)
          continue;
        String mac = String.format("%02X:%02X:%02X:%02X:%02X:%02X", adr[0], adr[1], adr[2], adr[3],
            adr[4], adr[5]);
        this.identifier = mac;
      }
    } catch (SocketException e) {
      e.printStackTrace();
    }

    this.plugins = new ArrayList<String>();

    Plugin[] plugins = Main.getInstance().getServer().getPluginManager().getPlugins();
    for (Plugin plugin : plugins) {
      this.plugins.add(plugin.getName() + " (" + plugin.getDescription().getVersion() + ")");
    }

    this.javaInformation = new ArrayList<String>();
    Runtime runtime = Runtime.getRuntime();

    this.javaInformation.add("memory.free: " + runtime.freeMemory());
    this.javaInformation.add("memory.max: " + runtime.maxMemory());
    this.javaInformation
        .add("java.specification.version: " + System.getProperty("java.specification.version"));
    this.javaInformation.add("java.vendor: " + System.getProperty("java.vendor"));
    this.javaInformation.add("java.version: " + System.getProperty("java.version"));
    this.javaInformation.add("os.arch: " + System.getProperty("os.arch"));
    this.javaInformation.add("os.name: " + System.getProperty("os.name"));
    this.javaInformation.add("os.version:  " + System.getProperty("os.version"));
    this.javaInformation.add("os.name: " + System.getProperty("os.name"));
    this.javaInformation.add("os.name: " + System.getProperty("os.name"));
  }

  public File getConfigFile() {
    File configFile = new File(Main.getInstance().getDataFolder(), "config.yml");
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

  public File getShopConfigFile() {
    return new File(Main.getInstance().getDataFolder(), "shop.yml");
  }
}
