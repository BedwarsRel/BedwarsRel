package io.github.bedwarsrel.commands;

import com.google.common.collect.ImmutableMap;
import io.github.bedwarsrel.BedwarsRel;
import io.github.bedwarsrel.game.Game;
import io.github.bedwarsrel.game.GameState;
import io.github.bedwarsrel.game.Team;
import io.github.bedwarsrel.utils.ChatWriter;
import io.github.bedwarsrel.utils.Utils;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.material.Bed;

public class SetTargetCommand extends BaseCommand implements ICommand {

  public SetTargetCommand(BedwarsRel plugin) {
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
          + BedwarsRel
          ._l(player, "errors.gamenotfound", ImmutableMap.of("game", args.get(0).toString()))));
      return false;
    }

    if (game.getState() == GameState.RUNNING) {
      sender.sendMessage(
          ChatWriter.pluginMessage(ChatColor.RED + BedwarsRel
              ._l(sender, "errors.notwhilegamerunning")));
      return false;
    }

    Team gameTeam = game.getTeam(team);

    if (gameTeam == null) {
      player.sendMessage(
          ChatWriter.pluginMessage(ChatColor.RED + BedwarsRel._l(player, "errors.teamnotfound")));
      return false;
    }

    Class<?> hashsetType = Utils.getGenericTypeOfParameter(player.getClass(), "getTargetBlock", 0);
    Method targetBlockMethod = this.getTargetBlockMethod(player);
    Block targetBlock = null;

    if (targetBlockMethod != null) {
      targetBlock = this.getTargetBlock(targetBlockMethod, hashsetType, player);
    }

    Block standingBlock = player.getLocation().getBlock().getRelative(BlockFace.DOWN);

    if (targetBlock == null || standingBlock == null) {
      player.sendMessage(
          ChatWriter.pluginMessage(ChatColor.RED + BedwarsRel._l(player, "errors.bedtargeting")));
      return false;
    }

    Material targetMaterial = game.getTargetMaterial();
    if (targetBlock.getType() != targetMaterial && standingBlock.getType() != targetMaterial) {
      player.sendMessage(
          ChatWriter.pluginMessage(ChatColor.RED + BedwarsRel._l(player, "errors.bedtargeting")));
      return false;
    }

    Block theBlock = null;
    if (targetBlock.getType() == targetMaterial) {
      theBlock = targetBlock;
    } else {
      theBlock = standingBlock;
    }

    if (targetMaterial.equals(Material.BED_BLOCK)) {
      Block neighbor = null;
      Bed theBed = (Bed) theBlock.getState().getData();

      if (!theBed.isHeadOfBed()) {
        neighbor = theBlock;
        theBlock = Utils.getBedNeighbor(neighbor);
      } else {
        neighbor = Utils.getBedNeighbor(theBlock);
      }

      gameTeam.setTargets(theBlock, neighbor);
    } else {
      gameTeam.setTargets(theBlock, null);
    }

    player.sendMessage(ChatWriter.pluginMessage(ChatColor.GREEN + BedwarsRel
        ._l(player, "success.bedset",
            ImmutableMap
                .of("team", gameTeam.getChatColor() + gameTeam.getName() + ChatColor.GREEN))));
    return true;
  }

  @Override
  public String[] getArguments() {
    return new String[]{"game", "team"};
  }

  @Override
  public String getCommand() {
    return "settarget";
  }

  @Override
  public String getDescription() {
    return BedwarsRel._l("commands.settarget.desc");
  }

  @Override
  public String getName() {
    return BedwarsRel._l("commands.settarget.name");
  }

  @Override
  public String getPermission() {
    return "setup";
  }

  private Block getTargetBlock(Method targetBlockMethod, Class<?> hashsetType, Player player) {
    Block targetBlock = null;
    HashSet<Material> transparent = new HashSet<Material>();
    transparent.add(Material.AIR);

    try {
      if (hashsetType.equals(Byte.class)) {
        targetBlock = (Block) targetBlockMethod.invoke(player, new Object[]{null, 15});
      } else {
        targetBlock = (Block) targetBlockMethod.invoke(player, new Object[]{transparent, 15});
      }

    } catch (Exception e) {
      BedwarsRel.getInstance().getBugsnag().notify(e);
      e.printStackTrace();
    }

    return targetBlock;
  }

  private Method getTargetBlockMethod(Player player) {
    Method targetBlockMethod = null;
    try {
      targetBlockMethod =
          player.getClass().getMethod("getTargetBlock", new Class<?>[]{Set.class, int.class});
    } catch (Exception ex) {
      BedwarsRel.getInstance().getBugsnag().notify(ex);
      try {
        targetBlockMethod = player.getClass().getMethod("getTargetBlock",
            new Class<?>[]{HashSet.class, int.class});
      } catch (Exception exc) {
        BedwarsRel.getInstance().getBugsnag().notify(exc);
        exc.printStackTrace();
      }
    }

    return targetBlockMethod;
  }

}
