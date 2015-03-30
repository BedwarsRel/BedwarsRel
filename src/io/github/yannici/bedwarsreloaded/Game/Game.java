package io.github.yannici.bedwarsreloaded.Game;

import io.github.yannici.bedwarsreloaded.ChatWriter;
import io.github.yannici.bedwarsreloaded.Main;
import io.github.yannici.bedwarsreloaded.Villager.MerchantCategory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import org.bukkit.ChatColor;
import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;

public class Game {

    private String name = null;
    private ArrayList<RessourceSpawner> resSpawner = null;
    private ArrayList<BukkitTask> runningTasks = null;
    private GameState state = null;
    private HashMap<String, Team> teams = null;
    private ArrayList<Player> freePlayers = null;
    private int minPlayers = 0;
    private Region region = null;
    private Location lobby = null;
    private HashMap<Player, PlayerStorage> storages = null;
    private Scoreboard scoreboard = null;
    private GameLobbyCountdown glc = null;
    private HashMap<Material, MerchantCategory> itemshop = null;
    private GameCycle cycle = null;

    private Location loc1 = null;
    private Location loc2 = null;

    private Main plugin = null;

    public Game(Main plugin, String name) {
        super();
        
        this.plugin = plugin;
        this.name = name;
        this.runningTasks = new ArrayList<BukkitTask>();

        this.freePlayers = new ArrayList<Player>();
        this.resSpawner = new ArrayList<RessourceSpawner>();
        this.teams = new HashMap<String, Team>();
        this.storages = new HashMap<Player, PlayerStorage>();
        this.state = GameState.STOPPED;
        this.scoreboard = Main.getInstance().getScoreboardManager().getNewScoreboard();
        this.glc = new GameLobbyCountdown(this);
        
        if(Main.getInstance().getConfig().getBoolean("bungee")) {
        	this.cycle = new BungeeGameCycle(this);
        } else {
        	this.cycle = new SingleGameCycle(this);
        }
    }

    /*
     * STATIC
     */

    public static Game getGameOfPlayer(Player p) {
        for(Game g : Main.getInstance().getGameManager().getGames()) {
            if(g.isInGame(p)) {
                return g;
            }
        }

        return null;
    }

    public static Team getPlayerTeam(Player p, Game g) {
        for(Team team : g.getTeams().values()) {
            if(team.isInTeam(p)) {
                return team;
            }
        }

        return null;
    }

    /*
     * PUBLIC
     */

    public boolean run(CommandSender sender) {
        if(this.state != GameState.STOPPED) {
            sender.sendMessage(ChatWriter.pluginMessage(ChatColor.RED + "Game is running! You can't start a running game again!"));
            return false;
        }

        if(!this.saveGame(sender, false)) {
            return false;
        }
        
        if(this.minPlayers == 0) {
            this.minPlayers = 1; //Math.round(this.getMaxPlayers()/2);
        }
        
        this.loadItemShopCategories();
        
        this.state = GameState.WAITING;
        return true;
    }

    public boolean start(CommandSender sender) {
        if(this.state != GameState.WAITING) {
            sender.sendMessage(ChatWriter.pluginMessage(ChatColor.RED + "Game have to be started out of the waiting mode!"));
            return false;
        }

        this.broadcast(ChatColor.GREEN + "Game starting ...");
        
        this.setTeamsFriendlyFire();
        this.cleanUsersInventory();
        this.moveFreePlayersToTeam();
        
        this.cycle.onGameStart();
        
        this.startRessourceSpawners();
        this.setPlayersScoreboard();
        this.teleportPlayersToTeamSpawn();

        this.state = GameState.RUNNING;
        this.getPlugin().getServer().broadcastMessage(ChatWriter.pluginMessage(ChatColor.GREEN + "Game " + this.name + " has just started!"));
        return true;
    }

    public boolean stop() {
        if(this.state == GameState.STOPPED) {
            return false;
        }

        this.stopWorkers();
        this.kickAllPlayers();
        this.state = GameState.STOPPED;
        return true;
    }

    public boolean isInGame(Player p) {
        for(Team t : this.teams.values()) {
            if(t.isInTeam(p)) {
                return true;
            }
        }

        return this.freePlayers.contains(p);
    }

