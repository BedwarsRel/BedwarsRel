package io.github.yannici.bedwars.Listener;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.DyeColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
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
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.spigotmc.event.player.PlayerSpawnLocationEvent;

import io.github.yannici.bedwars.ChatWriter;
import io.github.yannici.bedwars.Main;
import io.github.yannici.bedwars.Events.BedwarsOpenShopEvent;
import io.github.yannici.bedwars.Game.BungeeGameCycle;
import io.github.yannici.bedwars.Game.Game;
import io.github.yannici.bedwars.Game.GameLobbyCountdownRule;
import io.github.yannici.bedwars.Game.GameState;
import io.github.yannici.bedwars.Game.Team;
import io.github.yannici.bedwars.Shop.NewItemShop;
import io.github.yannici.bedwars.Villager.MerchantCategory;

public class PlayerListener extends BaseListener {

	public PlayerListener() {
		super();
	}

	/*
	 * GLOBAL
	 */

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onJoin(PlayerJoinEvent je) {

		if (Main.getInstance().isHologramsEnabled() && Main.getInstance().getHolographicInteractor() != null) {
			Main.getInstance().getHolographicInteractor().updateHolograms(je.getPlayer(), 60L);
		}

		if (Main.getInstance().isBungee()) {
			je.setJoinMessage("");
			ArrayList<Game> games = Main.getInstance().getGameManager().getGames();
			if (games.size() == 0) {
				return;
			}

			final Player player = je.getPlayer();
			final Game firstGame = games.get(0);

			if (firstGame.getState() == GameState.STOPPED && player.hasPermission("bw.setup")) {
				return;
			}

			if (!firstGame.playerJoins(player)) {
				new BukkitRunnable() {

					@Override
					public void run() {
						if (firstGame.getCycle() instanceof BungeeGameCycle) {
							((BungeeGameCycle) firstGame.getCycle())
									.bungeeSendToServer(Main.getInstance().getBungeeHub(), player, true);
						}
					}

				}.runTaskLater(Main.getInstance(), 5L);
			}

		}
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void onPlayerSpawnLocation(PlayerSpawnLocationEvent event) {
		if (Main.getInstance().isBungee()) {
			Player player = event.getPlayer();

			ArrayList<Game> games = Main.getInstance().getGameManager().getGames();
			if (games.size() == 0) {
				return;
			}

			Game firstGame = games.get(0);

			event.setSpawnLocation(firstGame.getPlayerTeleportLocation(player));
		}
	}

	@EventHandler
	public void onSwitchWorld(PlayerChangedWorldEvent change) {
		Game game = Main.getInstance().getGameManager().getGameOfPlayer(change.getPlayer());
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

		if (!Main.getInstance().isHologramsEnabled() || Main.getInstance().getHolographicInteractor() == null) {
			return;
		}

		Main.getInstance().getHolographicInteractor().updateHolograms(change.getPlayer());
	}

	/*
	 * GAME
	 */

	private void inGameInteractEntity(PlayerInteractEntityEvent iee, Game game, Player player) {

		if (iee.getPlayer().getItemInHand().getType().equals(Material.MONSTER_EGG)
				|| iee.getPlayer().getItemInHand().getType().equals(Material.MONSTER_EGGS)
				|| iee.getPlayer().getItemInHand().getType().equals(Material.DRAGON_EGG)) {
			iee.setCancelled(true);
			return;
		}

		if (iee.getRightClicked() != null) {
			if (!iee.getRightClicked().getType().equals(EntityType.VILLAGER)) {
				List<EntityType> preventClickTypes = Arrays.asList(EntityType.ITEM_FRAME);

				// armor stand in 1.8
				try {
					preventClickTypes.add(EntityType.valueOf("ARMOR_STAND"));
				} catch (Exception ex) {
					// nothing will happen, just not supported
				}

				if (preventClickTypes.contains(iee.getRightClicked().getType())) {
					iee.setCancelled(true);
				}

				return;
			}
		}

		iee.setCancelled(true);

		if (game.isSpectator(player)) {
			return;
		}

		BedwarsOpenShopEvent openShopEvent = new BedwarsOpenShopEvent(game, player, game.getItemShopCategories(),
				iee.getRightClicked());
		Main.getInstance().getServer().getPluginManager().callEvent(openShopEvent);

		if (openShopEvent.isCancelled()) {
			return;
		}

		if (game.isUsingOldShop(player)) {
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

	@EventHandler
	public void openInventory(InventoryOpenEvent ioe) {
		if (!(ioe.getPlayer() instanceof Player)) {
			return;
		}

		Player player = (Player) ioe.getPlayer();
		Game game = Main.getInstance().getGameManager().getGameOfPlayer(player);

		if (game == null) {
			return;
		}

		if (game.getState() != GameState.RUNNING) {
			return;
		}

		if (ioe.getInventory().getType() == InventoryType.ENCHANTING
				|| ioe.getInventory().getType() == InventoryType.BREWING
				|| (ioe.getInventory().getType() == InventoryType.CRAFTING
						&& !Main.getInstance().getBooleanConfig("allow-crafting", false))) {
			ioe.setCancelled(true);
			return;
		} else if (ioe.getInventory().getType() == InventoryType.CRAFTING
				&& Main.getInstance().getBooleanConfig("allow-crafting", false)) {
			return;
		}

		if (game.isSpectator(player)) {
			if (ioe.getInventory().getName().equals(Main._l("ingame.spectator"))) {
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

		InventoryHolder holder = ioe.getInventory().getHolder();
		for (Class<?> interfaze : holder.getClass().getInterfaces()) {

			if (interfaze.equals(BlockState.class)) {
				game.getRegion().addInventory(ioe.getInventory());
				return;
			}

			for (Class<?> interfaze2 : interfaze.getInterfaces()) {
				if (interfaze2.equals(BlockState.class)) {
					game.getRegion().addInventory(ioe.getInventory());
					return;
				}
			}
		}
	}

	@EventHandler
	public void onCraft(CraftItemEvent cie) {
		Player player = (Player) cie.getWhoClicked();
		Game game = Main.getInstance().getGameManager().getGameOfPlayer(player);

		if (game == null) {
			return;
		}

		if (game.getState() == GameState.STOPPED) {
			return;
		}

		if (Main.getInstance().getBooleanConfig("allow-crafting", false)) {
			return;
		}

		cie.setCancelled(true);
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerRespawn(PlayerRespawnEvent pre) {
		Player p = pre.getPlayer();
		Game game = Main.getInstance().getGameManager().getGameOfPlayer(p);

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
	public void onPlayerDie(PlayerDeathEvent pde) {
		final Player player = pde.getEntity();
		Game game = Main.getInstance().getGameManager().getGameOfPlayer(player);

		if (game == null) {
			return;
		}

		if (game.getState() == GameState.RUNNING) {
			pde.setDroppedExp(0);
			pde.setDeathMessage(null);

			if (!Main.getInstance().getBooleanConfig("player-drops", false)) {
				pde.getDrops().clear();
			}

			try {
				if (!Main.getInstance().isSpigot()) {
					Class<?> clazz = null;
					try {
						clazz = Class.forName("io.github.yannici.bedwars.Com." + Main.getInstance().getCurrentVersion()
								+ ".PerformRespawnRunnable");
					} catch (ClassNotFoundException ex) {
						clazz = Class.forName("io.github.yannici.bedwars.Com.Fallback.PerformRespawnRunnable");
					}

					BukkitRunnable respawnRunnable = (BukkitRunnable) clazz.getDeclaredConstructor(Player.class)
							.newInstance(player);
					respawnRunnable.runTaskLater(Main.getInstance(), 20L);
				} else {
					new BukkitRunnable() {

						@Override
						public void run() {
							player.spigot().respawn();
						}
					}.runTaskLater(Main.getInstance(), 20L);
				}

			} catch (Exception e) {
				e.printStackTrace();
			}

			try {
				pde.getClass().getMethod("setKeepInventory", new Class<?>[] { boolean.class });
				pde.setKeepInventory(false);
			} catch (Exception ex) {
				player.getInventory().clear();
			}

			Player killer = player.getKiller();
			if (killer == null) {
				killer = game.getPlayerDamager(player);
			}

			game.getCycle().onPlayerDies(player, killer);
		}
	}

	@EventHandler
	public void onInventoryClick(InventoryClickEvent ice) {
		Player player = (Player) ice.getWhoClicked();
		Game game = Main.getInstance().getGameManager().getGameOfPlayer(player);

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

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void onIngameInventoryClick(InventoryClickEvent ice, Player player, Game game) {
		if (!ice.getInventory().getName().equals(Main._l("ingame.shop.name"))) {
			if (game.isSpectator(player)
					|| (game.getCycle() instanceof BungeeGameCycle && game.getCycle().isEndGameRunning()
							&& Main.getInstance().getBooleanConfig("bungeecord.endgame-in-lobby", true))) {

				ItemStack clickedStack = ice.getCurrentItem();
				if (clickedStack == null) {
					return;
				}

				if (ice.getInventory().getName().equals(Main._l("ingame.spectator"))) {
					ice.setCancelled(true);
					if (!clickedStack.getType().equals(Material.SKULL_ITEM)) {
						return;
					}

					SkullMeta meta = (SkullMeta) clickedStack.getItemMeta();
					Player pl = Main.getInstance().getServer().getPlayer(meta.getOwner());
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

		if (game.isUsingOldShop(player)) {
			try {
				if (clickedStack.getType() == Material.SNOW_BALL) {
					game.notUseOldShop(player);

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

				Class clazz = Class.forName("io.github.yannici.bedwars.Com." + Main.getInstance().getCurrentVersion()
						+ ".VillagerItemShop");
				Object villagerItemShop = clazz.getDeclaredConstructor(Game.class, Player.class, MerchantCategory.class)
						.newInstance(game, player, cat);

				Method openTrade = clazz.getDeclaredMethod("openTrading", new Class[] {});
				openTrade.invoke(villagerItemShop, new Object[] {});
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		} else {
			game.getNewItemShop(player).handleInventoryClick(ice, game, player);
		}
	}

	private String getChatFormat(String format, Team team, boolean isSpectator, boolean all) {
		String form = format;

		if (all) {
			form = form.replace("$all$", Main._l("ingame.all") + ChatColor.RESET);
		}

		form = form.replace("$player$",
				((!isSpectator && team != null) ? team.getChatColor() : "") + "%1$s" + ChatColor.RESET);
		form = form.replace("$msg$", "%2$s");

		if (isSpectator) {
			form = form.replace("$team$", Main._l("ingame.spectator"));
		} else if (team != null) {
			form = form.replace("$team$", team.getDisplayName() + ChatColor.RESET);
		}

		return ChatColor.translateAlternateColorCodes('&', form);
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onChat(AsyncPlayerChatEvent ce) {
		if (ce.isCancelled()) {
			return;
		}

		Player player = ce.getPlayer();
		Game game = Main.getInstance().getGameManager().getGameOfPlayer(player);

		if (game == null) {
			boolean seperateGameChat = Main.getInstance().getBooleanConfig("seperate-game-chat", true);
			if (!seperateGameChat) {
				return;
			}

			Iterator<Player> recipients = ce.getRecipients().iterator();
			while (recipients.hasNext()) {
				Player recipient = recipients.next();
				Game recipientGame = Main.getInstance().getGameManager().getGameOfPlayer(recipient);
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

		if (Main.getInstance().getBooleanConfig("overwrite-names", false)) {
			if (team == null || isSpectator) {
				player.setDisplayName(ChatColor.stripColor(player.getName()));

				player.setPlayerListName(ChatColor.stripColor(player.getName()));
			} else {
				player.setDisplayName(team.getChatColor() + ChatColor.stripColor(player.getName()));
				player.setPlayerListName(team.getChatColor() + ChatColor.stripColor(player.getName()));
			}

		}

		if (Main.getInstance().getBooleanConfig("teamname-on-tab", false)) {
			if (team == null || isSpectator) {
				player.setPlayerListName(ChatColor.stripColor(player.getDisplayName()));
			} else {
				player.setPlayerListName(team.getChatColor() + team.getName() + ChatColor.WHITE + " | "
						+ team.getChatColor() + ChatColor.stripColor(player.getDisplayName()));
			}
		}

		if (game.getState() != GameState.RUNNING && game.getState() == GameState.WAITING) {
			String format = null;
			if (team == null) {
				format = this.getChatFormat(Main.getInstance().getStringConfig("lobby-chatformat", "$player$: $msg$"),
						null, false, true);
			} else {
				format = this.getChatFormat(
						Main.getInstance().getStringConfig("ingame-chatformat", "<$team$>$player$: $msg$"), team, false,
						true);
			}

			ce.setFormat(format);

			if (!Main.getInstance().getBooleanConfig("seperate-game-chat", true)) {
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

		String toAllPrefix = Main.getInstance().getConfig().getString("chat-to-all-prefix", "@");

		if (message.trim().startsWith(toAllPrefix) || isSpectator || (game.getCycle().isEndGameRunning()
				&& Main.getInstance().getBooleanConfig("global-chat-after-end", true))) {
			boolean seperateSpectatorChat = Main.getInstance().getBooleanConfig("seperate-spectator-chat", false);

			message = message.trim();
			String format = null;
			if (!isSpectator && !(game.getCycle().isEndGameRunning()
					&& Main.getInstance().getBooleanConfig("global-chat-after-end", true))) {
				ce.setMessage(message.substring(1, message.length()));
				format = this.getChatFormat(
						Main.getInstance().getStringConfig("ingame-chatformat-all", "[$all$] <$team$>$player$: $msg$"),
						team, false, true);
			} else {
				ce.setMessage(message);
				format = this.getChatFormat(
						Main.getInstance().getStringConfig("ingame-chatformat", "<$team$>$player$: $msg$"), team,
						isSpectator, true);
			}

			ce.setFormat(format);

			if (!Main.getInstance().isBungee() || seperateSpectatorChat) {
				Iterator<Player> recipiens = ce.getRecipients().iterator();
				while (recipiens.hasNext()) {
					Player recipient = recipiens.next();
					if (!game.isInGame(recipient)) {
						recipiens.remove();
						continue;
					}

					if (!seperateSpectatorChat || (game.getCycle().isEndGameRunning()
							&& Main.getInstance().getBooleanConfig("global-chat-after-end", true))) {
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
					Main.getInstance().getStringConfig("ingame-chatformat", "<$team$>$player$: $msg$"), team, false,
					false));

			Iterator<Player> recipiens = ce.getRecipients().iterator();
			while (recipiens.hasNext()) {
				Player recipient = recipiens.next();
				if (!game.isInGame(recipient) || !team.isInTeam(recipient)) {
					recipiens.remove();
				}
			}
		}
	}

	@EventHandler
	public void onPickup(PlayerPickupItemEvent ppie) {
		Player player = ppie.getPlayer();
		Game game = Main.getInstance().getGameManager().getGameOfPlayer(player);

		if (game == null) {
			return;
		}

		if (game.getState() != GameState.WAITING) {
			if (game.isSpectator(player)) {
				ppie.setCancelled(true);
			}

			return;
		}

		ppie.setCancelled(true);
	}

	/*
	 * LOBBY & GAME
	 */

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onCommand(PlayerCommandPreprocessEvent pcpe) {
		Player player = pcpe.getPlayer();
		Game game = Main.getInstance().getGameManager().getGameOfPlayer(player);

		if (game == null) {
			return;
		}

		if (game.getState() == GameState.STOPPED) {
			return;
		}

		String message = pcpe.getMessage();
		if (!message.startsWith("/" + Main.getInstance().getStringConfig("command-prefix", "bw"))) {

			for (String allowed : Main.getInstance().getAllowedCommands()) {
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
	public void onSleep(PlayerBedEnterEvent bee) {

		Player p = bee.getPlayer();

		Game g = Main.getInstance().getGameManager().getGameOfPlayer(p);
		if (g == null) {
			return;
		}

		if (g.getState() == GameState.STOPPED) {
			return;
		}

		bee.setCancelled(true);
	}

	@EventHandler
	public void onInteractEntity(PlayerInteractEntityEvent iee) {
		Player p = iee.getPlayer();
		Game g = Main.getInstance().getGameManager().getGameOfPlayer(p);
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

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onFly(PlayerToggleFlightEvent tfe) {
		Player p = tfe.getPlayer();

		Game g = Main.getInstance().getGameManager().getGameOfPlayer(p);
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

	/*
	 * LOBBY
	 */

	@EventHandler(priority = EventPriority.HIGH)
	public void onHunger(FoodLevelChangeEvent flce) {
		if (!(flce.getEntity() instanceof Player)) {
			return;
		}

		Player player = (Player) flce.getEntity();
		Game game = Main.getInstance().getGameManager().getGameOfPlayer(player);

		if (game == null) {
			return;
		}

		if (game.getState() == GameState.RUNNING) {
			if (game.isSpectator(player)) {
				flce.setCancelled(true);
				return;
			}

			flce.setCancelled(false);
			return;
		}

		flce.setCancelled(true);
	}

	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent pie) {
		Player player = pie.getPlayer();
		Game g = Main.getInstance().getGameManager().getGameOfPlayer(player);

		if (g == null) {
			if (pie.getAction() != Action.RIGHT_CLICK_BLOCK && pie.getAction() != Action.RIGHT_CLICK_AIR) {
				return;
			}

			Block clicked = pie.getClickedBlock();

			if (clicked == null) {
				return;
			}

			if (!(clicked.getState() instanceof Sign)) {
				return;
			}

			Game game = Main.getInstance().getGameManager().getGameBySignLocation(clicked.getLocation());
			if (game == null) {
				return;
			}

			if (game.playerJoins(player)) {
				player.sendMessage(ChatWriter.pluginMessage(ChatColor.GREEN + Main._l("success.joined")));
			}
			return;
		}

		if (g.getState() == GameState.STOPPED) {
			return;
		}

		Material interactingMaterial = pie.getMaterial();
		Block clickedBlock = pie.getClickedBlock();

		if (g.getState() == GameState.RUNNING) {
			if (pie.getAction() == Action.PHYSICAL) {
				if (clickedBlock != null
						&& (clickedBlock.getType() == Material.WHEAT || clickedBlock.getType() == Material.SOIL)) {
					pie.setCancelled(true);
					return;
				}
			}

			if (pie.getAction() != Action.RIGHT_CLICK_BLOCK && pie.getAction() != Action.RIGHT_CLICK_AIR) {
				return;
			}

			if (clickedBlock != null) {
				if (clickedBlock.getType() == Material.LEVER && !g.isSpectator(player)
						&& pie.getAction() == Action.RIGHT_CLICK_BLOCK) {
					if (!g.getRegion().isPlacedUnbreakableBlock(clickedBlock)) {
						g.getRegion().addPlacedUnbreakableBlock(clickedBlock, clickedBlock.getState());
					}
					return;
				}
			}

			if (g.isSpectator(player) || (g.getCycle() instanceof BungeeGameCycle && g.getCycle().isEndGameRunning()
					&& Main.getInstance().getBooleanConfig("bungeecord.endgame-in-lobby", true))) {
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

			if (clickedBlock != null) {
				if (clickedBlock.getType() == Material.ENDER_CHEST && !g.isSpectator(player)) {
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
						player.sendMessage(ChatWriter.pluginMessage(ChatColor.RED + Main._l("ingame.noturteamchest")));
					}

					return;
				}
			}

			return;
		} else if (g.getState() == GameState.WAITING) {
			if (interactingMaterial == null) {
				pie.setCancelled(true);
				return;
			}

			if (pie.getAction() == Action.PHYSICAL) {
				if (clickedBlock != null
						&& (clickedBlock.getType() == Material.WHEAT || clickedBlock.getType() == Material.SOIL)) {
					pie.setCancelled(true);
					return;
				}
			}

			if (pie.getAction() != Action.RIGHT_CLICK_BLOCK && pie.getAction() != Action.RIGHT_CLICK_AIR) {
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
					GameLobbyCountdownRule rule = Main.getInstance().getLobbyCountdownRule();
					if (rule.isRuleMet(g)) {
						g.start(player);
					} else {
						if (rule == GameLobbyCountdownRule.PLAYERS_IN_GAME
								|| rule == GameLobbyCountdownRule.ENOUGH_TEAMS_AND_PLAYERS) {
							player.sendMessage(
									ChatWriter.pluginMessage(ChatColor.RED + Main._l("lobby.notenoughplayers-rule0")));
						} else {
							player.sendMessage(
									ChatWriter.pluginMessage(ChatColor.RED + Main._l("lobby.notenoughplayers-rule1")));
						}
					}
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

	@SuppressWarnings("deprecation")
	private void onLobbyInventoryClick(InventoryClickEvent ice, Player player, Game game) {
		Inventory inv = ice.getInventory();
		ItemStack clickedStack = ice.getCurrentItem();

		if (!inv.getTitle().equals(Main._l("lobby.chooseteam"))) {
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
		Team team = game.getTeamByDyeColor(DyeColor.getByData(clickedStack.getData().getData()));
		if (team == null) {
			return;
		}

		game.playerJoinTeam(player, team);
		player.closeInventory();
	}

	@EventHandler
	public void onDrop(PlayerDropItemEvent die) {
		Player p = die.getPlayer();
		Game g = Main.getInstance().getGameManager().getGameOfPlayer(p);
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
	public void onQuit(PlayerQuitEvent pqe) {
		Player player = pqe.getPlayer();

		// Remove holographs
		if (Main.getInstance().isHologramsEnabled() && Main.getInstance().getHolographicInteractor() != null) {
			Main.getInstance().getHolographicInteractor().unloadAllHolograms(player);
		}

		Game g = Main.getInstance().getGameManager().getGameOfPlayer(player);

		if (g == null) {
			return;
		}

		g.playerLeave(player, false);
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
			Game game = Main.getInstance().getGameManager().getGameOfPlayer(player);

			if (game == null) {
				return;
			}

			if (game.getState() == GameState.WAITING) {
				ede.setCancelled(true);
			}

			return;
		}

		Player p = (Player) ede.getEntity();
		Game g = Main.getInstance().getGameManager().getGameOfPlayer(p);
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

			if (Main.getInstance().getBooleanConfig("die-on-void", false) && ede.getCause() == DamageCause.VOID) {
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
		} else if (g.getState() == GameState.WAITING) {
			if (ede.getCause() == EntityDamageEvent.DamageCause.VOID) {
				p.teleport(g.getLobby());
			}
		}

		ede.setCancelled(true);
	}

}
