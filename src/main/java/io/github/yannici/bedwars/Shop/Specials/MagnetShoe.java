package io.github.yannici.bedwars.Shop.Specials;

import org.bukkit.Material;

import io.github.yannici.bedwars.Main;
import io.github.yannici.bedwars.Utils;

public class MagnetShoe extends SpecialItem {

	public MagnetShoe() {
		super();
	}

	@SuppressWarnings("deprecation")
	@Override
	public Material getItemMaterial() {
		String item = Main.getInstance().getStringConfig("specials.magnetshoe.boots", "IRON_BOOTS");
		Material material = null;
		if (Utils.isNumber(item)) {
			material = Material.getMaterial(Integer.valueOf(item));
		} else {
			material = Material.getMaterial(item);
		}

		if (material == null) {
			return Material.IRON_BOOTS;
		}

		return material;
	}

	@Override
	public Material getActivatedMaterial() {
		return null;
	}

}