    public void addRessourceSpawner(int interval, Location location, ItemStack stack) {
        this.resSpawner.add(new RessourceSpawner(this, interval, location, stack));
    }

    public void addRessourceSpawner(RessourceSpawner rs) {
        this.resSpawner.add(rs);
    }

    public ArrayList<RessourceSpawner> getRessourceSpawner() {
        return this.resSpawner;
    }

    public void setLoc(Location loc, String type) {
        if(type.equalsIgnoreCase("loc1")) {
            this.loc1 = loc;
        } else {
            this.loc2 = loc;
        }
    }

    public boolean saveGame(CommandSender sender, boolean direct) {
        GameCheckCode check = this.checkGame();

        if(check != GameCheckCode.OK) {
            sender.sendMessage(ChatWriter.pluginMessage(ChatColor.RED + check.getCodeMessage()));
            return false;
        }

        File gameConfig = new File(this.getPlugin().getDataFolder() + "/" + GameManager.gamesPath + "/" + this.name + "/game.yml");
        gameConfig.mkdirs();

        if(gameConfig.exists()) {
            gameConfig.delete();
        }

        this.saveRegion(direct);
        this.createGameConfig(gameConfig);

        return true;
    }
    public int getMaxPlayers() {
        int max = 0;
        for(Team t : this.teams.values()) {
            max += t.getMaxPlayers();
        }

        return max;
    }

    public int getCurrentPlayerAmount() {
        int amount = 0;
        for(Team t : this.teams.values()) {
            amount += t.getPlayers().size();
        }

        return amount;
    }

    public boolean isFull() {
        return (this.getMaxPlayers() <= this.getCurrentPlayerAmount());
    }

    public void addTeam(String name, TeamColor color, int maxPlayers) {
        org.bukkit.scoreboard.Team newTeam = this.scoreboard.registerNewTeam(name);
        newTeam.setDisplayName(color.getChatColor() + name);
        this.teams.put(name, new Team(name, color, maxPlayers, newTeam));
    }

    public void addTeam(Team team) {
        org.bukkit.scoreboard.Team newTeam = this.scoreboard.registerNewTeam(team.getName());
        team.setScoreboardTeam(newTeam);
        this.teams.put(team.getName(), team);
    }

    public boolean playerJoins(Player p) {
        if(this.state != GameState.WAITING) {
            p.sendMessage(ChatWriter.pluginMessage(ChatColor.RED + "You can't join a running or stopped game!"));
            return false;
        }

        if(this.cycle.onPlayerJoins(p)) {
            return false;
        }

        this.freePlayers.add(p);

        PlayerStorage storage = this.addPlayerStorage(p);
        storage.store();
        storage.clean();

        p.teleport(this.lobby);
        storage.loadLobbyInventory();

        if(this.getPlayers().size() >= this.minPlayers) {
            try {
                this.glc.getTaskId();
                // scheduled
            } catch(Exception ex) {
            	// not scheduled
                this.glc.runTaskTimer(Main.getInstance(), 20L, 20L);
            }
        }

        return true;
    }

    public boolean playerLeave(Player p) {
    	Team team = Game.getPlayerTeam(p, this);
        if(team != null) {
            team.removePlayer(p);
        }

        if(this.freePlayers.contains(p)) {
            this.freePlayers.remove(p);
        }

        PlayerStorage storage = this.storages.get(p);
        storage.restore();
        
        p.setScoreboard(Main.getInstance().getScoreboardManager().getNewScoreboard());
        
        this.cycle.onPlayerLeave(p);
        this.broadcast(ChatWriter.pluginMessage(ChatColor.RED + "Player \"" + p.getName() + "\" has left the game!"));
        return true;
    }

    public PlayerStorage addPlayerStorage(Player p) {
        PlayerStorage storage = new PlayerStorage(p);
        this.storages.put(p, storage);

        return storage;
    }

    public void broadcast(String message) {
        for(Player p : this.getPlayers()) {
            if(p.isOnline()) {
                p.sendMessage(ChatWriter.pluginMessage(message));
            }
        }
    }

