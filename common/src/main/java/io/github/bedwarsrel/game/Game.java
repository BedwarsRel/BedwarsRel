package io.github.bedwarsrel.game;

import com.google.common.collect.ImmutableMap;
import io.github.bedwarsrel.BedwarsRel;
import io.github.bedwarsrel.events.BedwarsGameStartEvent;
import io.github.bedwarsrel.events.BedwarsGameStartedEvent;
import io.github.bedwarsrel.events.BedwarsPlayerJoinEvent;
import io.github.bedwarsrel.events.BedwarsPlayerJoinedEvent;
import io.github.bedwarsrel.events.BedwarsPlayerLeaveEvent;
import io.github.bedwarsrel.events.BedwarsSaveGameEvent;
import io.github.bedwarsrel.events.BedwarsTargetBlockDestroyedEvent;
import io.github.bedwarsrel.shop.NewItemShop;
import io.github.bedwarsrel.shop.Specials.SpecialItem;
import io.github.bedwarsrel.statistics.PlayerStatistic;
import io.github.bedwarsrel.utils.ChatWriter;
import io.github.bedwarsrel.utils.Utils;
import io.github.bedwarsrel.villager.MerchantCategory;
import io.github.bedwarsrel.villager.MerchantCategoryComparator;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import lombok.Data;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.DyeColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.material.Bed;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.util.Vector;

@Data
public class Game {

  private static final int MAX_OBJECTIVE_DISPLAY_LENGTH = 32;
  private static final int MAX_SCORE_LENGTH = 40;

  private boolean autobalance = false;
  private String builder = null;
  private YamlConfiguration config = null;
  private GameCycle cycle = null;
  private List<Player> freePlayers = null;
  private GameLobbyCountdown gameLobbyCountdown = null;
  private Location hologramLocation = null;
  private boolean isOver = false;
  private boolean isStopping = false;
  private HashMap<Location, GameJoinSign> joinSigns = null;
  private int length = 0;
  private Location lobby = null;
  private Location loc1 = null;
  private Location loc2 = null;
  private Location mainLobby = null;
  private int minPlayers = 0;
  private String name = null;
  // Itemshops
  private HashMap<Player, NewItemShop> newItemShops = null;
  private List<MerchantCategory> orderedShopCategories = null;
  private Map<Player, Player> playerDamages = null;
  private Map<Player, PlayerSettings> playerSettings = null;
  private HashMap<Player, PlayerStorage> playerStorages = null;
  private List<Team> playingTeams = null;
  private int record = 0;
  private List<String> recordHolders = null;
  private Region region = null;
  private String regionName = null;
  private List<ResourceSpawner> resourceSpawners = null;
  private Map<Player, RespawnProtectionRunnable> respawnProtections = null;
  private List<BukkitTask> runningTasks = null;
  private Scoreboard scoreboard = null;
  private HashMap<Material, MerchantCategory> shopCategories = null;
  private List<SpecialItem> specialItems = null;
  private GameState state = null;
  private Material targetMaterial = null;
  private HashMap<String, Team> teams = null;
  private int time = 1000;
  private int timeLeft = 0;

  public Game(String name) {
    super();

    this.name = name;
    this.runningTasks = new ArrayList<BukkitTask>();

    this.freePlayers = new ArrayList<Player>();
    this.resourceSpawners = new ArrayList<ResourceSpawner>();
    this.teams = new HashMap<String, Team>();
    this.playingTeams = new ArrayList<Team>();

    this.playerStorages = new HashMap<Player, PlayerStorage>();
    this.state = GameState.STOPPED;
    this.scoreboard = BedwarsRel.getInstance().getScoreboardManager().getNewScoreboard();

    this.gameLobbyCountdown = null;
    this.joinSigns = new HashMap<Location, GameJoinSign>();
    this.timeLeft = BedwarsRel.getInstance().getMaxLength();
    this.isOver = false;
    this.newItemShops = new HashMap<Player, NewItemShop>();
    this.respawnProtections = new HashMap<Player, RespawnProtectionRunnable>();
    this.playerDamages = new HashMap<Player, Player>();
    this.specialItems = new ArrayList<SpecialItem>();

    this.record = BedwarsRel.getInstance().getMaxLength();
    this.length = BedwarsRel.getInstance().getMaxLength();
    this.recordHolders = new ArrayList<String>();

    this.playerSettings = new HashMap<Player, PlayerSettings>();

    this.autobalance = BedwarsRel.getInstance().getBooleanConfig("global-autobalance", false);

    if (BedwarsRel.getInstance().isBungee()) {
      this.cycle = new BungeeGameCycle(this);
    } else {
      this.cycle = new SingleGameCycle(this);
    }
  }

  /*
   * STATIC
   */

  public static String bedExistString() {
    return "\u2714";
  }

  public static String bedLostString() {
    return "\u2718";
  }

  public static String getPlayerWithTeamString(Player player, Team team, ChatColor before) {
    if (BedwarsRel.getInstance().getBooleanConfig("teamname-in-chat", true)) {
      return player.getDisplayName() + before + " (" + team.getChatColor() + team.getDisplayName()
          + before + ")";
    }

    return player.getDisplayName() + before;
  }

  public static String getPlayerWithTeamString(Player player, Team team, ChatColor before,
      String playerAdding) {
    if (BedwarsRel.getInstance().getBooleanConfig("teamname-in-chat", true)) {
      return player.getDisplayName() + before + playerAdding + before + " (" + team.getChatColor()
          + team.getDisplayName() + before + ")";
    }

    return player.getDisplayName() + before + playerAdding + before;
  }

  /*
   * PUBLIC
   */

  public void addJoinSign(Location signLocation) {
    if (this.joinSigns.containsKey(signLocation)) {
      this.joinSigns.remove(signLocation);
    }

    this.joinSigns.put(signLocation, new GameJoinSign(this, signLocation));
    this.updateSignConfig();
  }

  public void addPlayerSettings(Player player) {
    this.playerSettings.put(player, new PlayerSettings(player));
  }

  public PlayerStorage addPlayerStorage(Player p) {
    PlayerStorage storage = new PlayerStorage(p);
    this.playerStorages.put(p, storage);

    return storage;
  }

  public RespawnProtectionRunnable addProtection(Player player) {
    RespawnProtectionRunnable rpr =
        new RespawnProtectionRunnable(this, player,
            BedwarsRel.getInstance().getRespawnProtectionTime());
    this.respawnProtections.put(player, rpr);

    return rpr;
  }

  public void addRecordHolder(String holder) {
    this.recordHolders.add(holder);
  }

  public void addResourceSpawner(ResourceSpawner rs) {
    this.resourceSpawners.add(rs);
  }

  public void addRunningTask(BukkitTask task) {
    this.runningTasks.add(task);
  }

  public void addSpecialItem(SpecialItem item) {
    this.specialItems.add(item);
  }

  public void addTeam(String name, TeamColor color, int maxPlayers) {
    org.bukkit.scoreboard.Team newTeam = this.scoreboard.registerNewTeam(name);
    newTeam.setDisplayName(name);
    newTeam.setPrefix(color.getChatColor().toString());

    Team theTeam = new Team(name, color, maxPlayers, newTeam);
    this.teams.put(name, theTeam);
  }

  public void addTeam(Team team) {
    org.bukkit.scoreboard.Team newTeam = this.scoreboard.registerNewTeam(team.getName());
    newTeam.setDisplayName(team.getName());
    newTeam.setPrefix(team.getChatColor().toString());

    team.setScoreboardTeam(newTeam);

    this.teams.put(team.getName(), team);
  }

  public void broadcastSound(Sound sound, float volume, float pitch) {
    for (Player p : this.getPlayers()) {
      if (p.isOnline()) {
        p.playSound(p.getLocation(), sound, volume, pitch);
      }
    }
  }

  public void broadcastSound(Sound sound, float volume, float pitch, List<Player> players) {
    for (Player p : players) {
      if (p.isOnline()) {
        p.playSound(p.getLocation(), sound, volume, pitch);
      }
    }
  }

