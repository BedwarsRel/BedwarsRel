package io.github.bedwarsrel.BedwarsRel.Commands;

import io.github.bedwarsrel.BedwarsRel.Game.Game;
import io.github.bedwarsrel.BedwarsRel.Main;
import io.github.bedwarsrel.BedwarsRel.Utils.ChatWriter;
import java.util.ArrayList;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class KickCommand extends BaseCommand implements ICommand {

  public KickCommand(Main plugin) {
    super(plugin);
  }

  @Override
  public boolean execute(CommandSender sender, ArrayList<String> args) {
    if (!super.hasPermission(sender) && !sender.isOp()) {
      return false;
    }

    Player player = (Player) sender;
    Game game = Main.getInstance().getGameManager().getGameOfPlayer(player);

    // find player
    Player kickPlayer = Main.getInstance().getServer().getPlayer(args.get(0).toString());

    if (game == null) {
      player.sendMessage(ChatWriter.pluginMessage(Main._l(player, "errors.notingameforkick")));
      return true;
    }

    if (kickPlayer == null || !kickPlayer.isOnline()) {
      player.sendMessage(ChatWriter.pluginMessage(Main._l(player, "errors.playernotfound")));
      return true;
    }

    if (!game.isInGame(kickPlayer)) {
      player.sendMessage(ChatWriter.pluginMessage(Main._l(player, "errors.playernotingame")));
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
    return Main._l("commands.kick.desc");
  }

  @Override
  public String getName() {
    return Main._l("commands.kick.name");
  }

  @Override
  public String getPermission() {
    return "kick";
  }

}
