package io.github.bedwarsrel.BedwarsRel.Commands;

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

import com.google.common.collect.ImmutableMap;

import io.github.bedwarsrel.BedwarsRel.Main;
import io.github.bedwarsrel.BedwarsRel.Game.Game;
import io.github.bedwarsrel.BedwarsRel.Game.GameState;
import io.github.bedwarsrel.BedwarsRel.Game.Team;
import io.github.bedwarsrel.BedwarsRel.Utils.ChatWriter;
import io.github.bedwarsrel.BedwarsRel.Utils.Utils;

public class SetTargetCommand extends BaseCommand implements ICommand {

  public SetTargetCommand(Main plugin) {
    super(plugin);
  }

  @Override
  public String getCommand() {
    return "settarget";
  }

  @Override
  public String getName() {
    return Main._l("commands.settarget.name");
  }

  @Override
  public String getDescription() {
    return Main._l("commands.settarget.desc");
  }

  @Override
  public String[] getArguments() {
    return new String[] {"game", "team"};
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
          + Main._l(player, "errors.gamenotfound", ImmutableMap.of("game", args.get(0).toString()))));
      return false;
    }

    if (game.getState() == GameState.RUNNING) {
      sender.sendMessage(
          ChatWriter.pluginMessage(ChatColor.RED + Main._l(sender, "errors.notwhilegamerunning")));
      return false;
    }

    Team gameTeam = game.getTeam(team);

    if (gameTeam == null) {
      player.sendMessage(ChatWriter.pluginMessage(ChatColor.RED + Main._l(player, "errors.teamnotfound")));
      return false;
    }

    Class<?> hashsetType = Utils.getGenericTypeOfParameter(player.getClass(), "getTargetBlock", 0);
    Method targetBlockMethod = this.getTargetBlockMethod(player);
    Block targetBlock = null;
    
    if(targetBlockMethod != null) {
    	targetBlock = this.getTargetBlock(targetBlockMethod, hashsetType, player);
    }

    Block standingBlock = player.getLocation().getBlock().getRelative(BlockFace.DOWN);

    if (targetBlock == null || standingBlock == null) {
      player.sendMessage(ChatWriter.pluginMessage(ChatColor.RED + Main._l(player, "errors.bedtargeting")));
      return false;
    }

    Material targetMaterial = game.getTargetMaterial();
    if (targetBlock.getType() != targetMaterial && standingBlock.getType() != targetMaterial) {
      player.sendMessage(ChatWriter.pluginMessage(ChatColor.RED + Main._l(player, "errors.bedtargeting")));
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

    player.sendMessage(ChatWriter.pluginMessage(ChatColor.GREEN + Main._l(player, "success.bedset",
        ImmutableMap.of("team", gameTeam.getChatColor() + gameTeam.getName() + ChatColor.GREEN))));
    return true;
  }
  
  private Block getTargetBlock(Method targetBlockMethod, Class<?> hashsetType, Player player) {
	  Block targetBlock = null;
	  HashSet<Material> transparent = new HashSet<Material>();
	  transparent.add(Material.AIR);
	  
	  try {
	      if (hashsetType.equals(Byte.class)) {
	        targetBlock = (Block) targetBlockMethod.invoke(player, new Object[] {null, 15});
	      } else {
	        targetBlock = (Block) targetBlockMethod.invoke(player, new Object[] {transparent, 15});
	      }

	    } catch (Exception e) {
	      Main.getInstance().getBugsnag().notify(e);
	      e.printStackTrace();
	    }
	  
	 return targetBlock;
  }
  
  private Method getTargetBlockMethod(Player player) {
	  Method targetBlockMethod = null;
	  try {
        targetBlockMethod =
            player.getClass().getMethod("getTargetBlock", new Class<?>[] {Set.class, int.class});
      } catch (Exception ex) {
        Main.getInstance().getBugsnag().notify(ex);
        try {
          targetBlockMethod = player.getClass().getMethod("getTargetBlock",
              new Class<?>[] {HashSet.class, int.class});
        } catch (Exception exc) {
          Main.getInstance().getBugsnag().notify(exc);
          exc.printStackTrace();
        }
      }
	  
	  return targetBlockMethod;
  }

  @Override
  public String getPermission() {
    return "setup";
  }

}
