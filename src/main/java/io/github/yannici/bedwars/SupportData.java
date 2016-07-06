package io.github.yannici.bedwars;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Collections;
import java.util.Enumeration;

import org.bukkit.plugin.Plugin;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SupportData {
  private String pluginVersion;
  private String serverVersion;
  private String bukkitVersion;
  private String serverMode;
  private String serverAddress;
  private String identifier;
  private String port;
  private String plugins;

  public SupportData() {
    this.pluginVersion = Main.getInstance().getDescription().getVersion();
    this.serverVersion = Main.getInstance().getServer().getVersion();
    this.bukkitVersion = Main.getInstance().getServer().getBukkitVersion();
    this.port = String.valueOf(Main.getInstance().getServer().getPort());
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

      Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
      this.serverAddress = "";
      while (networkInterfaces.hasMoreElements()) {
        NetworkInterface networkInterface = (NetworkInterface) networkInterfaces.nextElement();
        Enumeration<InetAddress> inetAddresses = networkInterface.getInetAddresses();
        while (inetAddresses.hasMoreElements()) {
          InetAddress inetAddress = (InetAddress) inetAddresses.nextElement();
          if (!inetAddress.isLoopbackAddress() && !inetAddress.isSiteLocalAddress()
              && !inetAddress.isLinkLocalAddress() && !inetAddress.isMulticastAddress()) {
            this.serverAddress = this.serverAddress + inetAddress.getHostAddress();
            if (inetAddresses.hasMoreElements()) {
              this.serverAddress = this.serverAddress + "; ";
            }
          }
        }
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
