package io.github.yannici.bedwars.Commands;

import java.util.ArrayList;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import com.google.common.collect.ImmutableMap;

import io.github.yannici.bedwars.ChatWriter;
import io.github.yannici.bedwars.Main;
import io.github.yannici.bedwars.Game.Game;

public class StartGameCommand extends BaseCommand implements ICommand {

  public StartGameCommand(Main plugin) {
    super(plugin);
  }

  @Override
  public String getCommand() {
    return "start";
  }

  @Override
  public String getName() {
    return Main._l("commands.start.name");
  }

  @Override
  public String getDescription() {
    return Main._l("commands.start.desc");
  }

  @Override
  public String[] getArguments() {
    return new String[] {"game"};
  }

  @Override
  public boolean execute(CommandSender sender, ArrayList<String> args) {
    if (!sender.hasPermission("bw." + this.getPermission())) {
      return false;
    }

    Game game = this.getPlugin().getGameManager().getGame(args.get(0));
    if (game == null) {
      sender.sendMessage(ChatWriter.pluginMessage(ChatColor.RED
          + Main._l("errors.gamenotfound", ImmutableMap.of("game", args.get(0).toString()))));
      return false;
    }

    game.run(sender);
    return true;
  }

  @Override
  public String getPermission() {
    return "setup";
  }

}
