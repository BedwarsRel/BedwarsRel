package io.github.bedwarsrel.commands;

import io.github.bedwarsrel.BedwarsRel;
import io.github.bedwarsrel.game.Game;
import io.github.bedwarsrel.utils.ChatWriter;
import java.util.ArrayList;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class KickCommand extends BaseCommand implements ICommand {

  public KickCommand(BedwarsRel plugin) {
    super(plugin);
  }

  @Override
  public boolean execute(CommandSender sender, ArrayList<String> args) {
    if (!super.hasPermission(sender) && !sender.isOp()) {
      return false;
    }

    Player player = (Player) sender;
    Game game = BedwarsRel.getInstance().getGameManager().getGameOfPlayer(player);

    // find player
    Player kickPlayer = BedwarsRel.getInstance().getServer().getPlayer(args.get(0).toString());

    if (game == null) {
      player
          .sendMessage(ChatWriter.pluginMessage(BedwarsRel._l(player, "errors.notingameforkick")));
      return true;
    }

    if (kickPlayer == null || !kickPlayer.isOnline()) {
      player.sendMessage(ChatWriter.pluginMessage(BedwarsRel._l(player, "errors.playernotfound")));
      return true;
    }

    if (!game.isInGame(kickPlayer)) {
      player.sendMessage(ChatWriter.pluginMessage(BedwarsRel._l(player, "errors.playernotingame")));
      return true;
    }

    game.playerLeave(kickPlayer, true);
    return true;
  }

  @Override
  public String[] getArguments() {
    return new String[]{"player"};
  }

  @Override
  public String getCommand() {
    return "kick";
  }

  @Override
  public String getDescription() {
    return BedwarsRel._l("commands.kick.desc");
  }

  @Override
  public String getName() {
    return BedwarsRel._l("commands.kick.name");
  }

  @Override
  public String getPermission() {
    return "kick";
  }

}
