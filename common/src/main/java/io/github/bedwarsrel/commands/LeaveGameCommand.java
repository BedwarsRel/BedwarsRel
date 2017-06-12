package io.github.bedwarsrel.commands;

import io.github.bedwarsrel.BedwarsRel;
import io.github.bedwarsrel.game.Game;
import java.util.ArrayList;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class LeaveGameCommand extends BaseCommand {

  public LeaveGameCommand(BedwarsRel plugin) {
    super(plugin);
  }

  @Override
  public boolean execute(CommandSender sender, ArrayList<String> args) {
    if (!super.hasPermission(sender)) {
      return false;
    }

    Player player = (Player) sender;
    Game game = BedwarsRel.getInstance().getGameManager().getGameOfPlayer(player);

    if (game == null) {
      return true;
    }

    game.playerLeave(player, false);
    return true;
  }

  @Override
  public String[] getArguments() {
    return new String[]{};
  }

  @Override
  public String getCommand() {
    return "leave";
  }

  @Override
  public String getDescription() {
    return BedwarsRel._l("commands.leave.desc");
  }

  @Override
  public String getName() {
    return BedwarsRel._l("commands.leave.name");
  }

  @Override
  public String getPermission() {
    return "base";
  }

}
