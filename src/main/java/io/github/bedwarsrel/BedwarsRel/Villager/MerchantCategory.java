package io.github.bedwarsrel.BedwarsRel.Villager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import io.github.bedwarsrel.BedwarsRel.Main;
import io.github.bedwarsrel.BedwarsRel.Game.Game;
import io.github.bedwarsrel.BedwarsRel.Utils.ChatWriter;
import io.github.bedwarsrel.BedwarsRel.Utils.Utils;

public class MerchantCategory {

  private String name = null;
  private Material item = null;
  private List<String> lores = null;
  private ArrayList<VillagerTrade> offers = null;
  private int order = 0;
  private String permission = null;

  public MerchantCategory(String name, Material item) {
    this(name, item, new ArrayList<VillagerTrade>(), new ArrayList<String>(), 0, "bw.base");
  }

  public MerchantCategory(String name, Material item, ArrayList<VillagerTrade> offers,
      List<String> lores, int order, String permission) {
    this.name = name;
    this.item = item;
    this.offers = offers;
    this.lores = lores;
    this.order = order;
    this.permission = permission;
  }

  public List<String> getLores() {
    return this.lores;
  }

  public int getOrder() {
    return this.order;
  }

  @SuppressWarnings({"unchecked", "deprecation"})
  public static HashMap<Material, MerchantCategory> loadCategories(FileConfiguration cfg) {
    if (cfg.getConfigurationSection("shop") == null) {
      return new HashMap<Material, MerchantCategory>();
    }

    HashMap<Material, MerchantCategory> mc = new HashMap<Material, MerchantCategory>();

    ConfigurationSection section = cfg.getConfigurationSection("shop");

    for (String cat : section.getKeys(false)) {
      String catName =
          ChatColor.translateAlternateColorCodes('&', section.getString(cat + ".name"));
      Material catItem = null;
      List<String> lores = new ArrayList<String>();
      String item = section.get(cat + ".item").toString();
      String permission = "bw.base";
      int order = 0;

      if (!Utils.isNumber(item)) {
        catItem = Material.getMaterial(section.getString(cat + ".item"));
      } else {
        catItem = Material.getMaterial(section.getInt(cat + ".item"));
      }

      if (section.contains(cat + ".lore")) {
        for (Object lore : section.getList(cat + ".lore")) {
          lores.add(ChatColor.translateAlternateColorCodes('&', lore.toString()));
        }
      }

      if (section.contains(cat + ".order") && section.isInt(cat + ".order")) {
        order = section.getInt(cat + ".order");
      }

      if (section.contains(cat + ".permission")) {
        permission = section.getString(cat + ".permission", "bw.base");
      }

      ArrayList<VillagerTrade> offers = new ArrayList<VillagerTrade>();

      for (Object offer : section.getList(cat + ".offers")) {
        if (offer instanceof String) {
          if (offer.toString().equalsIgnoreCase("empty")
              || offer.toString().equalsIgnoreCase("null")
              || offer.toString().equalsIgnoreCase("e")) {
            VillagerTrade trade =
                new VillagerTrade(new ItemStack(Material.AIR, 1), new ItemStack(Material.AIR, 1));
            offers.add(trade);
          }

          continue;
        }

        HashMap<String, List<Map<String, Object>>> offerSection =
            (HashMap<String, List<Map<String, Object>>>) offer;

        if (!offerSection.containsKey("price") || !offerSection.containsKey("reward")) {
          continue;
        }

        ItemStack item1 = null;

        try {
          item1 = setRessourceName(ItemStack.deserialize(offerSection.get("price").get(0)));
        } catch (Exception e) {
          // CATCH EXCEPTION
        }

        ItemStack item2 = null;
        if (offerSection.get("price").size() == 2) {
          try {
            item2 = setRessourceName(ItemStack.deserialize(offerSection.get("price").get(1)));
          } catch (Exception e) {
            // CATCH EXCEPTION
          }
        }
        ItemStack reward = null;

        try {
          reward = ItemStack.deserialize(offerSection.get("reward").get(0));
        } catch (Exception e) {
          // CATCH EXCEPTION
        }

        if (item1 == null || reward == null) {
          Main.getInstance().getServer().getConsoleSender().sendMessage(
              ChatWriter.pluginMessage(ChatColor.RED + "Couldn't parse item in category \""
                  + section.getString(cat + ".name") + "\": " + offerSection.toString()));
          continue;
        }

        VillagerTrade tradeObj = null;

        if (item2 != null) {
          tradeObj = new VillagerTrade(item1, item2, reward);
        } else {
          tradeObj = new VillagerTrade(item1, reward);
        }

        offers.add(tradeObj);
      }

      mc.put(catItem, new MerchantCategory(catName, catItem, offers, lores, order, permission));
    }

    return mc;
  }

