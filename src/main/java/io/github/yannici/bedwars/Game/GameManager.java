package io.github.yannici.bedwars.Game;

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
import org.bukkit.entity.Player;

import com.google.common.collect.ImmutableMap;

import io.github.yannici.bedwars.ChatWriter;
import io.github.yannici.bedwars.Main;
import io.github.yannici.bedwars.Utils;

public class GameManager {

	public static String gamesPath = "games";

	private ArrayList<Game> games = null;
	private Map<Player, Game> gamePlayer = null;

	public GameManager() {
		this.games = new ArrayList<Game>();
		this.gamePlayer = new HashMap<Player, Game>();
	}

	public Game addGame(String name) {
		Game existing = this.getGame(name);
		if (existing != null) {
			return null;
		}

		Game newGame = new Game(name);
		this.games.add(newGame);
		return newGame;
	}

	public Game getGameOfPlayer(Player player) {
		return this.gamePlayer.get(player);
	}

	public void addGamePlayer(Player player, Game game) {
		if (this.gamePlayer.containsKey(player)) {
			this.gamePlayer.remove(player);
		}

		this.gamePlayer.put(player, game);
	}

	public void removeGamePlayer(Player player) {
		this.gamePlayer.remove(player);
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

		this.gamePlayer.clear();
		this.loadGames();
	}

	public void removeGame(Game game) {
		if (game == null) {
			return;
		}

		File configs = new File(Main.getInstance().getDataFolder() + File.separator + GameManager.gamesPath
				+ File.separator + game.getName());

		if (configs.exists()) {
			configs.delete();
		}

		this.games.remove(game);
	}

	public void unloadGame(Game game) {
		if (game.getState() != GameState.STOPPED) {
			game.stop();
		}

		game.setState(GameState.STOPPED);
		game.setScoreboard(Main.getInstance().getScoreboardManager().getNewScoreboard());
		game.kickAllPlayers();
		game.resetRegion();
		game.updateSigns();
	}

