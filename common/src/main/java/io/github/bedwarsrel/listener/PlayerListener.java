package io.github.bedwarsrel.listener;

import io.github.bedwarsrel.BedwarsRel;
import io.github.bedwarsrel.events.BedwarsOpenShopEvent;
import io.github.bedwarsrel.events.BedwarsPlayerSetNameEvent;
import io.github.bedwarsrel.game.BungeeGameCycle;
import io.github.bedwarsrel.game.Game;
import io.github.bedwarsrel.game.GameState;
import io.github.bedwarsrel.game.Team;
import io.github.bedwarsrel.shop.NewItemShop;
import io.github.bedwarsrel.utils.ChatWriter;
import io.github.bedwarsrel.villager.MerchantCategory;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerBedEnterEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerToggleFlightEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.material.Wool;
import org.bukkit.scheduler.BukkitRunnable;

public class PlayerListener extends BaseListener {

  private String getChatFormat(String format, Team team, boolean isSpectator, boolean all) {
    String form = format;

    if (all) {
      form = form.replace("$all$", BedwarsRel._l("ingame.all") + ChatColor.RESET);
    }

    form = form.replace("$player$",
        ((!isSpectator && team != null) ? team.getChatColor() : "") + "%1$s" + ChatColor.RESET);
    form = form.replace("$msg$", "%2$s");

    if (isSpectator) {
      form = form.replace("$team$", BedwarsRel._l("ingame.spectator"));
    } else if (team != null) {
      form = form.replace("$team$", team.getDisplayName() + ChatColor.RESET);
    }

    return ChatColor.translateAlternateColorCodes('&', form);
  }

  @SuppressWarnings("deprecation")
  private void inGameInteractEntity(PlayerInteractEntityEvent iee, Game game, Player player) {

    if (BedwarsRel.getInstance().getCurrentVersion().startsWith("v1_8")) {
      if (iee.getPlayer().getItemInHand().getType().equals(Material.MONSTER_EGG)
          || iee.getPlayer().getItemInHand().getType().equals(Material.MONSTER_EGGS)
          || iee.getPlayer().getItemInHand().getType().equals(Material.DRAGON_EGG)) {
        iee.setCancelled(true);
        return;
      }
    } else {
      if (iee.getPlayer().getInventory().getItemInMainHand().getType().equals(Material.MONSTER_EGG)
          || iee.getPlayer().getInventory().getItemInMainHand().getType()
          .equals(Material.MONSTER_EGGS)
          || iee.getPlayer().getInventory().getItemInMainHand().getType()
          .equals(Material.DRAGON_EGG)
          || iee.getPlayer().getInventory().getItemInOffHand().getType()
          .equals(Material.MONSTER_EGG)
          || iee.getPlayer().getInventory().getItemInOffHand().getType()
          .equals(Material.MONSTER_EGGS)
          || iee.getPlayer().getInventory().getItemInOffHand().getType()
          .equals(Material.DRAGON_EGG)) {
        iee.setCancelled(true);
        return;
      }
    }

    if (iee.getRightClicked() != null
        && !iee.getRightClicked().getType().equals(EntityType.VILLAGER)) {
      List<EntityType> preventClickTypes =
          Arrays.asList(EntityType.ITEM_FRAME, EntityType.ARMOR_STAND);

      if (preventClickTypes.contains(iee.getRightClicked().getType())) {
        iee.setCancelled(true);
      }

      return;
    }

    if (game.isSpectator(player)) {
      return;
    }

    if (!BedwarsRel.getInstance().getBooleanConfig("use-build-in-shop", true)) {
      return;
    }

    iee.setCancelled(true);

    BedwarsOpenShopEvent openShopEvent =
        new BedwarsOpenShopEvent(game, player, game.getItemShopCategories(), iee.getRightClicked());
    BedwarsRel.getInstance().getServer().getPluginManager().callEvent(openShopEvent);

    if (openShopEvent.isCancelled()) {
      return;
    }

    if (game.getPlayerSettings(player).useOldShop()) {
      MerchantCategory.openCategorySelection(player, game);
    } else {
      NewItemShop itemShop = game.getNewItemShop(player);
      if (itemShop == null) {
        itemShop = game.openNewItemShop(player);
      }

      itemShop.setCurrentCategory(null);
      itemShop.openCategoryInventory(player);
    }
  }

  /*
   * GAME
   */

