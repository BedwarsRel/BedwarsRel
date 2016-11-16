package io.github.bedwarsrel.BedwarsRel.Reflection;

import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;
import org.inventivetalent.reflection.resolver.ConstructorResolver;
import org.inventivetalent.reflection.resolver.MethodResolver;
import org.inventivetalent.reflection.resolver.ResolverQuery;

public class SpawnEggReflected {

  private static Class<?> CraftItemStack =
      ReflectionHelper.obcClassResolver.resolveSilent("inventory.CraftItemStack");
  private static Class<?> NMSItemStack =
      ReflectionHelper.nmsClassResolver.resolveSilent("ItemStack");
  private static Class<?> NBTTagCompound =
      ReflectionHelper.nmsClassResolver.resolveSilent("NBTTagCompound");
  private static ConstructorResolver NBTTagCompoundConstructorResolver =
      new ConstructorResolver(NBTTagCompound);
  public static MethodResolver CraftItemStackMethodResolver = new MethodResolver(CraftItemStack);
  public static MethodResolver NMSItemStackMethodResolver = new MethodResolver(NMSItemStack);
  public static MethodResolver NBTTagCompoundMethodResolver = new MethodResolver(NBTTagCompound);

  private EntityType type;

  public SpawnEggReflected(EntityType type) {
    this.type = type;
  }

  public EntityType getSpawnedType() {
    return type;
  }

  public void setSpawnedType(EntityType type) {
    if (type.isAlive()) {
      this.type = type;
    }
  }

  public String toString() {
    return "SPAWN EGG{" + getSpawnedType() + "}";
  }

  public SpawnEggReflected clone() {
    return (SpawnEggReflected) this.clone();
  }

  public ItemStack toItemStack() {
    return toItemStack(1);
  }

  @SuppressWarnings("deprecation")
  public ItemStack toItemStack(int amount) {
    try {
      ItemStack item = new ItemStack(Material.MONSTER_EGG, amount);
      Object stack = new CraftItemStack(item).asNMSCopy();
      Object tagCompound = NMSItemStackMethodResolver
          .resolve(new ResolverQuery("getTag", NBTTagCompound)).invoke(stack);
      if (tagCompound == null) {
        tagCompound = NBTTagCompoundConstructorResolver.resolve(new Class[] {}).newInstance();
      }
      Object id = NBTTagCompoundConstructorResolver.resolve(new Class[] {}).newInstance();
      NBTTagCompoundMethodResolver.resolve("setString").invoke(id, "id", type.getName());
      NBTTagCompoundMethodResolver.resolve("set").invoke(tagCompound, "EntityTag", id);
      CraftItemStackMethodResolver.resolve("setTag").invoke(stack, tagCompound);
      return new CraftItemStack(stack).asBukkitCopy();
    } catch (Exception e) {
      e.printStackTrace();
    }
    return null;
  }

  @SuppressWarnings("deprecation")
  public static SpawnEggReflected fromItemStack(ItemStack item) {
    if (item == null)
      throw new IllegalArgumentException("item cannot be null");
    if (item.getType() != Material.MONSTER_EGG)
      throw new IllegalArgumentException("item is not a monster egg");
    try {
      Object stack = new CraftItemStack(item).asNMSCopy();
      Object tagCompound = NMSItemStackMethodResolver.resolve("getTag").invoke(stack);
      if (tagCompound != null) {
        Object entityTagTagCompound =
            NBTTagCompoundMethodResolver.resolve("getCompound").invoke(tagCompound, "EntityTag");
        Object entityTypeId;
        entityTypeId =
            NBTTagCompoundMethodResolver.resolve("getString").invoke(entityTagTagCompound, "id");
        if (entityTypeId != null) {
          EntityType type = EntityType.fromName((String) entityTypeId);
          if (type != null) {
            return new SpawnEggReflected(type);
          }
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    return null;
  }
}
