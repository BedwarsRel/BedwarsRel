package io.github.yannici.bedwars.Game;

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
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;

import io.github.yannici.bedwars.Main;
import io.github.yannici.bedwars.Utils;

@SerializableAs("Team")
public class Team implements ConfigurationSerializable {

	private TeamColor color = null;
	private org.bukkit.scoreboard.Team scoreboardTeam = null;
	private String name = null;
	private int maxPlayers = 0;
	private Location spawnLocation = null;
	private Location targetHeadBlock = null;
	private Location targetFeetBlock = null;
	private Inventory inventory = null;
	private List<Block> chests = null;

	public Team(Map<String, Object> deserialize) {
		this.name = deserialize.get("name").toString();
		this.maxPlayers = Integer.parseInt(deserialize.get("maxplayers").toString());
		this.color = TeamColor.valueOf(deserialize.get("color").toString().toUpperCase());
		this.spawnLocation = Utils.locationDeserialize(deserialize.get("spawn"));
		this.chests = new ArrayList<Block>();

		if (deserialize.containsKey("bedhead")) {
			this.targetHeadBlock = Utils.locationDeserialize(deserialize.get("bedhead"));

			if (this.targetHeadBlock != null) {
				if (deserialize.containsKey("bedfeed")
						&& this.targetHeadBlock.getBlock().getType().equals(Material.BED_BLOCK)) {
					this.targetFeetBlock = Utils.locationDeserialize(deserialize.get("bedfeed"));
				}
			}
		}
	}

	public Team(String name, TeamColor color, int maxPlayers, org.bukkit.scoreboard.Team sTeam) {
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

		try {
			boolean overwriteNames = Main.getInstance().getBooleanConfig("overwrite-names", false);
			if (overwriteNames) {
				player.setDisplayName(this.getChatColor() + ChatColor.stripColor(player.getName()));
				player.setPlayerListName(this.getChatColor() + ChatColor.stripColor(player.getName()));
			}

			boolean teamnameOnTab = Main.getInstance().getBooleanConfig("teamname-on-tab", true);
			if (teamnameOnTab) {
				player.setPlayerListName(this.getChatColor() + this.getName() + ChatColor.WHITE + " | "
						+ this.getChatColor() + ChatColor.stripColor(player.getDisplayName()));
			}
		} catch (Exception ex) {
			ex.printStackTrace();
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

	public void setTargets(Block head, Block feed) {
		this.targetHeadBlock = head.getLocation();
		if (feed != null) {
			this.targetFeetBlock = feed.getLocation();
		} else {
			this.targetFeetBlock = null;
		}
	}

	public Block getHeadTarget() {
		if (this.targetHeadBlock == null) {
			return null;
		}

		this.targetHeadBlock.getBlock().getChunk().load(true);
		return this.targetHeadBlock.getBlock();
	}

	public Block getFeetTarget() {
		if (this.targetFeetBlock == null) {
			return null;
		}

		this.targetFeetBlock.getBlock().getChunk().load(true);
		return this.targetFeetBlock.getBlock();
	}

	public void removePlayer(OfflinePlayer player) {
		if (this.scoreboardTeam.hasPlayer(player)) {
			this.scoreboardTeam.removePlayer(player);
		}

		boolean overwriteNames = Main.getInstance().getBooleanConfig("overwrite-names", false);
		if (overwriteNames) {
			if (player.isOnline()) {
				player.getPlayer().setDisplayName(ChatColor.RESET + ChatColor.stripColor(player.getPlayer().getName()));
				player.getPlayer().setPlayerListName(ChatColor.RESET + player.getPlayer().getName());
			}
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

	public List<Player> getPlayers() {
		List<Player> players = new ArrayList<>();
		for (OfflinePlayer player : this.scoreboardTeam.getPlayers()) {
			if (player.isOnline()) {
				players.add(player.getPlayer());
			}
		}

		return players;
	}

	public List<OfflinePlayer> getTeamPlayers() {
		List<OfflinePlayer> players = new ArrayList<>();
		for (OfflinePlayer player : this.scoreboardTeam.getPlayers()) {
			players.add(player);
		}

		return players;
	}

	public Location getSpawnLocation() {
		return this.spawnLocation;
	}

	public void setSpawnLocation(Location spawn) {
		this.spawnLocation = spawn;
	}

	public boolean isDead(Game game) {
		Material targetMaterial = game.getTargetMaterial();

		this.targetHeadBlock.getBlock().getChunk().load(true);
		if (this.targetFeetBlock == null) {
			return this.targetHeadBlock.getBlock().getType() != targetMaterial;
		}

		this.targetFeetBlock.getBlock().getChunk().load(true);
		return (this.targetHeadBlock.getBlock().getType() != targetMaterial
				&& this.targetFeetBlock.getBlock().getType() != targetMaterial);
	}

	@Override
	public Map<String, Object> serialize() {
		HashMap<String, Object> team = new HashMap<>();

		team.put("name", this.name);
		team.put("color", this.color.toString());
		team.put("maxplayers", this.maxPlayers);
		team.put("spawn", Utils.locationSerialize(this.spawnLocation));
		team.put("bedhead", Utils.locationSerialize(this.targetHeadBlock));

		if (this.targetFeetBlock != null) {
			team.put("bedfeed", Utils.locationSerialize(this.targetFeetBlock));
		}

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
		Inventory inv = Bukkit.createInventory(null, InventoryType.ENDER_CHEST, Main._l("ingame.teamchest"));
		this.inventory = inv;
	}

	public void equipPlayerWithLeather(Player player) {
		// helmet
		ItemStack helmet = new ItemStack(Material.LEATHER_HELMET, 1);
		LeatherArmorMeta meta = (LeatherArmorMeta) helmet.getItemMeta();
		meta.setColor(this.getColor().getColor());
		helmet.setItemMeta(meta);

		// chestplate
		ItemStack chestplate = new ItemStack(Material.LEATHER_CHESTPLATE, 1);
		meta = (LeatherArmorMeta) chestplate.getItemMeta();
		meta.setColor(this.getColor().getColor());
		chestplate.setItemMeta(meta);

		// leggings
		ItemStack leggings = new ItemStack(Material.LEATHER_LEGGINGS, 1);
		meta = (LeatherArmorMeta) leggings.getItemMeta();
		meta.setColor(this.getColor().getColor());
		leggings.setItemMeta(meta);

		// boots
		ItemStack boots = new ItemStack(Material.LEATHER_BOOTS, 1);
		meta = (LeatherArmorMeta) boots.getItemMeta();
		meta.setColor(this.getColor().getColor());
		boots.setItemMeta(meta);

		player.getInventory().setHelmet(helmet);
		player.getInventory().setChestplate(chestplate);
		player.getInventory().setLeggings(leggings);
		player.getInventory().setBoots(boots);
		player.updateInventory();
	}

}
