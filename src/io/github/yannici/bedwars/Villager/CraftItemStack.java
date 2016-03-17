package io.github.yannici.bedwars.Villager;

import java.lang.reflect.Method;

import org.bukkit.inventory.ItemStack;

import io.github.yannici.bedwars.Main;

public class CraftItemStack {

	@SuppressWarnings("rawtypes")
	private Class craftItemStack = null;
	private Object stack = null;

	public CraftItemStack(ItemStack stack) {
		this.craftItemStack = Main.getInstance().getCraftBukkitClass("inventory.CraftItemStack");
		this.stack = stack;
	}

	public CraftItemStack(Object stack) {
		this.craftItemStack = Main.getInstance().getCraftBukkitClass("inventory.CraftItemStack");
		this.stack = stack;
	}

	@SuppressWarnings("unchecked")
	public Object asNMSCopy() {
		try {
			Method m = this.craftItemStack.getDeclaredMethod("asNMSCopy", new Class[] { ItemStack.class });
			m.setAccessible(true);
			return m.invoke(null, new Object[] { this.stack });
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	public ItemStack asBukkitCopy() {
		try {
			Method m = this.craftItemStack.getDeclaredMethod("asBukkitCopy", new Class[] { ItemStack.class });
			m.setAccessible(true);
			return (ItemStack) m.invoke(null, new Object[] { this.stack });
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
}
