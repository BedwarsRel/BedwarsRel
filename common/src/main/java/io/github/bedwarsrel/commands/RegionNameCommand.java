package io.github.bedwarsrel.commands;

import com.google.common.collect.ImmutableMap;
import io.github.bedwarsrel.BedwarsRel;
import io.github.bedwarsrel.game.Game;
import io.github.bedwarsrel.game.GameState;
import io.github.bedwarsrel.utils.ChatWriter;
import java.util.ArrayList;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class RegionNameCommand extends BaseCommand implements ICommand {

  public RegionNameCommand(BedwarsRel plugin) {
    super(plugin);
  }

  @Override
  public boolean execute(CommandSender sender, ArrayList<String> args) {
    if (!sender.hasPermission("bw." + this.getPermission())) {
      return false;
    }

    Player player = (Player) sender;

    Game game = this.getPlugin().getGameManager().getGame(args.get(0));
    String name = args.get(1).toString();

    if (game == null) {
      player.sendMessage(ChatWriter.pluginMessage(ChatColor.RED
          + BedwarsRel
          ._l(sender, "errors.gamenotfound", ImmutableMap.of("game", args.get(0).toString()))));
      return false;
    }

    if (game.getState() == GameState.RUNNING) {
      sender.sendMessage(
          ChatWriter.pluginMessage(ChatColor.RED + BedwarsRel
              ._l(sender, "errors.notwhilegamerunning")));
      return false;
    }

    if (name.length() > 15) {
      player.sendMessage(
          ChatWriter
              .pluginMessage(ChatColor.RED + BedwarsRel._l(player, "errors.toolongregionname")));
      return true;
    }

    game.setRegionName(name);
    player
        .sendMessage(
            ChatWriter
                .pluginMessage(ChatColor.GREEN + BedwarsRel._l(player, "success.regionnameset")));
    return true;
  }

  @Override
  public String[] getArguments() {
    return new String[]{"game", "name"};
  }

  @Override
  public String getCommand() {
    return "regionname";
  }

  @Override
  public String getDescription() {
    return BedwarsRel._l("commands.regionname.desc");
  }

  @Override
  public String getName() {
    return BedwarsRel._l("commands.regionname.name");
  }

  @Override
  public String getPermission() {
    return "setup";
  }

}
