package io.github.bedwarsrel.BedwarsRel.Reflection;

import org.bukkit.inventory.ItemStack;
import org.inventivetalent.reflection.resolver.MethodResolver;
import org.inventivetalent.reflection.resolver.ResolverQuery;

public class CraftItemStack {

  private static Class<?> CraftItemStack = ReflectionHelper.obcClassResolver
  .resolveSilent("inventory.CraftItemStack");
  private static MethodResolver CraftItemStackMethodResolver = new MethodResolver(CraftItemStack);
  private Object stack;
  
  public CraftItemStack(ItemStack stack){
    this.stack = stack;
  }

  public CraftItemStack(Object stack){
    this.stack = stack;
  }
  
  public static Class<?> getClazz(){
    return CraftItemStack;
  }
  
  public Object asNMSCopy() {
    try {
      return CraftItemStackMethodResolver
      .resolve("asNMSCopy").invoke(null, this.stack);
    } catch (Exception e) {
      e.printStackTrace();
    }
    return null;
  }

  public ItemStack asBukkitCopy() {
    try {
      return (ItemStack) CraftItemStackMethodResolver
          .resolve(new ResolverQuery("asBukkitCopy", ItemStack.class)).invoke(null, this.stack);
    } catch (Exception e) {
      e.printStackTrace();
    }
    return null;
  }
}
