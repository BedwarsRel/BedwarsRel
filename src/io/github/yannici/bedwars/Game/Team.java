package io.github.yannici.bedwars.Game;

import io.github.yannici.bedwars.Main;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.SerializableAs;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;

@SerializableAs("Team")
public class Team implements ConfigurationSerializable {

	private TeamColor color = null;
	private org.bukkit.scoreboard.Team scoreboardTeam = null;
	private String name = null;
	private int maxPlayers = 0;
	private Location spawnLocation = null;
	private Location bedHeadBlock = null;
	private Location bedFeedBlock = null;
	private Inventory inventory = null;
	private List<Block> chests = null;

	public Team(Map<String, Object> deserialize) {
		this.name = deserialize.get("name").toString();
		this.maxPlayers = Integer.parseInt(deserialize.get("maxplayers")
				.toString());
		this.color = TeamColor.valueOf(deserialize.get("color").toString()
				.toUpperCase());
		this.spawnLocation = (Location) deserialize.get("spawn");
		this.chests = new ArrayList<Block>();

		if (deserialize.containsKey("bedhead") && deserialize.containsKey("bedfeed")) {
			this.bedHeadBlock = ((Location) deserialize.get("bedhead"));
			this.bedFeedBlock = ((Location) deserialize.get("bedfeed"));
		}

	}

	public Team(String name, TeamColor color, int maxPlayers,
			org.bukkit.scoreboard.Team sTeam) {
		this.name = name;
		this.color = color;
		this.maxPlayers = maxPlayers;
		this.scoreboardTeam = sTeam;
		this.chests = new ArrayList<Block>();
	}

	public boolean addPlayer(Player player) {
		if (this.scoreboardTeam.getPlayers().size() >= this.maxPlayers) {
			return false;
		}

		this.scoreboardTeam.addPlayer(player);
		return true;
	}

	public org.bukkit.scoreboard.Team getScoreboardTeam() {
		return this.scoreboardTeam;
	}

	public int getMaxPlayers() {
		return this.maxPlayers;
	}

	public void setBeds(Block head, Block feed) {
		this.bedHeadBlock = head.getLocation();
		this.bedFeedBlock = feed.getLocation();
	}

	public Block getHeadBed() {
		if(this.bedHeadBlock == null) {
			return null;
		}
		
		this.bedHeadBlock.getBlock().getChunk().load(true);
		return this.bedHeadBlock.getBlock();
	}
	
	public Block getFeedBed() {
		if(this.bedFeedBlock == null) {
			return null;
		}
		
		this.bedHeadBlock.getBlock().getChunk().load(true);
		return this.bedFeedBlock.getBlock();
	}

	public void removePlayer(OfflinePlayer player) {
		if (this.scoreboardTeam.hasPlayer(player)) {
			this.scoreboardTeam.removePlayer(player);
		}
	}

	public boolean isInTeam(Player p) {
		if (this.scoreboardTeam.hasPlayer(p)) {
			return true;
		}

		return false;
	}

	public void setScoreboardTeam(org.bukkit.scoreboard.Team sbt) {
		this.scoreboardTeam = sbt;
		sbt.setDisplayName(this.getChatColor() + this.name);
	}

	public TeamColor getColor() {
		return this.color;
	}

	public ChatColor getChatColor() {
		return this.color.getChatColor();
	}

	public String getName() {
		return this.name;
	}

	public ArrayList<Player> getPlayers() {
		ArrayList<Player> players = new ArrayList<>();
		for (OfflinePlayer player : this.scoreboardTeam.getPlayers()) {
			if (player.isOnline()) {
				players.add(player.getPlayer());
			}
		}

		return players;
	}

	public Location getSpawnLocation() {
		return this.spawnLocation;
	}

	public void setSpawnLocation(Location spawn) {
		this.spawnLocation = spawn;
	}

	public boolean isDead() {
		this.bedHeadBlock.getBlock().getChunk().load(true);
		this.bedFeedBlock.getBlock().getChunk().load(true);
		
		return (this.bedHeadBlock.getBlock().getType() != Material.BED_BLOCK
				&& this.bedFeedBlock.getBlock().getType() != Material.BED_BLOCK);
	}

	@Override
	public Map<String, Object> serialize() {
		HashMap<String, Object> team = new HashMap<>();

		team.put("name", this.name);
		team.put("color", this.color.toString());
		team.put("maxplayers", this.maxPlayers);
		team.put("spawn", this.spawnLocation);
		team.put("bedhead", this.bedHeadBlock);
		team.put("bedfeed", this.bedFeedBlock);
		return team;
	}

	public String getDisplayName() {
		return this.scoreboardTeam.getDisplayName();
	}

	public void setInventory(Inventory inv) {
		this.inventory = inv;
	}

	public Inventory getInventory() {
		return this.inventory;
	}

	public void addChest(Block chestBlock) {
		this.chests.add(chestBlock);
	}

	public void removeChest(Block chest) {
		this.chests.remove(chest);
		if (this.chests.size() == 0) {
			this.inventory = null;
		}
	}

	public List<Block> getChests() {
		return this.chests;
	}

	public void createTeamInventory() {
		Inventory inv = Bukkit.createInventory(null, InventoryType.ENDER_CHEST,
				Main._l("ingame.teamchest"));
		this.inventory = inv;
	}

}
