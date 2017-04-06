package io.github.bedwarsrel.BedwarsRel.Commands;

import com.google.common.collect.ImmutableMap;
import io.github.bedwarsrel.BedwarsRel.Main;
import io.github.bedwarsrel.BedwarsRel.Utils.ChatWriter;
import java.util.ArrayList;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class AddHoloCommand extends BaseCommand implements ICommand {

  public AddHoloCommand(Main plugin) {
    super(plugin);
  }

  @Override
  public boolean execute(CommandSender sender, ArrayList<String> args) {
    if (!super.hasPermission(sender)) {
      return false;
    }

    if (!Main.getInstance().isHologramsEnabled()) {
      String missingholodependency = Main.getInstance().getMissingHoloDependency();

      sender.sendMessage(ChatWriter.pluginMessage(ChatColor.RED
          + Main._l(sender, "errors.holodependencynotfound",
          ImmutableMap.of("dependency", missingholodependency))));
      return true;
    }

    Player player = (Player) sender;
    Main.getInstance().getHolographicInteractor().addHologramLocation(player.getEyeLocation());
    Main.getInstance().getHolographicInteractor().updateHolograms();
    return true;
  }

  @Override
  public String[] getArguments() {
    return new String[]{};
  }

  @Override
  public String getCommand() {
    return "addholo";
  }

  @Override
  public String getDescription() {
    return Main._l("commands.addholo.desc");
  }

  @Override
  public String getName() {
    return Main._l("commands.addholo.name");
  }

  @Override
  public String getPermission() {
    return "setup";
  }

}
