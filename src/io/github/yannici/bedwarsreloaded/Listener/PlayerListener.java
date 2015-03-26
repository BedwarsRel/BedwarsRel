package io.github.yannici.bedwarsreloaded.Listener;

import java.lang.reflect.Method;

import io.github.yannici.bedwarsreloaded.ChatWriter;
import io.github.yannici.bedwarsreloaded.Main;
import io.github.yannici.bedwarsreloaded.Game.Game;
import io.github.yannici.bedwarsreloaded.Game.GameState;
import io.github.yannici.bedwarsreloaded.Game.Team;
import io.github.yannici.bedwarsreloaded.Villager.MerchantCategory;

import org.bukkit.ChatColor;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerBedEnterEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerToggleFlightEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class PlayerListener extends BaseListener {

	public PlayerListener() {
		super();
	}
	
	/*
	 * GLOBAL
	 */

	@EventHandler
	public void onJoin(PlayerJoinEvent je) {
		FileConfiguration cfg = Main.getInstance().getConfig();
		Player player = je.getPlayer();
		
		if(cfg.contains("bungeecord")) {
			if(cfg.getBoolean("bungeecord")) {
				Game game = Main.getInstance().getGameManager().getGames().get(0);
				game.playerJoins(player);
			}
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
		
		MerchantCategory.openCategorySelection(player, game);
	}
	
	@EventHandler
	public void onInventoryClick(InventoryClickEvent ice) {
	    Player player = (Player)ice.getWhoClicked();
	    Game game = Game.getGameOfPlayer(player);
	    
	    if(game.getState() == GameState.WAITING) {
	        this.onLobbyInventoryClick(ice, player, game);
	    }
	    
	    if(game.getState() == GameState.RUNNING) {
	    	this.onIngameInventoryClick(ice, player, game);
	    }
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void onIngameInventoryClick(InventoryClickEvent ice, Player player, Game game) {
		if(!ice.getInventory().getName().equals("Itemshop")) {
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
	        
	        Class clazz = Class.forName("io.github.yannici.bedwarsreloaded.Villager.Version." + Main.getInstance().getCurrentVersion() + ".VillagerItemShop");
	        Object villagerItemShop = clazz.getDeclaredConstructor(Game.class, Player.class, MerchantCategory.class).newInstance(game, player, cat);
	        
	        Method openTrade = clazz.getDeclaredMethod("openTrading", new Class[]{});
	        openTrade.invoke(villagerItemShop, new Object[]{});
	    } catch(Exception ex) {
	        ex.printStackTrace();
	    }
	}
	
	/*
	 * LOBBY & GAME
	 */
	
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
	
	@SuppressWarnings("incomplete-switch")
	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent pie) {
		Player player = pie.getPlayer();
		Game g = Game.getGameOfPlayer(player);
		
		if(g == null) {
			return;
		}
		
		if(g.getState() != GameState.WAITING) {
			return;
		}
		
		if(pie.getAction() != Action.LEFT_CLICK_BLOCK 
				&& pie.getAction() != Action.LEFT_CLICK_AIR) {
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
		}
			
	}
	
	@SuppressWarnings("deprecation")
	private void onLobbyInventoryClick(InventoryClickEvent ice, Player player, Game game) {
		Inventory inv = ice.getInventory();
		ItemStack clickedStack = ice.getCurrentItem();
		
		if(!inv.getTitle().equals("Choose a team")) {
			return;
		}
		
		if(clickedStack.getType() != Material.WOOL) {
			return;
		}
		
		ice.setCancelled(true);
		Team team = game.getTeamByColor(DyeColor.getByData(clickedStack.getData().getData()).toString());
		if(team == null) {
			return;
		}
		
		game.nonFreePlayer(player);
		team.addPlayer(player);
		
		player.sendMessage(ChatWriter.pluginMessage(ChatColor.GREEN + "You successfully joined the team: " + team.getChatColor() + team.getName()));
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
		
		if(g.getState() != GameState.WAITING) {
			return;
		}
		
		if (ede.getCause() == EntityDamageEvent.DamageCause.VOID) {
		    p.teleport(g.getLobby());
		}
		
		ede.setCancelled(true);
	}

}