	public void loadGames() {
		String path = Main.getInstance().getDataFolder() + File.separator + GameManager.gamesPath;
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
				Main.getInstance().getServer().getConsoleSender()
						.sendMessage(ChatWriter.pluginMessage(ChatColor.RED + Main._l("errors.gamenotloaded")));
			} else {
				g.getCycle().onGameLoaded();
			}
		}
	}

	@SuppressWarnings("unchecked")
	private void loadGame(File configFile) {
		try {

			YamlConfiguration cfg = YamlConfiguration.loadConfiguration(configFile);
			String name = cfg.get("name").toString();
			if (name.isEmpty()) {
				return;
			}

			Game game = new Game(name);
			game.setConfig(cfg);

			Map<String, Object> teams = new HashMap<String, Object>();
			Map<String, Object> spawner = new HashMap<String, Object>();
			String targetMaterialObj = null;

			if (cfg.contains("teams")) {
				teams = cfg.getConfigurationSection("teams").getValues(false);
			}

			if (cfg.contains("spawner")) {
				if (cfg.isConfigurationSection("spawner")) {
					spawner = cfg.getConfigurationSection("spawner").getValues(false);

					for (Object obj : spawner.values()) {
						if (!(obj instanceof RessourceSpawner)) {
							continue;
						}

						RessourceSpawner rs = (RessourceSpawner) obj;
						rs.setGame(game);
						game.addRessourceSpawner(rs);
					}
				}

				if (cfg.isList("spawner")) {
					for (Object rs : cfg.getList("spawner")) {
						if (!(rs instanceof RessourceSpawner)) {
							continue;
						}

						RessourceSpawner rsp = (RessourceSpawner) rs;
						rsp.setGame(game);
						game.addRessourceSpawner(rsp);
					}
				}
			}

			for (Object obj : teams.values()) {
				if (!(obj instanceof Team)) {
					continue;
				}

				game.addTeam((Team) obj);
			}

			Location loc1 = Utils.locationDeserialize(cfg.get("loc1"));
			Location loc2 = Utils.locationDeserialize(cfg.get("loc2"));

			File signFile = new File(Main.getInstance().getDataFolder() + File.separator + GameManager.gamesPath
					+ File.separator + game.getName(), "sign.yml");
			if (signFile.exists()) {
				YamlConfiguration signConfig = YamlConfiguration.loadConfiguration(signFile);

				List<Object> signs = (List<Object>) signConfig.get("signs");
				for (Object sign : signs) {
					Location signLocation = Utils.locationDeserialize(sign);
					if (signLocation == null) {
						continue;
					}

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
			game.setLobby(Utils.locationDeserialize(cfg.get("lobby")));

			String regionName = loc1.getWorld().getName();

			if (cfg.contains("regionname")) {
				regionName = cfg.getString("regionname");
			}

			if (cfg.contains("time") && cfg.isInt("time")) {
				game.setTime(cfg.getInt("time"));
			}

			game.setRegionName(regionName);
			game.setRegion(new Region(loc1, loc2, regionName));

			if (cfg.contains("autobalance")) {
				game.setAutobalance(cfg.getBoolean("autobalance"));
			}

			if (cfg.contains("minplayers")) {
				game.setMinPlayers(cfg.getInt("minplayers"));
			}

			if (cfg.contains("mainlobby")) {
				game.setMainLobby(Utils.locationDeserialize(cfg.get("mainlobby")));
			}

			if (cfg.contains("record")) {
				game.setRecord(cfg.getInt("record", Main.getInstance().getMaxLength()));
			}

			if (cfg.contains("targetmaterial")) {
				targetMaterialObj = cfg.getString("targetmaterial");
				if (targetMaterialObj != null && !targetMaterialObj.equals("")) {
					game.setTargetMaterial(Utils.parseMaterial(targetMaterialObj));
				}
			}

			if (cfg.contains("builder")) {
				game.setBuilder(cfg.getString("builder"));
			}

			if (cfg.contains("record-holders")) {
				List<Object> list = (List<Object>) cfg.getList("record-holders", new ArrayList<Object>());
				for (Object holder : list) {
					game.addRecordHolder(holder.toString());
				}
			}

			game.getFreePlayers().clear();
			game.updateSigns();

			this.games.add(game);
			Main.getInstance().getServer().getConsoleSender().sendMessage(ChatWriter.pluginMessage(
					ChatColor.GREEN + Main._l("success.gameloaded", ImmutableMap.of("game", game.getName()))));
		} catch (Exception ex) {
			Main.getInstance().getServer().getConsoleSender().sendMessage(ChatWriter.pluginMessage(ChatColor.RED
					+ Main._l("errors.gameloaderror", ImmutableMap.of("game", configFile.getParentFile().getName()))));
		}
	}

	public void unloadGames() {
		for (Game g : this.games) {
			this.unloadGame(g);
		}

		this.games.clear();
	}

	public Game getGameByLocation(Location loc) {
		for (Game game : this.games) {
			if (game.getRegion() == null) {
				continue;
			}

			if (game.getRegion().getWorld() == null) {
				continue;
			}

			if (game.getRegion().isInRegion(loc)) {
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

	public List<Game> getGamesByWorld(World world) {
		List<Game> games = new ArrayList<Game>();

		for (Game game : this.games) {
			if (game.getRegion() == null) {
				continue;
			}

			if (game.getRegion().getWorld() == null) {
				continue;
			}

			if (game.getRegion().getWorld().equals(world)) {
				games.add(game);
			}
		}

		return games;
	}

	public Game getGameByChunkLocation(int x, int z) {
		for (Game game : this.games) {
			if (game.getRegion().chunkIsInRegion(x, z)) {
				return game;
			}
		}

		return null;
	}

	public int getGamePlayerAmount() {
		return this.gamePlayer.size();
	}

}
