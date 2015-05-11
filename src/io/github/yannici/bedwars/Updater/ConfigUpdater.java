package io.github.yannici.bedwars.Updater;

import io.github.yannici.bedwars.Main;

public class ConfigUpdater {
	
	public ConfigUpdater() {
		super();
	}
	
	public void addConfigs() {
		// <1.1.3>
		Main.getInstance().getConfig().addDefault("check-updates", true);
		// </1.1.3>
		
		// <1.1.4>
		Main.getInstance().getConfig().addDefault("specials.rescue-platform.break-time", 10);
		Main.getInstance().getConfig().addDefault("specials.rescue-platform.using-wait-time", 20);
		Main.getInstance().getConfig().addDefault("tnt.destroy-worldblocks", false);
		Main.getInstance().getConfig().addDefault("tnt.destroy-beds", false);
		Main.getInstance().getConfig().addDefault("tnt.drop-blocks", false);
		// </1.1.4>
	}
}