  public GameCheckCode checkGame() {
    if (this.loc1 == null || this.loc2 == null) {
      return GameCheckCode.LOC_NOT_SET_ERROR;
    }

    if (this.teams == null || this.teams.size() <= 1) {
      return GameCheckCode.TEAM_SIZE_LOW_ERROR;
    }

    GameCheckCode teamCheck = this.checkTeams();
    if (teamCheck != GameCheckCode.OK) {
      return teamCheck;
    }

    if (this.getRessourceSpawner().size() == 0) {
      return GameCheckCode.NO_RES_SPAWNER_ERROR;
    }

    if (this.lobby == null) {
      return GameCheckCode.NO_LOBBY_SET;
    }

    if (BedwarsRel.getInstance().toMainLobby() && this.mainLobby == null) {
      return GameCheckCode.NO_MAIN_LOBBY_SET;
    }

    return GameCheckCode.OK;
  }

  private GameCheckCode checkTeams() {
    for (Team t : this.teams.values()) {
      if (t.getSpawnLocation() == null) {
        return GameCheckCode.TEAMS_WITHOUT_SPAWNS;
      }

      Material targetMaterial = this.getTargetMaterial();

      if (targetMaterial.equals(Material.BED_BLOCK)) {
        if ((t.getHeadTarget() == null || t.getFeetTarget() == null)
            || (!Utils.isBedBlock(t.getHeadTarget()) || !Utils.isBedBlock(t.getFeetTarget()))) {
          return GameCheckCode.TEAM_NO_WRONG_BED;
        }
      } else {
        if (t.getHeadTarget() == null) {
          return GameCheckCode.TEAM_NO_WRONG_TARGET;
        }

        if (!t.getHeadTarget().getType().equals(targetMaterial)) {
          return GameCheckCode.TEAM_NO_WRONG_TARGET;
        }
      }

    }

    return GameCheckCode.OK;
  }

  private void cleanUsersInventory() {
    for (PlayerStorage storage : this.playerStorages.values()) {
      storage.clean();
    }
  }

  public void clearProtections() {
    for (RespawnProtectionRunnable protection : this.respawnProtections.values()) {
      try {
        protection.cancel();
      } catch (Exception ex) {
        BedwarsRel.getInstance().getBugsnag().notify(ex);
        // isn't running, ignore
      }
    }

    this.respawnProtections.clear();
  }

  private void createGameConfig(File config) {
    YamlConfiguration yml = new YamlConfiguration();

    yml.set("name", this.name);
    yml.set("world", this.getRegion().getWorld().getName());
    yml.set("loc1", Utils.locationSerialize(this.loc1));
    yml.set("loc2", Utils.locationSerialize(this.loc2));
    yml.set("lobby", Utils.locationSerialize(this.lobby));
    yml.set("minplayers", this.getMinPlayers());

    if (BedwarsRel.getInstance().getBooleanConfig("store-game-records", true)) {
      yml.set("record", this.record);

      if (BedwarsRel.getInstance().getBooleanConfig("store-game-records-holder", true)) {
        yml.set("record-holders", this.recordHolders);
      }
    }

    if (this.regionName == null) {
      this.regionName = this.region.getName();
    }

    yml.set("regionname", this.regionName);
    yml.set("time", this.time);

    yml.set("targetmaterial", this.getTargetMaterial().name());
    yml.set("builder", this.builder);

    if (this.hologramLocation != null) {
      yml.set("hololoc", Utils.locationSerialize(this.hologramLocation));
    }

    if (this.mainLobby != null) {
      yml.set("mainlobby", Utils.locationSerialize(this.mainLobby));
    }

    yml.set("autobalance", this.autobalance);

    yml.set("spawner", this.resourceSpawners);
    yml.createSection("teams", this.teams);

    try {
      yml.save(config);
      this.config = yml;
    } catch (IOException e) {
      BedwarsRel.getInstance().getBugsnag().notify(e);
      BedwarsRel.getInstance().getLogger().info(ChatWriter.pluginMessage(e.getMessage()));
    }
  }

  private void displayMapInfo() {
    for (Player player : this.getPlayers()) {
      this.displayMapInfo(player);
    }
  }

  private void displayMapInfo(Player player) {
    try {
      Class<?> clazz = Class.forName("io.github.bedwarsrel.com."
          + BedwarsRel.getInstance().getCurrentVersion().toLowerCase() + ".Title");
      Method showTitle = clazz.getMethod("showTitle", Player.class, String.class, double.class,
          double.class, double.class);
      double titleFadeIn = BedwarsRel.getInstance().getConfig()
          .getDouble("titles.map.title-fade-in");
      double titleStay = BedwarsRel.getInstance().getConfig().getDouble("titles.map.title-stay");
      double titleFadeOut = BedwarsRel.getInstance().getConfig()
          .getDouble("titles.map.title-fade-out");

      showTitle.invoke(null, player, this.getRegion().getName(), titleFadeIn, titleStay,
          titleFadeOut);

      if (this.builder != null) {
        Method showSubTitle = clazz.getMethod("showSubTitle", Player.class, String.class,
            double.class, double.class, double.class);
        double subtitleFadeIn =
            BedwarsRel.getInstance().getConfig().getDouble("titles.map.subtitle-fade-in");
        double subtitleStay = BedwarsRel.getInstance().getConfig()
            .getDouble("titles.map.subtitle-stay");
        double subtitleFadeOut =
            BedwarsRel.getInstance().getConfig().getDouble("titles.map.subtitle-fade-out");

        showSubTitle.invoke(null, player,
            BedwarsRel._l(player, "ingame.title.map-builder",
                ImmutableMap.of("builder",
                    ChatColor.translateAlternateColorCodes('&', this.builder))),
            subtitleFadeIn, subtitleStay, subtitleFadeOut);
      }

    } catch (Exception ex) {
      BedwarsRel.getInstance().getBugsnag().notify(ex);
      ex.printStackTrace();
    }
  }

  private void displayRecord() {
    for (Player player : this.getPlayers()) {
      this.displayRecord(player);
    }
  }

  private void displayRecord(Player player) {
    boolean displayHolders = BedwarsRel
        .getInstance().getBooleanConfig("store-game-records-holder", true);

    if (displayHolders && this.getRecordHolders().size() > 0) {
      StringBuilder holders = new StringBuilder();

      for (String holder : this.recordHolders) {
        if (holders.length() == 0) {
          holders.append(ChatColor.WHITE + holder);
        } else {
          holders.append(ChatColor.GOLD + ", " + ChatColor.WHITE + holder);
        }
      }

      player
          .sendMessage(ChatWriter.pluginMessage(BedwarsRel._l(player, "ingame.record-with-holders",
              ImmutableMap
                  .of("record", this.getFormattedRecord(), "holders", holders.toString()))));
    } else {
      player.sendMessage(ChatWriter.pluginMessage(
          BedwarsRel
              ._l(player, "ingame.record", ImmutableMap.of("record", this.getFormattedRecord()))));
    }
  }

  private void dropTargetBlock(Block targetBlock) {
    if (targetBlock.getType().equals(Material.BED_BLOCK)) {
      Block bedHead;
      Block bedFeet;
      Bed bedBlock = (Bed) targetBlock.getState().getData();

      if (!bedBlock.isHeadOfBed()) {
        bedFeet = targetBlock;
        bedHead = Utils.getBedNeighbor(bedFeet);
      } else {
        bedHead = targetBlock;
        bedFeet = Utils.getBedNeighbor(bedHead);
      }

      if (!BedwarsRel.getInstance().getCurrentVersion().startsWith("v1_12")) {
        bedFeet.setType(Material.AIR);
      } else {
        bedHead.setType(Material.AIR);
      }
    } else {
      targetBlock.setType(Material.AIR);
    }
  }

  private String formatLobbyScoreboardString(String str) {
    String finalStr = str;

    finalStr = finalStr.replace("$regionname$", this.region.getName());
    finalStr = finalStr.replace("$gamename$", this.name);
    finalStr = finalStr.replace("$players$", String.valueOf(this.getPlayerAmount()));
    finalStr = finalStr.replace("$maxplayers$", String.valueOf(this.getMaxPlayers()));

    finalStr = ChatColor.translateAlternateColorCodes('&', finalStr);
    //this is used for display name of the object and score name
    //the display name is only smaller so choose this limit
    return limitScoreboardLength(finalStr, MAX_OBJECTIVE_DISPLAY_LENGTH);
  }

