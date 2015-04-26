package io.github.yannici.bedwars.Game;

import io.github.yannici.bedwars.ChatWriter;
import io.github.yannici.bedwars.Main;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.configuration.file.YamlConfiguration;

import com.google.common.collect.ImmutableMap;

public class GameManager {

	public static String gamesPath = "games";

	private ArrayList<Game> games = null;
	private Main plugin = null;

	public GameManager(Main plugin) {
		this.plugin = plugin;
		this.games = new ArrayList<Game>();
	}

	public Game addGame(String name) {
		Game existing = this.getGame(name);
		if (existing != null) {
			return null;
		}

		Game newGame = new Game(this.plugin, name);
		this.games.add(newGame);
		return newGame;
	}

	public ArrayList<Game> getGames() {
		return this.games;
	}

	public Game getGame(String name) {
		for (Game game : this.games) {
			if (game.getName().equals(name)) {
				return game;
			}
		}

		return null;
	}
	
	public void reloadGames() {
	    this.unloadGames();
	    this.loadGames();
	}
	
	public void removeGame(Game game) {
		if(game == null) {
			return;
		}
		
		File configs = new File(Main.getInstance().getDataFolder() + "/"
				+ GameManager.gamesPath + "/" + game.getName());
		
		if(configs.exists()) {
			configs.delete();
		}
		
		this.games.remove(game);
	}
	
	public void unloadGame(Game game) {
		if (game.getState() != GameState.STOPPED) {
			game.stop();
		}
		
		game.setState(GameState.STOPPED);
		game.setScoreboard(Main.getInstance().getScoreboardManager()
				.getNewScoreboard());
		game.kickAllPlayers();
		game.resetRegion();
		game.updateSigns();
	}

	public void loadGames() {
		String path = Main.getInstance().getDataFolder() + "/"
				+ GameManager.gamesPath;
		File file = new File(path);

		if (!file.exists()) {
			return;
		}

		File[] files = file.listFiles(new FileFilter() {

			@Override
			public boolean accept(File pathname) {
				return pathname.isDirectory();
			}
		});

		if (files.length > 0) {
			for (File dir : files) {
				File[] configFiles = dir.listFiles();
				for (File cfg : configFiles) {
					if (!cfg.isFile()) {
						continue;
					}

					if (cfg.getName().equals("game.yml")) {
						this.loadGame(cfg);
					}
				}
			}
		}

		for (Game g : this.games) {
			if (!g.run(Main.getInstance().getServer().getConsoleSender())) {
				Main.getInstance()
						.getServer()
						.getConsoleSender()
						.sendMessage(
								ChatWriter.pluginMessage(ChatColor.RED
										+ Main._l("errors.gamenotloaded")));
			} else {
				g.getCycle().onGameLoaded();
			}
		}
	}

	@SuppressWarnings("unchecked")
	private void loadGame(File configFile) {
		try {

			YamlConfiguration cfg = YamlConfiguration
					.loadConfiguration(configFile);
			String name = cfg.get("name").toString();
			if (name.isEmpty()) {
				return;
			}

			Game game = new Game(Main.getInstance(), name);
			game.setConfig(cfg);

			Map<String, Object> teams = new HashMap<String, Object>();
			Map<String, Object> spawner = new HashMap<String, Object>();

			if (cfg.contains("teams")) {
				teams = cfg.getConfigurationSection("teams").getValues(false);
			}

			if (cfg.contains("spawner")) {
				spawner = cfg.getConfigurationSection("spawner").getValues(
						false);
			}

			for (Object obj : teams.values()) {
				if (!(obj instanceof Team)) {
					continue;
				}

				game.addTeam((Team) obj);
			}

			for (Object obj : spawner.values()) {
				if (!(obj instanceof RessourceSpawner)) {
					continue;
				}

				RessourceSpawner rs = (RessourceSpawner) obj;
				rs.setGame(game);
				game.addRessourceSpawner(rs);
			}

			Location loc1 = (Location) cfg.get("loc1");
			Location loc2 = (Location) cfg.get("loc2");

			File signFile = new File(Main.getInstance().getDataFolder() + "/"
					+ GameManager.gamesPath + "/" + game.getName()
					+ "/sign.yml");
			if (signFile.exists()) {
				YamlConfiguration signConfig = YamlConfiguration
						.loadConfiguration(signFile);
				List<Object> signs = (List<Object>) signConfig.get("signs");
				for (Object sign : signs) {
					if (!(sign instanceof Location)) {
						continue;
					}

					Location signLocation = (Location) sign;
					signLocation.getChunk().load(true);
					
					Block signBlock = signLocation.getBlock();
					if (!(signBlock.getState() instanceof Sign)) {
						continue;
					}
					
					signBlock.getState().update(true, true);
					game.addJoinSign(signBlock.getLocation());
				}
			}

			game.setLoc(loc1, "loc1");
			game.setLoc(loc2, "loc2");
			game.setLobby((Location) cfg.get("lobby"));
			
			String regionName = loc1.getWorld().getName();
			
			if(cfg.contains("regionname")) {
				regionName = cfg.getString("regionname");
			}
			
			if(cfg.contains("time") && cfg.isInt("time")) {
				game.setTime(cfg.getInt("time"));
			}
			
			game.setRegion(new Region(loc1, loc2, regionName));
			
			if (cfg.contains("minplayers")) {
				game.setMinPlayers(cfg.getInt("minplayers"));
			}

			if (cfg.contains("mainlobby")) {
				game.setMainLobby((Location) cfg.get("mainlobby"));
			}
			
			game.getFreePlayers().clear();
			game.updateSigns();
			
			this.games.add(game);
			Main.getInstance()
					.getServer()
					.getConsoleSender()
					.sendMessage(
							ChatWriter.pluginMessage(ChatColor.GREEN
									+ Main._l(
											"success.gameloaded",
											ImmutableMap.of("game",
													game.getName()))));
		} catch (Exception ex) {
			Main.getInstance()
					.getServer()
					.getConsoleSender()
					.sendMessage(
							ChatWriter.pluginMessage(ChatColor.RED
									+ Main._l(
											"errors.gameloaderror",
											ImmutableMap.of(
													"game",
													Main._l("success.gameloaded",
															ImmutableMap
																	.of("game",
																			configFile
																					.getParentFile()
																					.getName()))))));
		}
	}

	public void unloadGames() {
		for (Game g : this.games) {
			this.unloadGame(g);
		}

		this.games.clear();
	}

	public Game getGameByWorld(World world) {
		for (Game game : this.games) {
			if (game.getRegion().getWorld().equals(world)) {
				return game;
			}
		}

		return null;
	}

	public Game getGameBySignLocation(Location location) {
		for (Game game : this.games) {
			if (game.getSigns().containsKey(location)) {
				return game;
			}
		}

		return null;
	}

}
