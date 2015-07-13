package io.github.yannici.bedwars.Commands;

import io.github.yannici.bedwars.ChatWriter;
import io.github.yannici.bedwars.Main;
import io.github.yannici.bedwars.Utils;
import io.github.yannici.bedwars.Game.Game;
import io.github.yannici.bedwars.Game.GameState;
import io.github.yannici.bedwars.Game.Team;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import com.google.common.collect.ImmutableMap;

public class SetStandCommand extends BaseCommand {

	public SetStandCommand(Main plugin) {
        super(plugin);
    }

    @Override
	public String getPermission() {
		return "setup";
	}

	@Override
	public String getCommand() {
		return "setstand";
	}

	@Override
	public String getName() {
		return Main._l("commands.setstand.name");
	}

	@Override
	public String getDescription() {
		return Main._l("commands.setstand.desc");
	}

	@Override
	public String[] getArguments() {
		return new String[]{"game", "team"};
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
					+ Main._l("errors.gamenotfound",
							ImmutableMap.of("game", args.get(0).toString()))));
			return false;
		}
		
		if(game.getState() == GameState.RUNNING) {
			sender.sendMessage(ChatWriter.pluginMessage(ChatColor.RED
					+ Main._l("errors.notwhilegamerunning")));
			return false;
		}

		Team gameTeam = game.getTeam(team);

		if (gameTeam == null) {
			player.sendMessage(ChatWriter.pluginMessage(ChatColor.RED
					+ Main._l("errors.teamnotfound")));
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
                targetBlockMethod = player.getClass().getMethod("getTargetBlock", new Class<?>[]{Set.class, int.class});
			} catch(Exception ex) {
			    try {
			        targetBlockMethod = player.getClass().getMethod("getTargetBlock", new Class<?>[]{HashSet.class, int.class});
    			} catch(Exception exc) {
                    exc.printStackTrace();
                }
			}
			
			if(hashsetType.equals(Byte.class)) {
				targetBlock = (Block)targetBlockMethod.invoke(player, new Object[]{null, 15});
			} else {
				targetBlock = (Block)targetBlockMethod.invoke(player, new Object[]{transparent, 15});
			}
		
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		if(targetBlock == null || targetBlock.getRelative(BlockFace.UP).getType() != Material.AIR
				|| targetBlock.getRelative(0, 2, 0).getType() != Material.AIR) {
			player.sendMessage(ChatWriter.pluginMessage(ChatColor.RED
					+ Main._l("errors.armorstandtargeting")));
			return false;
		}
		
		// only in lobby
		if(game.getLobby() == null || !player.getWorld().equals(game.getLobby().getWorld())) {
			player.sendMessage(ChatWriter.pluginMessage(ChatColor.RED
					+ Main._l("errors.mustbeinlobbyworld")));
			return false;
		}
		
		Location targetLocation = targetBlock.getRelative(BlockFace.UP).getLocation().clone();
		targetLocation.setYaw((targetLocation.getYaw() > 180 ? targetLocation.getYaw() - 180 : targetLocation.getYaw() + 180));
		Location standLocation = new Location(game.getLobby().getWorld(), 
				targetLocation.getX(), 
				targetLocation.getY(), 
				targetLocation.getZ(), targetLocation.getYaw(), targetLocation.getPitch());
		ArmorStand stand = (ArmorStand) game.getLobby().getWorld().spawnEntity(standLocation, EntityType.ARMOR_STAND);
		stand.setArms(true);
		stand.setBasePlate(true);
		
		player.sendMessage(ChatWriter.pluginMessage(ChatColor.GREEN
				+ Main._l("success.armorstandadded")));
		return true;
	}

}
