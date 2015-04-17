package io.github.yannici.bedwars.Commands;

import io.github.yannici.bedwars.ChatWriter;
import io.github.yannici.bedwars.Main;
import io.github.yannici.bedwars.Game.Game;

import java.util.ArrayList;
import java.util.Arrays;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.google.common.collect.ImmutableMap;

public class SetSpawnerCommand extends BaseCommand {

	public SetSpawnerCommand(Main plugin) {
		super(plugin);
	}

	@Override
	public String getCommand() {
		return "setspawner";
	}

	@Override
	public String getName() {
		return Main._l("commands.setspawner.name");
	}

	@Override
	public String getDescription() {
		return Main._l("commands.setspawner.desc");
	}

	@Override
	public String[] getArguments() {
		return new String[] { "game", "gold;iron;bronze" };
	}

	private String[] getRessources() {
		return new String[] { "gold", "iron", "bronze" };
	}

	@Override
	public boolean execute(CommandSender sender, ArrayList<String> args) {
		if (!super.hasPermission(sender)) {
			return false;
		}

		Player player = (Player) sender;
		ArrayList<String> arguments = new ArrayList<String>(Arrays.asList(this
				.getRessources()));
		String material = args.get(1).toLowerCase();
		Game game = this.getPlugin().getGameManager().getGame(args.get(0));

		if (game == null) {
			player.sendMessage(ChatWriter.pluginMessage(ChatColor.RED
					+ Main._l("errors.gamenotfound",
							ImmutableMap.of("game", args.get(0).toString()))));
			return false;
		}

		if (!arguments.contains(material)) {
			player.sendMessage(ChatWriter.pluginMessage(ChatColor.RED
					+ Main._l("errors.spawnerargument")));
			return false;
		}

		Material droppingMaterial = null;
		String name = "Ress";
		switch (material) {
		case "gold":
			droppingMaterial = Material.GOLD_INGOT;
			name = ChatColor.translateAlternateColorCodes('§',
					Main._l("ressources.gold"));
			break;
		case "iron":
			droppingMaterial = Material.IRON_INGOT;
			name = ChatColor.translateAlternateColorCodes('§',
					Main._l("ressources.iron"));
			break;
		case "bronze":
			droppingMaterial = Material.CLAY_BRICK;
			name = ChatColor.translateAlternateColorCodes('§',
					Main._l("ressources.bronze"));
			break;
		}

		ItemStack stack = new ItemStack(droppingMaterial, this.getPlugin()
				.getConfig().getInt("ressource." + material + ".amount"));
		ItemMeta meta = stack.getItemMeta();
		meta.setDisplayName(name);
		stack.setItemMeta(meta);

		int interval = this.getPlugin().getConfig()
				.getInt("ressource." + material + ".spawninterval");
		Block downBlock = player.getLocation().getBlock()
				.getRelative(BlockFace.DOWN);

		if (downBlock == null) {
			player.sendMessage(ChatWriter.pluginMessage(ChatWriter
					.pluginMessage(ChatColor.RED
							+ Main._l("errors.blockdownnotfound"))));
			return false;
		}

		game.addRessourceSpawner(interval, downBlock.getLocation(), stack);
		player.sendMessage(ChatWriter.pluginMessage(ChatColor.GREEN
				+ Main._l("success.spawnerset")));
		return true;
	}

	@Override
	public String getPermission() {
		return "setup";
	}

}
