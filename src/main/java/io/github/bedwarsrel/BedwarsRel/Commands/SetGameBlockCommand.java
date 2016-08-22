package io.github.bedwarsrel.BedwarsRel.Commands;

import java.util.ArrayList;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;

import com.google.common.collect.ImmutableMap;

import io.github.bedwarsrel.BedwarsRel.ChatWriter;
import io.github.bedwarsrel.BedwarsRel.Main;
import io.github.bedwarsrel.BedwarsRel.Utils;
import io.github.bedwarsrel.BedwarsRel.Game.Game;
import io.github.bedwarsrel.BedwarsRel.Game.GameState;

public class SetGameBlockCommand extends BaseCommand implements ICommand {

  public SetGameBlockCommand(Main plugin) {
    super(plugin);
  }

  @Override
  public String getCommand() {
    return "setgameblock";
  }

  @Override
  public String getName() {
    return Main._l("commands.setgameblock.name");
  }

  @Override
  public String getDescription() {
    return Main._l("commands.setgameblock.desc");
  }

  @Override
  public String[] getArguments() {
    return new String[] {"game", "blocktype"};
  }

  @Override
  public boolean execute(CommandSender sender, ArrayList<String> args) {
    if (!sender.hasPermission("bw." + this.getPermission())) {
      return false;
    }

    Game game = this.getPlugin().getGameManager().getGame(args.get(0));
    String material = args.get(1).toString();

    if (game == null) {
      sender.sendMessage(ChatWriter.pluginMessage(ChatColor.RED
          + Main._l("errors.gamenotfound", ImmutableMap.of("game", args.get(0).toString()))));
      return false;
    }

    if (game.getState() == GameState.RUNNING) {
      sender.sendMessage(
          ChatWriter.pluginMessage(ChatColor.RED + Main._l("errors.notwhilegamerunning")));
      return false;
    }

    Material targetMaterial = Utils.parseMaterial(material);
    if (targetMaterial == null && !"DEFAULT".equals(material)) {
      sender
          .sendMessage(ChatWriter.pluginMessage(ChatColor.RED + Main._l("errors.novalidmaterial")));
      return true;
    }

    if ("DEFAULT".equalsIgnoreCase(material)) {
      game.setTargetMaterial(null);
    } else {
      game.setTargetMaterial(targetMaterial);
    }

    sender.sendMessage(ChatWriter.pluginMessage(ChatColor.GREEN + Main._l("success.materialset")));
    return true;
  }

  @Override
  public String getPermission() {
    return "setup";
  }

}
