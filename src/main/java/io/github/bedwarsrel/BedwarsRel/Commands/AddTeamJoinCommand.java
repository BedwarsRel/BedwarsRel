package io.github.bedwarsrel.BedwarsRel.Commands;

import com.google.common.collect.ImmutableMap;
import io.github.bedwarsrel.BedwarsRel.Game.Game;
import io.github.bedwarsrel.BedwarsRel.Game.GameState;
import io.github.bedwarsrel.BedwarsRel.Game.Team;
import io.github.bedwarsrel.BedwarsRel.Game.TeamJoinMetaDataValue;
import io.github.bedwarsrel.BedwarsRel.Main;
import io.github.bedwarsrel.BedwarsRel.Utils.ChatWriter;
import java.util.ArrayList;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class AddTeamJoinCommand extends BaseCommand {

  public AddTeamJoinCommand(Main plugin) {
    super(plugin);
  }

  @Override
  public boolean execute(CommandSender sender, ArrayList<String> args) {
    if (!super.hasPermission(sender)) {
      return false;
    }

    Player player = (Player) sender;
    String team = args.get(1);

    Game game = this.getPlugin().getGameManager().getGame(args.get(0));
    if (game == null) {
      player.sendMessage(ChatWriter.pluginMessage(ChatColor.RED
          + Main
          ._l(sender, "errors.gamenotfound", ImmutableMap.of("game", args.get(0).toString()))));
      return false;
    }

    if (game.getState() == GameState.RUNNING) {
      sender.sendMessage(
          ChatWriter.pluginMessage(ChatColor.RED + Main._l(sender, "errors.notwhilegamerunning")));
      return false;
    }

    Team gameTeam = game.getTeam(team);

    if (gameTeam == null) {
      player.sendMessage(
          ChatWriter.pluginMessage(ChatColor.RED + Main._l(player, "errors.teamnotfound")));
      return false;
    }

    // only in lobby
    if (game.getLobby() == null || !player.getWorld().equals(game.getLobby().getWorld())) {
      player.sendMessage(
          ChatWriter.pluginMessage(ChatColor.RED + Main._l(player, "errors.mustbeinlobbyworld")));
      return false;
    }

    if (player.hasMetadata("bw-addteamjoin")) {
      player.removeMetadata("bw-addteamjoin", Main.getInstance());
    }

    player.setMetadata("bw-addteamjoin", new TeamJoinMetaDataValue(gameTeam));
    final Player runnablePlayer = player;

    new BukkitRunnable() {

      @Override
      public void run() {
        try {
          if (!runnablePlayer.hasMetadata("bw-addteamjoin")) {
            return;
          }

          runnablePlayer.removeMetadata("bw-addteamjoin", Main.getInstance());
        } catch (Exception ex) {
          Main.getInstance().getBugsnag().notify(ex);
          // just ignore
        }
      }
    }.runTaskLater(Main.getInstance(), 20L * 10L);

    player.sendMessage(
        ChatWriter
            .pluginMessage(ChatColor.GREEN + Main._l(player, "success.selectteamjoinentity")));
    return true;
  }

  @Override
  public String[] getArguments() {
    return new String[]{"game", "team"};
  }

  @Override
  public String getCommand() {
    return "addteamjoin";
  }

  @Override
  public String getDescription() {
    return Main._l("commands.addteamjoin.desc");
  }

  @Override
  public String getName() {
    return Main._l("commands.addteamjoin.name");
  }

  @Override
  public String getPermission() {
    return "setup";
  }

}
