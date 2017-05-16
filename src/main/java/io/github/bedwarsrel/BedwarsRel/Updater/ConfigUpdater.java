package io.github.bedwarsrel.BedwarsRel.Updater;

import io.github.bedwarsrel.BedwarsRel.Main;
import io.github.bedwarsrel.BedwarsRel.Utils.ChatWriter;
import io.github.bedwarsrel.BedwarsRel.Utils.Utils;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class ConfigUpdater {

  @SuppressWarnings("unchecked")
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
    Main.getInstance().getConfig().addDefault("specials.trap.play-sound", true);
    // </1.1.9>

    // <1.1.11>
    Main.getInstance().getConfig().addDefault("specials.magnetshoe.probability", 75);
    Main.getInstance().getConfig().addDefault("specials.magnetshoe.boots", "IRON_BOOTS");
    // </1.1.11>

    // <1.1.13>
    Main.getInstance().getConfig().addDefault("specials.rescue-platform.block", "GLASS");
    Main.getInstance().getConfig().addDefault("specials.rescue-platform.block", "BLAZE_ROD");
    Main.getInstance().getConfig().addDefault("ingame-chatformat-all",
        "[$all$] <$team$>$player$: $msg$");
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

    if (Main.getInstance().getCurrentVersion().startsWith("v1_8")) {
      Main.getInstance().getConfig().addDefault("bed-sound", "ENDERDRAGON_GROWL");
    } else {
      Main.getInstance().getConfig().addDefault("bed-sound", "ENTITY_ENDERDRAGON_GROWL");
    }

    try {
      Sound.valueOf(
          Main.getInstance().getStringConfig("bed-sound", "ENDERDRAGON_GROWL").toUpperCase());
    } catch (Exception e) {
      if (Main.getInstance().getCurrentVersion().startsWith("v1_8")) {
        Main.getInstance().getConfig().set("bed-sound", "ENDERDRAGON_GROWL");
      } else {
        Main.getInstance().getConfig().set("bed-sound", "ENTITY_ENDERDRAGON_GROWL");
      }
    }
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
    Main.getInstance().getConfig().addDefault("statistics.bed-destroyed-kills", false);
    Main.getInstance().getConfig().addDefault("rewards.player-destroy-bed",
        Arrays.asList("/example {player} {score}"));
    Main.getInstance().getConfig().addDefault("rewards.player-kill",
        Arrays.asList("/example {player} 10"));
    Main.getInstance().getConfig().addDefault("specials.tntsheep.fuse-time", 8.0);
    Main.getInstance().getConfig().addDefault("titles.countdown.enabled", true);
    Main.getInstance().getConfig().addDefault("titles.countdown.format", "&3{countdown}");
    Main.getInstance().getConfig().addDefault("specials.tntsheep.speed", 0.4D);
    // </1.2.1>

    // <1.2.2>
    Main.getInstance().getConfig().addDefault("global-autobalance", false);
    Main.getInstance().getConfig().addDefault("scoreboard.format-bed-destroyed",
        "&c$status$ $team$");
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
    Main.getInstance().getConfig().addDefault("lobby-scoreboard.content",
        Arrays.asList("", "&fMap: &2$regionname$", "&fPlayers: &2$players$&f/&2$maxplayers$", "",
            "&fWaiting ...", ""));
    Main.getInstance().getConfig().addDefault("jointeam-entity.show-name", true);
    // </1.2.3>

    // <1.2.6>
    Main.getInstance().getConfig().addDefault("die-on-void", false);
    Main.getInstance().getConfig().addDefault("global-chat-after-end", true);
    // </1.2.6>

    // <1.2.7>
    Main.getInstance().getConfig().addDefault("holographic-stats.show-prefix", false);
    Main.getInstance().getConfig().addDefault("holographic-stats.name-color", "&7");
    Main.getInstance().getConfig().addDefault("holographic-stats.value-color", "&e");
    Main.getInstance().getConfig().addDefault("holographic-stats.head-line",
        "Your &eBEDWARS&f stats");
    Main.getInstance().getConfig().addDefault("lobby-gamemode", 0);
    Main.getInstance().getConfig().addDefault("statistics.show-on-game-end", true);
    Main.getInstance().getConfig().addDefault("allow-crafting", false);
    // </1.2.7>

    // <1.2.8>
    Main.getInstance().getConfig().addDefault("specials.tntsheep.explosion-factor", 1.0);
    Main.getInstance().getConfig().addDefault("bungeecord.full-restart", true);
    Main.getInstance().getConfig().addDefault("lobbytime-full", 15);
    Main.getInstance().getConfig().addDefault("bungeecord.endgame-in-lobby", true);
    // </1.2.8>

    // <1.3.0>
    Main.getInstance().getConfig().addDefault("hearts-in-halfs", true);
    // </1.3.0>

    // <1.3.1>
    if (Main.getInstance().getConfig().isString("chat-to-all-prefix")) {
      String chatToAllPrefixString = Main.getInstance().getConfig().getString("chat-to-all-prefix");
      Main.getInstance().getConfig().set("chat-to-all-prefix",
          Arrays.asList(chatToAllPrefixString));
    }
    if (Main.getInstance().getConfig().isList("breakable-blocks")) {
      List<String> breakableBlocks =
          (List<String>) Main.getInstance().getConfig().getList("breakable-blocks");
      Main.getInstance().getConfig().set("breakable-blocks.list", breakableBlocks);
    }
    Main.getInstance().getConfig().addDefault("breakable-blocks.use-as-blacklist", false);
    // </1.3.1>

    // <1.3.2>
    Main.getInstance().getConfig().addDefault("statistics.player-leave-kills", false);

    List<PotionEffect> oldPotions = new ArrayList<PotionEffect>();

    if (Main.getInstance().getConfig().getBoolean("specials.trap.blindness.enabled")) {
      oldPotions.add(new PotionEffect(PotionEffectType.BLINDNESS,
          Main.getInstance().getConfig().getInt("specials.trap.duration"),
          Main.getInstance().getConfig().getInt("specials.trap.blindness.amplifier"), true,
          Main.getInstance().getConfig().getBoolean("specials.trap.show-particles")));
    }
    if (Main.getInstance().getConfig().getBoolean("specials.trap.slowness.enabled")) {
      oldPotions.add(new PotionEffect(PotionEffectType.SLOW,
          Main.getInstance().getConfig().getInt("specials.trap.duration"),
          Main.getInstance().getConfig().getInt("specials.trap.slowness.amplifier"), true,
          Main.getInstance().getConfig().getBoolean("specials.trap.show-particles")));
    }
    if (Main.getInstance().getConfig().getBoolean("specials.trap.weakness.enabled")) {
      oldPotions.add(new PotionEffect(PotionEffectType.WEAKNESS,
          Main.getInstance().getConfig().getInt("specials.trap.duration"),
          Main.getInstance().getConfig().getInt("specials.trap.weakness.amplifier"), true,
          Main.getInstance().getConfig().getBoolean("specials.trap.show-particles")));
    }
    Main.getInstance().getConfig().addDefault("specials.trap.effects", oldPotions);
    Main.getInstance().getConfig().set("specials.trap.duration", null);
    Main.getInstance().getConfig().set("specials.trap.blindness", null);
    Main.getInstance().getConfig().set("specials.trap.slowness", null);
    Main.getInstance().getConfig().set("specials.trap.weakness", null);
    Main.getInstance().getConfig().set("specials.trap.show-particles", null);

    List<PotionEffect> potionEffectList = new ArrayList<PotionEffect>();
    potionEffectList.add(new PotionEffect(PotionEffectType.BLINDNESS, 5 * 20, 2, true, true));
    potionEffectList.add(new PotionEffect(PotionEffectType.WEAKNESS, 5 * 20, 2, true, true));
    potionEffectList.add(new PotionEffect(PotionEffectType.SLOW, 5 * 20, 2, true, true));
    Main.getInstance().getConfig().addDefault("specials.trap.effects", potionEffectList);
    // </1.3.2>

    // <1.3.3>
    Main.getInstance().getConfig().addDefault("show-team-in-actionbar", false);
    Main.getInstance().getConfig().addDefault("send-error-data", true);
    Main.getInstance().getConfig().addDefault("player-settings.old-shop-as-default", false);
    // </1.3.3>

    // <1.3.4>
    Main.getInstance().getConfig().addDefault("keep-inventory-on-death", false);
    Main.getInstance().getConfig().addDefault("use-internal-shop", true);
    Main.getInstance().getConfig().addDefault("save-inventory", true);
    Main.getInstance().getConfig().addDefault("specials.arrow-blocker.protection-time", 10);
    Main.getInstance().getConfig().addDefault("specials.arrow-blocker.using-wait-time", 30);
    Main.getInstance().getConfig().addDefault("specials.arrow-blocker.item", "ender_eye");
    // </1.3.4>

    // <1.3.5>
    Main.getInstance().getConfig().addDefault("spawn-resources-in-chest", true);
    Main.getInstance().getConfig().addDefault("database.table-prefix", "bw_");
    // </1.3.5>
  }
}
