package io.github.bedwarsrel.villager;

import org.bukkit.inventory.ItemStack;

public class VillagerTrade {

  private ItemStack item1;
  private ItemStack item2;
  private ItemStack rewardItem;

  public VillagerTrade(ItemStack item1, ItemStack item2, ItemStack rewardItem) {
    this.item1 = item1;
    this.item2 = item2;
    this.rewardItem = rewardItem;
  }

  public VillagerTrade(ItemStack item1, ItemStack rewardItem) {
    this(item1, null, rewardItem);
  }

  public VillagerTrade(MerchantRecipe handle) {
    this.item1 = new CraftItemStack(handle.getItem1()).asBukkitCopy();
    this.item2 =
        (handle.getItem1() == null ? null : new CraftItemStack(handle.getItem2()).asBukkitCopy());
    this.rewardItem = new CraftItemStack(handle.getRewardItem()).asBukkitCopy();
  }

  public MerchantRecipe getHandle() {
    if (this.item2 == null) {
      return new MerchantRecipe(new CraftItemStack(this.item1).asNMSCopy(),
          new CraftItemStack(this.rewardItem).asNMSCopy());
    }
    return new MerchantRecipe(new CraftItemStack(this.item1).asNMSCopy(),
        new CraftItemStack(this.item2).asNMSCopy(),
        new CraftItemStack(this.rewardItem).asNMSCopy());
  }

  public ItemStack getItem1() {
    return this.item1;
  }

  public ItemStack getItem2() {
    return this.item2;
  }

  public ItemStack getRewardItem() {
    return this.rewardItem;
  }

  public boolean hasItem2() {
    return this.item2 != null;
  }

}