  private String formatScoreboardTeam(Team team, boolean destroyed) {
    String format = null;

    if (team == null) {
      return "";
    }

    if (destroyed) {
      format = BedwarsRel.getInstance().getStringConfig("scoreboard.format-bed-destroyed",
          "&c$status$ $team$");
    } else {
      format =
          BedwarsRel
              .getInstance().getStringConfig("scoreboard.format-bed-alive", "&a$status$ $team$");
    }

    format = format.replace("$status$", (destroyed) ? Game.bedLostString() : Game.bedExistString());
    format = format.replace("$team$", team.getChatColor() + team.getName());

    format = ChatColor.translateAlternateColorCodes('&', format);
    return limitScoreboardLength(format, MAX_SCORE_LENGTH);
  }

  private String formatScoreboardTitle() {
    String format =
        BedwarsRel.getInstance()
            .getStringConfig("scoreboard.format-title", "&e$region$&f - $time$");

    // replaces
    format = format.replace("$region$", this.getRegion().getName());
    format = format.replace("$game$", this.name);
    format = format.replace("$time$", this.getFormattedTimeLeft());

    format = ChatColor.translateAlternateColorCodes('&', format);
    return limitScoreboardLength(format, MAX_OBJECTIVE_DISPLAY_LENGTH);
  }

  private String limitScoreboardLength(String fullText, int maxLength) {
    if (fullText.length() > maxLength) {
      return fullText.substring(0, maxLength);
    }

    return fullText;
  }

  public int getCurrentPlayerAmount() {
    int amount = 0;
    for (Team t : this.teams.values()) {
      amount += t.getPlayers().size();
    }

    return amount;
  }

  public String getFormattedRecord() {
    return Utils.getFormattedTime(this.record);
  }

  private String getFormattedTimeLeft() {
    int min = 0;
    int sec = 0;
    String minStr = "";
    String secStr = "";

    min = (int) Math.floor(this.timeLeft / 60);
    sec = this.timeLeft % 60;

    minStr = (min < 10) ? "0" + String.valueOf(min) : String.valueOf(min);
    secStr = (sec < 10) ? "0" + String.valueOf(sec) : String.valueOf(sec);

    return minStr + ":" + secStr;
  }

  public List<Player> getFreePlayersClone() {
    List<Player> players = new ArrayList<Player>();
    if (this.freePlayers.size() > 0) {
      players.addAll(this.freePlayers);
    }

    return players;
  }

  public HashMap<Material, MerchantCategory> getItemShopCategories() {
    return this.shopCategories;
  }

  public void setItemShopCategories(HashMap<Material, MerchantCategory> cats) {
    this.shopCategories = cats;
  }

  public GameLobbyCountdown getLobbyCountdown() {
    return this.gameLobbyCountdown;
  }

  public void setLobbyCountdown(GameLobbyCountdown glc) {
    this.gameLobbyCountdown = glc;
  }

  private Team getLowestTeam() {
    Team lowest = null;
    for (Team team : this.teams.values()) {
      if (lowest == null) {
        lowest = team;
        continue;
      }

      if (team.getPlayers().size() < lowest.getPlayers().size()) {
        lowest = team;
      }
    }

    return lowest;
  }

  public int getMaxPlayers() {
    int max = 0;
    for (Team t : this.teams.values()) {
      max += t.getMaxPlayers();
    }

    return max;
  }

  public NewItemShop getNewItemShop(Player player) {
    return this.newItemShops.get(player);
  }

  public List<Player> getNonVipPlayers() {
    List<Player> players = this.getPlayers();

    Iterator<Player> playerIterator = players.iterator();
    while (playerIterator.hasNext()) {
      Player player = playerIterator.next();
      if (player.hasPermission("bw.vip.joinfull") || player.hasPermission("bw.vip.forcestart")
          || player.hasPermission("bw.vip")) {
        playerIterator.remove();
      }
    }

    return players;
  }

  public List<MerchantCategory> getOrderedItemShopCategories() {
    return this.orderedShopCategories;
  }

  public int getPlayerAmount() {
    return this.getPlayers().size();
  }

  public Player getPlayerDamager(Player p) {
    return this.playerDamages.get(p);
  }

  public PlayerSettings getPlayerSettings(Player player) {
    return this.playerSettings.get(player);
  }

  public PlayerStorage getPlayerStorage(Player p) {
    return this.playerStorages.get(p);
  }

  public Team getPlayerTeam(Player p) {
    for (Team team : this.getTeams().values()) {
      if (team.isInTeam(p)) {
        return team;
      }
    }

    return null;
  }

  public Location getPlayerTeleportLocation(Player player) {
    if (this.isSpectator(player)
        && !(this.getCycle() instanceof BungeeGameCycle && this.getCycle().isEndGameRunning()
        && BedwarsRel.getInstance().getBooleanConfig("bungeecord.endgame-in-lobby", true))) {
      return ((Team) this.teams.values().toArray()[Utils.randInt(0, this.teams.size() - 1)])
          .getSpawnLocation();
    }

    if (this.getPlayerTeam(player) != null
        && !(this.getCycle() instanceof BungeeGameCycle && this.getCycle().isEndGameRunning()
        && BedwarsRel.getInstance().getBooleanConfig("bungeecord.endgame-in-lobby", true))) {
      return this.getPlayerTeam(player).getSpawnLocation();
    }

    return this.getLobby();
  }

  public ArrayList<Player> getPlayers() {
    ArrayList<Player> players = new ArrayList<>();

    players.addAll(this.freePlayers);

    for (Team team : this.teams.values()) {
      players.addAll(team.getPlayers());
    }

    return players;
  }

  public List<ResourceSpawner> getRessourceSpawner() {
    return this.resourceSpawners;
  }

  public HashMap<Location, GameJoinSign> getSigns() {
    return this.joinSigns;
  }

  public List<SpecialItem> getSpecialItems() {
    return this.specialItems;
  }

  public Material getTargetMaterial() {
    if (this.targetMaterial == null) {
      return Utils.getMaterialByConfig("game-block", Material.BED_BLOCK);
    }

    return this.targetMaterial;
  }

  public Team getTeam(String name) {
    return this.teams.get(name);
  }

  public Team getTeamByDyeColor(DyeColor color) {
    for (Team t : this.teams.values()) {
      if (t.getColor().getDyeColor().equals(color)) {
        return t;
      }
    }

    return null;
  }

  public Team getTeamOfBed(Block bed) {
    for (Team team : this.getTeams().values()) {
      if (team.getFeetTarget() == null) {
        if (team.getHeadTarget().equals(bed)) {
          return team;
        }
      } else {
        if (team.getHeadTarget().equals(bed) || team.getFeetTarget().equals(bed)) {
          return team;
        }
      }
    }

    return null;
  }

  public Team getTeamOfEnderChest(Block chest) {
    for (Team team : this.teams.values()) {
      if (team.getChests().contains(chest)) {
        return team;
      }
    }

    return null;
  }

  public ArrayList<Player> getTeamPlayers() {
    ArrayList<Player> players = new ArrayList<>();

    for (Team team : this.teams.values()) {
      players.addAll(team.getPlayers());
    }

    return players;
  }

  public HashMap<String, Team> getTeams() {
    return this.teams;
  }

