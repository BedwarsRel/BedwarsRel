package io.github.yannici.bedwars.Villager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
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
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionEffectType;

import io.github.yannici.bedwars.Main;
import io.github.yannici.bedwars.Utils;
import io.github.yannici.bedwars.Game.Game;

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

	public MerchantCategory(String name, Material item, ArrayList<VillagerTrade> offers, List<String> lores, int order,
			String permission) {
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

	@SuppressWarnings({ "unchecked", "deprecation" })
	public static HashMap<Material, MerchantCategory> loadCategories(FileConfiguration cfg) {
		if (cfg.getConfigurationSection("shop") == null) {
			return new HashMap<Material, MerchantCategory>();
		}

		HashMap<Material, MerchantCategory> mc = new HashMap<Material, MerchantCategory>();

		ConfigurationSection section = cfg.getConfigurationSection("shop");

		for (String cat : section.getKeys(false)) {
			String catName = ChatColor.translateAlternateColorCodes('&', section.getString(cat + ".name"));
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

			if (section.contains(cat + ".order")) {
				if (section.isInt(cat + ".order")) {
					order = section.getInt(cat + ".order");
				}
			}

			if (section.contains(cat + ".permission")) {
				permission = section.getString(cat + ".permission", "bw.base");
			}

			ArrayList<VillagerTrade> offers = new ArrayList<VillagerTrade>();

			for (Object offer : section.getList(cat + ".offers")) {
				if (offer instanceof String) {
					if (offer.toString().equalsIgnoreCase("empty") || offer.toString().equalsIgnoreCase("null")
							|| offer.toString().equalsIgnoreCase("e")) {
						VillagerTrade trade = new VillagerTrade(new ItemStack(Material.AIR, 1),
								new ItemStack(Material.AIR, 1));
						offers.add(trade);
					}

					continue;
				}

				LinkedHashMap<String, Object> offerSection = (LinkedHashMap<String, Object>) offer;

				if (!offerSection.containsKey("item1") || !offerSection.containsKey("reward")) {
					continue;
				}

				ItemStack item1 = MerchantCategory.createItemStackByConfig(offerSection.get("item1"));
				ItemStack item2 = null;
				if (offerSection.containsKey("item2")) {
					item2 = MerchantCategory.createItemStackByConfig(offerSection.get("item2"));
				}
				ItemStack reward = MerchantCategory.createItemStackByConfig(offerSection.get("reward"));

				if (item1 == null || reward == null) {
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

	@SuppressWarnings({ "unchecked", "rawtypes", "deprecation" })
	public static ItemStack createItemStackByConfig(Object section) {
		if (!(section instanceof LinkedHashMap)) {
			return null;
		}

		try {
			LinkedHashMap<String, Object> cfgSection = (LinkedHashMap<String, Object>) section;

			String materialString = cfgSection.get("item").toString();
			Material material = null;
			boolean hasMeta = false;
			byte meta = 0;
			ItemStack finalStack = null;
			int amount = 1;

			if (Utils.isNumber(materialString)) {
				material = Material.getMaterial(Integer.parseInt(materialString));
			} else {
				material = Material.getMaterial(materialString);
			}

			try {
				if (cfgSection.containsKey("amount")) {
					amount = Integer.parseInt(cfgSection.get("amount").toString());
				}
			} catch (Exception ex) {
				amount = 1;
			}

			if (cfgSection.containsKey("meta")) {
				if (!material.equals(Material.POTION) && !(Main.getInstance().getCurrentVersion().startsWith("v1_9")
						&& material.equals(Material.valueOf("TIPPED_ARROW")))) {
					try {
						meta = Byte.parseByte(cfgSection.get("meta").toString());
						hasMeta = true;
					} catch (Exception ex) {
						hasMeta = false;
					}
				} else {
					hasMeta = false;
				}
			}

			if (hasMeta) {
				finalStack = new ItemStack(material, amount, meta);
			} else {
				finalStack = new ItemStack(material, amount);
			}

			if (cfgSection.containsKey("lore")) {
				List<String> lores = new ArrayList<String>();
				ItemMeta im = finalStack.getItemMeta();

				for (Object lore : (List<String>) cfgSection.get("lore")) {
					lores.add(ChatColor.translateAlternateColorCodes('&', lore.toString()));
				}

				im.setLore(lores);
				finalStack.setItemMeta(im);
			}

			if (material.equals(Material.POTION)) {
				if (cfgSection.containsKey("effects")) {
					PotionMeta potionMeta = (PotionMeta) finalStack.getItemMeta();
					for (Object potionEffect : (List<Object>) cfgSection.get("effects")) {
						LinkedHashMap<String, Object> potionEffectSection = (LinkedHashMap<String, Object>) potionEffect;
						if (!potionEffectSection.containsKey("type")) {
							continue;
						}

						PotionEffectType potionEffectType = null;
						int duration = 0;
						int amplifier = 0;
						
						potionEffectType = PotionEffectType
								.getByName(potionEffectSection.get("type").toString().toUpperCase());

						if (potionEffectSection.containsKey("duration")) {
							duration = Integer.parseInt(potionEffectSection.get("duration").toString());
						}
						if (potionEffectSection.containsKey("amplifier")) {
							amplifier = Integer.parseInt(potionEffectSection.get("amplifier").toString()) - 1;
						}

						if (potionEffectType == null) {
							continue;
						}

						potionMeta.addCustomEffect(potionEffectType.createEffect(duration * 20, amplifier), true);
					}

					finalStack.setItemMeta(potionMeta);
				}
			}

			if (cfgSection.containsKey("enchants")) {
				Object cfgEnchants = cfgSection.get("enchants");

				if (cfgEnchants instanceof LinkedHashMap) {
					LinkedHashMap<Object, Object> enchantSection = (LinkedHashMap) cfgEnchants;
					for (Object sKey : enchantSection.keySet()) {
						String key = sKey.toString();

						if (finalStack.getType() != Material.POTION) {
							Enchantment en = null;
							int level = 0;

							if (Utils.isNumber(key)) {
								en = Enchantment.getById(Integer.parseInt(key));
								level = Integer.parseInt(enchantSection.get(Integer.parseInt(key)).toString());
							} else {
								en = Enchantment.getByName(key.toUpperCase());
								level = Integer.parseInt(enchantSection.get(key).toString()) - 1;
							}

							if (en == null) {
								continue;
							}

							finalStack.addUnsafeEnchantment(en, level);
						}
					}
				}
			}

			if (cfgSection.containsKey("name")) {
				String name = ChatColor.translateAlternateColorCodes('&', cfgSection.get("name").toString());
				ItemMeta im = finalStack.getItemMeta();

				im.setDisplayName(name);
				finalStack.setItemMeta(im);
			} else {

				ItemMeta im = finalStack.getItemMeta();
				String name = im.getDisplayName();

				// check if is ressource
				ConfigurationSection ressourceSection = Main.getInstance().getConfig()
						.getConfigurationSection("ressource");
				for (String key : ressourceSection.getKeys(false)) {
					Material ressMaterial = null;
					String itemType = ressourceSection.getString(key + ".item");

					if (Utils.isNumber(itemType)) {
						ressMaterial = Material.getMaterial(Integer.parseInt(itemType));
					} else {
						ressMaterial = Material.getMaterial(itemType);
					}

					if (finalStack.getType().equals(ressMaterial)) {
						name = ChatColor.translateAlternateColorCodes('&', ressourceSection.getString(key + ".name"));
					}
				}

				im.setDisplayName(name);
				finalStack.setItemMeta(im);
			}

			return finalStack;

		} catch (Exception ex) {
			ex.printStackTrace();
		}

		return null;
	}

	public static void openCategorySelection(Player p, Game g) {
		List<MerchantCategory> cats = g.getOrderedItemShopCategories();

		int nom = (cats.size() % 9 == 0) ? 9 : (cats.size() % 9);
		int size = (cats.size() + (9 - nom)) + 9;

		Inventory inv = Bukkit.createInventory(p, size, Main._l("ingame.shop.name"));
		for (MerchantCategory cat : cats) {
			if (p != null) {
				if (!p.hasPermission(cat.getPermission())) {
					continue;
				}
			}

			ItemStack is = new ItemStack(cat.getMaterial(), 1);
			ItemMeta im = is.getItemMeta();

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
			if (trade.getItem1().getType() == Material.AIR && trade.getRewardItem().getType() == Material.AIR) {
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
