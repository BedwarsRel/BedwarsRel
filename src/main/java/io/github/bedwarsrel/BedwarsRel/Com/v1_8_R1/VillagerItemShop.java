package io.github.bedwarsrel.BedwarsRel.Com.v1_8_R1;

import java.lang.reflect.Method;

import org.bukkit.craftbukkit.v1_8_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_8_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

import io.github.bedwarsrel.BedwarsRel.Main;
import io.github.bedwarsrel.BedwarsRel.Game.Game;
import io.github.bedwarsrel.BedwarsRel.Utils.Utils;
import io.github.bedwarsrel.BedwarsRel.Villager.MerchantCategory;
import net.minecraft.server.v1_8_R1.EntityHuman;
import net.minecraft.server.v1_8_R1.EntityVillager;
import net.minecraft.server.v1_8_R1.MerchantRecipe;
import net.minecraft.server.v1_8_R1.MerchantRecipeList;
import net.minecraft.server.v1_8_R1.StatisticList;

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
      Main.getInstance().getBugsnag().notify(e);
      e.printStackTrace();
    }

    return null;
  }

  private EntityHuman getEntityHuman() {
    try {
      return ((CraftPlayer) this.player).getHandle();
    } catch (Exception e) {
      Main.getInstance().getBugsnag().notify(e);
      e.printStackTrace();
    }
    return null;
  }

  public void openTrading() {
    // As task because of inventory issues
    new BukkitRunnable() {

      @SuppressWarnings({"unchecked", "deprecation"})
      @Override
      public void run() {
        try {
          EntityVillager entityVillager = VillagerItemShop.this.createVillager();
          EntityHuman entityHuman = VillagerItemShop.this.getEntityHuman();

          // set location
          MerchantRecipeList recipeList = entityVillager.getOffers(entityHuman);
          recipeList.clear();

          for (io.github.bedwarsrel.BedwarsRel.Villager.VillagerTrade trade : VillagerItemShop.this.category
              .getFilteredOffers()) {
            ItemStack reward = trade.getRewardItem();
            Method colorable = Utils.getColorableMethod(reward.getType());

            if (Utils.isColorable(reward)) {
              reward.setDurability(game.getPlayerTeam(player).getColor().getDyeColor().getWoolData());
            } else if (colorable != null) {
              ItemMeta meta = reward.getItemMeta();
              colorable.setAccessible(true);
              colorable.invoke(meta, new Object[] {VillagerItemShop.this.game
                  .getPlayerTeam(VillagerItemShop.this.player).getColor().getColor()});
              reward.setItemMeta(meta);
            }

            if (!(trade.getHandle().getInstance() instanceof MerchantRecipe)) {
              continue;
            }

            MerchantRecipe recipe = (MerchantRecipe) trade.getHandle().getInstance();
            recipe.a(1000);
            recipeList.add(recipe);
          }

          entityVillager.a_(entityHuman);
          ((CraftPlayer) player).getHandle().openTrade(entityVillager);
          ((CraftPlayer) player).getHandle().b(StatisticList.F);

        } catch (Exception ex) {
          Main.getInstance().getBugsnag().notify(ex);
          ex.printStackTrace();
        }
      }
    }.runTask(Main.getInstance());
  }

}