  public boolean handleDestroyTargetMaterial(Player p, Block block) {
    Team team = this.getPlayerTeam(p);
    if (team == null) {
      return false;
    }

    Team bedDestroyTeam = null;
    Block bedBlock = team.getHeadTarget();

    if (block.getType().equals(Material.BED_BLOCK)) {
      Block breakBlock = block;
      Block neighbor = null;
      Bed breakBed = (Bed) breakBlock.getState().getData();

      if (!breakBed.isHeadOfBed()) {
        neighbor = breakBlock;
        breakBlock = Utils.getBedNeighbor(neighbor);
      } else {
        neighbor = Utils.getBedNeighbor(breakBlock);
      }

      if (bedBlock.equals(breakBlock)) {
        p.sendMessage(
            ChatWriter
                .pluginMessage(ChatColor.RED + BedwarsRel._l(p, "ingame.blocks.ownbeddestroy")));
        return false;
      }

      bedDestroyTeam = this.getTeamOfBed(breakBlock);
      if (bedDestroyTeam == null) {
        return false;
      }
      this.dropTargetBlock(block);
    } else {
      if (bedBlock.equals(block)) {
        p.sendMessage(
            ChatWriter
                .pluginMessage(ChatColor.RED + BedwarsRel._l(p, "ingame.blocks.ownbeddestroy")));
        return false;
      }

      bedDestroyTeam = this.getTeamOfBed(block);
      if (bedDestroyTeam == null) {
        return false;
      }

      this.dropTargetBlock(block);
    }

    // set statistics
    if (BedwarsRel.getInstance().statisticsEnabled()) {
      PlayerStatistic statistic = BedwarsRel.getInstance().getPlayerStatisticManager()
          .getStatistic(p);
      statistic.setCurrentDestroyedBeds(statistic.getCurrentDestroyedBeds() + 1);
      statistic.setCurrentScore(statistic.getCurrentScore() + BedwarsRel.getInstance()
          .getIntConfig("statistics.scores.bed-destroy", 25));
    }

    // reward when destroy bed
    if (BedwarsRel.getInstance().getBooleanConfig("rewards.enabled", false)) {
      List<String> commands =
          BedwarsRel.getInstance().getConfig().getStringList("rewards.player-destroy-bed");
      BedwarsRel.getInstance()
          .dispatchRewardCommands(commands, ImmutableMap.of("{player}", p.getName(),
              "{score}",
              String.valueOf(
                  BedwarsRel.getInstance().getIntConfig("statistics.scores.bed-destroy", 25))));
    }

    BedwarsTargetBlockDestroyedEvent targetBlockDestroyedEvent =
        new BedwarsTargetBlockDestroyedEvent(this, p, bedDestroyTeam);
    BedwarsRel.getInstance().getServer().getPluginManager().callEvent(targetBlockDestroyedEvent);

    for (Player aPlayer : this.getPlayers()) {
      if (aPlayer.isOnline()) {
        aPlayer.sendMessage(
            ChatWriter.pluginMessage(ChatColor.RED + BedwarsRel
                ._l(aPlayer, "ingame.blocks.beddestroyed",
                    ImmutableMap.of("team",
                        bedDestroyTeam.getChatColor() + bedDestroyTeam.getName() + ChatColor.RED,
                        "player",
                        Game.getPlayerWithTeamString(p, team, ChatColor.RED)))));
      }
    }

    this.broadcastSound(
        Sound.valueOf(
            BedwarsRel.getInstance().getStringConfig("bed-sound", "ENDERDRAGON_GROWL")
                .toUpperCase()),
        30.0F, 10.0F);
    this.updateScoreboard();
    return true;
  }

  public boolean hasEnoughPlayers() {
    return this.getPlayers().size() >= this.getMinPlayers();
  }

  public boolean hasEnoughTeams() {
    int teamsWithPlayers = 0;
    for (Team team : this.getTeams().values()) {
      if (team.getPlayers().size() > 0) {
        teamsWithPlayers++;
      }
    }

    return (teamsWithPlayers > 1 || (teamsWithPlayers == 1 && this.getFreePlayers().size() >= 1)
        || (teamsWithPlayers == 0 && this.getFreePlayers().size() >= 2));
  }

  public boolean isAutobalanceEnabled() {
    if (BedwarsRel.getInstance().getBooleanConfig("global-autobalance", false)) {
      return true;
    }

    return this.autobalance;
  }

  /*
   * GETTER / SETTER
   */

  public boolean isFull() {
    return (this.getMaxPlayers() <= this.getPlayerAmount());
  }

  public boolean isInGame(Player p) {
    for (Team t : this.teams.values()) {
      if (t.isInTeam(p)) {
        return true;
      }
    }

    return this.freePlayers.contains(p);
  }

  public Team isOver() {
    if (this.isOver || this.state != GameState.RUNNING) {
      return null;
    }

    ArrayList<Player> players = this.getTeamPlayers();
    ArrayList<Team> teams = new ArrayList<>();

    if (players.size() == 0 || players.isEmpty()) {
      return null;
    }

    for (Player player : players) {
      Team playerTeam = this.getPlayerTeam(player);
      if (teams.contains(playerTeam)) {
        continue;
      }

      if (!player.isDead()) {
        teams.add(playerTeam);
      } else if (!playerTeam.isDead(this)) {
        teams.add(playerTeam);
      }
    }

    if (teams.size() == 1) {
      return teams.get(0);
    } else {
      return null;
    }
  }

  public boolean isOverSet() {
    return this.isOver;
  }

  public boolean isProtected(Player player) {
    return (this.respawnProtections.containsKey(player) && this.getState() == GameState.RUNNING);
  }

  public boolean isSpectator(Player player) {
    return (this.getState() == GameState.RUNNING && this.freePlayers.contains(player));
  }

  public boolean isStartable() {
    return (this.hasEnoughPlayers() && this.hasEnoughTeams());
  }

  public void kickAllPlayers() {
    for (Player p : this.getPlayers()) {
      this.playerLeave(p, false);
    }
  }

  public void loadItemShopCategories() {
    this.shopCategories = MerchantCategory.loadCategories(BedwarsRel.getInstance().getShopConfig());
    this.orderedShopCategories = this.loadOrderedItemShopCategories();
  }

  private List<MerchantCategory> loadOrderedItemShopCategories() {
    List<MerchantCategory> list = new ArrayList<MerchantCategory>(this.shopCategories.values());
    Collections.sort(list, new MerchantCategoryComparator());
    return list;
  }

  private void makeTeamsReady() {
    this.playingTeams.clear();

    for (Team team : this.teams.values()) {
      team.getScoreboardTeam()
          .setAllowFriendlyFire(BedwarsRel.getInstance().getConfig().getBoolean("friendlyfire"));
      if (team.getPlayers().size() == 0) {
        this.dropTargetBlock(team.getHeadTarget());
      } else {
        this.playingTeams.add(team);
      }
    }

    this.updateScoreboard();
  }

  private void moveFreePlayersToTeam() {
    for (Player player : this.freePlayers) {
      Team lowest = this.getLowestTeam();
      lowest.addPlayer(player);
    }

    this.freePlayers = new ArrayList<Player>();
    this.updateScoreboard();
  }

  public void nonFreePlayer(Player p) {
    if (this.freePlayers.contains(p)) {
      this.freePlayers.remove(p);
    }
  }

  public NewItemShop openNewItemShop(Player player) {
    NewItemShop newShop = new NewItemShop(this.orderedShopCategories);
    this.newItemShops.put(player, newShop);

    return newShop;
  }

  public void openSpectatorCompass(Player player) {
    if (!this.isSpectator(player)) {
      return;
    }

    int teamplayers = this.getTeamPlayers().size();
    int nom = (teamplayers % 9 == 0) ? 9 : (teamplayers % 9);
    int size = teamplayers + (9 - nom);
    Inventory compass = Bukkit
        .createInventory(null, size, BedwarsRel._l(player, "ingame.spectator"));
    for (Team t : this.getTeams().values()) {
      for (Player p : t.getPlayers()) {
        ItemStack head = new ItemStack(Material.SKULL_ITEM, 1, (short) 3);
        SkullMeta meta = (SkullMeta) head.getItemMeta();
        meta.setDisplayName(t.getChatColor() + p.getDisplayName());
        meta.setLore(Arrays.asList(t.getChatColor() + t.getDisplayName()));
        meta.setOwner(p.getName());
        head.setItemMeta(meta);

        compass.addItem(head);
      }
    }

    player.openInventory(compass);
  }

  public void playerJoinTeam(Player player, Team team) {
    if (team.getPlayers().size() >= team.getMaxPlayers()) {
      player.sendMessage(
          ChatWriter.pluginMessage(ChatColor.RED + BedwarsRel._l(player, "errors.teamfull")));
      return;
    }

    if (team.addPlayer(player)) {
      this.nonFreePlayer(player);

      // Team color chestplate
      ItemStack chestplate = new ItemStack(Material.LEATHER_CHESTPLATE, 1);
      LeatherArmorMeta meta = (LeatherArmorMeta) chestplate.getItemMeta();
      meta.setColor(team.getColor().getColor());
      meta.setDisplayName(team.getChatColor() + team.getDisplayName());
      chestplate.setItemMeta(meta);

      player.getInventory().setItem(7, chestplate);
      player.updateInventory();
    } else {
      player.sendMessage(
          ChatWriter.pluginMessage(ChatColor.RED + BedwarsRel._l(player, "errors.teamfull")));
      return;
    }

    this.updateScoreboard();

    if (this.isStartable() && this.getLobbyCountdown() == null) {
      GameLobbyCountdown lobbyCountdown = new GameLobbyCountdown(this);
      lobbyCountdown.runTaskTimer(BedwarsRel.getInstance(), 20L, 20L);
      this.setLobbyCountdown(lobbyCountdown);
    }

    player
        .sendMessage(ChatWriter.pluginMessage(ChatColor.GREEN + BedwarsRel
            ._l(player, "lobby.teamjoined",
                ImmutableMap.of("team", team.getDisplayName() + ChatColor.GREEN))));
  }