    public void broadcast(String message, ArrayList<Player> players) {
        for(Player p : players) {
            if(p.isOnline()) {
                p.sendMessage(ChatWriter.pluginMessage(message));
            }
        }
    }

    public GameCheckCode checkGame() {
        if(this.loc1 == null || this.loc2 == null) {
            return GameCheckCode.LOC_NOT_SET_ERROR;
        }

        if(this.teams.size() <= 1) {
            return GameCheckCode.TEAM_SIZE_LOW_ERROR;
        }
        
        GameCheckCode teamCheck = this.checkTeams();
        if(teamCheck != GameCheckCode.OK) {
        	return teamCheck;
        }
        
        if(this.getRessourceSpawner().size() == 0) {
            return GameCheckCode.NO_RES_SPAWNER_ERROR;
        }

        if(this.lobby == null) {
            return GameCheckCode.NO_LOBBY_SET;
        }

        return GameCheckCode.OK;
    }

    public void nonFreePlayer(Player p) {
        if(this.freePlayers.contains(p)) {
            this.freePlayers.remove(p);
        }
    }
    
    public void loadItemShopCategories() {
        if(this.itemshop != null) {
            return;
        }
        
        FileConfiguration cfg = Main.getInstance().getConfig();
        this.itemshop = MerchantCategory.loadCategories(cfg);
    }
    
    public void kickAllPlayers() {
        for(Player p : this.getPlayers()) {
            this.playerLeave(p);
        }
    }
    
    public void resetRegion() {
        if(this.region == null) {
            return;
        }

        File file = new File(this.getPlugin().getDataFolder() + "/" + GameManager.gamesPath + "/" + this.name + "/region.bw");
        this.region.reset(file);
    }

    /*
     * GETTER / SETTER
     */
    
    public GameCycle getCycle() {
    	return this.cycle;
    }
    
    public void setItemShopCategories(HashMap<Material, MerchantCategory> cats) {
        this.itemshop = cats;
    }
    
    public HashMap<Material, MerchantCategory> getItemShopCategories() {
        return this.itemshop;
    }
    
    public Team getTeamByDyeColor(DyeColor dyeColor) {
        for(Team t : this.teams.values()) {
            if(t.getColor().getDyeColor().equals(dyeColor)) {
                return t;
            }
        }

        return null;
    }

    public HashMap<String, Team> getTeams() {
        return this.teams;
    }

    public Region getRegion() {
        return this.region;
    }

    public ArrayList<Player> getTeamPlayers() {
        ArrayList<Player> players = new ArrayList<>();

        for(Team team : this.teams.values()) {
            players.addAll(team.getPlayers());
        }

        return players;
    }
    
    public ArrayList<Player> getPlayers() {
        ArrayList<Player> players = new ArrayList<>();
        
        players.addAll(this.freePlayers);

        for(Team team : this.teams.values()) {
            players.addAll(team.getPlayers());
        }

        return players;
    }

    public int getMinPlayers() {
        return this.minPlayers;
    }

    public GameState getState() {
        return state;
    }

    public void setState(GameState state) {
        this.state = state;
    }

    public Main getPlugin() {
        return this.plugin;
    }

    public String getName() {
        return this.name;
    }

    public void setRegion(Region region) {
        this.region = region;
    }

    public void setMinPlayers(int players) {
        int max = this.getMaxPlayers();
        if(max < players) {
            players = max;
        }

        this.minPlayers = players;
    }

    public Location getLobby() {
        return this.lobby;
    }

    public void setLobby(Player sender) {
        Location lobby = sender.getLocation();

        if(this.region != null) {
            if(this.region.getWorld().equals(lobby.getWorld())) {
                sender.sendMessage(ChatWriter.pluginMessage(ChatColor.RED + "Lobby can't be on the game world!"));
                return;
            }
        }

        this.lobby = lobby;
        sender.sendMessage(ChatWriter.pluginMessage(ChatColor.GREEN + "Lobby was set successfully!"));
    }

    public void setLobby(Location lobby) {
        if(this.region != null) {
            if(this.region.getWorld().equals(lobby.getWorld())) {
                Main.getInstance().getServer().getConsoleSender().sendMessage(ChatWriter.pluginMessage(ChatColor.RED + "Lobby can't be on the game world!"));
                return;
            }
        }

        this.lobby = lobby;
    }

