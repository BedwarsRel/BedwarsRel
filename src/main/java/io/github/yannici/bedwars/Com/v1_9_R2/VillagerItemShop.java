package io.github.yannici.bedwars.Com.v1_9_R2;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.craftbukkit.v1_9_R2.CraftWorld;
import org.bukkit.craftbukkit.v1_9_R2.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_9_R2.entity.CraftVillager;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.MerchantRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

import io.github.yannici.bedwars.Main;
import io.github.yannici.bedwars.Utils;
import io.github.yannici.bedwars.Game.Game;
import io.github.yannici.bedwars.Villager.MerchantCategory;
import net.minecraft.server.v1_9_R2.EntityHuman;
import net.minecraft.server.v1_9_R2.EntityVillager;
import net.minecraft.server.v1_9_R2.StatisticList;

public class VillagerItemShop {

  private Game game = null;
  private Player player = null;
  private MerchantCategory category = null;

  public VillagerItemShop(Game g, Player p, MerchantCategory category) {
    this.game = g;
    this.player = p;
    this.category = category;
  }

  private EntityVillager createVillager() {
    try {
      EntityVillager ev =
          new EntityVillager(((CraftWorld) this.game.getRegion().getWorld()).getHandle());

      return ev;
    } catch (Exception e) {
      e.printStackTrace();
    }

    return null;
  }

  private EntityHuman getEntityHuman() {
    try {
      return ((CraftPlayer) this.player).getHandle();
    } catch (Exception e) {
      e.printStackTrace();
    }
    return null;
  }

  public void openTrading() {
    // As task because of inventory issues
    new BukkitRunnable() {

      @SuppressWarnings("deprecation")
      @Override
      public void run() {
        try {
          EntityVillager entityVillager = VillagerItemShop.this.createVillager();
          CraftVillager craftVillager = (CraftVillager) entityVillager.getBukkitEntity();
          EntityHuman entityHuman = VillagerItemShop.this.getEntityHuman();

          // set location
          List<MerchantRecipe> recipeList = new ArrayList<MerchantRecipe>();

          for (io.github.yannici.bedwars.Villager.VillagerTrade trade : VillagerItemShop.this.category
              .getFilteredOffers()) {
            ItemStack reward = trade.getRewardItem();
            Method colorable = Utils.getColorableMethod(reward.getType());

            if (Utils.isColorable(reward)) {
              reward.setDurability(game.getPlayerTeam(player).getColor().getDyeColor().getData());
            } else if (colorable != null) {
              ItemMeta meta = reward.getItemMeta();
              colorable.setAccessible(true);
              colorable.invoke(meta, new Object[] {VillagerItemShop.this.game
                  .getPlayerTeam(VillagerItemShop.this.player).getColor().getColor()});
              reward.setItemMeta(meta);
            }

            if (!(trade.getHandle()
                .getInstance() instanceof net.minecraft.server.v1_9_R2.MerchantRecipe)) {
              continue;
            }

            MerchantRecipe recipe = new MerchantRecipe(trade.getRewardItem(), 1024);
            recipe.addIngredient(trade.getItem1());

            if (trade.getItem2() != null) {
              recipe.addIngredient(trade.getItem2());
            }

            recipeList.add(recipe);
          }

          craftVillager.setRecipes(recipeList);
          craftVillager.setRiches(0);
          entityVillager.setTradingPlayer(entityHuman);

          ((CraftPlayer) player).getHandle().openTrade(entityVillager);
          ((CraftPlayer) player).getHandle().b(StatisticList.F);
        } catch (Exception ex) {
          ex.printStackTrace();
        }
      }
    }.runTask(Main.getInstance());
  }

}
