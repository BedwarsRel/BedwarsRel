package io.github.yannici.bedwarsreloaded.Villager;

import java.util.Comparator;

import org.bukkit.configuration.ConfigurationSection;

public class MerchantCategoryComparator implements Comparator<String> {
	
	private ConfigurationSection configSection = null;
	
	public MerchantCategoryComparator(ConfigurationSection section) {
		this.configSection = section;
	}
	
	@Override
	public int compare(String o1, String o2) {
		if(!this.configSection.contains(o1 + ".order")) {
			return 1;
		}
		
		if(!this.configSection.contains(o2 + ".order")) {
			return -1;
		}
		
		if(!this.configSection.isInt(o1 + ".order")) {
			return 1;
		}
		
		if(!this.configSection.isInt(o2 + ".order")) {
			return -1;
		}
		
		int order1 = this.configSection.getInt(o1 + ".order");
		int order2 = this.configSection.getInt(o2 + ".order");
		
		return Integer.valueOf(order1).compareTo(Integer.valueOf(order2));
	}

}