  @EventHandler(priority = EventPriority.HIGHEST)
  public void onChat(AsyncPlayerChatEvent ce) {
    if (ce.isCancelled()) {
      return;
    }

    Player player = ce.getPlayer();
    Game game = BedwarsRel.getInstance().getGameManager().getGameOfPlayer(player);

    if (game == null) {
      boolean seperateGameChat = BedwarsRel
          .getInstance().getBooleanConfig("seperate-game-chat", true);
      if (!seperateGameChat) {
        return;
      }

      Iterator<Player> recipients = ce.getRecipients().iterator();
      while (recipients.hasNext()) {
        Player recipient = recipients.next();
        Game recipientGame = BedwarsRel.getInstance().getGameManager().getGameOfPlayer(recipient);
        if (recipientGame != null) {
          recipients.remove();
        }
      }
      return;
    }

    if (game.getState() == GameState.STOPPED) {
      return;
    }

    Team team = game.getPlayerTeam(player);
    String message = ce.getMessage();
    boolean isSpectator = game.isSpectator(player);

    String displayName = player.getDisplayName();
    String playerListName = player.getPlayerListName();

    if (BedwarsRel.getInstance().getBooleanConfig("overwrite-names", false)) {
      if (team == null) {
        displayName = ChatColor.stripColor(player.getName());

        playerListName = ChatColor.stripColor(player.getName());
      } else {
        displayName = team.getChatColor() + ChatColor.stripColor(player.getName());
        playerListName = team.getChatColor() + ChatColor.stripColor(player.getName());
      }

    }

    if (BedwarsRel.getInstance().getBooleanConfig("teamname-on-tab", false)) {
      if (team == null || isSpectator) {
        playerListName = ChatColor.stripColor(player.getDisplayName());
      } else {
        playerListName = team.getChatColor() + team.getName() + ChatColor.WHITE + " | "
            + team.getChatColor() + ChatColor.stripColor(player.getDisplayName());
      }
    }

    BedwarsPlayerSetNameEvent playerSetNameEvent =
        new BedwarsPlayerSetNameEvent(team, displayName, playerListName, player);
    BedwarsRel.getInstance().getServer().getPluginManager().callEvent(playerSetNameEvent);

    if (!playerSetNameEvent.isCancelled()) {
      player.setDisplayName(playerSetNameEvent.getDisplayName());
      player.setPlayerListName(playerSetNameEvent.getPlayerListName());
    }

    if (game.getState() != GameState.RUNNING && game.getState() == GameState.WAITING) {
      String format = null;
      if (team == null) {
        format = this.getChatFormat(
            BedwarsRel.getInstance().getStringConfig("lobby-chatformat", "$player$: $msg$"), null,
            false,
            true);
      } else {
        format = this.getChatFormat(
            BedwarsRel.getInstance()
                .getStringConfig("ingame-chatformat", "<$team$>$player$: $msg$"),
            team, false, true);
      }

      ce.setFormat(format);

      if (!BedwarsRel.getInstance().getBooleanConfig("seperate-game-chat", true)) {
        return;
      }

      Iterator<Player> recipiens = ce.getRecipients().iterator();
      while (recipiens.hasNext()) {
        Player recipient = recipiens.next();
        if (!game.isInGame(recipient)) {
          recipiens.remove();
        }
      }

      return;
    }

    @SuppressWarnings("unchecked")
    List<String> toAllPrefixList = (List<String>) BedwarsRel.getInstance().getConfig()
        .getList("chat-to-all-prefix", Arrays.asList("@"));

    String toAllPrefix = null;

    for (String oneToAllPrefix : toAllPrefixList) {
      if (message.trim().startsWith(oneToAllPrefix)) {
        toAllPrefix = oneToAllPrefix;
      }
    }

    if (toAllPrefix != null || isSpectator || (game.getCycle().isEndGameRunning()
        && BedwarsRel.getInstance().getBooleanConfig("global-chat-after-end", true))) {
      boolean seperateSpectatorChat =
          BedwarsRel.getInstance().getBooleanConfig("seperate-spectator-chat", false);

      message = message.trim();
      String format = null;
      if (!isSpectator && !(game.getCycle().isEndGameRunning()
          && BedwarsRel.getInstance().getBooleanConfig("global-chat-after-end", true))) {
        ce.setMessage(message.substring(toAllPrefix.length(), message.length()).trim());
        format = this
            .getChatFormat(BedwarsRel.getInstance().getStringConfig("ingame-chatformat-all",
                "[$all$] <$team$>$player$: $msg$"), team, false, true);
      } else {
        ce.setMessage(message);
        format = this.getChatFormat(
            BedwarsRel.getInstance()
                .getStringConfig("ingame-chatformat", "<$team$>$player$: $msg$"),
            team, isSpectator, true);
      }

      ce.setFormat(format);

      if (!BedwarsRel.getInstance().isBungee() || seperateSpectatorChat) {
        Iterator<Player> recipiens = ce.getRecipients().iterator();
        while (recipiens.hasNext()) {
          Player recipient = recipiens.next();
          if (!game.isInGame(recipient)) {
            recipiens.remove();
            continue;
          }

          if (!seperateSpectatorChat || (game.getCycle().isEndGameRunning()
              && BedwarsRel.getInstance().getBooleanConfig("global-chat-after-end", true))) {
            continue;
          }

          if (isSpectator && !game.isSpectator(recipient)) {
            recipiens.remove();
          } else if (!isSpectator && game.isSpectator(recipient)) {
            recipiens.remove();
          }
        }
      }
    } else {
      message = message.trim();
      ce.setMessage(message);
      ce.setFormat(this.getChatFormat(
          BedwarsRel.getInstance().getStringConfig("ingame-chatformat", "<$team$>$player$: $msg$"),
          team,
          false, false));

      Iterator<Player> recipiens = ce.getRecipients().iterator();
      while (recipiens.hasNext()) {
        Player recipient = recipiens.next();
        if (!game.isInGame(recipient) || !team.isInTeam(recipient)) {
          recipiens.remove();
        }
      }
    }
  }

