package io.github.yannici.bedwars.Shop;

import io.github.yannici.bedwars.ChatWriter;
import io.github.yannici.bedwars.Main;
import io.github.yannici.bedwars.Utils;
import io.github.yannici.bedwars.Game.Game;
import io.github.yannici.bedwars.Villager.MerchantCategory;
import io.github.yannici.bedwars.Villager.VillagerTrade;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;

public class NewItemShop {
	
	private List<MerchantCategory> categories = null;
	private MerchantCategory currentCategory = null;
	
	public NewItemShop(List<MerchantCategory> categories) {
		this.categories = categories;
	}
	
	public List<MerchantCategory> getCategories() {
		return this.categories;
	}
	
	public boolean hasOpenCategory() {
		return (this.currentCategory != null);
	}
	
	public boolean hasOpenCategory(MerchantCategory category) {
		if(this.currentCategory == null) {
			return false;
		}
		
		return (this.currentCategory.equals(category));
	}
	
	public void openCategoryInventory(Player player) {
		int size = (this.categories.size()-this.categories.size()%9)+(9*2);
		Inventory inventory = Bukkit.createInventory(player, size, Main._l("ingame.shop.name"));
		
		this.addCategoriesToInventory(inventory);
		
		ItemStack slime = new ItemStack(Material.SLIME_BALL, 1);
		ItemMeta slimeMeta = slime.getItemMeta();
		
		slimeMeta.setDisplayName(Main._l("ingame.shop.oldshop"));
		slimeMeta.setLore(new ArrayList<String>());
		slime.setItemMeta(slimeMeta);
		
		inventory.setItem(8+5, slime);
		
		player.openInventory(inventory);
	}
	
	private void addCategoriesToInventory(Inventory inventory) {
		for(MerchantCategory category : this.categories) {
			ItemStack is = new ItemStack(category.getMaterial(), 1);
            ItemMeta im = is.getItemMeta();
            
            if(this.currentCategory != null) {
                if(this.currentCategory.equals(category)) {
                    im.addEnchant(Enchantment.DAMAGE_ALL, 1, true);
                }
            }
            
            im.setDisplayName(category.getName());
            im.setLore(category.getLores());
            is.setItemMeta(im);
            
            inventory.addItem(is);
		}
	}
	
	public void handleInventoryClick(InventoryClickEvent ice, Game game, Player player) {
		if(!this.hasOpenCategory()) {
			if(ice.getCurrentItem().getType() == Material.SLIME_BALL) {
				this.changeToOldShop(game, player);
				return;
			}
			
			this.handleCategoryInventoryClick(ice, game, player);
		} else {
			this.handleBuyInventoryClick(ice, game, player);
		}
	}
	
	private void changeToOldShop(Game game, Player player) {
		game.useOldShop(player);
		
		// open old shop
		MerchantCategory.openCategorySelection(player, game);
	}
	
	private void handleCategoryInventoryClick(InventoryClickEvent ice, Game game, Player player) {
		MerchantCategory clickedCategory = this.getCategoryByMaterial(ice.getCurrentItem().getType());
		if(clickedCategory == null) {
			return;
		}
		
		this.openBuyInventory(clickedCategory, player, game);
	}
	
	private void openBuyInventory(MerchantCategory category, Player player, Game game) {
		List<VillagerTrade> offers = category.getOffers();
		int sizeCategories = (this.categories.size()-this.categories.size()%9)+9;
		int sizeItems =  (offers.size()-offers.size()%9)+9;
		int totalSize = sizeCategories + sizeItems;
		
		this.currentCategory = category;
		Inventory buyInventory = Bukkit.createInventory(player, totalSize, Main._l("ingame.shop.name"));
		this.addCategoriesToInventory(buyInventory);
		
		for(int i = 0; i < offers.size(); i++) {
		    VillagerTrade trade = offers.get(i);
		    int slot = sizeCategories + i;
		    ItemStack tradeStack = this.toItemStack(trade, player, game);
		    
		    buyInventory.setItem(slot, tradeStack);
		}
		
		player.openInventory(buyInventory);
	}
	
