package io.github.yannici.bedwarsreloaded.Game;

import io.github.yannici.bedwarsreloaded.ChatWriter;
import io.github.yannici.bedwarsreloaded.Main;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.YamlConfiguration;

public class GameManager {

    public static String gamesPath = "games";

    private ArrayList<Game> games = null;
    private Main plugin = null;

    public GameManager(Main plugin) {
        this.plugin = plugin;
        this.games = new ArrayList<Game>();
    }

    public boolean addGame(String name) {
        Game existing = this.getGame(name);
        if(existing != null) {
            return false;
        }

        this.games.add(new Game(this.plugin, name));
        return true;
    }

    public ArrayList<Game> getGames() {
        return this.games;
    }

    public Game getGame(String name) {
        for(Game game : this.games) {
            if(game.getName().equals(name)) {
                return game;
            }
        }

        return null;
    }

    public void loadGames() {
        String path = Main.getInstance().getDataFolder() + "/" + GameManager.gamesPath;
        File file = new File(path);

        if(!file.exists()) {
            return;
        }

        File[] files = file.listFiles(new FileFilter() {

            @Override
            public boolean accept(File pathname) {
                return pathname.isDirectory();
            }
        });

        if(files.length > 0) {
            for(File dir : files) {
                File[] configFiles = dir.listFiles();
                for(File cfg : configFiles) {
                    if(!cfg.isFile()) {
                        continue;
                    }

                    if(cfg.getName().equals("game.yml")) {
                        this.loadGame(cfg);
                    }
                }
            }
        }

        for(Game g : this.games) {
            if(!g.run(Main.getInstance().getServer().getConsoleSender())) {
                Main.getInstance().getServer().getConsoleSender().sendMessage(ChatWriter.pluginMessage(ChatColor.RED + "Couldn't start up the game!"));
            } else {
            	g.getCycle().onGameLoaded();
            }
        }
    }

    private void loadGame(File configFile) {
        try {

            YamlConfiguration cfg = YamlConfiguration.loadConfiguration(configFile);
            String name = cfg.get("name").toString();
            if(name.isEmpty()) {
                return;
            }

            Game game = new Game(Main.getInstance(), name);

            Map<String, Object> teams = new HashMap<String, Object>();
            Map<String, Object> spawner = new HashMap<String, Object>();

            if(cfg.contains("teams")) {
                teams = cfg.getConfigurationSection("teams").getValues(false);
            }

            if(cfg.contains("spawner")) {
                spawner = cfg.getConfigurationSection("spawner").getValues(false);
            }

            for(Object obj : teams.values()) {
                if(!(obj instanceof Team)) {
                    continue;
                }

                game.addTeam((Team)obj);
            }

            for(Object obj : spawner.values()) {
                if(!(obj instanceof RessourceSpawner)) {
                    continue;
                }

                RessourceSpawner rs = (RessourceSpawner)obj;
                rs.setGame(game);
                game.addRessourceSpawner(rs);
            }

            Location loc1 = (Location)cfg.get("loc1");
            Location loc2 = (Location)cfg.get("loc2");

            game.setLoc(loc1, "loc1");
            game.setLoc(loc2, "loc2");
            game.setLobby((Location)cfg.get("lobby"));
            game.setRegion(new Region(loc1, loc2));
            
            

            this.games.add(game);
            Main.getInstance().getServer().getConsoleSender().sendMessage(ChatWriter.pluginMessage(ChatColor.GREEN + "Load Game \"" + game.getName() + "\" successfully!"));
        } catch(Exception ex) {
            Main.getInstance().getServer().getConsoleSender().sendMessage(ChatWriter.pluginMessage(ChatColor.RED + "Loading game \"" + configFile.getParentFile().getName() + "\" throws an error!"));
        }
    }

    public void unloadGames() {
        for(Game g : this.games) {
            g.kickAllPlayers();
        }

        this.games.clear();
    }

    public Game getGameByWorld(World world) {
        for(Game game : this.games) {
            if(game.getRegion().getWorld().equals(world)) {
                return game;
            }
        }
        
        return null;
    }

}
