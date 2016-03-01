package io.github.yannici.bedwars.Updater;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

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
		Main.getInstance().getConfig().addDefault("sign.third-line",
				"Players &7[&b$currentplayers$&7/&b$maxplayers$&7]");
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
		Main.getInstance().getConfig().addDefault("player-drops", false);
		Main.getInstance().getConfig().addDefault("bungeecord.spigot-restart", true);
		Main.getInstance().getConfig().addDefault("place-in-liquid", true);
		Main.getInstance().getConfig().addDefault("friendlybreak", true);
		Main.getInstance().getConfig().addDefault("breakable-blocks", Arrays.asList("none"));
		Main.getInstance().getConfig().addDefault("update-infos", true);
		Main.getInstance().getConfig().addDefault("lobby-chatformat", "$player$: $msg$");
		// <1.2.0>

		// <1.2.1>
		this.excludeShop();
		Main.getInstance().getConfig().addDefault("statistics.bed-destroyed-kills", false);
		Main.getInstance().getConfig().addDefault("rewards.player-destroy-bed",
				Arrays.asList("/example {player} {score}"));
		Main.getInstance().getConfig().addDefault("rewards.player-kill", Arrays.asList("/example {player} 10"));
		Main.getInstance().getConfig().addDefault("specials.tntsheep.fuse-time", 8.0);
		Main.getInstance().getConfig().addDefault("titles.countdown.enabled", true);
		Main.getInstance().getConfig().addDefault("titles.countdown.format", "&3{countdown}");
		Main.getInstance().getConfig().addDefault("specials.tntsheep.speed", 0.4D);
		// </1.2.1>

		// <1.2.2>
		Main.getInstance().getConfig().addDefault("global-autobalance", false);
		Main.getInstance().getConfig().addDefault("scoreboard.format-bed-destroyed", "&c$status$ $team$");
		Main.getInstance().getConfig().addDefault("scoreboard.format-bed-alive", "&a$status$ $team$");
		Main.getInstance().getConfig().addDefault("scoreboard.format-title", "&e$region$&f - $time$");
		Main.getInstance().getConfig().addDefault("teamname-on-tab", false);
		// </1.2.2>

		// <1.2.3>
		Main.getInstance().getConfig().addDefault("bungeecord.motds.full", "&c[Full]");
		Main.getInstance().getConfig().addDefault("teamname-in-chat", false);
		Main.getInstance().getConfig().addDefault("hearts-on-death", true);
		Main.getInstance().getConfig().addDefault("lobby-scoreboard.title", "&eBEDWARS");
		Main.getInstance().getConfig().addDefault("lobby-scoreboard.enabled", true);
		Main.getInstance().getConfig().addDefault("lobby-scoreboard.content", Arrays.asList("", "&fMap: &2$regionname$",
				"&fPlayers: &2$players$&f/&2$maxplayers$", "", "&fWaiting ...", ""));
		Main.getInstance().getConfig().addDefault("jointeam-entity.show-name", true);
		// </1.2.3>

		// <1.2.6>
		Main.getInstance().getConfig().addDefault("die-on-void", false);
		Main.getInstance().getConfig().addDefault("global-chat-after-end", true);
		// </1.2.6>

		// <1.2.7>
		Main.getInstance().getConfig().addDefault("overwrite-display-names", true);
		Main.getInstance().getConfig().addDefault("holographic-stats.show-prefix", false);
		Main.getInstance().getConfig().addDefault("holographic-stats.name-color", "&7");
		Main.getInstance().getConfig().addDefault("holographic-stats.value-color", "&e");
		Main.getInstance().getConfig().addDefault("holographic-stats.head-line", "Your &eBEDWARS&f stats");
		Main.getInstance().getConfig().addDefault("lobby-gamemode", 0);
		Main.getInstance().getConfig().addDefault("statistics.show-on-game-end", true);
		Main.getInstance().getConfig().addDefault("allow-crafting", false);
		Main.getInstance().getConfig().addDefault("command-prefix", "bw");
		Main.getInstance().getConfig().addDefault("database.connection-pooling.max-pool-size", 50);
		// </1.2.7>

		// <1.2.8>
		Main.getInstance().getConfig().addDefault("specials.tntsheep.explosion-factor", 1.0);
		Main.getInstance().getConfig().addDefault("bungeecord.full-restart", true);
		Main.getInstance().getConfig().addDefault("lobbytime-full", 15);
		Main.getInstance().getConfig().addDefault("bungeecord.endgame-in-lobby", true);
		// </1.2.8>
	}

	private void excludeShop() {
		if (Main.getInstance().getConfig().contains("shop")) {
			ConfigurationSection shop = Main.getInstance().getConfig().getConfigurationSection("shop");

			// move to new file
			File file = new File(Main.getInstance().getDataFolder(), "shop.yml");
			if (file.exists()) {
				// shop exists already, only remove old section
				this.removeShopSection();
				return;
			}

			// file not exists, so create one
			try {
				file.createNewFile();
			} catch (IOException e) {
				// couldn't create file, exit
				e.printStackTrace();
				return;
			}

			YamlConfiguration config = new YamlConfiguration();
			config.set("shop", shop);
			this.saveShopFile(config, file);
			this.removeShopSection();
		}
	}

	private void saveShopFile(YamlConfiguration config, File file) {
		try {
			String data = Main.getInstance().getYamlDump(config);

			FileOutputStream stream = new FileOutputStream(file);
			OutputStreamWriter writer = new OutputStreamWriter(stream, "UTF-8");

			try {
				writer.write(data);
			} finally {
				writer.close();
				stream.close();
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	private void removeShopSection() {
		Main.getInstance().getConfig().set("shop", null);
	}
}
