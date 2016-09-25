package io.github.bedwarsrel.BedwarsRel.Game;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;

import io.github.bedwarsrel.BedwarsRel.Main;
import io.github.bedwarsrel.BedwarsRel.Events.BedwarsOpenTeamSelectionEvent;

public class PlayerStorage {

  private Player player = null;

  private ItemStack[] inventory = null;
  private ItemStack[] armor = null;
  private float xp = 0.0F;
  private Collection<PotionEffect> effects = null;
  private GameMode mode = null;
  private Location left = null;
  private int level = 0;
  private String displayName = null;
  private String listName = null;
  private int foodLevel = 0;

  public PlayerStorage(Player p) {
    super();

    this.player = p;
  }

  public void store() {
    this.inventory = this.player.getInventory().getContents();
    this.armor = this.player.getInventory().getArmorContents();
    this.xp = Float.valueOf(this.player.getExp());
    this.effects = this.player.getActivePotionEffects();
    this.mode = this.player.getGameMode();
    this.left = this.player.getLocation();
    this.level = this.player.getLevel();
    this.listName = this.player.getPlayerListName();
    this.displayName = this.player.getDisplayName();
    this.foodLevel = this.player.getFoodLevel();
  }

  public void clean() {

    PlayerInventory inv = this.player.getInventory();
    inv.setArmorContents(new ItemStack[4]);
    inv.setContents(new ItemStack[] {});

    this.player.setAllowFlight(false);
    this.player.setFlying(false);
    this.player.setExp(0.0F);
    this.player.setLevel(0);
    this.player.setSneaking(false);
    this.player.setSprinting(false);
    this.player.setFoodLevel(20);
    this.player.setSaturation(10);
    this.player.setExhaustion(0);
    this.player.setMaxHealth(20.0D);
    this.player.setHealth(20.0D);
    this.player.setFireTicks(0);
    this.player.setGameMode(GameMode.SURVIVAL);

    boolean teamnameOnTab = Main.getInstance().getBooleanConfig("teamname-on-tab", true);
    boolean overwriteNames = Main.getInstance().getBooleanConfig("overwrite-names", false);
    if (overwriteNames) {
      Game game = Main.getInstance().getGameManager().getGameOfPlayer(this.player);
      if (game != null) {
        Team team = game.getPlayerTeam(this.player);
        if (team != null) {
          this.player
              .setDisplayName(team.getChatColor() + ChatColor.stripColor(this.player.getName()));
        } else {
          this.player.setDisplayName(ChatColor.stripColor(this.player.getName()));
        }
      }
    }

    if (teamnameOnTab) {
      Game game = Main.getInstance().getGameManager().getGameOfPlayer(this.player);
      if (game != null) {
        Team team = game.getPlayerTeam(this.player);
        if (team != null) {
          this.player.setPlayerListName(team.getChatColor() + team.getName() + ChatColor.WHITE
              + " | " + team.getChatColor() + ChatColor.stripColor(this.player.getDisplayName()));
        } else {
          this.player.setPlayerListName(ChatColor.stripColor(this.player.getDisplayName()));
        }
      }
    }

    if (this.player.isInsideVehicle()) {
      this.player.leaveVehicle();
    }

    for (PotionEffect e : this.player.getActivePotionEffects()) {
      this.player.removePotionEffect(e.getType());
    }

    this.player.updateInventory();
  }

  public void restore() {
    if (!Main.getInstance().getBooleanConfig("save-inventory", true)) {
      this.player.getInventory().setContents(this.inventory);
      this.player.getInventory().setArmorContents(this.armor);

      this.player.addPotionEffects(this.effects);
      this.player.setLevel(this.level);
      this.player.setExp(this.xp);
      this.player.setFoodLevel(this.foodLevel);

      for (PotionEffect e : this.player.getActivePotionEffects()) {
        this.player.removePotionEffect(e.getType());
      }

      this.player.addPotionEffects(this.effects);
    }

    this.player.setPlayerListName(this.listName);
    this.player.setDisplayName(this.displayName);

    this.player.setGameMode(this.mode);

    if (this.mode == GameMode.CREATIVE) {
      this.player.setAllowFlight(true);
    }
    this.player.updateInventory();
  }

  public Location getLeft() {
    return this.left;
  }

