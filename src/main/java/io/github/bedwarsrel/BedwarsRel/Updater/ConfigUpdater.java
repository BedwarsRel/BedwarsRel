package io.github.bedwarsrel.BedwarsRel.Updater;

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

import io.github.bedwarsrel.BedwarsRel.Main;
import io.github.bedwarsrel.BedwarsRel.Utils.ChatWriter;
import io.github.bedwarsrel.BedwarsRel.Utils.Utils;

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
      Main.getInstance().getBugsnag().notify(e);
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
    this.excludeShop();
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
    Main.getInstance().getConfig().addDefault("database.connection-pooling.max-pool-size", 50);
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
    // </1.3.4>
  }

  @SuppressWarnings({"unchecked", "deprecation"})
  public void updateShop() {

    File file = new File(Main.getInstance().getDataFolder(), "shop.yml");
    if (!file.exists()) {
      return;
    }

    YamlConfiguration shopConfig = new YamlConfiguration();

    try {
      BufferedReader reader =
          new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF-8"));
      shopConfig.load(reader);
    } catch (Exception e) {
      Main.getInstance().getBugsnag().notify(e);
      Main.getInstance().getServer().getConsoleSender().sendMessage(
          ChatWriter.pluginMessage(ChatColor.RED + "Couldn't load shop! Error in parsing shop!"));
      e.printStackTrace();
    }

    Integer schemaVersion = null;
    if (shopConfig.contains("schema-version")) {
      schemaVersion = shopConfig.getInt("schema-version");
    } else {
      shopConfig.set("schema-version", 0);
      schemaVersion = 0;
    }

    ConfigurationSection oldConfsection = shopConfig.getConfigurationSection("shop");

    if (schemaVersion < 1) {
      for (String cat : oldConfsection.getKeys(false)) {
        for (Object oldOffer : oldConfsection.getList(cat + ".offers")) {
          HashMap<String, Object> offer = new HashMap<String, Object>();
          if (oldOffer instanceof String) {
            continue;
          }

          LinkedHashMap<String, Object> oldOfferSection = (LinkedHashMap<String, Object>) oldOffer;

          if (!oldOfferSection.containsKey("item1") || !oldOfferSection.containsKey("reward")) {
            continue;
          }
          List<Object> costs = new ArrayList<Object>();
          List<Object> rewards = new ArrayList<Object>();

          Object oldRewardSection = oldOfferSection.get("reward");
          ItemStack finalRewardStack = null;

          if (!(oldRewardSection instanceof LinkedHashMap)) {
            continue;
          }

          try {
            LinkedHashMap<String, Object> oldCfgSection =
                (LinkedHashMap<String, Object>) oldRewardSection;

            String materialString = oldCfgSection.get("item").toString();
            Material material = null;
            boolean hasMeta = false;
            boolean hasPotionMeta = false;
            boolean potionIsSplash = false;
            PotionEffectType potionMetaEffectType = null;
            int potionMetaDuration = 1;
            int potionMetaAmplifier = 0;
            byte meta = 0;

            int amount = 1;
            short potionMeta = 0;

            if (Utils.isNumber(materialString)) {
              material = Material.getMaterial(Integer.parseInt(materialString));
            } else {
              material = Material.getMaterial(materialString);
            }

            try {
              if (oldCfgSection.containsKey("amount")) {
                amount = Integer.parseInt(oldCfgSection.get("amount").toString());
              }
            } catch (Exception ex) {
              Main.getInstance().getBugsnag().notify(ex);
              amount = 1;
            }

            if (oldCfgSection.containsKey("meta")) {
              if (!material.equals(Material.POTION)
                  && !((Main.getInstance().getCurrentVersion().startsWith("v1_9")
                      || Main.getInstance().getCurrentVersion().startsWith("v1_10"))
                      && (material.equals(Material.valueOf("TIPPED_ARROW"))
                          || material.equals(Material.valueOf("LINGERING_POTION"))
                          || material.equals(Material.valueOf("SPLASH_POTION"))))) {

                try {
                  meta = Byte.parseByte(oldCfgSection.get("meta").toString());
                  hasMeta = true;
                } catch (Exception ex) {
                  Main.getInstance().getBugsnag().notify(ex);
                  hasMeta = false;
                }
              } else {
                hasPotionMeta = true;
                potionMeta = Short.parseShort(oldCfgSection.get("meta").toString());
                if (potionMeta > 16000) {
                  potionIsSplash = true;
                }
                switch (potionMeta) {
                  case 8193:
                    potionMetaEffectType = PotionEffectType.REGENERATION;
                    potionMetaDuration = 45;
                    potionMetaAmplifier = 0;
                    break;
                  case 8194:
                    potionMetaEffectType = PotionEffectType.SPEED;
                    potionMetaDuration = 3 * 60;
                    potionMetaAmplifier = 0;
                    break;
                  case 8195:
                    potionMetaEffectType = PotionEffectType.FIRE_RESISTANCE;
                    potionMetaDuration = 3 * 60;
                    potionMetaAmplifier = 0;
                    break;
                  case 8196:
                    potionMetaEffectType = PotionEffectType.POISON;
                    potionMetaDuration = 45;
                    potionMetaAmplifier = 0;
                    break;
                  case 8197:
                    potionMetaEffectType = PotionEffectType.HEAL;
                    potionMetaDuration = 1;
                    potionMetaAmplifier = 0;
                    break;
                  case 8198:
                    potionMetaEffectType = PotionEffectType.NIGHT_VISION;
                    potionMetaDuration = 3 * 60;
                    potionMetaAmplifier = 0;
                    break;
                  case 8200:
                    potionMetaEffectType = PotionEffectType.WEAKNESS;
                    potionMetaDuration = 90;
                    potionMetaAmplifier = 0;
                    break;
                  case 8201:
                    potionMetaEffectType = PotionEffectType.INCREASE_DAMAGE;
                    potionMetaDuration = 3 * 60;
                    potionMetaAmplifier = 0;
                    break;
                  case 8202:
                    potionMetaEffectType = PotionEffectType.SLOW;
                    potionMetaDuration = 90;
                    potionMetaAmplifier = 0;
                    break;
                  case 8204:
                    potionMetaEffectType = PotionEffectType.HARM;
                    potionMetaDuration = 1;
                    potionMetaAmplifier = 0;
                    break;
                  case 8205:
                    potionMetaEffectType = PotionEffectType.WATER_BREATHING;
                    potionMetaDuration = 3 * 60;
                    potionMetaAmplifier = 0;
                    break;
                  case 8206:
                    potionMetaEffectType = PotionEffectType.INVISIBILITY;
                    potionMetaDuration = 3 * 60;
                    potionMetaAmplifier = 0;
                    break;
                  case 8225:
                    potionMetaEffectType = PotionEffectType.REGENERATION;
                    potionMetaDuration = 22;
                    potionMetaAmplifier = 1;
                    break;
                  case 8226:
                    potionMetaEffectType = PotionEffectType.SPEED;
                    potionMetaDuration = 90;
                    potionMetaAmplifier = 1;
                    break;
                  case 8228:
                    potionMetaEffectType = PotionEffectType.POISON;
                    potionMetaDuration = 22;
                    potionMetaAmplifier = 1;
                    break;
                  case 8229:
                    potionMetaEffectType = PotionEffectType.HEAL;
                    potionMetaDuration = 1;
                    potionMetaAmplifier = 1;
                    break;
                  case 8233:
                    potionMetaEffectType = PotionEffectType.INCREASE_DAMAGE;
                    potionMetaDuration = 90;
                    potionMetaAmplifier = 1;
                    break;
                  case 8235:
                    potionMetaEffectType = PotionEffectType.JUMP;
                    potionMetaDuration = 90;
                    potionMetaAmplifier = 1;
                    break;
                  case 8236:
                    potionMetaEffectType = PotionEffectType.HARM;
                    potionMetaDuration = 1;
                    potionMetaAmplifier = 1;
                    break;
                  case 8257:
                    potionMetaEffectType = PotionEffectType.REGENERATION;
                    potionMetaDuration = 2 * 60;
                    potionMetaAmplifier = 0;
                    break;
                  case 8258:
                    potionMetaEffectType = PotionEffectType.SPEED;
                    potionMetaDuration = 8 * 60;
                    potionMetaAmplifier = 0;
                    break;
                  case 8259:
                    potionMetaEffectType = PotionEffectType.FIRE_RESISTANCE;
                    potionMetaDuration = 8 * 60;
                    potionMetaAmplifier = 0;
                    break;
                  case 8260:
                    potionMetaEffectType = PotionEffectType.POISON;
                    potionMetaDuration = 2 * 60;
                    potionMetaAmplifier = 0;
                    break;
                  case 8262:
                    potionMetaEffectType = PotionEffectType.NIGHT_VISION;
                    potionMetaDuration = 8 * 60;
                    potionMetaAmplifier = 0;
                    break;
                  case 8264:
                    potionMetaEffectType = PotionEffectType.WEAKNESS;
                    potionMetaDuration = 4 * 60;
                    potionMetaAmplifier = 0;
                    break;
                  case 8265:
                    potionMetaEffectType = PotionEffectType.INCREASE_DAMAGE;
                    potionMetaDuration = 8 * 60;
                    potionMetaAmplifier = 0;
                    break;
                  case 8266:
                    potionMetaEffectType = PotionEffectType.SLOW;
                    potionMetaDuration = 4 * 60;
                    potionMetaAmplifier = 0;
                    break;
                  case 8267:
                    potionMetaEffectType = PotionEffectType.JUMP;
                    potionMetaDuration = 3 * 60;
                    potionMetaAmplifier = 0;
                    break;
                  case 8269:
                    potionMetaEffectType = PotionEffectType.WATER_BREATHING;
                    potionMetaDuration = 8 * 60;
                    potionMetaAmplifier = 0;
                    break;
                  case 8270:
                    potionMetaEffectType = PotionEffectType.INVISIBILITY;
                    potionMetaDuration = 8 * 60;
                    potionMetaAmplifier = 0;
                    break;
                  case 8289:
                    potionMetaEffectType = PotionEffectType.REGENERATION;
                    potionMetaDuration = 60;
                    potionMetaAmplifier = 1;
                    break;
                  case 8290:
                    potionMetaEffectType = PotionEffectType.SPEED;
                    potionMetaDuration = 4 * 60;
                    potionMetaAmplifier = 1;
                    break;
                  case 8292:
                    potionMetaEffectType = PotionEffectType.POISON;
                    potionMetaDuration = 60;
                    potionMetaAmplifier = 1;
                    break;
                  case 8297:
                    potionMetaEffectType = PotionEffectType.INCREASE_DAMAGE;
                    potionMetaDuration = 4 * 60;
                    potionMetaAmplifier = 1;
                    break;
                  case 16385:
                    potionMetaEffectType = PotionEffectType.REGENERATION;
                    potionMetaDuration = 33;
                    potionMetaAmplifier = 0;
                    break;
                  case 16386:
                    potionMetaEffectType = PotionEffectType.SPEED;
                    potionMetaDuration = 135;
                    potionMetaAmplifier = 0;
                    break;
                  case 16387:
                    potionMetaEffectType = PotionEffectType.FIRE_RESISTANCE;
                    potionMetaDuration = 135;
                    potionMetaAmplifier = 0;
                    break;
                  case 16388:
                    potionMetaEffectType = PotionEffectType.POISON;
                    potionMetaDuration = 33;
                    potionMetaAmplifier = 0;
                    break;
                  case 16389:
                    potionMetaEffectType = PotionEffectType.HEAL;
                    potionMetaDuration = 1;
                    potionMetaAmplifier = 0;
                    break;
                  case 16390:
                    potionMetaEffectType = PotionEffectType.NIGHT_VISION;
                    potionMetaDuration = 135;
                    potionMetaAmplifier = 0;
                    break;
                  case 16392:
                    potionMetaEffectType = PotionEffectType.WEAKNESS;
                    potionMetaDuration = 67;
                    potionMetaAmplifier = 0;
                    break;
                  case 16393:
                    potionMetaEffectType = PotionEffectType.INCREASE_DAMAGE;
                    potionMetaDuration = 135;
                    potionMetaAmplifier = 0;
                    break;
                  case 16394:
                    potionMetaEffectType = PotionEffectType.SLOW;
                    potionMetaDuration = 67;
                    potionMetaAmplifier = 0;
                    break;
                  case 16396:
                    potionMetaEffectType = PotionEffectType.HARM;
                    potionMetaDuration = 1;
                    potionMetaAmplifier = 0;
                    break;
                  case 16397:
                    potionMetaEffectType = PotionEffectType.WATER_BREATHING;
                    potionMetaDuration = 135;
                    potionMetaAmplifier = 0;
                    break;
                  case 16398:
                    potionMetaEffectType = PotionEffectType.INVISIBILITY;
                    potionMetaDuration = 135;
                    potionMetaAmplifier = 0;
                    break;
                  case 16417:
                    potionMetaEffectType = PotionEffectType.REGENERATION;
                    potionMetaDuration = 16;
                    potionMetaAmplifier = 1;
                    break;
                  case 16418:
                    potionMetaEffectType = PotionEffectType.SPEED;
                    potionMetaDuration = 67;
                    potionMetaAmplifier = 1;
                    break;
                  case 16420:
                    potionMetaEffectType = PotionEffectType.POISON;
                    potionMetaDuration = 16;
                    potionMetaAmplifier = 1;
                    break;
                  case 16421:
                    potionMetaEffectType = PotionEffectType.HEAL;
                    potionMetaDuration = 1;
                    potionMetaAmplifier = 1;
                    break;
                  case 16425:
                    potionMetaEffectType = PotionEffectType.INCREASE_DAMAGE;
                    potionMetaDuration = 67;
                    potionMetaAmplifier = 1;
                    break;
                  case 16427:
                    potionMetaEffectType = PotionEffectType.JUMP;
                    potionMetaDuration = 67;
                    potionMetaAmplifier = 1;
                    break;
                  case 16428:
                    potionMetaEffectType = PotionEffectType.HARM;
                    potionMetaDuration = 1;
                    potionMetaAmplifier = 1;
                    break;
                  case 16449:
                    potionMetaEffectType = PotionEffectType.REGENERATION;
                    potionMetaDuration = 90;
                    potionMetaAmplifier = 0;
                    break;
                  case 16450:
                    potionMetaEffectType = PotionEffectType.SPEED;
                    potionMetaDuration = 6 * 60;
                    potionMetaAmplifier = 0;
                    break;
                  case 16451:
                    potionMetaEffectType = PotionEffectType.FIRE_RESISTANCE;
                    potionMetaDuration = 6 * 60;
                    potionMetaAmplifier = 0;
                    break;
                  case 16452:
                    potionMetaEffectType = PotionEffectType.POISON;
                    potionMetaDuration = 90;
                    potionMetaAmplifier = 0;
                    break;
                  case 16454:
                    potionMetaEffectType = PotionEffectType.NIGHT_VISION;
                    potionMetaDuration = 6 * 60;
                    potionMetaAmplifier = 0;
                    break;
                  case 16456:
                    potionMetaEffectType = PotionEffectType.WEAKNESS;
                    potionMetaDuration = 3 * 60;
                    potionMetaAmplifier = 0;
                    break;
                  case 16457:
                    potionMetaEffectType = PotionEffectType.INCREASE_DAMAGE;
                    potionMetaDuration = 6 * 60;
                    potionMetaAmplifier = 0;
                    break;
                  case 16458:
                    potionMetaEffectType = PotionEffectType.SLOW;
                    potionMetaDuration = 3 * 60;
                    potionMetaAmplifier = 0;
                    break;
                  case 16459:
                    potionMetaEffectType = PotionEffectType.JUMP;
                    potionMetaDuration = 135;
                    potionMetaAmplifier = 0;
                    break;
                  case 16461:
                    potionMetaEffectType = PotionEffectType.WATER_BREATHING;
                    potionMetaDuration = 6 * 60;
                    potionMetaAmplifier = 0;
                    break;
                  case 16462:
                    potionMetaEffectType = PotionEffectType.INVISIBILITY;
                    potionMetaDuration = 6 * 60;
                    potionMetaAmplifier = 0;
                    break;
                  case 16481:
                    potionMetaEffectType = PotionEffectType.REGENERATION;
                    potionMetaDuration = 45;
                    potionMetaAmplifier = 1;
                    break;
                  case 16482:
                    potionMetaEffectType = PotionEffectType.SPEED;
                    potionMetaDuration = 3 * 60;
                    potionMetaAmplifier = 1;
                    break;
                  case 16484:
                    potionMetaEffectType = PotionEffectType.POISON;
                    potionMetaDuration = 45;
                    potionMetaAmplifier = 1;
                    break;
                  case 16489:
                    potionMetaEffectType = PotionEffectType.INCREASE_DAMAGE;
                    potionMetaDuration = 3 * 60;
                    potionMetaAmplifier = 1;
                    break;
                  default:
                    break;
                }
              }
            }

            if (hasMeta) {
              if (material.equals(Material.MONSTER_EGG) && meta == 91
                  && (Main.getInstance().getCurrentVersion().startsWith("v1_9")
                      || Main.getInstance().getCurrentVersion().startsWith("v1_10"))) {
                if (Main.getInstance().getCurrentVersion().equalsIgnoreCase("v1_9_R1")) {
                  finalRewardStack =
                      new io.github.bedwarsrel.BedwarsRel.Com.v1_9_R1.SpawnEgg1_9(EntityType.SHEEP)
                          .toItemStack(amount);
                } else if (Main.getInstance().getCurrentVersion().equalsIgnoreCase("v1_9_R2")) {
                  finalRewardStack =
                      new io.github.bedwarsrel.BedwarsRel.Com.v1_9_R2.SpawnEgg1_9(EntityType.SHEEP)
                          .toItemStack(amount);
                } else if (Main.getInstance().getCurrentVersion().equalsIgnoreCase("v1_10_R1")) {
                  finalRewardStack = new io.github.bedwarsrel.BedwarsRel.Com.v1_10_R1.SpawnEgg1_10(
                      EntityType.SHEEP).toItemStack(amount);
                }
              } else {
                finalRewardStack = new ItemStack(material, amount, meta);
              }
            } else if (hasPotionMeta) {
              if ((Main.getInstance().getCurrentVersion().startsWith("v1_9")
                  || Main.getInstance().getCurrentVersion().startsWith("v1_10"))
                  && potionIsSplash) {
                finalRewardStack = new ItemStack(Material.valueOf("SPLASH_POTION"), amount);
              } else {
                finalRewardStack = new ItemStack(material, amount);
              }

            } else {
              finalRewardStack = new ItemStack(material, amount);
            }

            if (oldCfgSection.containsKey("lore")) {
              List<String> lores = new ArrayList<String>();
              ItemMeta im = finalRewardStack.getItemMeta();

              for (Object lore : (List<String>) oldCfgSection.get("lore")) {
                lores.add(ChatColor.translateAlternateColorCodes('&', lore.toString()));
              }

              im.setLore(lores);
              finalRewardStack.setItemMeta(im);
            }

            if (material.equals(Material.POTION)
                || ((Main.getInstance().getCurrentVersion().startsWith("v1_9")
                    || Main.getInstance().getCurrentVersion().startsWith("v1_10"))
                    && (material.equals(Material.valueOf("TIPPED_ARROW"))
                        || material.equals(Material.valueOf("LINGERING_POTION"))
                        || material.equals(Material.valueOf("SPLASH_POTION"))))) {

              if (!hasPotionMeta && (oldCfgSection.containsKey("effects"))) {
                PotionMeta customPotionMeta = (PotionMeta) finalRewardStack.getItemMeta();
                for (Object potionEffect : (List<Object>) oldCfgSection.get("effects")) {
                  LinkedHashMap<String, Object> potionEffectSection =
                      (LinkedHashMap<String, Object>) potionEffect;
                  if (!potionEffectSection.containsKey("type")) {
                    continue;
                  }

                  PotionEffectType potionEffectType = null;
                  int duration = 1;
                  int amplifier = 0;

                  potionEffectType = PotionEffectType
                      .getByName(potionEffectSection.get("type").toString().toUpperCase());

                  if (potionEffectSection.containsKey("duration")) {
                    duration =
                        Integer.parseInt(potionEffectSection.get("duration").toString()) * 20;
                  }

                  if (potionEffectSection.containsKey("amplifier")) {
                    amplifier =
                        Integer.parseInt(potionEffectSection.get("amplifier").toString()) - 1;
                  }

                  if (potionEffectType == null) {
                    continue;
                  }

                  customPotionMeta.addCustomEffect(
                      new PotionEffect(potionEffectType, duration, amplifier), true);
                }
                finalRewardStack.setItemMeta(customPotionMeta);
              }
              if (hasPotionMeta) {
                PotionMeta customPotionMeta = (PotionMeta) finalRewardStack.getItemMeta();
                if (potionMetaDuration != 1) {
                  potionMetaDuration = potionMetaDuration * 20;
                }
                customPotionMeta.addCustomEffect(
                    new PotionEffect(potionMetaEffectType, potionMetaDuration, potionMetaAmplifier),
                    true);
                finalRewardStack.setItemMeta(customPotionMeta);
              }
            }

            if (oldCfgSection.containsKey("enchants")) {
              Object cfgEnchants = oldCfgSection.get("enchants");

              if (cfgEnchants instanceof LinkedHashMap) {
                LinkedHashMap<Object, Object> enchantSection =
                    (LinkedHashMap<Object, Object>) cfgEnchants;
                for (Object sKey : enchantSection.keySet()) {
                  String key = sKey.toString();

                  if (!finalRewardStack.getType().equals(Material.POTION)
                      && !((Main.getInstance().getCurrentVersion().startsWith("v1_9")
                          || Main.getInstance().getCurrentVersion().startsWith("v1_10"))
                          && (finalRewardStack.getType().equals(Material.valueOf("TIPPED_ARROW"))
                              || finalRewardStack.getType()
                                  .equals(Material.valueOf("LINGERING_POTION"))
                              || finalRewardStack.getType()
                                  .equals(Material.valueOf("SPLASH_POTION"))))) {
                    Enchantment en = null;
                    int level = 0;

                    if (Utils.isNumber(key)) {
                      en = Enchantment.getById(Integer.parseInt(key));
                      level =
                          Integer.parseInt(enchantSection.get(Integer.parseInt(key)).toString());
                    } else {
                      en = Enchantment.getByName(key.toUpperCase());
                      level = Integer.parseInt(enchantSection.get(key).toString()) - 1;
                    }

                    if (en == null) {
                      continue;
                    }

                    finalRewardStack.addUnsafeEnchantment(en, level);
                  }
                }
              }
            }

            if (oldCfgSection.containsKey("name")) {
              String name =
                  ChatColor.translateAlternateColorCodes('&', oldCfgSection.get("name").toString());
              ItemMeta im = finalRewardStack.getItemMeta();

              im.setDisplayName(name);
              finalRewardStack.setItemMeta(im);
            } else {

              ItemMeta im = finalRewardStack.getItemMeta();
              String name = im.getDisplayName();

              // check if is ressource
              ConfigurationSection ressourceSection =
                  Main.getInstance().getConfig().getConfigurationSection("ressource");
              for (String key : ressourceSection.getKeys(false)) {
                Material ressMaterial = null;
                String itemType = ressourceSection.getString(key + ".item");

                if (Utils.isNumber(itemType)) {
                  ressMaterial = Material.getMaterial(Integer.parseInt(itemType));
                } else {
                  ressMaterial = Material.getMaterial(itemType);
                }

                if (finalRewardStack.getType().equals(ressMaterial)) {
                  name = ChatColor.translateAlternateColorCodes('&',
                      ressourceSection.getString(key + ".name"));
                }
              }

              if (finalRewardStack.getType().equals(Material.POTION) || ((Main.getInstance()
                  .getCurrentVersion().startsWith("v1_9")
                  || Main.getInstance().getCurrentVersion().startsWith("v1_10"))
                  && (finalRewardStack.getType().equals(Material.valueOf("LINGERING_POTION"))
                      || finalRewardStack.getType().equals(Material.valueOf("SPLASH_POTION"))))) {
                PotionMeta finalRewardStackPotionMeta = (PotionMeta) finalRewardStack.getItemMeta();
                if (finalRewardStackPotionMeta.getCustomEffects().size() >= 1) {
                  String effectName =
                      finalRewardStackPotionMeta.getCustomEffects().get(0).getType().getName();
                  name = "";
                  String[] effectNameParts = effectName.split("_");
                  for (String effectNamePart : effectNameParts) {
                    name = name + effectNamePart.substring(0, 1).toUpperCase()
                        + effectNamePart.substring(1).toLowerCase() + " ";
                  }
                  name = name
                      + (finalRewardStackPotionMeta.getCustomEffects().get(0).getAmplifier() + 1);
                }
              }

              im.setDisplayName(name);
              finalRewardStack.setItemMeta(im);
            }

          } catch (Exception ex) {
            Main.getInstance().getBugsnag().notify(ex);
            ex.printStackTrace();
          }

          if (finalRewardStack == null) {
            continue;
          }

          rewards.add(finalRewardStack.serialize());
          offer.put("reward", rewards);

          Object oldCostSection = oldOfferSection.get("item1");
          ItemStack finalCostStack = null;

          if (!(oldCostSection instanceof LinkedHashMap)) {
            continue;
          }

          try {
            LinkedHashMap<String, Object> oldCfgSection =
                (LinkedHashMap<String, Object>) oldCostSection;

            String materialString = oldCfgSection.get("item").toString();
            Material material = null;
            int amount = 1;

            if (Utils.isNumber(materialString)) {
              material = Material.getMaterial(Integer.parseInt(materialString));
            } else {
              material = Material.getMaterial(materialString);
            }

            try {
              if (oldCfgSection.containsKey("amount")) {
                amount = Integer.parseInt(oldCfgSection.get("amount").toString());
              }
            } catch (Exception ex) {
              Main.getInstance().getBugsnag().notify(ex);
              amount = 1;
            }

            finalCostStack = new ItemStack(material, amount);

          } catch (Exception ex) {
            Main.getInstance().getBugsnag().notify(ex);
            ex.printStackTrace();
          }

          if (finalCostStack == null) {
            continue;
          }

          costs.add(finalCostStack.serialize());

          oldCostSection = oldOfferSection.get("item2");
          finalCostStack = null;

          if (oldCostSection instanceof LinkedHashMap) {
            try {
              LinkedHashMap<String, Object> oldCfgSection =
                  (LinkedHashMap<String, Object>) oldCostSection;

              String materialString = oldCfgSection.get("item").toString();
              Material material = null;
              int amount = 1;

              if (Utils.isNumber(materialString)) {
                material = Material.getMaterial(Integer.parseInt(materialString));
              } else {
                material = Material.getMaterial(materialString);
              }

              try {
                if (oldCfgSection.containsKey("amount")) {
                  amount = Integer.parseInt(oldCfgSection.get("amount").toString());
                }
              } catch (Exception ex) {
                Main.getInstance().getBugsnag().notify(ex);
                amount = 1;
              }

              finalCostStack = new ItemStack(material, amount);

            } catch (Exception ex) {
              Main.getInstance().getBugsnag().notify(ex);
              ex.printStackTrace();
            }

            if (finalCostStack == null) {
              continue;
            }

            costs.add(finalCostStack.serialize());
          }

          oldOfferSection.clear();
          oldOfferSection.put("reward", rewards);
          oldOfferSection.put("price", costs);

        }
      }
      shopConfig.set("schema-version", 1);
      schemaVersion = 1;
    }

    // Save shop in UTF-8
    this.saveShopFile(shopConfig, file);
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
        Main.getInstance().getBugsnag().notify(e);
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
      Main.getInstance().getBugsnag().notify(ex);
      ex.printStackTrace();
    }
  }

  private void removeShopSection() {
    Main.getInstance().getConfig().set("shop", null);
  }
}
