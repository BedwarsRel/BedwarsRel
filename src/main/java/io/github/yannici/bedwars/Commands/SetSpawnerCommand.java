package io.github.yannici.bedwars.Commands;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.google.common.collect.ImmutableMap;

import io.github.yannici.bedwars.ChatWriter;
import io.github.yannici.bedwars.Main;
import io.github.yannici.bedwars.Game.Game;
import io.github.yannici.bedwars.Game.GameState;
import io.github.yannici.bedwars.Game.RessourceSpawner;

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
		return new String[] { "game", "ressource" };
	}

	private String[] getRessources() {
		ConfigurationSection section = Main.getInstance().getConfig().getConfigurationSection("ressource");
		if (section == null) {
			return new String[] {};
		}

		List<String> ressources = new ArrayList<String>();
		for (String key : section.getKeys(false)) {
			ressources.add(key.toLowerCase());
		}

		return ressources.toArray(new String[ressources.size()]);
	}

	@Override
	public boolean execute(CommandSender sender, ArrayList<String> args) {
		if (!super.hasPermission(sender)) {
			return false;
		}

		Player player = (Player) sender;
		ArrayList<String> arguments = new ArrayList<String>(Arrays.asList(this.getRessources()));
		String material = args.get(1).toString().toLowerCase();
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

		if (!arguments.contains(material)) {
			player.sendMessage(ChatWriter.pluginMessage(ChatColor.RED + Main._l("errors.spawnerargument")));
			return false;
		}

		Object section = Main.getInstance().getConfig().get("ressource." + material);
		ItemStack stack = RessourceSpawner.createSpawnerStackByConfig(section);

		Location location = player.getLocation();
		RessourceSpawner spawner = new RessourceSpawner(game, material, location);
		game.addRessourceSpawner(spawner);
		player.sendMessage(ChatWriter.pluginMessage(ChatColor.GREEN + Main._l("success.spawnerset",
				ImmutableMap.of("name", stack.getItemMeta().getDisplayName() + ChatColor.GREEN))));
		return true;
	}

	@Override
	public String getPermission() {
		return "setup";
	}

}
