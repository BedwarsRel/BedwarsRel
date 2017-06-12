package io.github.bedwarsrel.commands;

import io.github.bedwarsrel.BedwarsRel;
import io.github.bedwarsrel.utils.ChatWriter;
import io.github.bedwarsrel.utils.HastebinUtility;
import io.github.bedwarsrel.utils.SupportData;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.command.CommandSender;

public class DebugPasteCommand extends BaseCommand implements ICommand {

  public DebugPasteCommand(BedwarsRel plugin) {
    super(plugin);
  }

  @Override
  public boolean execute(final CommandSender sender, ArrayList<String> args) {
    if (!super.hasPermission(sender) && !sender.isOp()) {
      return false;
    }

    BedwarsRel.getInstance().getServer().getScheduler()
        .runTaskAsynchronously(BedwarsRel.getInstance(),
            new Runnable() {
              @Override
              public void run() {
                try {
                  String configYML = HastebinUtility.upload(SupportData.getConfigFile());
                  String shopYML = HastebinUtility.upload(SupportData.getShopConfigFile());
                  String latestLOG;
                  try {
                    latestLOG = HastebinUtility
                        .upload(new File(BedwarsRel.getInstance().getDataFolder(),
                            "../../logs/latest.log"));
                  } catch (IOException ignored) {
                    sender.sendMessage(
                        ChatWriter
                            .pluginMessage("&clatest.log is too big to be pasted, will ignore"));
                    latestLOG = "too big :(";
                  }
                  StringBuilder b = new StringBuilder();
                  b.append(
                      "# Welcome to this paste\n# It is meant to provide us with better information about your problem\n\n# We will start with some informational files\n");
                  b.append("files:\n");
                  b.append("  config_yml: ").append("\"" + configYML + "\"").append('\n');
                  b.append("  shop_yml: ").append("\"" + shopYML + "\"").append('\n');
                  b.append("  latest_log: ").append("\"" + latestLOG + "\"").append('\n');
                  b.append("\n# General Information\n");
                  b.append("version:\n");
                  b.append("  plugin: ")
                      .append("\"" + BedwarsRel.getInstance().getDescription().getVersion() + "\"");
                  if (SupportData.getPluginVersionArray().length == 3
                      && !SupportData.getPluginVersionArray()[1].equals("unknown")) {
                    b.append("(https://github.com/BedwarsRel/BedwarsRel/tree/"
                        + SupportData.getPluginVersionArray()[1] + ")");
                  }
                  b.append('\n');
                  b.append("  server: ").append("\"" + SupportData.getServerVersion() + "\"")
                      .append('\n');
                  b.append("  bukkit: ").append("\"" + SupportData.getBukkitVersion() + "\"")
                      .append('\n');
                  b.append("online_mode: ")
                      .append(BedwarsRel.getInstance().getServer().getOnlineMode())
                      .append('\n');
                  b.append("plugins("
                      + BedwarsRel.getInstance().getServer().getPluginManager().getPlugins().length
                      + "):");
                  for (String plugin : SupportData.getPlugins()) {
                    b.append("\n  ").append("\"" + plugin + "\"");
                  }
                  b.append("\n\n# JVM\n");
                  Runtime runtime = Runtime.getRuntime();
                  b.append("memory:\n");
                  b.append("  free: ").append(runtime.freeMemory() / 1000000).append(" MB\n");
                  b.append("  max: ").append(runtime.maxMemory() / 1000000).append(" MB\n");
                  b.append("java:\n");
                  b.append("  specification:\n    version: \"")
                      .append(System.getProperty("java.specification.version")).append("\"\n");
                  b.append("  vendor: \"").append(System.getProperty("java.vendor")).append("\"\n");
                  b.append("  version: \"").append(System.getProperty("java.version"))
                      .append("\"\n");
                  b.append("os:\n");
                  b.append("  arch: \"").append(System.getProperty("os.arch")).append("\"\n");
                  b.append("  name: \"").append(System.getProperty("os.name")).append("\"\n");
                  b.append("  version: \"").append(System.getProperty("os.version"))
                      .append("\"\n\n");
                  b.append("# Date\n");
                  b.append("date: ").append(new Date())
                      .append('\n');
                  b.append("\n# Please add a link to this file to your bug report!");
                  b.append("\n# https://github.com/BedwarsRel/BedwarsRel/issues");

                  String link = HastebinUtility.upload(b.toString());
                  sender.sendMessage(ChatWriter
                      .pluginMessage(ChatColor.GREEN + "Success! Support data pasted on " + link));
                } catch (IOException e) {
                  e.printStackTrace();
                }
              }
            });
    return true;
  }

  @Override
  public String[] getArguments() {
    return new String[]{};
  }

  @Override
  public String getCommand() {
    return "debugpaste";
  }

  @Override
  public String getDescription() {
    return BedwarsRel._l("commands.debugpaste.desc");
  }

  @Override
  public String getName() {
    return BedwarsRel._l("commands.debugpaste.name");
  }

  @Override
  public String getPermission() {
    return "setup";
  }

}