  @EventHandler(priority = EventPriority.HIGHEST)
  public void onCommand(PlayerCommandPreprocessEvent pcpe) {
    Player player = pcpe.getPlayer();
    Game game = BedwarsRel.getInstance().getGameManager().getGameOfPlayer(player);

    if (game == null) {
      return;
    }

    if (game.getState() == GameState.STOPPED) {
      return;
    }

    String message = pcpe.getMessage();
    if (!message.startsWith("/bw")) {

      for (String allowed : BedwarsRel.getInstance().getAllowedCommands()) {
        if (!allowed.startsWith("/")) {
          allowed = "/" + allowed;
        }

        if (message.startsWith(allowed.trim())) {
          return;
        }
      }

      if (player.hasPermission("bw.cmd")) {
        return;
      }

      pcpe.setCancelled(true);
      return;
    }
  }

  @EventHandler
  public void onCraft(CraftItemEvent cie) {
    Player player = (Player) cie.getWhoClicked();
    Game game = BedwarsRel.getInstance().getGameManager().getGameOfPlayer(player);

    if (game == null) {
      return;
    }

    if (game.getState() == GameState.STOPPED) {
      return;
    }

    if (BedwarsRel.getInstance().getBooleanConfig("allow-crafting", false)) {
      return;
    }

    cie.setCancelled(true);
  }

  @EventHandler
  public void onDamage(EntityDamageEvent ede) {
    if (!(ede.getEntity() instanceof Player)) {
      if (!(ede instanceof EntityDamageByEntityEvent)) {
        return;
      }

      EntityDamageByEntityEvent edbee = (EntityDamageByEntityEvent) ede;
      if (edbee.getDamager() == null || !(edbee.getDamager() instanceof Player)) {
        return;
      }

      Player player = (Player) edbee.getDamager();
      Game game = BedwarsRel.getInstance().getGameManager().getGameOfPlayer(player);

      if (game == null) {
        return;
      }

      if (game.getState() == GameState.WAITING) {
        ede.setCancelled(true);
      }

      return;
    }

    Player p = (Player) ede.getEntity();
    Game g = BedwarsRel.getInstance().getGameManager().getGameOfPlayer(p);
    if (g == null) {
      return;
    }

    if (g.getState() == GameState.STOPPED) {
      return;
    }

    if (g.getState() == GameState.RUNNING) {
      if (g.isSpectator(p)) {
        ede.setCancelled(true);
        return;
      }

      if (g.isProtected(p) && ede.getCause() != DamageCause.VOID) {
        ede.setCancelled(true);
        return;
      }

      if (BedwarsRel.getInstance().getBooleanConfig("die-on-void", false)
          && ede.getCause() == DamageCause.VOID) {
        ede.setCancelled(true);
        p.setHealth(0);
        return;
      }

      if (ede instanceof EntityDamageByEntityEvent) {
        EntityDamageByEntityEvent edbee = (EntityDamageByEntityEvent) ede;

        if (edbee.getDamager() instanceof Player) {
          Player damager = (Player) edbee.getDamager();
          if (g.isSpectator(damager)) {
            ede.setCancelled(true);
            return;
          }

          g.setPlayerDamager(p, damager);
        } else if (edbee.getDamager().getType().equals(EntityType.ARROW)) {
          Arrow arrow = (Arrow) edbee.getDamager();
          if (arrow.getShooter() instanceof Player) {
            Player shooter = (Player) arrow.getShooter();
            if (g.isSpectator(shooter)) {
              ede.setCancelled(true);
              return;
            }

            g.setPlayerDamager(p, (Player) arrow.getShooter());
          }
        }
      }

      if (!g.getCycle().isEndGameRunning()) {
        return;
      } else if (ede.getCause() == DamageCause.VOID) {
        p.teleport(g.getPlayerTeam(p).getSpawnLocation());
      }
    } else if (g.getState() == GameState.WAITING
        && ede.getCause() == EntityDamageEvent.DamageCause.VOID) {
      p.teleport(g.getLobby());
    }

    ede.setCancelled(true);
  }

