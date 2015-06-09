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
		// </1.1.8>
		
		// <1.1.9>
		Main.getInstance().getConfig().addDefault("specials.trap.duration", 10);
		Main.getInstance().getConfig().addDefault("specials.trap.blindness.amplifier", 2);
		Main.getInstance().getConfig().addDefault("specials.trap.slowness.amplifier", 2);
		Main.getInstance().getConfig().addDefault("specials.trap.weakness.amplifier", 2);
		Main.getInstance().getConfig().addDefault("specials.trap.blindness.enabled", true);
		Main.getInstance().getConfig().addDefault("specials.trap.slowness.enabled", true);
		Main.getInstance().getConfig().addDefault("specials.trap.weakness.enabled", true);
		Main.getInstance().getConfig().addDefault("specials.trap.show-particles", true);
		Main.getInstance().getConfig().addDefault("specials.trap.play-sound", true);
		// </1.1.9>
		
		// <1.1.11>
		Main.getInstance().getConfig().addDefault("specials.magnetshoe.probability", 75);
		Main.getInstance().getConfig().addDefault("specials.magnetshoe.boots", "IRON_BOOTS");
		// </1.1.11>
		
		// <1.1.13>
		Main.getInstance().getConfig().addDefault("specials.rescue-platform.block", "GLASS");
		Main.getInstance().getConfig().addDefault("specials.rescue-platform.block", "BLAZE_ROD");
		Main.getInstance().getConfig().addDefault("ingame-chatformat-all", "[$all$] <$team$>$player$: $msg$");
		Main.getInstance().getConfig().addDefault("ingame-chatformat", "<$team$>$player$: $msg$");
		// </1.1.13>
		
		// <1.1.14>
		Main.getInstance().getConfig().addDefault("overwrite-names", false);
		Main.getInstance().getConfig().addDefault("specials.protection-wall.break-time", 0);
		Main.getInstance().getConfig().addDefault("specials.protection-wall.wait-time", 20);
		Main.getInstance().getConfig().addDefault("specials.protection-wall.can-break", true);
		Main.getInstance().getConfig().addDefault("specials.protection-wall.item", "BRICK");
		Main.getInstance().getConfig().addDefault("specials.protection-wall.block", "SANDSTONE");
		Main.getInstance().getConfig().addDefault("specials.protection-wall.width", 4);
		Main.getInstance().getConfig().addDefault("specials.protection-wall.height", 4);
		Main.getInstance().getConfig().addDefault("specials.protection-wall.distance", 2);
		Main.getInstance().getConfig().addDefault("bed-sound", "ENDERDRAGON_GROWL");
		// </1.1.14>
		
		// <1.1.15>
		Main.getInstance().getConfig().addDefault("store-game-records", true);
		Main.getInstance().getConfig().addDefault("store-game-records-holder", true);
		Main.getInstance().getConfig().addDefault("statistics.scores.record", 100);
		Main.getInstance().getConfig().addDefault("game-block", "BED_BLOCK");
		// </1.1.15>
		
		// <1.2.0>
		Main.getInstance().getConfig().addDefault("titles.win.enabled", true);
		Main.getInstance().getConfig().addDefault("titles.win.title", "&6Congratulations!");
		Main.getInstance().getConfig().addDefault("titles.win.subtitle", "&6Team {team}&6 won in &e{time}");
		Main.getInstance().getConfig().addDefault("titles.win.title-fade-in", 1.5);
		Main.getInstance().getConfig().addDefault("titles.win.title-stay", 5.0);
		Main.getInstance().getConfig().addDefault("titles.win.title-fade-out", 2.0);
		Main.getInstance().getConfig().addDefault("titles.win.subtitle-fade-in", 1.5);
		Main.getInstance().getConfig().addDefault("titles.win.subtitle-stay", 5.0);
		Main.getInstance().getConfig().addDefault("titles.win.subtitle-fade-out", 2.0);
		Main.getInstance().getConfig().addDefault("titles.map.enabled", false);
		Main.getInstance().getConfig().addDefault("titles.map.title-fade-in", 1.5);
		Main.getInstance().getConfig().addDefault("titles.map.title-stay", 5.0);
		Main.getInstance().getConfig().addDefault("titles.map.title-fade-out", 2.0);
		Main.getInstance().getConfig().addDefault("titles.map.subtitle-fade-in", 1.5);
		Main.getInstance().getConfig().addDefault("titles.map.subtitle-stay", 5.0);
		Main.getInstance().getConfig().addDefault("titles.map.subtitle-fade-out", 2.0);
		// <1.2.0>
	}
}
