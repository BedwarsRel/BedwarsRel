package io.github.bedwarsrel.shop;

import io.github.bedwarsrel.BedwarsRel;
import io.github.bedwarsrel.game.Game;
import io.github.bedwarsrel.utils.ChatWriter;
import io.github.bedwarsrel.utils.SoundMachine;
import io.github.bedwarsrel.utils.Utils;
import io.github.bedwarsrel.villager.MerchantCategory;
import io.github.bedwarsrel.villager.VillagerTrade;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
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
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;

public class NewItemShop {

  private List<MerchantCategory> categories = null;
  private MerchantCategory currentCategory = null;

  public NewItemShop(List<MerchantCategory> categories) {
    this.categories = categories;
  }

  @SuppressWarnings("deprecation")
  private void addCategoriesToInventory(Inventory inventory, Player player, Game game) {
    for (MerchantCategory category : this.categories) {

      if (category.getMaterial() == null) {
        BedwarsRel.getInstance().getServer().getConsoleSender()
            .sendMessage(ChatWriter.pluginMessage(ChatColor.RED
                + "Careful: Not supported material in shop category '" + category.getName() + "'"));
        continue;
      }

      if (player != null && !player.hasPermission(category.getPermission())) {
        continue;
      }

      ItemStack is = new ItemStack(category.getMaterial(), 1);
      ItemMeta im = is.getItemMeta();

      if (Utils.isColorable(is)) {
        is.setDurability(game.getPlayerTeam(player).getColor().getDyeColor().getWoolData());
      }
      if (this.currentCategory != null && this.currentCategory.equals(category)) {
        im.addEnchant(Enchantment.DAMAGE_ALL, 1, true);
        im.addItemFlags(ItemFlag.HIDE_ENCHANTS);
      }

      im.setDisplayName(category.getName());
      im.setLore(category.getLores());
      im.addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_POTION_EFFECTS);
      is.setItemMeta(im);

      inventory.addItem(is);
    }

  }

  @SuppressWarnings("unchecked")
  private boolean buyItem(VillagerTrade trade, ItemStack item, Player player) {
    PlayerInventory inventory = player.getInventory();
    boolean success = true;

    int item1ToPay = trade.getItem1().getAmount();
    Iterator<?> stackIterator = inventory.all(trade.getItem1().getType()).entrySet().iterator();

    int firstItem1 = inventory.first(trade.getItem1());
    if (firstItem1 > -1) {
      inventory.clear(firstItem1);
    } else {
      // pay
      while (stackIterator.hasNext()) {
        Entry<Integer, ? extends ItemStack> entry =
            (Entry<Integer, ? extends ItemStack>) stackIterator.next();
        ItemStack stack = (ItemStack) entry.getValue();

        int endAmount = stack.getAmount() - item1ToPay;
        if (endAmount < 0) {
          endAmount = 0;
        }

        item1ToPay = item1ToPay - stack.getAmount();
        stack.setAmount(endAmount);
        inventory.setItem(entry.getKey(), stack);

        if (item1ToPay <= 0) {
          break;
        }
      }
    }

    if (trade.getItem2() != null) {
      int item2ToPay = trade.getItem2().getAmount();
      stackIterator = inventory.all(trade.getItem2().getType()).entrySet().iterator();

      int firstItem2 = inventory.first(trade.getItem2());
      if (firstItem2 > -1) {
        inventory.clear(firstItem2);
      } else {
        // pay item2
        while (stackIterator.hasNext()) {
          Entry<Integer, ? extends ItemStack> entry =
              (Entry<Integer, ? extends ItemStack>) stackIterator.next();
          ItemStack stack = (ItemStack) entry.getValue();

          int endAmount = stack.getAmount() - item2ToPay;
          if (endAmount < 0) {
            endAmount = 0;
          }

          item2ToPay = item2ToPay - stack.getAmount();
          stack.setAmount(endAmount);
          inventory.setItem(entry.getKey(), stack);

          if (item2ToPay <= 0) {
            break;
          }
        }
      }
    }

    ItemStack addingItem = item.clone();
    ItemMeta meta = addingItem.getItemMeta();
    List<String> lore = meta.getLore();

    if (lore.size() > 0) {
      lore.remove(lore.size() - 1);
      if (trade.getItem2() != null) {
        lore.remove(lore.size() - 1);
      }
    }

    meta.setLore(lore);
    addingItem.setItemMeta(meta);

    HashMap<Integer, ItemStack> notStored = inventory.addItem(addingItem);
    if (notStored.size() > 0) {
      ItemStack notAddedItem = notStored.get(0);
      int removingAmount = addingItem.getAmount() - notAddedItem.getAmount();
      addingItem.setAmount(removingAmount);
      inventory.removeItem(addingItem);

      // restore
      inventory.addItem(trade.getItem1());
      if (trade.getItem2() != null) {
        inventory.addItem(trade.getItem2());
      }

      success = false;
    }

    player.updateInventory();
    return success;
  }

  private void changeToOldShop(Game game, Player player) {
    game.getPlayerSettings(player).setUseOldShop(true);

    player.playSound(player.getLocation(), SoundMachine.get("CLICK", "UI_BUTTON_CLICK"),
        Float.valueOf("1.0"), Float.valueOf("1.0"));

    // open old shop
    MerchantCategory.openCategorySelection(player, game);
  }

  private int getBuyInventorySize(int sizeCategories, int sizeOffers) {
    return this.getInventorySize(sizeCategories) + this.getInventorySize(sizeOffers);
  }

  public List<MerchantCategory> getCategories() {
    return this.categories;
  }

  private int getCategoriesSize(Player player) {
    int size = 0;
    for (MerchantCategory cat : this.categories) {
      if (cat.getMaterial() == null) {
        continue;
      }

      if (player != null && !player.hasPermission(cat.getPermission())) {
        continue;
      }

      size++;
    }

    return size;
  }

  private MerchantCategory getCategoryByMaterial(Material material) {
    for (MerchantCategory category : this.categories) {
      if (category.getMaterial() == material) {
        return category;
      }
    }

    return null;
  }

  private int getInventorySize(int itemAmount) {
    int nom = (itemAmount % 9 == 0) ? 9 : (itemAmount % 9);
    return itemAmount + (9 - nom);
  }

  private VillagerTrade getTradingItem(MerchantCategory category, ItemStack stack, Game game,
      Player player) {
    for (VillagerTrade trade : category.getOffers()) {
      if (trade.getItem1().getType() == Material.AIR
          && trade.getRewardItem().getType() == Material.AIR) {
        continue;
      }
      ItemStack iStack = this.toItemStack(trade, player, game);
      if (iStack.getType() == Material.ENDER_CHEST && stack.getType() == Material.ENDER_CHEST) {
        return trade;
      } else if ((iStack.getType() == Material.POTION
          || (!BedwarsRel.getInstance().getCurrentVersion().startsWith("v1_8")
          && (iStack.getType().equals(Material.valueOf("TIPPED_ARROW"))
          || iStack.getType().equals(Material.valueOf("LINGERING_POTION"))
          || iStack.getType().equals(Material.valueOf("SPLASH_POTION")))))) {
        if (BedwarsRel.getInstance().getCurrentVersion().startsWith("v1_8")) {
          if (iStack.getItemMeta().equals(stack.getItemMeta())) {
            return trade;
          }
        } else {
          PotionMeta iStackMeta = (PotionMeta) iStack.getItemMeta();
          PotionMeta stackMeta = (PotionMeta) stack.getItemMeta();
          if (iStackMeta.getBasePotionData().equals(stackMeta.getBasePotionData()) && iStackMeta
              .getCustomEffects().equals(stackMeta.getCustomEffects())) {
            return trade;
          }
        }
      } else if (iStack.equals(stack)) {
        return trade;
      }
    }

    return null;
  }

  private void handleBuyInventoryClick(InventoryClickEvent ice, Game game, Player player) {

    int sizeCategories = this.getCategoriesSize(player);
    List<VillagerTrade> offers = this.currentCategory.getOffers();
    int sizeItems = offers.size();
    int totalSize = this.getBuyInventorySize(sizeCategories, sizeItems);

    ItemStack item = ice.getCurrentItem();
    boolean cancel = false;
    int bought = 0;
    boolean oneStackPerShift = game.getPlayerSettings(player).oneStackPerShift();

    if (this.currentCategory == null) {
      player.closeInventory();
      return;
    }

    if (ice.getRawSlot() < sizeCategories) {
      // is category click
      ice.setCancelled(true);

      if (item == null) {
        return;
      }

      if (item.getType().equals(this.currentCategory.getMaterial())) {
        // back to default category view
        this.currentCategory = null;
        this.openCategoryInventory(player);
      } else {
        // open the clicked buy inventory
        this.handleCategoryInventoryClick(ice, game, player);
      }
    } else if (ice.getRawSlot() < totalSize) {
      // its a buying item
      ice.setCancelled(true);

      if (item == null || item.getType() == Material.AIR) {
        return;
      }

      MerchantCategory category = this.currentCategory;
      VillagerTrade trade = this.getTradingItem(category, item, game, player);

      if (trade == null) {
        return;
      }

      player.playSound(player.getLocation(), SoundMachine.get("ITEM_PICKUP", "ENTITY_ITEM_PICKUP"),
          Float.valueOf("1.0"), Float.valueOf("1.0"));

      // enough ressources?
      if (!this.hasEnoughRessource(player, trade)) {
        player
            .sendMessage(
                ChatWriter.pluginMessage(ChatColor.RED + BedwarsRel
                    ._l(player, "errors.notenoughress")));
        return;
      }

      if (ice.isShiftClick()) {
        while (this.hasEnoughRessource(player, trade) && !cancel) {
          cancel = !this.buyItem(trade, ice.getCurrentItem(), player);
          if (!cancel && oneStackPerShift) {
            bought = bought + item.getAmount();
            cancel = ((bought + item.getAmount()) > 64);
          }
        }

        bought = 0;
      } else {
        this.buyItem(trade, ice.getCurrentItem(), player);
      }
    } else {
      if (ice.isShiftClick()) {
        ice.setCancelled(true);
      } else {
        ice.setCancelled(false);
      }

      return;
    }
  }

  private void handleCategoryInventoryClick(InventoryClickEvent ice, Game game, Player player) {

    int catSize = this.getCategoriesSize(player);
    int sizeCategories = this.getInventorySize(catSize) + 9;
    int rawSlot = ice.getRawSlot();

    if (rawSlot >= this.getInventorySize(catSize) && rawSlot < sizeCategories) {
      ice.setCancelled(true);
      if (ice.getCurrentItem().getType() == Material.SLIME_BALL) {
        this.changeToOldShop(game, player);
        return;
      }

      if (ice.getCurrentItem().getType() == Material.BUCKET) {
        game.getPlayerSettings(player).setOneStackPerShift(false);
        player.playSound(player.getLocation(), SoundMachine.get("CLICK", "UI_BUTTON_CLICK"),
            Float.valueOf("1.0"), Float.valueOf("1.0"));
        this.openCategoryInventory(player);
        return;
      } else if (ice.getCurrentItem().getType() == Material.LAVA_BUCKET) {
        game.getPlayerSettings(player).setOneStackPerShift(true);
        player.playSound(player.getLocation(), SoundMachine.get("CLICK", "UI_BUTTON_CLICK"),
            Float.valueOf("1.0"), Float.valueOf("1.0"));
        this.openCategoryInventory(player);
        return;
      }

    }

    if (rawSlot >= sizeCategories) {
      if (ice.isShiftClick()) {
        ice.setCancelled(true);
        return;
      }

      ice.setCancelled(false);
      return;
    }

    MerchantCategory clickedCategory = this.getCategoryByMaterial(ice.getCurrentItem().getType());
    if (clickedCategory == null) {
      if (ice.isShiftClick()) {
        ice.setCancelled(true);
        return;
      }

      ice.setCancelled(false);
      return;
    }

    this.openBuyInventory(clickedCategory, player, game);
  }

  public void handleInventoryClick(InventoryClickEvent ice, Game game, Player player) {
    if (!this.hasOpenCategory()) {
      this.handleCategoryInventoryClick(ice, game, player);
    } else {
      this.handleBuyInventoryClick(ice, game, player);
    }
  }

  private boolean hasEnoughRessource(Player player, VillagerTrade trade) {
    ItemStack item1 = trade.getItem1();
    ItemStack item2 = trade.getItem2();
    PlayerInventory inventory = player.getInventory();

    if (item2 != null) {
      if (!inventory.contains(item1.getType(), item1.getAmount())
          || !inventory.contains(item2.getType(), item2.getAmount())) {
        return false;
      }
    } else {
      if (!inventory.contains(item1.getType(), item1.getAmount())) {
        return false;
      }
    }

    return true;
  }

  public boolean hasOpenCategory() {
    return (this.currentCategory != null);
  }

  public boolean hasOpenCategory(MerchantCategory category) {
    if (this.currentCategory == null) {
      return false;
    }

    return (this.currentCategory.equals(category));
  }

  private void openBuyInventory(MerchantCategory category, Player player, Game game) {
    List<VillagerTrade> offers = category.getOffers();
    int sizeCategories = this.getCategoriesSize(player);
    int sizeItems = offers.size();
    int invSize = this.getBuyInventorySize(sizeCategories, sizeItems);

    player.playSound(player.getLocation(), SoundMachine.get("CLICK", "UI_BUTTON_CLICK"),
        Float.valueOf("1.0"), Float.valueOf("1.0"));

    this.currentCategory = category;
    Inventory buyInventory = Bukkit
        .createInventory(player, invSize, BedwarsRel._l(player, "ingame.shop.name"));
    this.addCategoriesToInventory(buyInventory, player, game);

    for (int i = 0; i < offers.size(); i++) {
      VillagerTrade trade = offers.get(i);
      if (trade.getItem1().getType() == Material.AIR
          && trade.getRewardItem().getType() == Material.AIR) {
        continue;
      }

      int slot = (this.getInventorySize(sizeCategories)) + i;
      ItemStack tradeStack = this.toItemStack(trade, player, game);

      buyInventory.setItem(slot, tradeStack);
    }

    player.openInventory(buyInventory);
  }

  public void openCategoryInventory(Player player) {
    int catSize = this.getCategoriesSize(player);
    int nom = (catSize % 9 == 0) ? 9 : (catSize % 9);
    int size = (catSize + (9 - nom)) + 9;

    Inventory inventory = Bukkit.createInventory(player, size, BedwarsRel
        ._l(player, "ingame.shop.name"));

    Game game = BedwarsRel.getInstance().getGameManager().getGameOfPlayer(player);

    this.addCategoriesToInventory(inventory, player, game);

    ItemStack slime = new ItemStack(Material.SLIME_BALL, 1);
    ItemMeta slimeMeta = slime.getItemMeta();

    slimeMeta.setDisplayName(BedwarsRel._l(player, "ingame.shop.oldshop"));
    slimeMeta.setLore(new ArrayList<String>());
    slime.setItemMeta(slimeMeta);
    ItemStack stack = null;

    if (game != null) {
      if (game.getPlayerSettings(player).oneStackPerShift()) {
        stack = new ItemStack(Material.BUCKET, 1);
        ItemMeta meta = stack.getItemMeta();

        meta.setDisplayName(
            ChatColor.AQUA + BedwarsRel._l(player, "default.currently") + ": " + ChatColor.WHITE
                + BedwarsRel._l(player, "ingame.shop.onestackpershift"));
        meta.setLore(new ArrayList<String>());
        stack.setItemMeta(meta);
      } else {
        stack = new ItemStack(Material.LAVA_BUCKET, 1);
        ItemMeta meta = stack.getItemMeta();

        meta.setDisplayName(
            ChatColor.AQUA + BedwarsRel._l(player, "default.currently") + ": " + ChatColor.WHITE
                + BedwarsRel._l(player, "ingame.shop.fullstackpershift"));
        meta.setLore(new ArrayList<String>());
        stack.setItemMeta(meta);
      }

      if (stack != null) {
        inventory.setItem(size - 4, stack);
      }
    }

    if (stack == null) {
      inventory.setItem(size - 5, slime);
    } else {
      inventory.setItem(size - 6, slime);
    }

    player.openInventory(inventory);
  }

  public void setCurrentCategory(MerchantCategory category) {
    this.currentCategory = category;
  }

  @SuppressWarnings("deprecation")
  private ItemStack toItemStack(VillagerTrade trade, Player player, Game game) {
    ItemStack tradeStack = trade.getRewardItem().clone();
    Method colorable = Utils.getColorableMethod(tradeStack.getType());
    ItemMeta meta = tradeStack.getItemMeta();
    ItemStack item1 = trade.getItem1();
    ItemStack item2 = trade.getItem2();
    if (Utils.isColorable(tradeStack)) {
      tradeStack.setDurability(game.getPlayerTeam(player).getColor().getDyeColor().getWoolData());
    } else if (colorable != null) {
      colorable.setAccessible(true);
      try {
        colorable.invoke(meta, new Object[]{game.getPlayerTeam(player).getColor().getColor()});
      } catch (Exception e) {
        BedwarsRel.getInstance().getBugsnag().notify(e);
        e.printStackTrace();
      }
    }
    List<String> lores = meta.getLore();
    if (lores == null) {
      lores = new ArrayList<String>();
    }

    lores.add(ChatColor.WHITE + String.valueOf(item1.getAmount()) + " "
        + item1.getItemMeta().getDisplayName());
    if (item2 != null) {
      lores.add(ChatColor.WHITE + String.valueOf(item2.getAmount()) + " "
          + item2.getItemMeta().getDisplayName());
    }

    meta.setLore(lores);
    tradeStack.setItemMeta(meta);
    return tradeStack;
  }
}