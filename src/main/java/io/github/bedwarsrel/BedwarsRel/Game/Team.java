package io.github.bedwarsrel.BedwarsRel.Game;

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

import io.github.bedwarsrel.BedwarsRel.Main;
import io.github.bedwarsrel.BedwarsRel.Utils;
import lombok.Data;

@Data
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
    this.setName(deserialize.get("name").toString());
    this.setMaxPlayers(Integer.parseInt(deserialize.get("maxplayers").toString()));
    this.setColor(TeamColor.valueOf(deserialize.get("color").toString().toUpperCase()));
    this.setSpawnLocation(Utils.locationDeserialize(deserialize.get("spawn")));
    this.setChests(new ArrayList<Block>());

    if (deserialize.containsKey("bedhead")) {
      this.setTargetHeadBlock(Utils.locationDeserialize(deserialize.get("bedhead")));

      if (this.getTargetHeadBlock() != null && deserialize.containsKey("bedfeed")
          && this.getTargetHeadBlock().getBlock().getType().equals(Material.BED_BLOCK)) {
        this.setTargetFeetBlock(Utils.locationDeserialize(deserialize.get("bedfeed")));
      }
    }
  }

  public Team(String name, TeamColor color, int maxPlayers,
      org.bukkit.scoreboard.Team scoreboardTeam) {
    this.setName(name);
    this.setColor(color);
    this.setMaxPlayers(maxPlayers);
    this.setScoreboardTeam(scoreboardTeam);
    this.setChests(new ArrayList<Block>());
  }

  public void addChest(Block chestBlock) {
    this.getChests().add(chestBlock);
  }

  public boolean addPlayer(Player player) {
    if (this.getScoreboardTeam().getPlayers().size() >= this.getMaxPlayers()) {
      return false;
    }

    if (Main.getInstance().getBooleanConfig("overwrite-names", false)) {
      player.setDisplayName(this.getChatColor() + ChatColor.stripColor(player.getName()));
      player.setPlayerListName(this.getChatColor() + ChatColor.stripColor(player.getName()));
    }

    if (Main.getInstance().getBooleanConfig("teamname-on-tab", true)) {
      player.setPlayerListName(this.getChatColor() + this.getName() + ChatColor.WHITE + " | "
          + this.getChatColor() + ChatColor.stripColor(player.getDisplayName()));
    }

    this.getScoreboardTeam().addPlayer(player);
    this.equipPlayerWithLeather(player);

    return true;
  }

  public void createTeamInventory() {
    Inventory inventory =
        Bukkit.createInventory(null, InventoryType.ENDER_CHEST, Main._l("ingame.teamchest"));
    this.setInventory(inventory);
  }

  private void equipPlayerWithLeather(Player player) {
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

  public ChatColor getChatColor() {
    return this.getColor().getChatColor();
  }

  public String getDisplayName() {
    return this.getScoreboardTeam().getDisplayName();
  }

  public Block getFeetTarget() {
    if (this.getTargetFeetBlock() == null) {
      return null;
    }

    this.getTargetFeetBlock().getBlock().getChunk().load(true);
    return this.getTargetFeetBlock().getBlock();
  }

  public Block getHeadTarget() {
    if (this.targetHeadBlock == null) {
      return null;
    }

    this.getTargetHeadBlock().getBlock().getChunk().load(true);
    return this.getTargetHeadBlock().getBlock();
  }

  public List<Player> getPlayers() {
    List<Player> players = new ArrayList<>();
    for (OfflinePlayer offlinePlayer : this.getScoreboardTeam().getPlayers()) {
      Player player = Main.getInstance().getServer().getPlayer(offlinePlayer.getName());
      if (player != null && Main.getInstance().getGameManager().getGameOfPlayer(player) != null
          && !Main.getInstance().getGameManager().getGameOfPlayer(player).isSpectator(player)) {
        players.add(player);
      }
    }

    return players;
  }

  public boolean isDead(Game game) {
    Material targetMaterial = game.getTargetMaterial();

    this.getTargetHeadBlock().getBlock().getChunk().load(true);
    if (this.getTargetFeetBlock() == null) {
      return this.getTargetHeadBlock().getBlock().getType() != targetMaterial;
    }

    this.getTargetFeetBlock().getBlock().getChunk().load(true);
    return (this.getTargetHeadBlock().getBlock().getType() != targetMaterial
        && this.getTargetFeetBlock().getBlock().getType() != targetMaterial);
  }

  public boolean isInTeam(Player p) {
    return this.getScoreboardTeam().hasPlayer(p);
  }

  public void removeChest(Block chest) {
    this.getChests().remove(chest);
    if (this.getChests().size() == 0) {
      this.setInventory(null);
    }
  }

  public void removePlayer(Player player) {
    if (this.getScoreboardTeam().hasPlayer(player)) {
      this.getScoreboardTeam().removePlayer(player);
    }

    if (Main.getInstance().getBooleanConfig("overwrite-names", false) && player.isOnline()) {
      player.setDisplayName(ChatColor.RESET + ChatColor.stripColor(player.getName()));
      player.setPlayerListName(ChatColor.RESET + player.getPlayer().getName());
    }
  }

  @Override
  public Map<String, Object> serialize() {
    HashMap<String, Object> team = new HashMap<>();

    team.put("name", this.getName());
    team.put("color", this.getColor().toString());
    team.put("maxplayers", this.getMaxPlayers());
    team.put("spawn", Utils.locationSerialize(this.getSpawnLocation()));
    team.put("bedhead", Utils.locationSerialize(this.getTargetHeadBlock()));

    if (this.targetFeetBlock != null) {
      team.put("bedfeed", Utils.locationSerialize(this.targetFeetBlock));
    }

    return team;
  }

  public void setScoreboardTeam(org.bukkit.scoreboard.Team scoreboardTeam) {
    scoreboardTeam.setDisplayName(this.getChatColor() + this.name);
    this.scoreboardTeam = scoreboardTeam;
  }

  public void setTargets(Block headBlock, Block feetBlock) {
    this.setTargetHeadBlock(headBlock.getLocation());
    if (feetBlock != null) {
      this.setTargetFeetBlock(feetBlock.getLocation());
    } else {
      this.setTargetFeetBlock(null);
    }
  }

}