  @EventHandler
  public void onDrop(PlayerDropItemEvent die) {
    Player p = die.getPlayer();
    Game g = BedwarsRel.getInstance().getGameManager().getGameOfPlayer(p);
    if (g == null) {
      return;
    }

    if (g.getState() != GameState.WAITING) {
      if (g.isSpectator(p)) {
        die.setCancelled(true);
      }

      return;
    }

    die.setCancelled(true);
  }

  @EventHandler(priority = EventPriority.HIGHEST)
  public void onFly(PlayerToggleFlightEvent tfe) {
    Player p = tfe.getPlayer();

    Game g = BedwarsRel.getInstance().getGameManager().getGameOfPlayer(p);
    if (g == null) {
      return;
    }

    if (g.getState() == GameState.STOPPED) {
      return;
    }

    if (g.getState() == GameState.RUNNING && g.isSpectator(p)) {
      tfe.setCancelled(false);
      return;
    }

    tfe.setCancelled(true);
  }

  @EventHandler(priority = EventPriority.HIGH)
  public void onHunger(FoodLevelChangeEvent flce) {
    if (!(flce.getEntity() instanceof Player)) {
      return;
    }

    Player player = (Player) flce.getEntity();
    Game game = BedwarsRel.getInstance().getGameManager().getGameOfPlayer(player);

    if (game == null) {
      return;
    }

    if (game.getState() == GameState.RUNNING) {
      if (game.isSpectator(player) || game.getCycle().isEndGameRunning()) {
        flce.setCancelled(true);
        return;
      }

      flce.setCancelled(false);
      return;
    }

    flce.setCancelled(true);
  }

  @SuppressWarnings({"rawtypes", "unchecked"})
  private void onIngameInventoryClick(InventoryClickEvent ice, Player player, Game game) {
    if (!ice.getInventory().getName().equals(BedwarsRel._l(player, "ingame.shop.name"))) {
      if (game.isSpectator(player)
          || (game.getCycle() instanceof BungeeGameCycle && game.getCycle().isEndGameRunning()
          && BedwarsRel.getInstance().getBooleanConfig("bungeecord.endgame-in-lobby", true))) {

        ItemStack clickedStack = ice.getCurrentItem();
        if (clickedStack == null) {
          return;
        }

        if (ice.getInventory().getName().equals(BedwarsRel._l(player, "ingame.spectator"))) {
          ice.setCancelled(true);
          if (!clickedStack.getType().equals(Material.SKULL_ITEM)) {
            return;
          }

          SkullMeta meta = (SkullMeta) clickedStack.getItemMeta();
          Player pl = BedwarsRel.getInstance().getServer().getPlayer(meta.getOwner());
          if (pl == null) {
            return;
          }

          if (!game.isInGame(pl)) {
            return;
          }

          player.teleport(pl);
          player.closeInventory();
          return;
        }

        Material clickedMat = ice.getCurrentItem().getType();
        if (clickedMat.equals(Material.SLIME_BALL)) {
          game.playerLeave(player, false);
        }

        if (clickedMat.equals(Material.COMPASS)) {
          game.openSpectatorCompass(player);
        }
      }
      return;
    }

    ice.setCancelled(true);
    ItemStack clickedStack = ice.getCurrentItem();

    if (clickedStack == null) {
      return;
    }

    if (game.getPlayerSettings(player).useOldShop()) {
      try {
        if (clickedStack.getType() == Material.SNOW_BALL) {
          game.getPlayerSettings(player).setUseOldShop(false);

          // open new shop
          NewItemShop itemShop = game.openNewItemShop(player);
          itemShop.setCurrentCategory(null);
          itemShop.openCategoryInventory(player);
          return;
        }

        MerchantCategory cat = game.getItemShopCategories().get(clickedStack.getType());
        if (cat == null) {
          return;
        }

        Class clazz = Class.forName("io.github.bedwarsrel.com."
            + BedwarsRel.getInstance().getCurrentVersion().toLowerCase() + ".VillagerItemShop");
        Object villagerItemShop =
            clazz.getDeclaredConstructor(Game.class, Player.class, MerchantCategory.class)
                .newInstance(game, player, cat);

        Method openTrade = clazz.getDeclaredMethod("openTrading", new Class[]{});
        openTrade.invoke(villagerItemShop, new Object[]{});
      } catch (Exception ex) {
        BedwarsRel.getInstance().getBugsnag().notify(ex);
        ex.printStackTrace();
      }
    } else {
      game.getNewItemShop(player).handleInventoryClick(ice, game, player);
    }
  }

