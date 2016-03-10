package io.github.yannici.bedwars.Game;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.SerializableAs;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Item;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionEffectType;

import io.github.yannici.bedwars.Main;
import io.github.yannici.bedwars.Utils;

@SerializableAs("RessourceSpawner")
public class RessourceSpawner implements Runnable, ConfigurationSerializable {

	private Game game = null;
	private Location location = null;
	private int interval = 1000;
	private ItemStack itemstack = null;
	private String name = null;
	private double spread = 1.0;

	public RessourceSpawner(Map<String, Object> deserialize) {
		this.location = Utils.locationDeserialize(deserialize.get("location"));

		if (deserialize.containsKey("name")) {
			this.name = deserialize.get("name").toString();

			if (!Main.getInstance().getConfig().contains("ressource." + this.name)) {
				this.itemstack = (ItemStack) deserialize.get("itemstack");
				this.interval = Integer.parseInt(deserialize.get("interval").toString());
				if (deserialize.containsKey("spread")) {
					this.spread = Double.parseDouble(deserialize.get("spread").toString());
				}
			} else {
				this.itemstack = RessourceSpawner
						.createSpawnerStackByConfig(Main.getInstance().getConfig().get("ressource." + this.name));
				this.interval = Main.getInstance().getIntConfig("ressource." + this.name + ".spawn-interval", 1000);
				this.spread = Main.getInstance().getConfig().getDouble("ressource." + this.name + ".spread", 1.0);
			}
		} else {
			ItemStack stack = (ItemStack) deserialize.get("itemstack");
			this.name = this.getNameByMaterial(stack.getType());

			if (this.name == null) {
				this.itemstack = stack;
				this.interval = Integer.parseInt(deserialize.get("interval").toString());
				if (deserialize.containsKey("spread")) {
					this.spread = Double.parseDouble(deserialize.get("spread").toString());
				}
			} else {
				this.itemstack = RessourceSpawner
						.createSpawnerStackByConfig(Main.getInstance().getConfig().get("ressource." + this.name));
				this.interval = Main.getInstance().getIntConfig("ressource." + this.name + ".spawn-interval", 1000);
				this.spread = Main.getInstance().getConfig().getDouble("ressource." + this.name + ".spread", 1.0);
			}
		}
	}

	public RessourceSpawner(Game game, String name, Location location) {
		this.game = game;
		this.name = name;
		this.interval = Main.getInstance().getIntConfig("ressource." + this.name + ".spawn-interval", 1000);
		this.location = location;
		this.itemstack = RessourceSpawner
				.createSpawnerStackByConfig(Main.getInstance().getConfig().get("ressource." + this.name));
		;
		this.spread = Main.getInstance().getConfig().getDouble("ressource." + this.name + ".spread", 1.0);
	}

	private String getNameByMaterial(Material material) {
		for (String key : Main.getInstance().getConfig().getConfigurationSection("ressource").getKeys(true)) {
			ConfigurationSection keySection = Main.getInstance().getConfig()
					.getConfigurationSection("ressource." + key);
			if (keySection == null) {
				continue;
			}

			if (!keySection.contains("item")) {
				continue;
			}

			Material mat = Utils.parseMaterial(keySection.getString("item"));
			if (mat.equals(material)) {
				return key;
			}
		}

		return null;
	}

	public int getInterval() {
		return this.interval;
	}

	public void setGame(Game game) {
		this.game = game;
	}

	@Override
	public void run() {
		Location dropLocation = this.location;
		Item item = this.game.getRegion().getWorld().dropItemNaturally(dropLocation, this.itemstack);
		item.setPickupDelay(0);

		if (this.spread != 1.0) {
			item.setVelocity(item.getVelocity().multiply(this.spread));
		}
	}

	@Override
	public Map<String, Object> serialize() {
		HashMap<String, Object> rs = new HashMap<>();

		rs.put("location", Utils.locationSerialize(this.location));
		rs.put("name", this.name);
		return rs;
	}

	public ItemStack getItemStack() {
		return this.itemstack;
	}

	public Location getLocation() {
		return this.location;
	}

	@SuppressWarnings({ "deprecation", "unchecked" })
	public static ItemStack createSpawnerStackByConfig(Object section) {
		LinkedHashMap<String, Object> linkedMap = new LinkedHashMap<String, Object>();

		if (!(section instanceof LinkedHashMap)) {
			ConfigurationSection newSection = (ConfigurationSection) section;
			for (String key : newSection.getKeys(false)) {
				linkedMap.put(key, newSection.get(key));
			}
		} else {
			linkedMap = (LinkedHashMap<String, Object>) section;
		}

		try {
			LinkedHashMap<String, Object> cfgSection = linkedMap;

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

			if (material.equals(Material.POTION) || (Main.getInstance().getCurrentVersion().startsWith("v1_9")
					&& material.equals(Material.valueOf("TIPPED_ARROW")))) {
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
					LinkedHashMap<Object, Object> enchantSection = (LinkedHashMap<Object, Object>) cfgEnchants;
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
			}

			return finalStack;

		} catch (Exception ex) {
			ex.printStackTrace();
		}

		return null;
	}

}