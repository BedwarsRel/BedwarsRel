package io.github.bedwarsrel.BedwarsRel.Commands;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import io.github.bedwarsrel.BedwarsRel.Main;
import io.github.bedwarsrel.BedwarsRel.Utils.ChatWriter;
import io.github.bedwarsrel.BedwarsRel.Utils.HastebinUtility;
import io.github.bedwarsrel.BedwarsRel.Utils.SupportData;
import net.md_5.bungee.api.ChatColor;

public class DebugPasteCommand extends BaseCommand implements ICommand {

  public DebugPasteCommand(Main plugin) {
    super(plugin);
  }

  @Override
  public String getCommand() {
    return "debugpaste";
  }

  @Override
  public String getName() {
    return Main._l("commands.debugpaste.name");
  }

  @Override
  public String getDescription() {
    return Main._l("commands.debugpaste.desc");
  }

  @Override
  public String[] getArguments() {
    return new String[] {};
  }

  @Override
  public boolean execute(CommandSender sender, ArrayList<String> args) {
    if (!super.hasPermission(sender) && !sender.isOp()) {
      return false;
    }

    final Player player = (Player) sender;

    Main.getInstance().getServer().getScheduler().runTaskAsynchronously(Main.getInstance(),
        new Runnable() {
          @Override
          public void run() {
            SupportData supportData = new SupportData();
            try {
              String configYML = HastebinUtility.upload(supportData.getConfigFile());
              String shopYML = HastebinUtility.upload(supportData.getShopConfigFile());
              String latestLOG;
              try {
                latestLOG = HastebinUtility
                    .upload(new File(Main.getInstance().getDataFolder(), "../../logs/latest.log"));
              } catch (IOException ignored) {
                player.sendMessage(
                    ChatWriter.pluginMessage("&clatest.log is too big to be pasted, will ignore"));
                latestLOG = "too big :(";
              }
              StringBuilder b = new StringBuilder();
              b.append(
                  "# Welcome to this paste\n# It is meant to provide us with better information about your problem\n\n# We will start with some informational files\n");
              b.append("files:\n");
              b.append("  config_yml: ").append("\'" + configYML + "\'").append('\n');
              b.append("  shop_yml: ").append("\'" + shopYML + "\'").append('\n');
              b.append("  latest_log: ").append("\'" + latestLOG + "\'").append('\n');
              b.append("\n# General Information\n");
              b.append("version:\n");
              b.append("  plugin: ").append("\'" + supportData.getPluginVersion() + "\'")
                  .append('\n');
              b.append("  server: ").append("\'" + supportData.getServerVersion() + "\'")
                  .append('\n');
              b.append("  bukkit: ").append("\'" + supportData.getBukkitVersion() + "\'")
                  .append('\n');
              b.append("online_mode: ").append(Main.getInstance().getServer().getOnlineMode())
                  .append('\n');
              b.append("plugins:");
              for (String plugin : supportData.getPlugins()) {
                b.append("\n  ").append("\'" + plugin + "\'");
              }
              b.append("\n\n# JVM\n");
              Runtime runtime = Runtime.getRuntime();
              b.append("memory:\n");
              b.append("  free: ").append(runtime.freeMemory()).append('\n');
              b.append("  max: ").append(runtime.maxMemory()).append('\n');
              b.append("java:\n");
              b.append("  specification:\n    version: '")
                  .append(System.getProperty("java.specification.version")).append("'\n");
              b.append("  vendor: '").append(System.getProperty("java.vendor")).append("'\n");
              b.append("  version: '").append(System.getProperty("java.version")).append("'\n");
              b.append("os:\n");
              b.append("  arch: '").append(System.getProperty("os.arch")).append("'\n");
              b.append("  name: '").append(System.getProperty("os.name")).append("'\n");
              b.append("  version: '").append(System.getProperty("os.version")).append("'\n\n");
              b.append("# Please add a link to this file to your your bug report!");
              b.append("\n# https://github.com/BedwarsRel/BedwarsRel/issues");

              String link = HastebinUtility.upload(b.toString());
              player.sendMessage(ChatWriter
                  .pluginMessage(ChatColor.GREEN + "Success! Support data pasted on " + link));
            } catch (IOException e) {
              e.printStackTrace();
            }
          }
        });
    return true;
  }

  @Override
  public String getPermission() {
    return "setup";
  }

}