  @EventHandler
  public void onInteractEntity(PlayerInteractEntityEvent iee) {
    Player p = iee.getPlayer();
    Game g = BedwarsRel.getInstance().getGameManager().getGameOfPlayer(p);
    if (g == null) {
      return;
    }

    if (g.getState() == GameState.WAITING) {
      iee.setCancelled(true);
      return;
    }

    if (g.getState() == GameState.RUNNING) {
      this.inGameInteractEntity(iee, g, p);
    }
  }

  @EventHandler
  public void onInventoryClick(InventoryClickEvent ice) {
    Player player = (Player) ice.getWhoClicked();
    Game game = BedwarsRel.getInstance().getGameManager().getGameOfPlayer(player);

    if (game == null) {
      return;
    }

    if (game.getState() == GameState.WAITING) {
      this.onLobbyInventoryClick(ice, player, game);
    }

    if (game.getState() == GameState.RUNNING) {
      this.onIngameInventoryClick(ice, player, game);
    }
  }

  /*
   * LOBBY & GAME
   */

  @EventHandler(priority = EventPriority.HIGHEST)
  public void onJoin(PlayerJoinEvent je) {

    final Player player = je.getPlayer();

    if (BedwarsRel.getInstance().statisticsEnabled()) {
      BedwarsRel.getInstance().getPlayerStatisticManager().loadStatistic(player.getUniqueId());
    }

    if (BedwarsRel.getInstance().isHologramsEnabled()
        && BedwarsRel.getInstance().getHolographicInteractor() != null && BedwarsRel.getInstance()
        .getHolographicInteractor().getType().equalsIgnoreCase("HolographicDisplays")) {
      BedwarsRel.getInstance().getHolographicInteractor().updateHolograms(player, 60L);
    }

    ArrayList<Game> games = BedwarsRel.getInstance().getGameManager().getGames();
    if (games.size() == 0) {
      return;
    }

    if (!BedwarsRel.getInstance().isBungee()) {
      Game game = BedwarsRel.getInstance().getGameManager().getGameByLocation(player.getLocation());

      if (game != null) {
        if (game.getMainLobby() != null) {
          player.teleport(game.getMainLobby());
        } else {
          game.playerJoins(player);
        }
        return;
      }
    }

    if (BedwarsRel.getInstance().isBungee()) {
      je.setJoinMessage(null);
      final Game firstGame = games.get(0);

      if (firstGame.getState() == GameState.STOPPED && player.hasPermission("bw.setup")) {
        return;
      }

      firstGame.playerJoins(player);

    }
  }