  public boolean playerJoins(final Player p) {

    if (this.state == GameState.STOPPED
        || (this.state == GameState.RUNNING && !BedwarsRel.getInstance().spectationEnabled())) {
      if (this.cycle instanceof BungeeGameCycle) {
        ((BungeeGameCycle) this.cycle).sendBungeeMessage(p,
            ChatWriter.pluginMessage(ChatColor.RED + BedwarsRel._l(p, "errors.cantjoingame")));
      } else {
        p.sendMessage(ChatWriter.pluginMessage(ChatColor.RED + BedwarsRel
            ._l(p, "errors.cantjoingame")));
      }
      return false;
    }

    if (!this.cycle.onPlayerJoins(p)) {
      return false;
    }

    BedwarsPlayerJoinEvent joiningEvent = new BedwarsPlayerJoinEvent(this, p);
    BedwarsRel.getInstance().getServer().getPluginManager().callEvent(joiningEvent);

    if (joiningEvent.isCancelled()) {
      if (joiningEvent.getKickOnCancel()) {
        new BukkitRunnable() {
          @Override
          public void run() {
            if (Game.this.getCycle() instanceof BungeeGameCycle) {
              ((BungeeGameCycle) Game.this.getCycle())
                  .bungeeSendToServer(BedwarsRel.getInstance().getBungeeHub(), p, true);
            }
          }
        }.runTaskLater(BedwarsRel.getInstance(), 5L);
      }
      return false;
    }

    BedwarsRel.getInstance().getGameManager().addGamePlayer(p, this);
    if (BedwarsRel.getInstance().statisticsEnabled()) {
      // load statistics
      BedwarsRel.getInstance().getPlayerStatisticManager().getStatistic(p);
    }

    // add damager and set it to null
    this.playerDamages.put(p, null);

    // add player settings
    this.addPlayerSettings(p);

    new BukkitRunnable() {

      @Override
      public void run() {
        for (Player playerInGame : Game.this.getPlayers()) {
          playerInGame.hidePlayer(p);
          p.hidePlayer(playerInGame);
        }
      }

    }.runTaskLater(BedwarsRel.getInstance(), 5L);

    if (this.state == GameState.RUNNING) {
      this.toSpectator(p);
      this.displayMapInfo(p);
    } else {

      PlayerStorage storage = this.addPlayerStorage(p);
      storage.store();
      storage.clean();

      if (!BedwarsRel.getInstance().isBungee()) {
        final Location location = this.getPlayerTeleportLocation(p);
        if (!p.getLocation().equals(location)) {
          this.getPlayerSettings(p).setTeleporting(true);
          if (BedwarsRel.getInstance().isBungee()) {
            new BukkitRunnable() {

              @Override
              public void run() {
                p.teleport(location);
              }

            }.runTaskLater(BedwarsRel.getInstance(), 10L);
          } else {
            p.teleport(location);
          }
        }
      }

      storage.loadLobbyInventory(this);

      new BukkitRunnable() {

        @Override
        public void run() {
          Game.this.setPlayerGameMode(p);
          Game.this.setPlayerVisibility(p);
        }

      }.runTaskLater(BedwarsRel.getInstance(), 15L);

      for (Player aPlayer : this.getPlayers()) {
        if (aPlayer.isOnline()) {
          aPlayer.sendMessage(
              ChatWriter.pluginMessage(ChatColor.GREEN + BedwarsRel._l(aPlayer, "lobby.playerjoin",
                  ImmutableMap.of("player", p.getDisplayName() + ChatColor.GREEN))));
        }
      }

      if (!this.isAutobalanceEnabled()) {
        this.freePlayers.add(p);
      } else {
        Team team = this.getLowestTeam();
        team.addPlayer(p);
      }

      if (BedwarsRel.getInstance().getBooleanConfig("store-game-records", true)) {
        this.displayRecord(p);
      }

      if (this.isStartable()) {
        if (this.gameLobbyCountdown == null) {
          this.gameLobbyCountdown = new GameLobbyCountdown(this);
          this.gameLobbyCountdown.runTaskTimer(BedwarsRel.getInstance(), 20L, 20L);
        }
      } else {
        if (!this.hasEnoughPlayers()) {
          int playersNeeded = this.getMinPlayers() - this.getPlayerAmount();
          for (Player aPlayer : this.getPlayers()) {
            if (aPlayer.isOnline()) {
              aPlayer.sendMessage(ChatWriter
                  .pluginMessage(
                      ChatColor.GREEN + BedwarsRel._l(aPlayer, "lobby.moreplayersneeded", "count",
                          ImmutableMap.of("count", String.valueOf(playersNeeded)))));
            }
          }
        } else if (!this.hasEnoughTeams()) {
          for (Player aPlayer : this.getPlayers()) {
            if (aPlayer.isOnline()) {
              aPlayer.sendMessage(ChatWriter
                  .pluginMessage(ChatColor.RED + BedwarsRel._l(aPlayer, "lobby.moreteamsneeded")));
            }
          }
        }
      }
    }

    BedwarsPlayerJoinedEvent joinEvent = new BedwarsPlayerJoinedEvent(this, null, p);
    BedwarsRel.getInstance().getServer().getPluginManager().callEvent(joinEvent);

    this.updateScoreboard();
    this.updateSigns();
    return true;

  }

