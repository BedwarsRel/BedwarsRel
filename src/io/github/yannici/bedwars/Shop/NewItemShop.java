package io.github.yannici.bedwars.Shop;

import io.github.yannici.bedwars.Main;
import io.github.yannici.bedwars.Game.Game;
import io.github.yannici.bedwars.Villager.MerchantCategory;
import io.github.yannici.bedwars.Villager.VillagerTrade;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
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
		
		inventory.setItem(8+5, new ItemStack(Material.SLIME_BALL, 1));
		player.openInventory(inventory);
	}
	
	private void addCategoriesToInventory(Inventory inventory) {
		for(MerchantCategory category : this.categories) {
			ItemStack is = new ItemStack(category.getMaterial(), 1);
            ItemMeta im = is.getItemMeta();
            
            im.setDisplayName(category.getName());
            im.setLore(category.getLores());
            is.setItemMeta(im);
            
            inventory.addItem(is);
		}
	}
	
	public void handleInventoryClick(InventoryClickEvent ice, Game game, Player player) {
		if(!this.hasOpenCategory()) {
			this.handleCategoryInventoryClick(ice, game, player);
		} else {
			this.handleBuyInventoryClick(ice, game, player);
		}
	}
	
	private void handleCategoryInventoryClick(InventoryClickEvent ice, Game game, Player player) {
		MerchantCategory clickedCategory = this.getCategoryByMaterial(ice.getCurrentItem().getType());
		if(clickedCategory == null) {
			return;
		}
		
		this.openBuyInventory(clickedCategory, player);
	}
	
	private void openBuyInventory(MerchantCategory category, Player player) {
		List<VillagerTrade> offers = category.getOffers();
		int sizeCategories = (this.categories.size()-this.categories.size()%9)+9;
		int sizeItems =  (offers.size()-offers.size()%9)+9;
		int totalSize = sizeCategories + sizeItems;
				
		Inventory buyInventory = Bukkit.createInventory(player, totalSize);
		this.addCategoriesToInventory(buyInventory);
		
		// TODO: Add reward to inventory (lore: item1 and item2)
	}
	
	private void handleBuyInventoryClick(InventoryClickEvent ice, Game game, Player player) {
		
	}
	
	private MerchantCategory getCategoryByMaterial(Material material) {
		for(MerchantCategory category : this.categories) {
			if(category.getMaterial() == material) {
				return category;
			}
		}
		
		return null;
	}
}