  private void onLobbyInventoryClick(InventoryClickEvent ice, Player player, Game game) {
    Inventory inv = ice.getInventory();
    ItemStack clickedStack = ice.getCurrentItem();

    if (!inv.getTitle().equals(BedwarsRel._l(player, "lobby.chooseteam"))) {
      ice.setCancelled(true);
      return;
    }

    if (clickedStack == null) {
      ice.setCancelled(true);
      return;
    }

    if (clickedStack.getType() != Material.WOOL) {
      ice.setCancelled(true);
      return;
    }

    ice.setCancelled(true);
    Wool wool = (Wool) clickedStack.getData();
    Team team = game.getTeamByDyeColor(wool.getColor());
    if (team == null) {
      return;
    }

    game.playerJoinTeam(player, team);
    player.closeInventory();
  }

  @EventHandler(priority = EventPriority.HIGHEST)
  public void onPlayerDie(PlayerDeathEvent pde) {
    final Player player = pde.getEntity();
    Game game = BedwarsRel.getInstance().getGameManager().getGameOfPlayer(player);

    if (game == null) {
      return;
    }

    if (game.getState() == GameState.RUNNING) {
      pde.setDroppedExp(0);
      pde.setDeathMessage(null);

      if (!BedwarsRel.getInstance().getBooleanConfig("player-drops", false)) {
        pde.getDrops().clear();
      }

      try {
        if (!BedwarsRel.getInstance().isSpigot()) {
          Class<?> clazz = null;
          try {
            clazz = Class.forName("io.github.bedwarsrel.com."
                + BedwarsRel.getInstance().getCurrentVersion().toLowerCase()
                + ".PerformRespawnRunnable");
          } catch (ClassNotFoundException ex) {
            BedwarsRel.getInstance().getBugsnag().notify(ex);
            clazz = Class
                .forName("io.github.bedwarsrel.com.fallback.PerformRespawnRunnable");
          }

          BukkitRunnable respawnRunnable =
              (BukkitRunnable) clazz.getDeclaredConstructor(Player.class).newInstance(player);
          respawnRunnable.runTaskLater(BedwarsRel.getInstance(), 20L);
        } else {
          new BukkitRunnable() {

            @Override
            public void run() {
              player.spigot().respawn();
            }
          }.runTaskLater(BedwarsRel.getInstance(), 20L);
        }

      } catch (Exception e) {
        BedwarsRel.getInstance().getBugsnag().notify(e);
        e.printStackTrace();
      }

      pde.setKeepInventory(
          BedwarsRel.getInstance().getBooleanConfig("keep-inventory-on-death", false));

      Player killer = player.getKiller();
      if (killer == null) {
        killer = game.getPlayerDamager(player);
      }

      game.getCycle().onPlayerDies(player, killer);
    }
  }

  /*
   * LOBBY
   */