  public boolean playerLeave(Player p, boolean kicked) {
    this.getPlayerSettings(p).setTeleporting(true);
    Team team = this.getPlayerTeam(p);

    BedwarsPlayerLeaveEvent leaveEvent = new BedwarsPlayerLeaveEvent(this, p, team);
    BedwarsRel.getInstance().getServer().getPluginManager().callEvent(leaveEvent);

    PlayerStatistic statistic = null;
    if (BedwarsRel.getInstance().statisticsEnabled()) {
      statistic = BedwarsRel.getInstance().getPlayerStatisticManager().getStatistic(p);
    }

    if (this.isSpectator(p)) {
      if (!this.getCycle().isEndGameRunning()) {
        for (Player player : this.getPlayers()) {
          if (player.equals(p)) {
            continue;
          }

          player.showPlayer(p);
          p.showPlayer(player);
        }
      }
    } else {
      if (this.state == GameState.RUNNING && !this.getCycle().isEndGameRunning()) {
        if (!team.isDead(this) && !p.isDead() && BedwarsRel.getInstance().statisticsEnabled()
            && BedwarsRel.getInstance().getBooleanConfig("statistics.player-leave-kills", false)) {
          statistic.setCurrentDeaths(statistic.getCurrentDeaths() + 1);
          statistic.setCurrentScore(statistic.getCurrentScore() + BedwarsRel.getInstance()
              .getIntConfig("statistics.scores.die", 0));
          if (this.getPlayerDamager(p) != null) {
            PlayerStatistic killerPlayer = BedwarsRel.getInstance().getPlayerStatisticManager()
                .getStatistic(this.getPlayerDamager(p));
            killerPlayer.setCurrentKills(killerPlayer.getCurrentKills() + 1);
            killerPlayer.setCurrentScore(killerPlayer.getCurrentScore() + BedwarsRel.getInstance()
                .getIntConfig("statistics.scores.kill", 10));
          }
          statistic.setCurrentLoses(statistic.getCurrentLoses() + 1);
          statistic.setCurrentScore(statistic.getCurrentScore() + BedwarsRel.getInstance()
              .getIntConfig("statistics.scores.lose", 0));
        }
      }
    }

    if (this.isProtected(p)) {
      this.removeProtection(p);
    }

    this.playerDamages.remove(p);
    if (team != null && BedwarsRel.getInstance().getGameManager().getGameOfPlayer(p) != null
        && !BedwarsRel.getInstance().getGameManager().getGameOfPlayer(p).isSpectator(p)) {
      if (kicked) {
        for (Player aPlayer : this.getPlayers()) {
          if (aPlayer.isOnline()) {
            aPlayer.sendMessage(
                ChatWriter.pluginMessage(ChatColor.RED + BedwarsRel
                    ._l(aPlayer, "ingame.player.kicked", ImmutableMap.of("player",
                        Game.getPlayerWithTeamString(p, team, ChatColor.RED) + ChatColor.RED))));
          }
        }
      } else {
        for (Player aPlayer : this.getPlayers()) {
          if (aPlayer.isOnline()) {
            aPlayer.sendMessage(
                ChatWriter.pluginMessage(
                    ChatColor.RED + BedwarsRel
                        ._l(aPlayer, "ingame.player.left", ImmutableMap.of("player",
                            Game.getPlayerWithTeamString(p, team, ChatColor.RED)
                                + ChatColor.RED))));
          }
        }
      }
      team.removePlayer(p);
    }

    BedwarsRel.getInstance().getGameManager().removeGamePlayer(p);

    if (this.freePlayers.contains(p)) {
      this.freePlayers.remove(p);
    }

    if (BedwarsRel.getInstance().isBungee()) {
      this.cycle.onPlayerLeave(p);
    }

    if (BedwarsRel.getInstance().statisticsEnabled()) {

      if (BedwarsRel.getInstance().isHologramsEnabled()
          && BedwarsRel.getInstance().getHolographicInteractor() != null && BedwarsRel.getInstance()
          .getHolographicInteractor().getType().equalsIgnoreCase("HolographicDisplays")) {
        BedwarsRel.getInstance().getHolographicInteractor().updateHolograms(p);
      }

      if (BedwarsRel.getInstance().getBooleanConfig("statistics.show-on-game-end", true)) {
        BedwarsRel.getInstance().getServer().dispatchCommand(p, "bw stats");
      }
      BedwarsRel.getInstance().getPlayerStatisticManager().storeStatistic(statistic);

      BedwarsRel.getInstance().getPlayerStatisticManager().unloadStatistic(p);
    }

    PlayerStorage storage = this.playerStorages.get(p);
    storage.clean();
    storage.restore();

    this.playerSettings.remove(p);
    this.updateScoreboard();

    try {
      p.setScoreboard(BedwarsRel.getInstance().getScoreboardManager().getMainScoreboard());
    } catch (Exception e) {
      BedwarsRel.getInstance().getBugsnag().notify(e);
    }

    this.removeNewItemShop(p);

    if (!BedwarsRel.getInstance().isBungee() && p.isOnline()) {
      if (kicked) {
        p.sendMessage(
            ChatWriter.pluginMessage(ChatColor.RED + BedwarsRel._l(p, "ingame.player.waskicked")));
      } else {
        p.sendMessage(ChatWriter.pluginMessage(ChatColor.GREEN + BedwarsRel._l(p, "success.left")));
      }
    }

    if (!BedwarsRel.getInstance().isBungee()) {
      this.cycle.onPlayerLeave(p);
    }

    this.updateSigns();
    this.playerStorages.remove(p);
    return true;
  }

  public void removeJoinSign(Location location) {
    this.joinSigns.remove(location);
    this.updateSignConfig();
  }

  public void removeNewItemShop(Player player) {
    if (!this.newItemShops.containsKey(player)) {
      return;
    }

    this.newItemShops.remove(player);
  }

  public void removePlayerSettings(Player player) {
    this.playerSettings.remove(player);
  }

  public void removeProtection(Player player) {
    RespawnProtectionRunnable rpr = this.respawnProtections.get(player);
    if (rpr == null) {
      return;
    }

    try {
      rpr.cancel();
    } catch (Exception ex) {
      BedwarsRel.getInstance().getBugsnag().notify(ex);
      // isn't running, ignore
    }

    this.respawnProtections.remove(player);
  }

  public void removeRunningTask(BukkitTask task) {
    this.runningTasks.remove(task);
  }

  public void removeRunningTask(BukkitRunnable bukkitRunnable) {
    this.runningTasks.remove(bukkitRunnable);
  }

  public void removeSpecialItem(SpecialItem item) {
    this.specialItems.remove(item);
  }

  public void removeTeam(Team team) {
    this.teams.remove(team.getName());
    this.updateSigns();
  }

  public void resetRegion() {
    if (this.region == null) {
      return;
    }

    this.region.reset(this);
  }

  public void resetScoreboard() {
    this.timeLeft = BedwarsRel.getInstance().getMaxLength();
    this.length = this.timeLeft;
    this.scoreboard.clearSlot(DisplaySlot.SIDEBAR);
  }

  public boolean run(CommandSender sender) {
    if (this.state != GameState.STOPPED) {
      sender
          .sendMessage(
              ChatWriter
                  .pluginMessage(ChatColor.RED + BedwarsRel._l(sender, "errors.cantstartagain")));
      return false;
    }

    GameCheckCode gcc = this.checkGame();
    if (gcc != GameCheckCode.OK) {
      sender.sendMessage(ChatWriter.pluginMessage(ChatColor.RED + gcc.getCodeMessage()));
      return false;
    }

    if (sender instanceof Player) {
      sender.sendMessage(
          ChatWriter.pluginMessage(ChatColor.GREEN + BedwarsRel._l(sender, "success.gamerun")));
    }

    this.isStopping = false;
    this.state = GameState.WAITING;
    this.updateSigns();
    return true;
  }

  public boolean saveGame(CommandSender sender, boolean direct) {
    BedwarsSaveGameEvent saveEvent = new BedwarsSaveGameEvent(this, sender);
    BedwarsRel.getInstance().getServer().getPluginManager().callEvent(saveEvent);

    if (saveEvent.isCancelled()) {
      return true;
    }

    GameCheckCode check = this.checkGame();

    if (check != GameCheckCode.OK) {
      sender.sendMessage(ChatWriter.pluginMessage(ChatColor.RED + check.getCodeMessage()));
      return false;
    }

    File gameConfig = new File(
        BedwarsRel.getInstance().getDataFolder() + "/" + GameManager.gamesPath
            + "/" + this.name + "/game.yml");
    gameConfig.mkdirs();

    if (gameConfig.exists()) {
      gameConfig.delete();
    }

    this.saveRegion(direct);
    this.createGameConfig(gameConfig);

    return true;
  }

  public void saveRecord() {
    File gameConfig = new File(
        BedwarsRel.getInstance().getDataFolder() + "/" + GameManager.gamesPath
            + "/" + this.name + "/game.yml");

    if (!gameConfig.exists()) {
      return;
    }

    this.config.set("record", this.record);
    if (BedwarsRel.getInstance().getBooleanConfig("store-game-records-holder", true)) {
      this.config.set("record-holders", this.recordHolders);
    }

    try {
      this.config.save(gameConfig);
    } catch (IOException e) {
      BedwarsRel.getInstance().getBugsnag().notify(e);
      e.printStackTrace();
    }
  }

  private void saveRegion(boolean direct) {
    if (this.region == null || direct) {
      if (this.regionName == null) {
        this.regionName = this.loc1.getWorld().getName();
      }

      this.region = new Region(this.loc1, this.loc2, this.regionName);
    }

    // nametag the villager
    this.region.setVillagerNametag();

    this.updateSigns();
  }

  public void setGameLobbyCountdown(GameLobbyCountdown countdown) {
    this.gameLobbyCountdown = countdown;
  }

  public void setLobby(Player sender) {
    Location lobby = sender.getLocation();

    if (this.region != null && this.region.getWorld().equals(lobby.getWorld())) {
      sender.sendMessage(
          ChatWriter
              .pluginMessage(ChatColor.RED + BedwarsRel._l(sender, "errors.lobbyongameworld")));
      return;
    }

    this.lobby = lobby;
    sender.sendMessage(
        ChatWriter.pluginMessage(ChatColor.GREEN + BedwarsRel._l(sender, "success.lobbyset")));
  }

