package io.github.yannici.bedwarsreloaded.Villager;

import io.github.yannici.bedwarsreloaded.Main;
import io.github.yannici.bedwarsreloaded.Utils;
import io.github.yannici.bedwarsreloaded.Game.Game;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
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
    private List<String> lores = null;
    private ArrayList<VillagerTrade> offers = null;
    private int order = 0;

    public MerchantCategory(String name, Material item) {
        this(name, item, new ArrayList<VillagerTrade>(), new ArrayList<String>(), 0);
    }
    
    public MerchantCategory(String name, Material item, ArrayList<VillagerTrade> offers, List<String> lores, int order) {
        this.name = name;
        this.item = item;
        this.offers = offers;
        this.lores = lores;
        this.order = order;
    }
    
    public List<String> getLores() {
    	return this.lores;
    }
    
    public int getOrder() {
        return this.order;
    }
    
    @SuppressWarnings({ "unchecked", "deprecation" })
    public static HashMap<Material, MerchantCategory> loadCategories(FileConfiguration cfg) {
        if(cfg.getConfigurationSection("shop") == null) {
            return new HashMap<Material, MerchantCategory>();
        }
        
        HashMap<Material, MerchantCategory> mc = new HashMap<Material, MerchantCategory>();
        
        ConfigurationSection section = cfg.getConfigurationSection("shop");

        for(String cat : section.getKeys(false)) {
            String catName = ChatColor.translateAlternateColorCodes('§', section.getString(cat + ".name"));
            Material catItem = null;
            List<String> lores = new ArrayList<String>();
            String item = section.get(cat + ".item").toString();
            int order = 0;
            
            if(!Utils.isNumber(item)) {
                catItem = Material.getMaterial(section.getString(cat + ".item"));
            } else {
                catItem = Material.getMaterial(section.getInt(cat + ".item"));
            }
            
            if(section.contains(cat + ".lore")) {
            	for(Object lore : section.getList(cat + ".lore")) {
            		lores.add(ChatColor.translateAlternateColorCodes('§', lore.toString()));
            	}
            }
            
            if(section.contains(cat + ".order")) {
                if(section.isInt(cat + ".order")) {
                    order = section.getInt(cat + ".order");
                }
            }
            
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
            
            mc.put(catItem, new MerchantCategory(catName, catItem, offers, lores, order));
        }
        
        return mc;
    }
    
    @SuppressWarnings({ "unchecked", "rawtypes", "deprecation" })
    public static ItemStack createItemStackByConfig(Object section) {
        if(!(section instanceof LinkedHashMap)) {
            return null;
        }
        
        try {
            LinkedHashMap<String, Object> cfgSection = (LinkedHashMap<String, Object>)section;
            
            String materialString = cfgSection.get("item").toString();
            Material material = null;
            boolean hasMeta = false;
            boolean hasPotionMeta = false;
            byte meta = 0;
            ItemStack finalStack = null;
            int amount = 1;
            short potionMeta = 0;
            
            if(Utils.isNumber(materialString)) {
                material = Material.getMaterial(Integer.parseInt(materialString));
            } else {
                material = Material.getMaterial(materialString);
            }
            
            try {         
                if(cfgSection.containsKey("amount")) {
                    amount = Integer.parseInt(cfgSection.get("amount").toString());
                }
            } catch(Exception ex) {
                amount = 1;
            }
            
            if(cfgSection.containsKey("meta")) {
                if(!material.equals(Material.POTION)) {
                    try {
                        meta = Byte.parseByte(cfgSection.get("meta").toString());
                        hasMeta = true;
                    } catch(Exception ex) {
                        hasMeta = false;
                    }
                } else {
                    hasPotionMeta = true;
                    potionMeta = Short.parseShort(cfgSection.get("meta").toString());
                }
            }
            
            if(hasMeta) {
                finalStack = new ItemStack(material, amount, meta);
            } else if(hasPotionMeta) {
                finalStack = new ItemStack(material, amount, potionMeta);
            } else {
                finalStack = new ItemStack(material, amount);
            }
            
            if(cfgSection.containsKey("enchants")) {
                Object cfgEnchants = cfgSection.get("enchants");
                
                if(cfgEnchants instanceof LinkedHashMap) {
                    LinkedHashMap<Object, Object> enchantSection = (LinkedHashMap)cfgEnchants;
                    for(Object sKey : enchantSection.keySet()) {
                        String key = sKey.toString();
                        
                        if(finalStack.getType() != Material.POTION) {
                            Enchantment en = null;
                            int level = 0;
                            
                            if(Utils.isNumber(key)) {
                                en = Enchantment.getById(Integer.parseInt(key));
                                level = Integer.parseInt(enchantSection.get(Integer.parseInt(key)).toString());
                            } else {
                                en = Enchantment.getByName(key.toUpperCase());
                                level = Integer.parseInt(enchantSection.get(key).toString())-1;
                            }
                            
                            if(en == null) {
                                continue;
                            }

                            finalStack.addUnsafeEnchantment(en, level);
                        }
                    }
                }
            }
            
            if(cfgSection.containsKey("name")) {
                String name = ChatColor.translateAlternateColorCodes('§', cfgSection.get("name").toString());
                ItemMeta im = finalStack.getItemMeta();
                
                im.setDisplayName(name);
                finalStack.setItemMeta(im);
            } else {
                
                ItemMeta im = finalStack.getItemMeta();
                String name = im.getDisplayName();
                
                switch(finalStack.getType()) {
                    case CLAY_BRICK:
                        name = Main._l("ressources.bronze");
                        break;
                    case IRON_INGOT:
                        name = Main._l("ressources.iron");
                        break;
                    case GOLD_INGOT:
                        name = Main._l("ressources.gold");
                        break;
                    default:
                        break;
                }
                
                im.setDisplayName(name);
                finalStack.setItemMeta(im);
            }
            
            return finalStack;
            
        } catch(Exception ex) {
            ex.printStackTrace();
        }
        
        return null;
    }
    
    public static void openCategorySelection(Player p, Game g) {
        List<MerchantCategory> cats = g.getOrderedItemShopCategories();
        
        Inventory inv = Bukkit.createInventory(p, (cats.size()-cats.size()%9)+9, Main._l("ingame.shop.name"));
        for(MerchantCategory cat : cats) {
            ItemStack is = new ItemStack(cat.getMaterial(), 1);
            ItemMeta im = is.getItemMeta();
            
            im.setDisplayName(cat.getName());
            im.setLore(cat.getLores());
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