  @EventHandler
  public void onPlayerInteract(PlayerInteractEvent pie) {
    Player player = pie.getPlayer();
    Game g = BedwarsRel.getInstance().getGameManager().getGameOfPlayer(player);

    if (g == null) {
      if (pie.getAction() != Action.RIGHT_CLICK_BLOCK
          && pie.getAction() != Action.RIGHT_CLICK_AIR) {
        return;
      }

      Block clicked = pie.getClickedBlock();

      if (clicked == null) {
        return;
      }

      if (!(clicked.getState() instanceof Sign)) {
        return;
      }

      Game game = BedwarsRel.getInstance().getGameManager()
          .getGameBySignLocation(clicked.getLocation());
      if (game == null) {
        return;
      }

      if (game.playerJoins(player)) {
        player.sendMessage(
            ChatWriter.pluginMessage(ChatColor.GREEN + BedwarsRel._l(player, "success.joined")));
      }
      return;
    }

    if (g.getState() == GameState.STOPPED) {
      return;
    }

    Material interactingMaterial = pie.getMaterial();
    Block clickedBlock = pie.getClickedBlock();

    if (g.getState() == GameState.RUNNING) {
      if (pie.getAction() == Action.PHYSICAL && clickedBlock != null
          && (clickedBlock.getType() == Material.WHEAT
          || clickedBlock.getType() == Material.SOIL)) {
        pie.setCancelled(true);
        return;
      }

      if (pie.getAction() != Action.RIGHT_CLICK_BLOCK
          && pie.getAction() != Action.RIGHT_CLICK_AIR) {
        return;
      }

      if (clickedBlock != null && clickedBlock.getType() == Material.LEVER && !g.isSpectator(player)
          && pie.getAction() == Action.RIGHT_CLICK_BLOCK) {
        if (!g.getRegion().isPlacedUnbreakableBlock(clickedBlock)) {
          g.getRegion().addPlacedUnbreakableBlock(clickedBlock, clickedBlock.getState());
        }
        return;
      }

      if (g.isSpectator(player)
          || (g.getCycle() instanceof BungeeGameCycle && g.getCycle().isEndGameRunning()
          && BedwarsRel.getInstance().getBooleanConfig("bungeecord.endgame-in-lobby", true))) {
        if (interactingMaterial == Material.SLIME_BALL) {
          g.playerLeave(player, false);
          return;
        }

        if (interactingMaterial == Material.COMPASS) {
          g.openSpectatorCompass(player);
          pie.setCancelled(true);
          return;
        }
      }

      // Spectators want to block
      if (clickedBlock != null) {
        try {
          GameMode.valueOf("SPECTATOR");
        } catch (Exception ex) {
          BedwarsRel.getInstance().getBugsnag().notify(ex);
          for (Player p : g.getFreePlayers()) {
            if (!g.getRegion().isInRegion(p.getLocation())) {
              continue;
            }

            if (pie.getClickedBlock().getLocation().distance(p.getLocation()) < 2) {
              Location oldLocation = p.getLocation();
              if (oldLocation.getY() >= pie.getClickedBlock().getLocation().getY()) {
                oldLocation.setY(oldLocation.getY() + 2);
              } else {
                oldLocation.setY(oldLocation.getY() - 2);
              }

              p.teleport(oldLocation);
            }
          }
        }
      }

      if (clickedBlock != null && clickedBlock.getType() == Material.ENDER_CHEST
          && !g.isSpectator(player)) {
        pie.setCancelled(true);

        Block chest = pie.getClickedBlock();
        Team chestTeam = g.getTeamOfEnderChest(chest);
        Team playerTeam = g.getPlayerTeam(player);

        if (chestTeam == null) {
          return;
        }

        if (chestTeam.equals(playerTeam)) {
          player.openInventory(chestTeam.getInventory());
        } else {
          player.sendMessage(
              ChatWriter
                  .pluginMessage(ChatColor.RED + BedwarsRel._l(player, "ingame.noturteamchest")));
        }

        return;
      }

      return;
    } else if (g.getState() == GameState.WAITING) {
      if (interactingMaterial == null) {
        pie.setCancelled(true);
        return;
      }

      if (pie.getAction() == Action.PHYSICAL) {
        if (clickedBlock != null && (clickedBlock.getType() == Material.WHEAT
            || clickedBlock.getType() == Material.SOIL)) {
          pie.setCancelled(true);
          return;
        }
      }

      if (pie.getAction() != Action.RIGHT_CLICK_BLOCK
          && pie.getAction() != Action.RIGHT_CLICK_AIR) {
        return;
      }

      switch (interactingMaterial) {
        case BED:
          pie.setCancelled(true);
          if (!g.isAutobalanceEnabled()) {
            g.getPlayerStorage(player).openTeamSelection(g);
          }

          break;
        case DIAMOND:
          pie.setCancelled(true);
          if (player.isOp() || player.hasPermission("bw.setup")) {
            g.start(player);
          } else if (player.hasPermission("bw.vip.forcestart")) {
            if (g.isStartable()) {
              g.start(player);
            } else {
              if (!g.hasEnoughPlayers()) {
                player.sendMessage(ChatWriter.pluginMessage(
                    ChatColor.RED + BedwarsRel._l(player, "lobby.cancelstart.not_enough_players")));
              } else if (!g.hasEnoughTeams()) {
                player.sendMessage(ChatWriter
                    .pluginMessage(
                        ChatColor.RED + BedwarsRel
                            ._l(player, "lobby.cancelstart.not_enough_teams")));
              }
            }
          }
          break;
        case EMERALD:
          pie.setCancelled(true);
          if ((player.isOp() || player.hasPermission("bw.setup")
              || player.hasPermission("bw.vip.reducecountdown"))
              && g.getGameLobbyCountdown().getCounter() > g.getGameLobbyCountdown()
              .getLobbytimeWhenFull()) {
            g.getGameLobbyCountdown().setCounter(g.getGameLobbyCountdown().getLobbytimeWhenFull());
          }
          break;
        case SLIME_BALL:
          pie.setCancelled(true);
          g.playerLeave(player, false);
          break;
        case LEATHER_CHESTPLATE:
          pie.setCancelled(true);
          player.updateInventory();
          break;
        default:
          break;
      }
    }
  }

