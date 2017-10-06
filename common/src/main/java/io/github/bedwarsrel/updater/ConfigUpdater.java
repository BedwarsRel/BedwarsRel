package io.github.bedwarsrel.updater;

import io.github.bedwarsrel.BedwarsRel;
import io.github.bedwarsrel.villager.ItemStackParser;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class ConfigUpdater {

  @SuppressWarnings("unchecked")
  public void addConfigs() {
    // <1.1.3>
    BedwarsRel.getInstance().getConfig().addDefault("check-updates", true);
    // </1.1.3>

    // <1.1.4>
    BedwarsRel.getInstance().getConfig().addDefault("sign.first-line", "$title$");
    BedwarsRel.getInstance().getConfig().addDefault("sign.second-line", "$regionname$");
    BedwarsRel.getInstance().getConfig().addDefault("sign.third-line",
        "Players &7[&b$currentplayers$&7/&b$maxplayers$&7]");
    BedwarsRel.getInstance().getConfig().addDefault("sign.fourth-line", "$status$");
    BedwarsRel.getInstance().getConfig().addDefault("specials.rescue-platform.break-time", 10);
    BedwarsRel.getInstance().getConfig().addDefault("specials.rescue-platform.using-wait-time", 20);
    BedwarsRel.getInstance().getConfig().addDefault("explodes.destroy-worldblocks", false);
    BedwarsRel.getInstance().getConfig().addDefault("explodes.destroy-beds", false);
    BedwarsRel.getInstance().getConfig().addDefault("explodes.drop-blocking", false);
    BedwarsRel.getInstance().getConfig().addDefault("rewards.enabled", false);

    List<String> defaultRewards = new ArrayList<String>();
    defaultRewards.add("/example {player} {score}");
    BedwarsRel.getInstance().getConfig().addDefault("rewards.player-win", defaultRewards);
    BedwarsRel.getInstance().getConfig().addDefault("rewards.player-end-game", defaultRewards);
    // </1.1.4>

    // <1.1.6>
    BedwarsRel.getInstance().getConfig().addDefault("global-messages", true);
    BedwarsRel.getInstance().getConfig().addDefault("player-settings.one-stack-on-shift", false);
    // </1.1.6>

    // <1.1.8>
    BedwarsRel.getInstance().getConfig().addDefault("seperate-game-chat", true);
    BedwarsRel.getInstance().getConfig().addDefault("seperate-spectator-chat", false);
    // </1.1.8>

    // <1.1.9>
    BedwarsRel.getInstance().getConfig().addDefault("specials.trap.play-sound", true);
    // </1.1.9>

    // <1.1.11>
    BedwarsRel.getInstance().getConfig().addDefault("specials.magnetshoe.probability", 75);
    BedwarsRel.getInstance().getConfig().addDefault("specials.magnetshoe.boots", "IRON_BOOTS");
    // </1.1.11>

    // <1.1.13>
    BedwarsRel.getInstance().getConfig().addDefault("specials.rescue-platform.block", "GLASS");
    BedwarsRel.getInstance().getConfig().addDefault("specials.rescue-platform.block", "BLAZE_ROD");
    BedwarsRel.getInstance().getConfig().addDefault("ingame-chatformat-all",
        "[$all$] <$team$>$player$: $msg$");
    BedwarsRel.getInstance().getConfig().addDefault("ingame-chatformat", "<$team$>$player$: $msg$");
    // </1.1.13>

    // <1.1.14>
    BedwarsRel.getInstance().getConfig().addDefault("overwrite-names", false);
    BedwarsRel.getInstance().getConfig().addDefault("specials.protection-wall.break-time", 0);
    BedwarsRel.getInstance().getConfig().addDefault("specials.protection-wall.wait-time", 20);
    BedwarsRel.getInstance().getConfig().addDefault("specials.protection-wall.can-break", true);
    BedwarsRel.getInstance().getConfig().addDefault("specials.protection-wall.item", "BRICK");
    BedwarsRel.getInstance().getConfig().addDefault("specials.protection-wall.block", "SANDSTONE");
    BedwarsRel.getInstance().getConfig().addDefault("specials.protection-wall.width", 4);
    BedwarsRel.getInstance().getConfig().addDefault("specials.protection-wall.height", 4);
    BedwarsRel.getInstance().getConfig().addDefault("specials.protection-wall.distance", 2);

    if (BedwarsRel.getInstance().getCurrentVersion().startsWith("v1_8")) {
      BedwarsRel.getInstance().getConfig().addDefault("bed-sound", "ENDERDRAGON_GROWL");
    } else {
      BedwarsRel.getInstance().getConfig().addDefault("bed-sound", "ENTITY_ENDERDRAGON_GROWL");
    }

    try {
      Sound.valueOf(
          BedwarsRel.getInstance().getStringConfig("bed-sound", "ENDERDRAGON_GROWL").toUpperCase());
    } catch (Exception e) {
      if (BedwarsRel.getInstance().getCurrentVersion().startsWith("v1_8")) {
        BedwarsRel.getInstance().getConfig().set("bed-sound", "ENDERDRAGON_GROWL");
      } else {
        BedwarsRel.getInstance().getConfig().set("bed-sound", "ENTITY_ENDERDRAGON_GROWL");
      }
    }
    // </1.1.14>

    // <1.1.15>
    BedwarsRel.getInstance().getConfig().addDefault("store-game-records", true);
    BedwarsRel.getInstance().getConfig().addDefault("store-game-records-holder", true);
    BedwarsRel.getInstance().getConfig().addDefault("statistics.scores.record", 100);
    BedwarsRel.getInstance().getConfig().addDefault("game-block", "BED_BLOCK");
    // </1.1.15>

    // <1.2.0>
    BedwarsRel.getInstance().getConfig().addDefault("titles.win.enabled", true);
    BedwarsRel.getInstance().getConfig().addDefault("titles.win.title-fade-in", 1.5);
    BedwarsRel.getInstance().getConfig().addDefault("titles.win.title-stay", 5.0);
    BedwarsRel.getInstance().getConfig().addDefault("titles.win.title-fade-out", 2.0);
    BedwarsRel.getInstance().getConfig().addDefault("titles.win.subtitle-fade-in", 1.5);
    BedwarsRel.getInstance().getConfig().addDefault("titles.win.subtitle-stay", 5.0);
    BedwarsRel.getInstance().getConfig().addDefault("titles.win.subtitle-fade-out", 2.0);
    BedwarsRel.getInstance().getConfig().addDefault("titles.map.enabled", false);
    BedwarsRel.getInstance().getConfig().addDefault("titles.map.title-fade-in", 1.5);
    BedwarsRel.getInstance().getConfig().addDefault("titles.map.title-stay", 5.0);
    BedwarsRel.getInstance().getConfig().addDefault("titles.map.title-fade-out", 2.0);
    BedwarsRel.getInstance().getConfig().addDefault("titles.map.subtitle-fade-in", 1.5);
    BedwarsRel.getInstance().getConfig().addDefault("titles.map.subtitle-stay", 5.0);
    BedwarsRel.getInstance().getConfig().addDefault("titles.map.subtitle-fade-out", 2.0);
    BedwarsRel.getInstance().getConfig().addDefault("player-drops", false);
    BedwarsRel.getInstance().getConfig().addDefault("bungeecord.spigot-restart", true);
    BedwarsRel.getInstance().getConfig().addDefault("place-in-liquid", true);
    BedwarsRel.getInstance().getConfig().addDefault("friendlybreak", true);
    BedwarsRel.getInstance().getConfig().addDefault("breakable-blocks", Arrays.asList("none"));
    BedwarsRel.getInstance().getConfig().addDefault("update-infos", true);
    BedwarsRel.getInstance().getConfig().addDefault("lobby-chatformat", "$player$: $msg$");
    // <1.2.0>

    // <1.2.1>
    BedwarsRel.getInstance().getConfig().addDefault("statistics.bed-destroyed-kills", false);
    BedwarsRel.getInstance().getConfig().addDefault("rewards.player-destroy-bed",
        Arrays.asList("/example {player} {score}"));
    BedwarsRel.getInstance().getConfig().addDefault("rewards.player-kill",
        Arrays.asList("/example {player} 10"));
    BedwarsRel.getInstance().getConfig().addDefault("specials.tntsheep.fuse-time", 8.0);
    BedwarsRel.getInstance().getConfig().addDefault("titles.countdown.enabled", true);
    BedwarsRel.getInstance().getConfig().addDefault("titles.countdown.format", "&3{countdown}");
    BedwarsRel.getInstance().getConfig().addDefault("specials.tntsheep.speed", 0.4D);
    // </1.2.1>

    // <1.2.2>
    BedwarsRel.getInstance().getConfig().addDefault("global-autobalance", false);
    BedwarsRel.getInstance().getConfig().addDefault("scoreboard.format-bed-destroyed",
        "&c$status$ $team$");
    BedwarsRel
        .getInstance().getConfig().addDefault("scoreboard.format-bed-alive", "&a$status$ $team$");
    BedwarsRel
        .getInstance().getConfig().addDefault("scoreboard.format-title", "&e$region$&f - $time$");
    BedwarsRel.getInstance().getConfig().addDefault("teamname-on-tab", false);
    // </1.2.2>

    // <1.2.3>
    BedwarsRel.getInstance().getConfig().addDefault("bungeecord.motds.full", "&c[Full]");
    BedwarsRel.getInstance().getConfig().addDefault("teamname-in-chat", false);
    BedwarsRel.getInstance().getConfig().addDefault("hearts-on-death", true);
    BedwarsRel.getInstance().getConfig().addDefault("lobby-scoreboard.title", "&eBEDWARS");
    BedwarsRel.getInstance().getConfig().addDefault("lobby-scoreboard.enabled", true);
    BedwarsRel.getInstance().getConfig().addDefault("lobby-scoreboard.content",
        Arrays.asList("", "&fMap: &2$regionname$", "&fPlayers: &2$players$&f/&2$maxplayers$", "",
            "&fWaiting ...", ""));
    BedwarsRel.getInstance().getConfig().addDefault("jointeam-entity.show-name", true);
    // </1.2.3>

    // <1.2.6>
    BedwarsRel.getInstance().getConfig().addDefault("die-on-void", false);
    BedwarsRel.getInstance().getConfig().addDefault("global-chat-after-end", true);
    // </1.2.6>

    // <1.2.7>
    BedwarsRel.getInstance().getConfig().addDefault("holographic-stats.show-prefix", false);
    BedwarsRel.getInstance().getConfig().addDefault("holographic-stats.name-color", "&7");
    BedwarsRel.getInstance().getConfig().addDefault("holographic-stats.value-color", "&e");
    BedwarsRel.getInstance().getConfig().addDefault("holographic-stats.head-line",
        "Your &eBEDWARS&f stats");
    BedwarsRel.getInstance().getConfig().addDefault("lobby-gamemode", 0);
    BedwarsRel.getInstance().getConfig().addDefault("statistics.show-on-game-end", true);
    BedwarsRel.getInstance().getConfig().addDefault("allow-crafting", false);
    // </1.2.7>

    // <1.2.8>
    BedwarsRel.getInstance().getConfig().addDefault("specials.tntsheep.explosion-factor", 1.0);
    BedwarsRel.getInstance().getConfig().addDefault("bungeecord.full-restart", true);
    BedwarsRel.getInstance().getConfig().addDefault("lobbytime-full", 15);
    BedwarsRel.getInstance().getConfig().addDefault("bungeecord.endgame-in-lobby", true);
    // </1.2.8>

    // <1.3.0>
    BedwarsRel.getInstance().getConfig().addDefault("hearts-in-halfs", true);
    // </1.3.0>

    // <1.3.1>
    if (BedwarsRel.getInstance().getConfig().isString("chat-to-all-prefix")) {
      String chatToAllPrefixString = BedwarsRel.getInstance().getConfig()
          .getString("chat-to-all-prefix");
      BedwarsRel.getInstance().getConfig().set("chat-to-all-prefix",
          Arrays.asList(chatToAllPrefixString));
    }
    if (BedwarsRel.getInstance().getConfig().isList("breakable-blocks")) {
      List<String> breakableBlocks =
          (List<String>) BedwarsRel.getInstance().getConfig().getList("breakable-blocks");
      BedwarsRel.getInstance().getConfig().set("breakable-blocks.list", breakableBlocks);
    }
    BedwarsRel.getInstance().getConfig().addDefault("breakable-blocks.use-as-blacklist", false);
    // </1.3.1>

    // <1.3.2>
    BedwarsRel.getInstance().getConfig().addDefault("statistics.player-leave-kills", false);

    List<PotionEffect> oldPotions = new ArrayList<PotionEffect>();

    if (BedwarsRel.getInstance().getConfig().getBoolean("specials.trap.blindness.enabled")) {
      oldPotions.add(new PotionEffect(PotionEffectType.BLINDNESS,
          BedwarsRel.getInstance().getConfig().getInt("specials.trap.duration"),
          BedwarsRel.getInstance().getConfig().getInt("specials.trap.blindness.amplifier"), true,
          BedwarsRel.getInstance().getConfig().getBoolean("specials.trap.show-particles")));
    }
    if (BedwarsRel.getInstance().getConfig().getBoolean("specials.trap.slowness.enabled")) {
      oldPotions.add(new PotionEffect(PotionEffectType.SLOW,
          BedwarsRel.getInstance().getConfig().getInt("specials.trap.duration"),
          BedwarsRel.getInstance().getConfig().getInt("specials.trap.slowness.amplifier"), true,
          BedwarsRel.getInstance().getConfig().getBoolean("specials.trap.show-particles")));
    }
    if (BedwarsRel.getInstance().getConfig().getBoolean("specials.trap.weakness.enabled")) {
      oldPotions.add(new PotionEffect(PotionEffectType.WEAKNESS,
          BedwarsRel.getInstance().getConfig().getInt("specials.trap.duration"),
          BedwarsRel.getInstance().getConfig().getInt("specials.trap.weakness.amplifier"), true,
          BedwarsRel.getInstance().getConfig().getBoolean("specials.trap.show-particles")));
    }
    BedwarsRel.getInstance().getConfig().addDefault("specials.trap.effects", oldPotions);
    BedwarsRel.getInstance().getConfig().set("specials.trap.duration", null);
    BedwarsRel.getInstance().getConfig().set("specials.trap.blindness", null);
    BedwarsRel.getInstance().getConfig().set("specials.trap.slowness", null);
    BedwarsRel.getInstance().getConfig().set("specials.trap.weakness", null);
    BedwarsRel.getInstance().getConfig().set("specials.trap.show-particles", null);

    List<PotionEffect> potionEffectList = new ArrayList<>();
    potionEffectList.add(new PotionEffect(PotionEffectType.BLINDNESS, 5 * 20, 2, true, true));
    potionEffectList.add(new PotionEffect(PotionEffectType.WEAKNESS, 5 * 20, 2, true, true));
    potionEffectList.add(new PotionEffect(PotionEffectType.SLOW, 5 * 20, 2, true, true));
    BedwarsRel.getInstance().getConfig().addDefault("specials.trap.effects", potionEffectList);
    // </1.3.2>

    // <1.3.3>
    BedwarsRel.getInstance().getConfig().addDefault("show-team-in-actionbar", false);
    BedwarsRel.getInstance().getConfig().addDefault("send-error-data", true);
    BedwarsRel.getInstance().getConfig().addDefault("player-settings.old-shop-as-default", false);
    // </1.3.3>

    // <1.3.4>
    BedwarsRel.getInstance().getConfig().addDefault("keep-inventory-on-death", false);
    BedwarsRel.getInstance().getConfig().addDefault("use-internal-shop", true);
    BedwarsRel.getInstance().getConfig().addDefault("save-inventory", true);
    BedwarsRel.getInstance().getConfig().addDefault("specials.arrow-blocker.protection-time", 10);
    BedwarsRel.getInstance().getConfig().addDefault("specials.arrow-blocker.using-wait-time", 30);
    BedwarsRel.getInstance().getConfig().addDefault("specials.arrow-blocker.item", "ender_eye");
    // </1.3.4>

    // <1.3.5>
    BedwarsRel.getInstance().getConfig().addDefault("spawn-resources-in-chest", true);
    BedwarsRel.getInstance().getConfig().addDefault("database.table-prefix", "bw_");
    Object ressourceObject = BedwarsRel.getInstance().getConfig().get("ressource");
    if (ressourceObject != null) {
      BedwarsRel.getInstance().getConfig().set("resource", ressourceObject);
      BedwarsRel.getInstance().getConfig().set("ressource", null);
    }

    ConfigurationSection resourceSection = BedwarsRel.getInstance().getConfig()
        .getConfigurationSection("resource");
    for (Entry<String, Object> entry : resourceSection.getValues(false).entrySet()) {
      if (!BedwarsRel.getInstance().getConfig().isList("resource." + entry.getKey() + ".item")) {
        ItemStackParser parser = new ItemStackParser(entry.getValue());
        ItemStack item = parser.parse();
        if (item != null) {
          List<Map<String, Object>> itemList = new ArrayList<>();
          itemList.add(item.serialize());
          resourceSection.set(entry.getKey() + ".item", itemList);
          resourceSection.set(entry.getKey() + ".amount", null);
          resourceSection.set(entry.getKey() + ".name", null);
        }
      }
    }
    // </1.3.5>

    // <1.3.7>
    BedwarsRel.getInstance().getConfig().addDefault("specials.rescue-platform.distance", 1);
    // </1.3.7>
  }
}
