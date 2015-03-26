package io.github.yannici.bedwarsreloaded.Villager;

import io.github.yannici.bedwarsreloaded.Game.Game;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
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
    
    @SuppressWarnings("unchecked")
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
                LinkedHashMap<String, Object> offerSection = (LinkedHashMap<String, Object>)offer;
                
                if(!offerSection.containsKey("item1") || !offerSection.containsKey("reward")) {
                    continue;
                }
                
                ItemStack item1 = MerchantCategory.createItemStackByConfig(offerSection.get("item1"));
                ItemStack item2 = null;
                if(offerSection.containsKey("item2")) {
                    item2 = MerchantCategory.createItemStackByConfig(offerSection.get("item2"));
                }
                ItemStack reward = MerchantCategory.createItemStackByConfig(offerSection.get("reward"));
                
                if(item1 == null || reward == null) {
                    continue;
                }
                
                VillagerTrade tradeObj = null;
                
                if(item2 != null) {
                    tradeObj = new VillagerTrade(item1, item2, reward);
                } else {
                    tradeObj = new VillagerTrade(item1, reward);
                }
                
                offers.add(tradeObj);
            }
            
            mc.put(catItem, new MerchantCategory(catName, catItem, offers));
        }
        
        return mc;
    }
    
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public static ItemStack createItemStackByConfig(Object section) {
        if(!(section instanceof LinkedHashMap)) {
            return null;
        }
        
        try {
            LinkedHashMap<String, Object> cfgSection = (LinkedHashMap<String, Object>)section;
            Material material = Material.getMaterial(cfgSection.get("item").toString().toUpperCase());
            int amount = Integer.valueOf(cfgSection.get("amount").toString());
            
            ItemStack finalStack = new ItemStack(material, amount);
            
            if(cfgSection.containsKey("enchants")) {
                Object cfgEnchants = cfgSection.get("enchants");
                
                if(cfgEnchants instanceof LinkedHashMap) {
                    LinkedHashMap<String, Object> enchantSection = (LinkedHashMap)cfgEnchants;
                    for(String key : enchantSection.keySet()) {
                        finalStack.addEnchantment(Enchantment.getByName(key.toUpperCase()) , Integer.valueOf(enchantSection.get(key).toString()));
                    }
                }
            }
            
            return finalStack;
            
        } catch(Exception ex) {
            ex.printStackTrace();
        }
        
        return null;
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