  public void loadLobbyInventory(Game game) {
    ItemMeta im = null;

    // choose team only when autobalance is disabled
    if (!game.isAutobalanceEnabled()) {
      // Choose team (Wool)
      ItemStack teamSelection = new ItemStack(Material.BED, 1);
      im = teamSelection.getItemMeta();
      im.setDisplayName(Main._l("lobby.chooseteam"));
      teamSelection.setItemMeta(im);
      this.player.getInventory().addItem(teamSelection);
    }

    // Leave Game (Slimeball)
    ItemStack leaveGame = new ItemStack(Material.SLIME_BALL, 1);
    im = leaveGame.getItemMeta();
    im.setDisplayName(Main._l("lobby.leavegame"));
    leaveGame.setItemMeta(im);
    this.player.getInventory().setItem(8, leaveGame);

    if ((this.player.hasPermission("bw.setup") || this.player.isOp()
        || this.player.hasPermission("bw.vip.forcestart"))
        || (game.getGameLobbyCountdown() != null && (this.player.hasPermission("bw.setup")
            || this.player.isOp() || this.player.hasPermission("bw.vip.forcestart")))) {
      this.addGameStartItem();
    }

    if (game.getGameLobbyCountdown() != null
        && game.getGameLobbyCountdown().getLobbytime() > game.getGameLobbyCountdown()
            .getLobbytimeWhenFull()
        && (this.player.hasPermission("bw.setup") || this.player.isOp()
            || this.player.hasPermission("bw.vip.reducecountdown"))) {
      this.addReduceCountdownItem();
    }

    this.player.updateInventory();
  }

  public void addGameStartItem() {
    ItemStack startGame = new ItemStack(Material.DIAMOND, 1);
    ItemMeta im = startGame.getItemMeta();
    im.setDisplayName(Main._l("lobby.startgame"));
    startGame.setItemMeta(im);
    this.player.getInventory().addItem(startGame);
  }

  public void addReduceCountdownItem() {
    ItemStack reduceCountdownItem = new ItemStack(Material.EMERALD, 1);
    ItemMeta im = reduceCountdownItem.getItemMeta();
    im.setDisplayName(Main._l("lobby.reduce_countdown"));
    reduceCountdownItem.setItemMeta(im);
    this.player.getInventory().addItem(reduceCountdownItem);
  }

  @SuppressWarnings("deprecation")
  public void openTeamSelection(Game game) {
    BedwarsOpenTeamSelectionEvent openEvent = new BedwarsOpenTeamSelectionEvent(game, this.player);
    Main.getInstance().getServer().getPluginManager().callEvent(openEvent);

    if (openEvent.isCancelled()) {
      return;
    }

    HashMap<String, Team> teams = game.getTeams();

    int nom = (teams.size() % 9 == 0) ? 9 : (teams.size() % 9);
    Inventory inv =
        Bukkit.createInventory(this.player, teams.size() + (9 - nom), Main._l("lobby.chooseteam"));
    for (Team team : teams.values()) {
      List<Player> players = team.getPlayers();
      if (players.size() >= team.getMaxPlayers()) {
        continue;
      }

      ItemStack is = new ItemStack(Material.WOOL, 1, team.getColor().getDyeColor().getData());
      ItemMeta im = is.getItemMeta();
      im.setDisplayName(team.getChatColor() + team.getName());
      ArrayList<String> teamplayers = new ArrayList<>();

      int teamPlayerSize = team.getPlayers().size();
      int maxPlayers = team.getMaxPlayers();

      String current = "0";
      if (teamPlayerSize >= maxPlayers) {
        current = ChatColor.RED + String.valueOf(teamPlayerSize);
      } else {
        current = ChatColor.YELLOW + String.valueOf(teamPlayerSize);
      }

      teamplayers.add(ChatColor.GRAY + "(" + current + ChatColor.GRAY + "/" + ChatColor.YELLOW
          + String.valueOf(maxPlayers) + ChatColor.GRAY + ")");
      teamplayers.add(ChatColor.WHITE + "---------");

      for (Player teamPlayer : players) {
        teamplayers.add(team.getChatColor() + ChatColor.stripColor(teamPlayer.getDisplayName()));
      }

      im.setLore(teamplayers);
      is.setItemMeta(im);
      inv.addItem(is);
    }

    this.player.openInventory(inv);
  }

}