  @EventHandler(priority = EventPriority.HIGHEST)
  public void onPlayerRespawn(PlayerRespawnEvent pre) {
    Player p = pre.getPlayer();
    Game game = BedwarsRel.getInstance().getGameManager().getGameOfPlayer(p);

    if (game == null) {
      return;
    }

    if (game.getState() == GameState.RUNNING) {
      game.getCycle().onPlayerRespawn(pre, p);
      return;
    }

    if (game.getState() == GameState.WAITING) {
      pre.setRespawnLocation(game.getLobby());
    }
  }

  @EventHandler(priority = EventPriority.HIGHEST)
  public void onQuit(PlayerQuitEvent pqe) {
    Player player = pqe.getPlayer();

    if (BedwarsRel.getInstance().isBungee()) {
      pqe.setQuitMessage(null);
    }

    // Remove holographs
    if (BedwarsRel.getInstance().isHologramsEnabled()
        && BedwarsRel.getInstance().getHolographicInteractor() != null && BedwarsRel.getInstance()
        .getHolographicInteractor().getType().equalsIgnoreCase("HolographicDisplays")) {
      BedwarsRel.getInstance().getHolographicInteractor().unloadAllHolograms(player);
    }

    if (BedwarsRel.getInstance().statisticsEnabled()) {
      BedwarsRel.getInstance().getPlayerStatisticManager().unloadStatistic(player);
    }

    Game g = BedwarsRel.getInstance().getGameManager().getGameOfPlayer(player);

    if (g == null) {
      return;
    }

    g.playerLeave(player, false);
  }

  @EventHandler
  public void onSleep(PlayerBedEnterEvent bee) {

    Player p = bee.getPlayer();

    Game g = BedwarsRel.getInstance().getGameManager().getGameOfPlayer(p);
    if (g == null) {
      return;
    }

    if (g.getState() == GameState.STOPPED) {
      return;
    }

    bee.setCancelled(true);
  }

  @EventHandler
  public void onSwitchWorld(PlayerChangedWorldEvent change) {
    Game game = BedwarsRel.getInstance().getGameManager().getGameOfPlayer(change.getPlayer());
    if (game != null) {
      if (game.getState() == GameState.RUNNING) {
        if (!game.getCycle().isEndGameRunning()) {
          if (!game.getPlayerSettings(change.getPlayer()).isTeleporting()) {
            game.playerLeave(change.getPlayer(), false);
          } else {
            game.getPlayerSettings(change.getPlayer()).setTeleporting(false);
          }
        }
      } else if (game.getState() == GameState.WAITING) {
        if (!game.getPlayerSettings(change.getPlayer()).isTeleporting()) {
          game.playerLeave(change.getPlayer(), false);
        } else {
          game.getPlayerSettings(change.getPlayer()).setTeleporting(false);
        }
      }
    }

    if (!BedwarsRel.getInstance().isHologramsEnabled()
        || BedwarsRel.getInstance().getHolographicInteractor() == null) {
      return;
    }

    BedwarsRel.getInstance().getHolographicInteractor().updateHolograms(change.getPlayer());
  }

  @EventHandler
  public void openInventory(InventoryOpenEvent ioe) {
    if (!(ioe.getPlayer() instanceof Player)) {
      return;
    }

    Player player = (Player) ioe.getPlayer();
    Game game = BedwarsRel.getInstance().getGameManager().getGameOfPlayer(player);

    if (game == null) {
      return;
    }

    if (game.getState() != GameState.RUNNING) {
      return;
    }

    if (ioe.getInventory().getType() == InventoryType.ENCHANTING
        || ioe.getInventory().getType() == InventoryType.BREWING
        || (ioe.getInventory().getType() == InventoryType.CRAFTING
        && !BedwarsRel.getInstance().getBooleanConfig("allow-crafting", false))) {
      ioe.setCancelled(true);
      return;
    } else if (ioe.getInventory().getType() == InventoryType.CRAFTING
        && BedwarsRel.getInstance().getBooleanConfig("allow-crafting", false)) {
      return;
    }

    if (game.isSpectator(player)) {
      if (ioe.getInventory().getName().equals(BedwarsRel._l(player, "ingame.spectator"))) {
        return;
      }

      ioe.setCancelled(true);
    }

    if (ioe.getInventory().getHolder() == null) {
      return;
    }

    if (game.getRegion().getInventories().contains(ioe.getInventory())) {
      return;
    }

    game.getRegion().addInventory(ioe.getInventory());
  }

}
