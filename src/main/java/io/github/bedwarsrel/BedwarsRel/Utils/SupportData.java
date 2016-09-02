package io.github.bedwarsrel.BedwarsRel.Utils;

import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Collections;

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
  private String plugins;

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

    this.plugins = "";

    Plugin[] plugins = Main.getInstance().getServer().getPluginManager().getPlugins();
    int pluginsAmount = plugins.length;
    int pluginsCount = 0;
    for (Plugin plugin : plugins) {
      this.plugins =
          this.plugins + plugin.getName() + " (" + plugin.getDescription().getVersion() + ")";
      if (pluginsCount < pluginsAmount - 1) {
        this.plugins = this.plugins + ", ";
      }
      pluginsCount++;
    }

  }
}
