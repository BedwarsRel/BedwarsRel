package io.github.bedwarsrel.game;

import io.github.bedwarsrel.BedwarsRel;
import io.github.bedwarsrel.events.BedwarsOpenTeamSelectionEvent;
import io.github.bedwarsrel.events.BedwarsPlayerSetNameEvent;
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
import org.bukkit.material.Wool;
import org.bukkit.potion.PotionEffect;

public class PlayerStorage {

  private ItemStack[] armor = null;
  private String displayName = null;
  private Collection<PotionEffect> effects = null;
  private int foodLevel = 0;
  private ItemStack[] inventory = null;
  private Location left = null;
  private int level = 0;
  private String listName = null;
  private GameMode mode = null;
  private Player player = null;
  private float xp = 0.0F;

  public PlayerStorage(Player p) {
    super();

    this.player = p;
  }

  public void addGameStartItem() {
    ItemStack startGame = new ItemStack(Material.DIAMOND, 1);
    ItemMeta im = startGame.getItemMeta();
    im.setDisplayName(BedwarsRel._l(player, "lobby.startgame"));
    startGame.setItemMeta(im);
    this.player.getInventory().addItem(startGame);
  }

  public void addReduceCountdownItem() {
    ItemStack reduceCountdownItem = new ItemStack(Material.EMERALD, 1);
    ItemMeta im = reduceCountdownItem.getItemMeta();
    im.setDisplayName(BedwarsRel._l(player, "lobby.reduce_countdown"));
    reduceCountdownItem.setItemMeta(im);
    this.player.getInventory().addItem(reduceCountdownItem);
  }

  public void clean() {

    PlayerInventory inv = this.player.getInventory();
    inv.setArmorContents(new ItemStack[4]);
    inv.setContents(new ItemStack[]{});

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

    boolean teamnameOnTab = BedwarsRel.getInstance().getBooleanConfig("teamname-on-tab", true);
    boolean overwriteNames = BedwarsRel.getInstance().getBooleanConfig("overwrite-names", false);

    String displayName = this.player.getDisplayName();
    String playerListName = this.player.getPlayerListName();

    if (overwriteNames || teamnameOnTab) {
      Game game = BedwarsRel.getInstance().getGameManager().getGameOfPlayer(this.player);
      if (game != null) {
        game.setPlayerGameMode(player);
        Team team = game.getPlayerTeam(this.player);

        if (overwriteNames) {
          if (team != null) {
            displayName = team.getChatColor() + ChatColor.stripColor(this.player.getName());
          } else {
            displayName = ChatColor.stripColor(this.player.getName());
          }
        }

        if (teamnameOnTab) {
          if (team != null) {
            playerListName = team.getChatColor() + team.getName() + ChatColor.WHITE + " | "
                + team.getChatColor() + ChatColor.stripColor(this.player.getDisplayName());
          } else {
            playerListName = ChatColor.stripColor(this.player.getDisplayName());
          }
        }

        BedwarsPlayerSetNameEvent playerSetNameEvent =
            new BedwarsPlayerSetNameEvent(team, displayName, playerListName, player);
        BedwarsRel.getInstance().getServer().getPluginManager().callEvent(playerSetNameEvent);

        if (!playerSetNameEvent.isCancelled()) {
          this.player.setDisplayName(playerSetNameEvent.getDisplayName());
          this.player.setPlayerListName(playerSetNameEvent.getPlayerListName());
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
      im.setDisplayName(BedwarsRel._l(this.player, "lobby.chooseteam"));
      teamSelection.setItemMeta(im);
      this.player.getInventory().addItem(teamSelection);
    }

    // Leave game (Slimeball)
    ItemStack leaveGame = new ItemStack(Material.SLIME_BALL, 1);
    im = leaveGame.getItemMeta();
    im.setDisplayName(BedwarsRel._l(this.player, "lobby.leavegame"));
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

  public void openTeamSelection(Game game) {
    BedwarsOpenTeamSelectionEvent openEvent = new BedwarsOpenTeamSelectionEvent(game, this.player);
    BedwarsRel.getInstance().getServer().getPluginManager().callEvent(openEvent);

    if (openEvent.isCancelled()) {
      return;
    }

    HashMap<String, Team> teams = game.getTeams();

    int nom = (teams.size() % 9 == 0) ? 9 : (teams.size() % 9);
    Inventory inv =
        Bukkit.createInventory(this.player, teams.size() + (9 - nom),
            BedwarsRel._l(this.player, "lobby.chooseteam"));
    for (Team team : teams.values()) {
      List<Player> players = team.getPlayers();
      if (players.size() >= team.getMaxPlayers()) {
        continue;
      }
      Wool wool = new Wool(team.getColor().getDyeColor());
      ItemStack is = wool.toItemStack(1);
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

  public void restore() {
    if (BedwarsRel.getInstance().getBooleanConfig("save-inventory", true)) {
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

  public void store() {
    this.inventory = this.player.getInventory().getContents();
    this.armor = this.player.getInventory().getArmorContents();
    this.xp = this.player.getExp();
    this.effects = this.player.getActivePotionEffects();
    this.mode = this.player.getGameMode();
    this.left = this.player.getLocation();
    this.level = this.player.getLevel();
    this.listName = this.player.getPlayerListName();
    this.displayName = this.player.getDisplayName();
    this.foodLevel = this.player.getFoodLevel();
  }

}
