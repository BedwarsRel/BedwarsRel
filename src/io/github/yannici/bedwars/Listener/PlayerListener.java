package io.github.yannici.bedwars.Listener;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;

import io.github.yannici.bedwars.ChatWriter;
import io.github.yannici.bedwars.Main;
import io.github.yannici.bedwars.Events.BedwarsOpenShopEvent;
import io.github.yannici.bedwars.Game.Game;
import io.github.yannici.bedwars.Game.GameState;
import io.github.yannici.bedwars.Game.Team;
import io.github.yannici.bedwars.Shop.NewItemShop;
import io.github.yannici.bedwars.Villager.MerchantCategory;

import org.bukkit.ChatColor;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerBedEnterEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerToggleFlightEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import com.google.common.collect.ImmutableMap;

public class PlayerListener extends BaseListener {

	public PlayerListener() {
		super();
	}
	
	/*
	 * GLOBAL
	 */

	@EventHandler
	public void onJoin(PlayerJoinEvent je) {
		if(Main.getInstance().isBungee()) {
			ArrayList<Game> games = Main.getInstance().getGameManager().getGames();
			if(games.size() == 0) {
				return;
			}
			
			Player player = je.getPlayer();
			Game firstGame = games.get(0);
			
			firstGame.playerJoins(player);
		}
	}
	
	/*
	 * GAME
	 */
	
    private void inGameInteractEntity(PlayerInteractEntityEvent iee, Game game, Player player) {
		if (!iee.getRightClicked().getType().equals(EntityType.VILLAGER)) {
	      return;
	    }
		
		iee.setCancelled(true);
		
		BedwarsOpenShopEvent openShopEvent = new BedwarsOpenShopEvent(game, player, game.getItemShopCategories(), iee.getRightClicked());
		Main.getInstance().getServer().getPluginManager().callEvent(openShopEvent);
		
		if(openShopEvent.isCancelled()) {
			return;
		}
		
		MerchantCategory.openCategorySelection(player, game);
		/*NewItemShop itemShop = game.getNewItemShop(player);
		if(itemShop == null) {
		    itemShop = game.openNewItemShop(player);
		}
		
		itemShop.setCurrentCategory(null);
		itemShop.openCategoryInventory(player);*/
	}
    
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerRespawn(PlayerRespawnEvent pre) {
    	Player p = pre.getPlayer();
    	Game game = Game.getGameOfPlayer(p);
    	
    	if(game == null) {
    		return;
    	}
    	
    	if(game.getState() == GameState.RUNNING) {
    		game.getCycle().onPlayerRespawn(pre, p);
    		return;
    	}
    	
    	if(game.getState() == GameState.WAITING) {
    		pre.setRespawnLocation(game.getLobby());
    	}
    }
    
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerDie(PlayerDeathEvent pde) {
    	Player player = pde.getEntity();
    	Game game = Game.getGameOfPlayer(player);
    	
    	if(game == null) {
    		return;
    	}
    	
    	if(game.getState() == GameState.RUNNING) {
    		pde.setDroppedExp(0);
    		pde.setDeathMessage(null);
    		pde.getDrops().clear();
    		
    		try { 
    		Class<?> clazz = Class.forName("io.github.yannici.bedwars.Com." + Main.getInstance().getCurrentVersion() + ".PerformRespawnRunnable");
    		BukkitRunnable respawnRunnable = (BukkitRunnable)clazz.getDeclaredConstructor(Player.class).newInstance(player);
    		respawnRunnable.runTaskLater(Main.getInstance(), 20L);
            } catch (Exception e) {
                e.printStackTrace();
            }
    		
    		pde.setKeepInventory(false);
    		game.getCycle().onPlayerDies(player, player.getKiller());
    	}
    }
	