  public void setLobby(Location lobby) {
    if (this.region != null) {
      if (this.region.getWorld().equals(lobby.getWorld())) {
        BedwarsRel.getInstance().getServer().getConsoleSender().sendMessage(
            ChatWriter.pluginMessage(ChatColor.RED + BedwarsRel
                ._l(BedwarsRel.getInstance().getServer().getConsoleSender(),
                    "errors.lobbyongameworld")));
        return;
      }
    }

    this.lobby = lobby;
  }

  /*
   * PRIVATE
   */

  public void setLoc(Location loc, String type) {
    if (type.equalsIgnoreCase("loc1")) {
      this.loc1 = loc;
    } else {
      this.loc2 = loc;
    }
  }

  public void setMinPlayers(int players) {
    int max = this.getMaxPlayers();
    int minPlayers = players;

    if (max < players && max > 0) {
      minPlayers = max;
    }

    this.minPlayers = minPlayers;
  }

  public void setPlayerDamager(Player p, Player damager) {
    this.playerDamages.remove(p);
    this.playerDamages.put(p, damager);
  }

  public void setPlayerGameMode(Player player) {
    if (this.isSpectator(player)
        && !(this.getCycle() instanceof BungeeGameCycle && this.getCycle().isEndGameRunning()
        && BedwarsRel.getInstance().getBooleanConfig("bungeecord.endgame-in-lobby", true))) {

      player.setAllowFlight(true);
      player.setFlying(true);
      player.setGameMode(GameMode.SPECTATOR);

    } else {
      if (this.getState().equals(GameState.RUNNING)) {
        player.setGameMode(GameMode.SURVIVAL);
      } else if (this.getState().equals(GameState.WAITING)) {
        Integer gameMode = BedwarsRel.getInstance().getIntConfig("lobby-gamemode", 0);
        if (gameMode == 0) {
          player.setGameMode(GameMode.SURVIVAL);
        } else if (gameMode == 1) {
          player.setGameMode(GameMode.CREATIVE);
        } else if (gameMode == 2) {
          player.setGameMode(GameMode.ADVENTURE);
        } else if (gameMode == 3) {
          player.setGameMode(GameMode.SPECTATOR);
        }
      }
    }
  }

  public void setPlayerVisibility(Player player) {
    ArrayList<Player> players = new ArrayList<Player>();
    players.addAll(this.getPlayers());

    if (this.state == GameState.RUNNING
        && !(this.getCycle() instanceof BungeeGameCycle && this.getCycle().isEndGameRunning()
        && BedwarsRel.getInstance().getBooleanConfig("bungeecord.endgame-in-lobby", true))) {
      if (this.isSpectator(player)) {
        if (player.getGameMode().equals(GameMode.SURVIVAL)) {
          for (Player playerInGame : players) {
            playerInGame.hidePlayer(player);
            player.showPlayer(playerInGame);
          }
        } else {
          for (Player teamPlayer : this.getTeamPlayers()) {
            teamPlayer.hidePlayer(player);
            player.showPlayer(teamPlayer);
          }
          for (Player freePlayer : this.getFreePlayers()) {
            freePlayer.showPlayer(player);
            player.showPlayer(freePlayer);
          }
        }
      } else {
        for (Player playerInGame : players) {
          playerInGame.showPlayer(player);
          player.showPlayer(playerInGame);
        }
      }
    } else {
      for (Player playerInGame : players) {
        if (!playerInGame.equals(player)) {
          playerInGame.showPlayer(player);
          player.showPlayer(playerInGame);
        }
      }
    }

  }

  public void setScoreboard(Scoreboard sb) {
    this.scoreboard = sb;
  }

  public void setState(GameState state) {
    this.state = state;
    this.updateSigns();
  }

  public boolean start(CommandSender sender) {
    if (this.state != GameState.WAITING) {
      sender.sendMessage(
          ChatWriter
              .pluginMessage(ChatColor.RED + BedwarsRel._l(sender, "errors.startoutofwaiting")));
      return false;
    }

    BedwarsGameStartEvent startEvent = new BedwarsGameStartEvent(this);
    BedwarsRel.getInstance().getServer().getPluginManager().callEvent(startEvent);

    if (startEvent.isCancelled()) {
      return false;
    }

    this.isOver = false;
    for (Player aPlayer : this.getPlayers()) {
      if (aPlayer.isOnline()) {
        aPlayer.sendMessage(
            ChatWriter
                .pluginMessage(ChatColor.GREEN + BedwarsRel._l(aPlayer, "ingame.gamestarting")));
      }
    }

    // load shop categories again (if shop was changed)
    this.loadItemShopCategories();

    this.runningTasks.clear();
    this.cleanUsersInventory();
    this.clearProtections();
    this.moveFreePlayersToTeam();
    this.makeTeamsReady();

    this.cycle.onGameStart();
    this.startResourceSpawners();

    // Update world time before game starts
    this.getRegion().getWorld().setTime(this.time);

    this.teleportPlayersToTeamSpawn();

    this.state = GameState.RUNNING;

    for (Player player : this.getPlayers()) {
      this.setPlayerGameMode(player);
      this.setPlayerVisibility(player);
    }

    this.startActionBarRunnable();
    this.updateScoreboard();

    if (BedwarsRel.getInstance().getBooleanConfig("store-game-records", true)) {
      this.displayRecord();
    }

    this.startTimerCountdown();

    if (BedwarsRel.getInstance().getBooleanConfig("titles.map.enabled", false)) {
      this.displayMapInfo();
    }

    this.updateSigns();

    if (BedwarsRel.getInstance().getBooleanConfig("global-messages", true)) {
      for (Player aPlayer : BedwarsRel.getInstance().getServer().getOnlinePlayers()) {
        aPlayer.sendMessage(ChatWriter.pluginMessage(ChatColor.GREEN
            + BedwarsRel._l(aPlayer, "ingame.gamestarted",
            ImmutableMap.of("game", this.getRegion().getName()))));
      }
      BedwarsRel.getInstance().getServer().getConsoleSender()
          .sendMessage(ChatWriter.pluginMessage(ChatColor.GREEN
              + BedwarsRel
              ._l(BedwarsRel.getInstance().getServer().getConsoleSender(), "ingame.gamestarted",
                  ImmutableMap.of("game", this.getRegion().getName()))));
    }

    BedwarsGameStartedEvent startedEvent = new BedwarsGameStartedEvent(this);
    BedwarsRel.getInstance().getServer().getPluginManager().callEvent(startedEvent);

    return true;
  }

  private void startActionBarRunnable() {
    if (BedwarsRel.getInstance().getBooleanConfig("show-team-in-actionbar", false)) {
      try {
        Class<?> clazz = Class.forName("io.github.bedwarsrel.com."
            + BedwarsRel.getInstance().getCurrentVersion().toLowerCase() + ".ActionBar");
        final Method sendActionBar =
            clazz.getDeclaredMethod("sendActionBar", Player.class, String.class);

        BukkitTask task = new BukkitRunnable() {

          @Override
          public void run() {
            for (Team team : Game.this.getTeams().values()) {
              for (Player player : team.getPlayers()) {
                try {
                  sendActionBar.invoke(null, player,
                      team.getChatColor() + BedwarsRel._l(player, "ingame.team") + " " + team
                          .getDisplayName());
                } catch (IllegalAccessException | IllegalArgumentException
                    | InvocationTargetException e) {
                  BedwarsRel.getInstance().getBugsnag().notify(e);
                  e.printStackTrace();
                }
              }
            }
          }
        }.runTaskTimer(BedwarsRel.getInstance(), 0L, 20L);
        this.addRunningTask(task);
      } catch (Exception ex) {
        BedwarsRel.getInstance().getBugsnag().notify(ex);
        ex.printStackTrace();
      }
    }
  }

  private void startResourceSpawners() {
    for (ResourceSpawner rs : this.getRessourceSpawner()) {
      rs.setGame(this);
      this.runningTasks.add(BedwarsRel.getInstance().getServer().getScheduler().runTaskTimer(
          BedwarsRel.getInstance(), rs, Math.round((((double) rs.getInterval()) / 1000.0) * 20.0),
          Math.round((((double) rs.getInterval()) / 1000.0) * 20.0)));
    }
  }