  @SuppressWarnings("deprecation")
  private static ItemStack setRessourceName(ItemStack item) {

    ItemMeta im = item.getItemMeta();
    String name = im.getDisplayName();

    // check if is ressource
    ConfigurationSection ressourceSection =
        Main.getInstance().getConfig().getConfigurationSection("ressource");
    for (String key : ressourceSection.getKeys(false)) {
      Material ressMaterial = null;
      String itemType = ressourceSection.getString(key + ".item");

      if (Utils.isNumber(itemType)) {
        ressMaterial = Material.getMaterial(Integer.parseInt(itemType));
      } else {
        ressMaterial = Material.getMaterial(itemType);
      }

      if (item.getType().equals(ressMaterial)) {
        name =
            ChatColor.translateAlternateColorCodes('&', ressourceSection.getString(key + ".name"));
      }
    }

    im.setDisplayName(name);
    item.setItemMeta(im);

    return item;
  }

  @SuppressWarnings("deprecation")
  public static void openCategorySelection(Player p, Game g) {
    List<MerchantCategory> cats = g.getOrderedItemShopCategories();

    int nom = (cats.size() % 9 == 0) ? 9 : (cats.size() % 9);
    int size = (cats.size() + (9 - nom)) + 9;

    Inventory inv = Bukkit.createInventory(p, size, Main._l("ingame.shop.name"));
    for (MerchantCategory cat : cats) {
      if (p != null && !p.hasPermission(cat.getPermission())) {
        continue;
      }

      ItemStack is = new ItemStack(cat.getMaterial(), 1);
      ItemMeta im = is.getItemMeta();

      if (Utils.isColorable(is)) {
        is.setDurability(g.getPlayerTeam(p).getColor().getDyeColor().getData());
      }

      im.setDisplayName(cat.getName());
      im.setLore(cat.getLores());
      is.setItemMeta(im);

      inv.addItem(is);
    }

    ItemStack snow = new ItemStack(Material.SNOW_BALL, 1);
    ItemMeta snowMeta = snow.getItemMeta();

    snowMeta.setDisplayName(Main._l("ingame.shop.newshop"));
    snowMeta.setLore(new ArrayList<String>());
    snow.setItemMeta(snowMeta);

    inv.setItem(size - 5, snow);
    p.openInventory(inv);
  }

  public String getName() {
    return this.name;
  }

  public Material getMaterial() {
    return this.item;
  }

  @SuppressWarnings("unchecked")
  public ArrayList<VillagerTrade> getFilteredOffers() {
    ArrayList<VillagerTrade> trades = (ArrayList<VillagerTrade>) this.offers.clone();
    Iterator<VillagerTrade> iterator = trades.iterator();

    while (iterator.hasNext()) {
      VillagerTrade trade = iterator.next();
      if (trade.getItem1().getType() == Material.AIR
          && trade.getRewardItem().getType() == Material.AIR) {
        iterator.remove();
      }
    }

    return trades;
  }

  public ArrayList<VillagerTrade> getOffers() {
    return this.offers;
  }

  public String getPermission() {
    return this.permission;
  }

}
