package io.github.yannici.bedwars.Commands;

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

import io.github.yannici.bedwars.ChatWriter;
import io.github.yannici.bedwars.Main;
import io.github.yannici.bedwars.Utils;
import io.github.yannici.bedwars.Game.Game;
import io.github.yannici.bedwars.Game.GameState;
import io.github.yannici.bedwars.Game.Team;

public class SetBedCommand extends BaseCommand implements ICommand {

	public SetBedCommand(Main plugin) {
		super(plugin);
	}

	@Override
	public String getCommand() {
		return "setbed";
	}

	@Override
	public String getName() {
		return Main._l("commands.setbed.name");
	}

	@Override
	public String getDescription() {
		return Main._l("commands.setbed.desc");
	}

	@Override
	public String[] getArguments() {
		return new String[] { "game", "team" };
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
			player.sendMessage(ChatWriter.pluginMessage(
					ChatColor.RED + Main._l("errors.gamenotfound", ImmutableMap.of("game", args.get(0).toString()))));
			return false;
		}

		if (game.getState() == GameState.RUNNING) {
			sender.sendMessage(ChatWriter.pluginMessage(ChatColor.RED + Main._l("errors.notwhilegamerunning")));
			return false;
		}

		Team gameTeam = game.getTeam(team);

		if (gameTeam == null) {
			player.sendMessage(ChatWriter.pluginMessage(ChatColor.RED + Main._l("errors.teamnotfound")));
			return false;
		}

		HashSet<Material> transparent = new HashSet<Material>();
		transparent.add(Material.AIR);

		Class<?> hashsetType = Utils.getGenericTypeOfParameter(player.getClass(), "getTargetBlock", 0);
		Method targetBlockMethod = null;
		Block targetBlock = null;

		// 1.7 compatible
		try {
			try {
				targetBlockMethod = player.getClass().getMethod("getTargetBlock",
						new Class<?>[] { Set.class, int.class });
			} catch (Exception ex) {
				try {
					targetBlockMethod = player.getClass().getMethod("getTargetBlock",
							new Class<?>[] { HashSet.class, int.class });
				} catch (Exception exc) {
					exc.printStackTrace();
				}
			}

			if (hashsetType.equals(Byte.class)) {
				targetBlock = (Block) targetBlockMethod.invoke(player, new Object[] { null, 15 });
			} else {
				targetBlock = (Block) targetBlockMethod.invoke(player, new Object[] { transparent, 15 });
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		Block standingBlock = player.getLocation().getBlock().getRelative(BlockFace.DOWN);

		if (targetBlock == null || standingBlock == null) {
			player.sendMessage(ChatWriter.pluginMessage(ChatColor.RED + Main._l("errors.bedtargeting")));
			return false;
		}

		Material targetMaterial = game.getTargetMaterial();
		if (targetBlock.getType() != targetMaterial && standingBlock.getType() != targetMaterial) {
			player.sendMessage(ChatWriter.pluginMessage(ChatColor.RED + Main._l("errors.bedtargeting")));
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

		player.sendMessage(ChatWriter.pluginMessage(ChatColor.GREEN + Main._l("success.bedset",
				ImmutableMap.of("team", gameTeam.getChatColor() + gameTeam.getName() + ChatColor.GREEN))));
		return true;
	}

	@Override
	public String getPermission() {
		return "setup";
	}

}