	private ItemStack toItemStack(VillagerTrade trade, Player player, Game game) {
	    ItemStack tradeStack = trade.getRewardItem().clone();
        Method colorable = Utils.getColorableMethod(tradeStack.getType());
        ItemMeta meta = tradeStack.getItemMeta();
        ItemStack item1 = trade.getItem1();
        ItemStack item2 = trade.getItem2();
        
        if(colorable != null) {
            colorable.setAccessible(true);
            try {
                colorable.invoke(meta, new Object[]{Game.getPlayerTeam(player, game).getColor().getColor()});
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        
        List<String> lores = meta.getLore();
        if(lores == null) {
            lores = new ArrayList<String>();
        }
        
        lores.add(String.valueOf(item1.getAmount()) + " " + item1.getItemMeta().getDisplayName());
        if(item2 != null) {
            lores.add(String.valueOf(item2.getAmount()) + " " + item2.getItemMeta().getDisplayName());
        }
        
        meta.setLore(lores);
        
        tradeStack.setItemMeta(meta);
        return tradeStack;
	}
	
	private void handleBuyInventoryClick(InventoryClickEvent ice, Game game, Player player) {
	    int sizeCategories = (this.categories.size()-this.categories.size()%9)+9;
	    ItemStack item = ice.getCurrentItem();
	    
	    if(this.currentCategory == null) {
	        player.closeInventory();
	        return;
	    }
	    
	    if(ice.getRawSlot() < sizeCategories) {
	        // is category click
	        if(item.getType().equals(this.currentCategory.getMaterial())) {
	            // back to default category view
	            this.currentCategory = null;
	            this.openCategoryInventory(player);
	        } else {
	            // open the clicked buy inventory
	            this.handleCategoryInventoryClick(ice, game, player);
	        }
	    } else {
	        // its a buying item
	        MerchantCategory category = this.currentCategory;
	        VillagerTrade trade = this.getTradingItem(category, ice.getCurrentItem(), game, player);
	        
	        if(trade == null) {
	            player.closeInventory();
	            return;
	        }
	        
	     // enough ressources?
	        if(!this.hasEnoughRessource(player, trade)) {
	            player.sendMessage(ChatWriter.pluginMessage(ChatColor.RED + Main._l("errors.notenoughress")));
                return;
	        }
	        
	        if(ice.isShiftClick()) {
	        	while(this.hasEnoughRessource(player, trade)) {
	        		this.buyItem(item, trade, player);
	        	}
	        } else {
	        	this.buyItem(item, trade, player);
	        }
	    }
	}
	
    @SuppressWarnings("unchecked")
    private void buyItem(ItemStack item, VillagerTrade trade, Player player) {
        PlayerInventory inventory = player.getInventory();
        
	    int item1ToPay = trade.getItem1().getAmount();
	    Iterator<?> stackIterator = inventory.all(trade.getItem1().getType()).entrySet().iterator();
	    
	    // pay
	   while(stackIterator.hasNext()) {
	        Entry<Integer, ? extends ItemStack> entry = (Entry<Integer, ? extends ItemStack>) stackIterator.next();
	        ItemStack stack = (ItemStack)entry.getValue();
	        
	        int endAmount = stack.getAmount()-item1ToPay;
	        if(endAmount < 0) {
	        	endAmount = 0;
	        }
	        
	        item1ToPay = item1ToPay - stack.getAmount();
	        stack.setAmount(endAmount);
	        inventory.setItem(entry.getKey(), stack);
	        
	        if(item1ToPay <= 0) {
	        	break;
	        }
	    }
	    
	    if(trade.getItem2() != null) {
	       int item2ToPay = trade.getItem2().getAmount();
	       stackIterator = inventory.all(trade.getItem2().getType()).entrySet().iterator();
	       
	       // pay item2
	        while(stackIterator.hasNext()) {
		        Entry<Integer, ? extends ItemStack> entry = (Entry<Integer, ? extends ItemStack>) stackIterator.next();
		        ItemStack stack = (ItemStack)entry.getValue();
		        
		        int endAmount = stack.getAmount()-item2ToPay;
		        if(endAmount < 0) {
		        	endAmount = 0;
		        }
		        
		        item2ToPay = item2ToPay - stack.getAmount();
		        stack.setAmount(endAmount);
		        inventory.setItem(entry.getKey(), stack);
		        
		        if(item2ToPay <= 0) {
		        	break;
		        }
		    }
	    }
	    
	    inventory.addItem(item);
	    
	    player.updateInventory();
	}
	
	private boolean hasEnoughRessource(Player player, VillagerTrade trade) {
	    ItemStack item1 = trade.getItem1();
        ItemStack item2 = trade.getItem2();
        PlayerInventory inventory = player.getInventory();
        
	    if(item2 != null) {
            if(!inventory.contains(item1.getType(), item1.getAmount())
                    || !inventory.contains(item2.getType(), item2.getAmount())) {
                return false;
            }
        } else {
            if(!inventory.contains(item1.getType(), item1.getAmount())) {
                return false;
            }
        }
	    
	    return true;
	}
	
	private VillagerTrade getTradingItem(MerchantCategory category, ItemStack stack, Game game, Player player) {
	    for(VillagerTrade trade : category.getOffers()) {
	        ItemStack iStack = this.toItemStack(trade, player, game);
	        if(iStack.equals(stack)) {
	            return trade;
	        }
	    }
	    
	    return null;
	}
	
	private MerchantCategory getCategoryByMaterial(Material material) {
		for(MerchantCategory category : this.categories) {
			if(category.getMaterial() == material) {
				return category;
			}
		}
		
		return null;
	}

    public void setCurrentCategory(MerchantCategory category) {
        this.currentCategory = category;
    }
}
