package io.github.yannici.bedwarsreloaded.Villager;

import io.github.yannici.bedwarsreloaded.Game.Game;

import java.util.ArrayList;
import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class MerchantCategory {
    
    private String name = null;
    private Material item = null;
    private ArrayList<VillagerTrade> offers = null;

    public MerchantCategory(String name, Material item) {
        this.name = name;
        this.item = item;
        this.offers = new ArrayList<VillagerTrade>();
    }
    
    public MerchantCategory(String name, Material item, ArrayList<VillagerTrade> offers) {
        this.name = name;
        this.item = item;
        this.offers = offers;
    }
    
    public static HashMap<Material, MerchantCategory> loadCategories(FileConfiguration cfg) {
        if(cfg.getConfigurationSection("shop") == null) {
            return new HashMap<Material, MerchantCategory>();
        }
        
        HashMap<Material, MerchantCategory> mc = new HashMap<Material, MerchantCategory>();
        
        ConfigurationSection section = cfg.getConfigurationSection("shop");
        for(String cat : section.getKeys(false)) {
            String catName = section.getString(cat + ".name");
            Material catItem = Material.getMaterial(section.getString(cat + ".item"));
            ArrayList<VillagerTrade> offers = new ArrayList<VillagerTrade>();
            
            for(Object offer : section.getList(cat + ".offers")) {
                String trade = (String)offer;
                String[] tradeArr = trade.split(";");
                VillagerTrade tradeObj = null;
                
                if(tradeArr.length < 4 || tradeArr.length > 6) {
                    continue;
                }
                
                if(tradeArr.length == 4) {
                    tradeObj = new VillagerTrade(new ItemStack(Material.getMaterial(tradeArr[0]), Integer.parseInt(tradeArr[1])), new ItemStack(Material.getMaterial(tradeArr[2]), Integer.parseInt(tradeArr[3])));
                } else if(tradeArr.length == 3) {
                    tradeObj = new VillagerTrade(new ItemStack(Material.getMaterial(tradeArr[0]), Integer.parseInt(tradeArr[1])), new ItemStack(Material.getMaterial(tradeArr[2]), Integer.parseInt(tradeArr[3])), new ItemStack(Material.getMaterial(tradeArr[4]), Integer.parseInt(tradeArr[5])));
                }
                
                offers.add(tradeObj);
            }
            
            mc.put(catItem, new MerchantCategory(catName, catItem, offers));
        }
        
        return mc;
    }
    
    public static void openCategorySelection(Player p, Game g) {
        HashMap<Material, MerchantCategory> cats = g.getItemShopCategories();
        
        Inventory inv = Bukkit.createInventory(p, (cats.size()-cats.size()%9)+9, "Itemshop");
        for(MerchantCategory cat : cats.values()) {
            ItemStack is = new ItemStack(cat.getMaterial(), 1);
            ItemMeta im = is.getItemMeta();
            
            im.setDisplayName(cat.getName());
            is.setItemMeta(im);
            
            inv.addItem(is);
        }
        p.openInventory(inv);
    }
    
    public String getName() {
        return this.name;
    }
    
    public Material getMaterial() {
        return this.item;
    }
    
    public ArrayList<VillagerTrade> getOffers() {
        return this.offers;
    }
    
}
