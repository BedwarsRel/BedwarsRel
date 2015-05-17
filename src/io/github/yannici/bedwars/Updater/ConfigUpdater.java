package io.github.yannici.bedwars.Updater;

import java.util.ArrayList;
import java.util.List;

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
		Main.getInstance().getConfig().addDefault("sign.first-line", "$title$");
		Main.getInstance().getConfig().addDefault("sign.second-line", "$regionname$");
		Main.getInstance().getConfig().addDefault("sign.third-line", "Players &7[&b$currentplayers$&7/&b$maxplayers$&7]");
		Main.getInstance().getConfig().addDefault("sign.fourth-line", "$status$");
		Main.getInstance().getConfig().addDefault("specials.rescue-platform.break-time", 10);
		Main.getInstance().getConfig().addDefault("specials.rescue-platform.using-wait-time", 20);
		Main.getInstance().getConfig().addDefault("explodes.destroy-worldblocks", false);
		Main.getInstance().getConfig().addDefault("explodes.destroy-beds", false);
		Main.getInstance().getConfig().addDefault("explodes.drop-blocking", false);
		Main.getInstance().getConfig().addDefault("rewards.enabled", false);
		
		List<String> defaultRewards = new ArrayList<String>();
		defaultRewards.add("/example {player} {score}");
		Main.getInstance().getConfig().addDefault("rewards.player-win", defaultRewards);
		Main.getInstance().getConfig().addDefault("rewards.player-end-game", defaultRewards);
		// </1.1.4>
		
		// <1.1.6>
		Main.getInstance().getConfig().addDefault("global-messages", true);
		Main.getInstance().getConfig().addDefault("player-settings.one-stack-on-shift", false);
		// </1.1.6>
		
		// <1.1.8>
		Main.getInstance().getConfig().addDefault("seperate-game-chat", true);
		Main.getInstance().getConfig().addDefault("seperate-spectator-chat", false);
		Main.getInstance().getConfig().addDefault("specials.warp-powder.teleport-time", 6);
		// </1.1.8>
	}
}
