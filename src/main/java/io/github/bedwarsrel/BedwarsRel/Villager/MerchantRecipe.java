package io.github.bedwarsrel.BedwarsRel.Villager;

import io.github.bedwarsrel.BedwarsRel.Main;
import java.lang.reflect.Method;

@SuppressWarnings({"unchecked", "rawtypes"})
public class MerchantRecipe {

  private Object instance = null;
  private Class merchantRecipe = null;

  public MerchantRecipe(Object recipe) {
    this.instance = recipe;
  }

  public MerchantRecipe(Object item1, Object item2, Object reward) {
    this.merchantRecipe = Main.getInstance().getMinecraftServerClass("MerchantRecipe");
    Class isClass = Main.getInstance().getMinecraftServerClass("ItemStack");
    try {
      this.instance = this.merchantRecipe.getDeclaredConstructor(isClass, isClass, isClass)
          .newInstance(item1, item2, reward);
    } catch (Exception e) {
      Main.getInstance().getBugsnag().notify(e);
      e.printStackTrace();
    }
  }

  public MerchantRecipe(Object item1, Object reward) {
    this(item1, null, reward);
  }

  public static Class getReflectionClass() {
    return Main.getInstance().getMinecraftServerClass("MerchantRecipe");
  }

  public Object getInstance() {
    return this.instance;
  }

  public Object getItem1() {
    try {
      Method m = this.merchantRecipe.getDeclaredMethod("getBuyItem1");
      m.setAccessible(true);
      return m.invoke(this.merchantRecipe);
    } catch (Exception e) {
      Main.getInstance().getBugsnag().notify(e);
      e.printStackTrace();
    }
    return null;
  }

  public Object getItem2() {
    try {
      Method m = this.merchantRecipe.getDeclaredMethod("getBuyItem2");
      m.setAccessible(true);
      return m.invoke(this.merchantRecipe);
    } catch (Exception e) {
      Main.getInstance().getBugsnag().notify(e);
      e.printStackTrace();
    }
    return null;
  }

  public Object getRewardItem() {
    try {
      Method m = this.merchantRecipe.getDeclaredMethod("getBuyItem3");
      m.setAccessible(true);
      return m.invoke(this.merchantRecipe);
    } catch (Exception e) {
      Main.getInstance().getBugsnag().notify(e);
      e.printStackTrace();
    }
    return null;
  }

}