    public Team getTeam(String name) {
        return this.teams.get(name);
    }

    public PlayerStorage getPlayerStorage(Player p) {
        return this.storages.get(p);
    }

    /*
     * PRIVATE
     */
    
    private void setPlayersScoreboard() {
    	Objective obj = this.scoreboard.registerNewObjective("display", "dummy");
    	obj.setDisplaySlot(DisplaySlot.SIDEBAR);
    	
    	obj.setDisplayName("Teams");
    	
    	for(Team t : this.teams.values()) {
    		Score score = obj.getScore(ChatColor.RED + "✘ " + t.getChatColor() + t.getName() + ChatColor.RED);
    		score.setScore(t.getPlayers().size());
    	}
    	
    	for(Player player : this.getPlayers()) {
    		player.setScoreboard(this.scoreboard);
    	}
    }
    
    private void setTeamsFriendlyFire() {
        for(org.bukkit.scoreboard.Team team : this.scoreboard.getTeams()) {
            team.setAllowFriendlyFire(Main.getInstance().getConfig().getBoolean("friendlyfire"));
        }
    }
    
    private void cleanUsersInventory() {
        for(PlayerStorage storage : this.storages.values()) {
            storage.clean();
        }
    }
    
    private void createGameConfig(File config) {
        YamlConfiguration yml = new YamlConfiguration();
        HashMap<String, RessourceSpawner> spawnerMap = new HashMap<>();
        int i = 0;

        for(RessourceSpawner rs : this.resSpawner) {
            spawnerMap.put("SPAWNER_" + i, rs);
            i++;
        }

        yml.set("name", this.name);
        yml.set("world", this.getRegion().getWorld().getName());
        yml.set("loc1", this.loc1);
        yml.set("loc2", this.loc2);
        yml.set("lobby", this.lobby);
        yml.createSection("teams", this.teams);
        yml.createSection("spawner", spawnerMap);

        try {
            yml.save(config);
        } catch (IOException e) {
            Main.getInstance().getLogger().info(ChatWriter.pluginMessage(e.getMessage()));
        }
    }

    private void stopWorkers() {
        for(BukkitTask task : this.runningTasks) {
            task.cancel();
        }
    }

    private void saveRegion(boolean direct) {
        try {

            if(this.region == null || direct) {
                this.region = new Region(this.loc1, this.loc2);
            }

            File file = new File(this.getPlugin().getDataFolder() + "/" + GameManager.gamesPath + "/" + this.name + "/region.bw");

            if(file.exists()) {
                file.delete();
            }

            this.region.save(file, direct);
        } catch (IOException e) {
            Main.getInstance().getLogger().info(ChatWriter.pluginMessage(e.getMessage()));
        }
    }

    private void startRessourceSpawners() {
        for(RessourceSpawner rs : this.getRessourceSpawner()) {
            rs.setGame(this);
            this.runningTasks.add(this.getPlugin().getServer().getScheduler().runTaskTimer(this.getPlugin(), rs, ((long)(1000/1000)*20), ((long)(rs.getInterval()/1000)*20)));
        }
    }

    private void teleportPlayersToTeamSpawn() {
        for(Team team : this.teams.values()) {
            for(Player player : team.getPlayers()) {
                player.teleport(team.getSpawnLocation());
            }
        }
    }

    private Team getLowestTeam() {
        Team lowest = null;
        for(Team team : this.teams.values()) {
            if(lowest == null) {
                lowest = team;
                continue;
            }

            if(team.getPlayers().size() < lowest.getPlayers().size()) {
                lowest = team;
            }
        }

        return lowest;
    }

    private void moveFreePlayersToTeam() {
        for(Player player : this.freePlayers) {
            Team lowest = this.getLowestTeam();
            lowest.addPlayer(player);
        }

        this.freePlayers = new ArrayList<Player>();
    }
    
    private GameCheckCode checkTeams() {
    	for(Team t : this.teams.values()) {
    		if(t.getSpawnLocation() == null) {
    			return GameCheckCode.TEAMS_WITHOUT_SPAWNS;
    		}
    	}
    	
    	return GameCheckCode.OK;
    }

}