	@EventHandler
	public void onInventoryClick(InventoryClickEvent ice) {
	    Player player = (Player)ice.getWhoClicked();
	    Game game = Game.getGameOfPlayer(player);
	    
	    if(game == null) {
	    	return;
	    }
	    
	    if(game.getState() == GameState.WAITING) {
	        this.onLobbyInventoryClick(ice, player, game);
	    }
	    
	    if(game.getState() == GameState.RUNNING) {
	    	this.onIngameInventoryClick(ice, player, game);
	    }
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void onIngameInventoryClick(InventoryClickEvent ice, Player player, Game game) {
		if(!ice.getInventory().getName().equals(Main._l("ingame.shop.name"))) {
	        return;
	    }
	    
	    ice.setCancelled(true);
	    ItemStack clickedStack = ice.getCurrentItem();
	    
	    if(clickedStack == null) {
	        return;
	    }
	    
	    try {
	        MerchantCategory cat = game.getItemShopCategories().get(clickedStack.getType());
	        if(cat == null) {
	            return;
	        }
	        
	        Class clazz = Class.forName("io.github.yannici.bedwars.Com." + Main.getInstance().getCurrentVersion() + ".VillagerItemShop");
	        Object villagerItemShop = clazz.getDeclaredConstructor(Game.class, Player.class, MerchantCategory.class).newInstance(game, player, cat);
	        
	        Method openTrade = clazz.getDeclaredMethod("openTrading", new Class[]{});
	        openTrade.invoke(villagerItemShop, new Object[]{});
	    } catch(Exception ex) {
	        ex.printStackTrace();
	    }
	    
	    //game.getNewItemShop(player).handleInventoryClick(ice, game, player);
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onChat(AsyncPlayerChatEvent ce) {
	    Player player = ce.getPlayer();
	    Game game = Game.getGameOfPlayer(player);
	    
	    if(game == null) {
	        return;
	    }
	    
	    Team team = Game.getPlayerTeam(player, game);
	    
	    if(game.getState() != GameState.RUNNING) {
	        return;
	    }
	    
	    String message = ce.getMessage();
	    if(message.trim().startsWith("@")) {
	        message = message.trim();
	        ce.setMessage(message.substring(1, message.length()-1));
	        ce.setFormat("[" + Main._l("ingame.all") + "] <" + team.getDisplayName() + ChatColor.RESET + ">" + "%1$s" + ChatColor.RESET + ": %2$s");
	        Iterator<Player> recipiens = ce.getRecipients().iterator();
	        while(recipiens.hasNext()) {
	            if(!game.isInGame(recipiens.next())) {
	                recipiens.remove();
	            }
	        }
	    } else {
	        message = message.trim();
	        ce.setMessage(message);
	        ce.setFormat("<" + team.getDisplayName() + ChatColor.RESET + ">" + "%1$s" + ChatColor.RESET + ": %2$s");
	        Iterator<Player> recipiens = ce.getRecipients().iterator();
            while(recipiens.hasNext()) {
                Player recipient = recipiens.next();
                if(!game.isInGame(recipient) || !team.isInTeam(recipient)) {
                    recipiens.remove();
                }
            }
	    }
	}
	
	/*
	 * LOBBY & GAME
	 */
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onCommand(PlayerCommandPreprocessEvent pcpe) {
		Player player = pcpe.getPlayer();
		Game game = Game.getGameOfPlayer(player);
		
		if(game == null) {
			return;
		}
		
		if(game.getState() == GameState.STOPPED) {
			return;
		}
		
		if(!pcpe.getMessage().startsWith("/bw") && !player.hasPermission("bw.cmd")) {
			return;
		}
	}
	
	@EventHandler
	public void onSleep(PlayerBedEnterEvent bee) {
	    
		Player p = bee.getPlayer();
		
		Game g = Game.getGameOfPlayer(p);
		if(g == null) {
			return;
		}
		
		if(g.getState() == GameState.STOPPED) {
			return;
		}
		
		bee.setCancelled(true);
	}
	
	@EventHandler
	public void onInteractEntity(PlayerInteractEntityEvent iee) {
		Player p = iee.getPlayer();
		Game g = Game.getGameOfPlayer(p);
		if(g == null) {
			return;
		}
		
		if(g.getState() == GameState.WAITING) {
			iee.setCancelled(true);
		}
		
		if(g.getState() == GameState.RUNNING) {
			this.inGameInteractEntity(iee, g, p);
		}
 	}
	
	
	@EventHandler
	public void onFly(PlayerToggleFlightEvent tfe) {
		Player p = tfe.getPlayer();
		
		Game g = Game.getGameOfPlayer(p);
		if(g == null) {
			return;
		}
		
		if(g.getState() == GameState.STOPPED) {
			return;
		}
		
		tfe.setCancelled(true);
	}

	/*
	 * LOBBY
	 */
	
	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent pie) {
		Player player = pie.getPlayer();
		Game g = Game.getGameOfPlayer(player);
		
		if(g == null) {
		    if(pie.getAction() != Action.RIGHT_CLICK_BLOCK 
	                && pie.getAction() != Action.RIGHT_CLICK_AIR) {
	            return;
	        }
		    
		    Block clicked = pie.getClickedBlock();
		    Material type = clicked.getType();
		    
		    if(type != Material.SIGN && type != Material.SIGN_POST && type != Material.WALL_SIGN) {
		        return;
		    }
		    
		    Game game = Main.getInstance().getGameManager().getGameBySignLocation(clicked.getLocation());
		    if(game == null) {
		        return;
		    }
		    
		    if(game.playerJoins(player)) {
	            player.sendMessage(ChatWriter.pluginMessage(ChatColor.GREEN + Main._l("success.joined")));
	        }
			return;
		}
		
		if(g.getState() != GameState.WAITING) {
			return;
		}
		
		if(pie.getAction() != Action.RIGHT_CLICK_BLOCK 
				&& pie.getAction() != Action.RIGHT_CLICK_AIR) {
			return;
		}
		
		Material interactingMaterial = pie.getMaterial();
		switch(interactingMaterial) {
			case BED:
				pie.setCancelled(true);
				g.getPlayerStorage(player).openTeamSelection(g);
				break;
			case DIAMOND:
				pie.setCancelled(true);
				if(player.isOp() || player.hasPermission("bw.setup")) {
					g.start(player);
				}
				break;
			case SLIME_BALL:
				pie.setCancelled(true);
				g.playerLeave(player);
				break;
			default:
				break;
		}
			
	}
	
	@SuppressWarnings("deprecation")
	private void onLobbyInventoryClick(InventoryClickEvent ice, Player player, Game game) {
		Inventory inv = ice.getInventory();
		ItemStack clickedStack = ice.getCurrentItem();
		
		if(!inv.getTitle().equals(Main._l("lobby.chooseteam"))) {
			return;
		}
		
		if(clickedStack.getType() != Material.WOOL) {
			return;
		}
		
		ice.setCancelled(true);
		Team team = game.getTeamByDyeColor(DyeColor.getByData(clickedStack.getData().getData()));
		if(team == null) {
			return;
		}
		
		game.nonFreePlayer(player);
		team.addPlayer(player);
		
		for(Player p : game.getPlayers()) {
		    p.setScoreboard(game.getScoreboard());
		}
		
		player.sendMessage(ChatWriter.pluginMessage(ChatColor.GREEN + Main._l("lobby.teamjoined", ImmutableMap.of("team", team.getDisplayName() + ChatColor.GREEN))));
		player.closeInventory();
	}
	
	@EventHandler
	public void onDrop(PlayerDropItemEvent die) {
		Player p = die.getPlayer();
		Game g = Game.getGameOfPlayer(p);
		if(g == null) {
			return;
		}
		
		if(g.getState() != GameState.WAITING) {
			return;
		}
		
		die.setCancelled(true);
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onQuit(PlayerQuitEvent pqe) {
	    Player player = pqe.getPlayer();
        Game g = Game.getGameOfPlayer(player);
        
        if(g == null) {
            return;
        }
        
        g.playerLeave(player);
	}
	
	@EventHandler
	public void onDamage(EntityDamageEvent ede) {
		if(!(ede.getEntity() instanceof Player)) {
			return;
		}
		
		Player p = (Player)ede.getEntity();
		Game g = Game.getGameOfPlayer(p);
		if(g == null) {
			return;
		}
		
		if(g.getState() == GameState.STOPPED) {
			return;
		}
		
		if(g.getState() == GameState.RUNNING) {
		    if(!g.getCycle().isEndGameRunning()) {
		        return;
		    }
		} else if(g.getState() == GameState.WAITING) {
		    if (ede.getCause() == EntityDamageEvent.DamageCause.VOID) {
	            p.teleport(g.getLobby());
	        }
		}
		
		
		ede.setCancelled(true);
	}

}