  private void startTimerCountdown() {
    this.timeLeft = BedwarsRel.getInstance().getMaxLength();
    this.length = BedwarsRel.getInstance().getMaxLength();
    BukkitRunnable task = new BukkitRunnable() {

      @Override
      public void run() {
        Game.this.updateScoreboardTimer();
        if (Game.this.timeLeft == 0) {
          Game.this.isOver = true;
          Game.this.getCycle().checkGameOver();
          this.cancel();
          return;
        }

        Game.this.timeLeft--;
      }
    };

    this.runningTasks.add(task.runTaskTimer(BedwarsRel.getInstance(), 0L, 20L));
  }

  public boolean stop() {
    if (this.state == GameState.STOPPED) {
      return false;
    }

    this.isStopping = true;

    this.stopWorkers();
    this.clearProtections();

    try {
      this.kickAllPlayers();
    } catch (Exception e) {
      BedwarsRel.getInstance().getBugsnag().notify(e);
      e.printStackTrace();
    }
    this.resetRegion();
    this.state = GameState.STOPPED;
    this.updateSigns();

    this.isStopping = false;
    return true;
  }

  public void stopWorkers() {
    for (BukkitTask task : this.runningTasks) {
      try {
        task.cancel();
      } catch (Exception ex) {
        BedwarsRel.getInstance().getBugsnag().notify(ex);
        // already cancelled
      }
    }

    this.runningTasks.clear();
  }

  private void teleportPlayersToTeamSpawn() {
    for (Team team : this.teams.values()) {
      for (Player player : team.getPlayers()) {
        if (!player.getWorld().equals(team.getSpawnLocation().getWorld())) {
          this.getPlayerSettings(player).setTeleporting(true);
        }
        player.setVelocity(new Vector(0, 0, 0));
        player.setFallDistance(0.0F);
        player.teleport(team.getSpawnLocation());
        if (this.getPlayerStorage(player) != null) {
          this.getPlayerStorage(player).clean();
        }
      }
    }
  }

  public void toSpectator(Player player) {
    final Player p = player;

    if (!this.freePlayers.contains(player)) {
      this.freePlayers.add(player);
    }

    PlayerStorage storage = this.getPlayerStorage(player);
    if (storage != null) {
      storage.clean();
    } else {
      storage = this.addPlayerStorage(player);
      storage.store();
      storage.clean();
    }

    final Location location = this.getPlayerTeleportLocation(p);

    if (!p.getLocation().getWorld().equals(location.getWorld())) {
      this.getPlayerSettings(p).setTeleporting(true);
      if (BedwarsRel.getInstance().isBungee()) {
        new BukkitRunnable() {

          @Override
          public void run() {
            p.teleport(location);
          }

        }.runTaskLater(BedwarsRel.getInstance(), 10L);

      } else {
        p.teleport(location);
      }
    }

    new BukkitRunnable() {

      @Override
      public void run() {
        Game.this.setPlayerGameMode(p);
        Game.this.setPlayerVisibility(p);
      }

    }.runTaskLater(BedwarsRel.getInstance(), 15L);

    // Leave game (Slimeball)
    ItemStack leaveGame = new ItemStack(Material.SLIME_BALL, 1);
    ItemMeta im = leaveGame.getItemMeta();
    im.setDisplayName(BedwarsRel._l(player, "lobby.leavegame"));
    leaveGame.setItemMeta(im);
    p.getInventory().setItem(8, leaveGame);

    if (this.getCycle() instanceof BungeeGameCycle && this.getCycle().isEndGameRunning()
        && BedwarsRel.getInstance().getBooleanConfig("bungeecord.endgame-in-lobby", true)) {
      p.updateInventory();
      return;
    }

    // Teleport to player (Compass)
    ItemStack teleportPlayer = new ItemStack(Material.COMPASS, 1);
    im = teleportPlayer.getItemMeta();
    im.setDisplayName(BedwarsRel._l(p, "ingame.spectate"));
    teleportPlayer.setItemMeta(im);
    p.getInventory().setItem(0, teleportPlayer);

    p.updateInventory();
    this.updateScoreboard();

  }

  private void updateLobbyScoreboard() {
    this.scoreboard.clearSlot(DisplaySlot.SIDEBAR);

    Objective obj = this.scoreboard.getObjective("lobby");
    if (obj != null) {
      obj.unregister();
    }

    obj = this.scoreboard.registerNewObjective("lobby", "dummy");
    obj.setDisplaySlot(DisplaySlot.SIDEBAR);
    obj.setDisplayName(this.formatLobbyScoreboardString(
        BedwarsRel.getInstance().getStringConfig("lobby-scoreboard.title", "&eBEDWARS")));

    List<String> rows = BedwarsRel.getInstance().getConfig()
        .getStringList("lobby-scoreboard.content");
    int rowMax = rows.size();
    if (rows == null || rows.isEmpty()) {
      return;
    }

    for (String row : rows) {
      if (row.trim().equals("")) {
        for (int i = 0; i <= rowMax; i++) {
          row = row + " ";
        }
      }

      Score score = obj.getScore(this.formatLobbyScoreboardString(row));
      score.setScore(rowMax);
      rowMax--;
    }

    for (Player player : this.getPlayers()) {
      player.setScoreboard(this.scoreboard);
    }
  }

  public void updateScoreboard() {
    if (this.state == GameState.WAITING
        && BedwarsRel.getInstance().getBooleanConfig("lobby-scoreboard.enabled", true)) {
      this.updateLobbyScoreboard();
      return;
    }

    Objective obj = this.scoreboard.getObjective("display");
    if (obj == null) {
      obj = this.scoreboard.registerNewObjective("display", "dummy");
    }

    obj.setDisplaySlot(DisplaySlot.SIDEBAR);
    obj.setDisplayName(this.formatScoreboardTitle());

    for (Team t : this.teams.values()) {
      this.scoreboard.resetScores(this.formatScoreboardTeam(t, false));
      this.scoreboard.resetScores(this.formatScoreboardTeam(t, true));

      boolean teamDead = (t.isDead(this) && this.getState() == GameState.RUNNING) ? true : false;
      Score score = obj.getScore(this.formatScoreboardTeam(t, teamDead));
      score.setScore(t.getPlayers().size());
    }

    for (Player player : this.getPlayers()) {
      player.setScoreboard(this.scoreboard);
    }
  }

  private void updateScoreboardTimer() {
    Objective obj = this.scoreboard.getObjective("display");
    if (obj == null) {
      obj = this.scoreboard.registerNewObjective("display", "dummy");
    }

    obj.setDisplayName(this.formatScoreboardTitle());

    for (Player player : this.getPlayers()) {
      player.setScoreboard(this.scoreboard);
    }
  }

  private void updateSignConfig() {
    try {
      File config = new File(
          BedwarsRel.getInstance().getDataFolder() + "/" + GameManager.gamesPath + "/"
              + this.name + "/sign.yml");

      YamlConfiguration cfg = new YamlConfiguration();
      if (config.exists()) {
        cfg = YamlConfiguration.loadConfiguration(config);
      }

      List<Map<String, Object>> locList = new ArrayList<Map<String, Object>>();
      for (Location loc : this.joinSigns.keySet()) {
        locList.add(Utils.locationSerialize(loc));
      }

      cfg.set("signs", locList);
      cfg.save(config);
    } catch (Exception ex) {
      BedwarsRel.getInstance().getBugsnag().notify(ex);
      BedwarsRel.getInstance().getServer().getConsoleSender()
          .sendMessage(ChatWriter.pluginMessage(ChatColor.RED + BedwarsRel
              ._l(BedwarsRel.getInstance().getServer().getConsoleSender(), "errors.savesign")));
    }
  }

  public void updateSigns() {
    boolean removedItem = false;

    Iterator<GameJoinSign> iterator = Game.this.joinSigns.values().iterator();
    while (iterator.hasNext()) {
      GameJoinSign sign = iterator.next();

      Chunk signChunk = sign.getSign().getLocation().getChunk();
      if (!signChunk.isLoaded()) {
        signChunk.load(true);
      }

      if (sign.getSign() == null) {
        iterator.remove();
        removedItem = true;
        continue;
      }

      Block signBlock = sign.getSign().getLocation().getBlock();
      if (!(signBlock.getState() instanceof Sign)) {
        iterator.remove();
        removedItem = true;
        continue;
      }
      sign.updateSign();
    }

    if (removedItem) {
      Game.this.updateSignConfig();
    }
  }
}
