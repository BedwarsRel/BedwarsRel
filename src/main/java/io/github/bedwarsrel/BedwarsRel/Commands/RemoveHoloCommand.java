package io.github.bedwarsrel.BedwarsRel.Commands;

import io.github.bedwarsrel.BedwarsRel.Main;
import io.github.bedwarsrel.BedwarsRel.Utils.ChatWriter;
import java.util.ArrayList;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;

public class RemoveHoloCommand extends BaseCommand implements ICommand {

  public RemoveHoloCommand(Main plugin) {
    super(plugin);
  }

  @Override
  public boolean execute(CommandSender sender, ArrayList<String> args) {
    if (!super.hasPermission(sender)) {
      return false;
    }

    final Player player = (Player) sender;
    player.setMetadata("bw-remove-holo", new FixedMetadataValue(Main.getInstance(), true));
    if (Main.getInstance().getHolographicInteractor().getType()
        .equalsIgnoreCase("HolographicDisplays")) {
      player.sendMessage(
          ChatWriter
              .pluginMessage(ChatColor.GREEN + Main._l(player, "commands.removeholo.explain")));

    } else if (Main.getInstance().getHolographicInteractor().getType()
        .equalsIgnoreCase("HologramAPI")) {

      for (Location location : Main.getInstance().getHolographicInteractor()
          .getHologramLocations()) {
        if (player.getEyeLocation().getBlockX() == location.getBlockX()
            && player.getEyeLocation().getBlockY() == location.getBlockY()
            && player.getEyeLocation().getBlockZ() == location.getBlockZ()) {
          Main.getInstance().getHolographicInteractor().onHologramTouch(player, location);
        }
      }
      Main.getInstance().getServer().getScheduler().runTaskLater(Main.getInstance(),
          new Runnable() {

            @Override
            public void run() {
              if (player.hasMetadata("bw-remove-holo")) {
                player.removeMetadata("bw-remove-holo", Main.getInstance());
              }
            }

          }, 10L * 20L);

    }
    return true;
  }

  @Override
  public String[] getArguments() {
    return new String[]{};
  }

  @Override
  public String getCommand() {
    return "removeholo";
  }

  @Override
  public String getDescription() {
    return Main._l("commands.removeholo.desc");
  }

  @Override
  public String getName() {
    return Main._l("commands.removeholo.name");
  }

  @Override
  public String getPermission() {
    return "setup";
  }

}
