package io.github.yannici.bedwars;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemorySection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;

import io.github.yannici.bedwars.Game.Game;
import io.github.yannici.bedwars.Game.Team;

public final class Utils {

  public static String implode(String glue, ArrayList<String> strings) {
    if (strings.isEmpty()) {
      return "";
    }

    StringBuilder builder = new StringBuilder();
    builder.append(strings.remove(0));

    for (String str : strings) {
      builder.append(glue);
      builder.append(str);
    }

    return builder.toString();
  }

  public static void equipArmorStand(LivingEntity armor, Team team) {
    if (!(armor instanceof ArmorStand)) {
      return;
    }

    ArmorStand stand = (ArmorStand) armor;

    // helmet
    ItemStack helmet = new ItemStack(Material.LEATHER_HELMET, 1);
    LeatherArmorMeta meta = (LeatherArmorMeta) helmet.getItemMeta();
    meta.setColor(team.getColor().getColor());
    helmet.setItemMeta(meta);

    // chestplate
    ItemStack chestplate = new ItemStack(Material.LEATHER_CHESTPLATE, 1);
    meta = (LeatherArmorMeta) chestplate.getItemMeta();
    meta.setColor(team.getColor().getColor());
    chestplate.setItemMeta(meta);

    // leggings
    ItemStack leggings = new ItemStack(Material.LEATHER_LEGGINGS, 1);
    meta = (LeatherArmorMeta) leggings.getItemMeta();
    meta.setColor(team.getColor().getColor());
    leggings.setItemMeta(meta);

    // boots
    ItemStack boots = new ItemStack(Material.LEATHER_BOOTS, 1);
    meta = (LeatherArmorMeta) boots.getItemMeta();
    meta.setColor(team.getColor().getColor());
    boots.setItemMeta(meta);

    stand.setHelmet(helmet);
    stand.setChestplate(chestplate);
    stand.setLeggings(leggings);
    stand.setBoots(boots);
  }

  public static void createParticleInGame(Game game, String particle, Location loc) {
    try {
      Class<?> clazz = Class.forName("io.github.yannici.bedwars.Com."
          + Main.getInstance().getCurrentVersion() + ".ParticleSpawner");

      Method particleMethod = clazz.getDeclaredMethod("spawnParticle", List.class, String.class,
          float.class, float.class, float.class);
      particleMethod.invoke(null, game.getPlayers(), particle, (float) loc.getX(),
          (float) loc.getY(), (float) loc.getZ());
    } catch (Exception ex) {
      try {
        Class<?> clazz = Class.forName("io.github.yannici.bedwars.Com.Fallback.ParticleSpawner");
        Method particleMethod = clazz.getDeclaredMethod("spawnParticle", List.class, String.class,
            float.class, float.class, float.class);
        particleMethod.invoke(null, game.getPlayers(), particle, (float) loc.getX(),
            (float) loc.getY(), (float) loc.getZ());
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  }

  public static Location getDirectionLocation(Location location, int blockOffset) {
    Location loc = location.clone();
    return loc.add(loc.getDirection().setY(0).normalize().multiply(blockOffset));
  }

  public static Block getBedNeighbor(Block head) {
    if (Utils.isBedBlock(head.getRelative(BlockFace.EAST))) {
      return head.getRelative(BlockFace.EAST);
    } else if (Utils.isBedBlock(head.getRelative(BlockFace.WEST))) {
      return head.getRelative(BlockFace.WEST);
    } else if (Utils.isBedBlock(head.getRelative(BlockFace.SOUTH))) {
      return head.getRelative(BlockFace.SOUTH);
    } else {
      return head.getRelative(BlockFace.NORTH);
    }
  }

  public static boolean isBedBlock(Block isBed) {
    if (isBed == null) {
      return false;
    }

    return (isBed.getType() == Material.BED || isBed.getType() == Material.BED_BLOCK);
  }

  @SuppressWarnings("deprecation")
  public static Material getMaterialByConfig(String key, Material defaultMaterial) {
    try {
      String cfg = Main.getInstance().getStringConfig(key, defaultMaterial.name());
      if (Utils.isNumber(cfg)) {
        return Material.getMaterial(Integer.valueOf(cfg));
      } else {
        return Material.getMaterial(cfg.toUpperCase());
      }
    } catch (Exception ex) {
      // just return default
    }

    return defaultMaterial;
  }

  public static Object getCraftPlayer(Player player) {
    try {
      Class<?> craftPlayerClass = Main.getInstance().getCraftBukkitClass("entity.CraftPlayer");
      Method getHandle = craftPlayerClass.getMethod("getHandle", new Class[] {});
      getHandle.setAccessible(true);

      return getHandle.invoke(player, new Object[] {});
    } catch (Exception e) {
      return null;
    }
  }

  public static boolean isNumber(String numberString) {
    try {
      Integer.parseInt(numberString);
      return true;
    } catch (Exception ex) {
      return false;
    }
  }

  public static Method getColorableMethod(Material mat) {
    try {
      ItemStack tempStack = new ItemStack(mat, 1);
      Method method =
          tempStack.getItemMeta().getClass().getMethod("setColor", new Class[] {Color.class});
      if (method != null) {
        return method;
      }
    } catch (Exception ex) {
      // it's no error
    }

    return null;
  }

  public static boolean isColorable(ItemStack itemstack) {
    if (itemstack.getType().equals(Material.STAINED_CLAY)
        || itemstack.getType().equals(Material.WOOL) || itemstack.getType().equals(Material.CARPET)
        || itemstack.getType().equals(Material.STAINED_GLASS)
        || itemstack.getType().equals(Material.STAINED_GLASS_PANE)) {
      return true;
    }
    return false;
  }

  public static boolean checkBungeePlugin() {
    try {
      Class.forName("net.md_5.bungee.BungeeCord");
      return true;
    } catch (Exception e) {
    }

    return false;
  }

  @SuppressWarnings("resource")
  public static String[] getResourceListing(Class<?> clazz, String path)
      throws URISyntaxException, IOException {
    URL dirURL = clazz.getClassLoader().getResource(path);
    if (dirURL != null && dirURL.getProtocol().equals("file")) {
      /* A file path: easy enough */
      return new File(dirURL.toURI()).list();
    }

    if (dirURL == null) {
      /*
       * In case of a jar file, we can't actually find a directory. Have to assume the same jar as
       * clazz.
       */
      String me = clazz.getName().replace(".", "/") + ".class";
      dirURL = clazz.getClassLoader().getResource(me);
    }

    if (dirURL.getProtocol().equals("jar")) {
      /* A JAR path */
      String jarPath = dirURL.getPath().substring(5, dirURL.getPath().indexOf("!")); // strip
                                                                                     // out
                                                                                     // only
                                                                                     // the
                                                                                     // JAR
                                                                                     // file
      JarFile jar = new JarFile(URLDecoder.decode(jarPath, "UTF-8"));
      Enumeration<JarEntry> entries = jar.entries(); // gives ALL entries
                                                     // in jar
      Set<String> result = new HashSet<String>(); // avoid duplicates in
                                                  // case it is a
                                                  // subdirectory
      while (entries.hasMoreElements()) {
        String name = entries.nextElement().getName();
        if (name.startsWith(path)) { // filter according to the path
          String entry = name.substring(path.length());
          int checkSubdir = entry.indexOf("/");
          if (checkSubdir >= 0) {
            // if it is a subdirectory, we just return the directory
            // name
            entry = entry.substring(0, checkSubdir);
          }
          result.add(entry);
        }
      }
      return result.toArray(new String[result.size()]);
    }

    throw new UnsupportedOperationException("Cannot list files for URL " + dirURL);
  }

  public static int randInt(int min, int max) {
    Random rand = new Random();
    int randomNum = rand.nextInt((max - min) + 1) + min;

    return randomNum;
  }

  public static Map<String, Object> locationSerialize(Location location) {
    Map<String, Object> section = new HashMap<String, Object>();
    section.put("x", location.getX());
    section.put("y", location.getY());
    section.put("z", location.getZ());
    section.put("pitch", (double) location.getPitch());
    section.put("yaw", (double) location.getYaw());
    section.put("world", location.getWorld().getName());

    return section;
  }

  @SuppressWarnings("unchecked")
  public static Location locationDeserialize(Object obj) {
    if (obj instanceof Location) {
      return (Location) obj;
    }

    Map<String, Object> section = new HashMap<String, Object>();
    if (obj instanceof MemorySection) {
      MemorySection sec = (MemorySection) obj;
      for (String key : sec.getKeys(false)) {
        section.put(key, sec.get(key));
      }
    } else if (obj instanceof ConfigurationSection) {
      ConfigurationSection sec = (ConfigurationSection) obj;
      for (String key : sec.getKeys(false)) {
        section.put(key, sec.get(key));
      }
    } else {
      section = (Map<String, Object>) obj;
    }

    try {
      if (section == null) {
        return null;
      }

      double x = Double.valueOf(section.get("x").toString());
      double y = Double.valueOf(section.get("y").toString());
      double z = Double.valueOf(section.get("z").toString());
      float yaw = Float.valueOf(section.get("yaw").toString());
      float pitch = Float.valueOf(section.get("pitch").toString());
      World world = Main.getInstance().getServer().getWorld(section.get("world").toString());

      if (world == null) {
        return null;
      }

      return new Location(world, x, y, z, yaw, pitch);
    } catch (Exception ex) {
      ex.printStackTrace();
    }

    return null;
  }

  @SuppressWarnings("unchecked")
  public static Location locationDeserialize(String key, FileConfiguration config) {
    if (!config.contains(key)) {
      return null;
    }

    Object locSec = config.get(key);
    if (locSec instanceof Location) {
      return (Location) locSec;
    }

    try {
      Map<String, Object> section = (Map<String, Object>) config.get(key);
      if (section == null) {
        return null;
      }

      double x = Double.valueOf(section.get("x").toString());
      double y = Double.valueOf(section.get("y").toString());
      double z = Double.valueOf(section.get("z").toString());
      float yaw = Float.valueOf(section.get("yaw").toString());
      float pitch = Float.valueOf(section.get("pitch").toString());
      World world = Main.getInstance().getServer().getWorld(section.get("world").toString());

      if (world == null) {
        return null;
      }

      return new Location(world, x, y, z, yaw, pitch);
    } catch (Exception ex) {
      ex.printStackTrace();
    }

    return null;
  }

  public static Class<?> getPrimitiveWrapper(Class<?> primitive) {
    if (!primitive.isPrimitive()) {
      return primitive;
    }

    if (primitive.getSimpleName().equals("int")) {
      return Integer.class;
    } else if (primitive.getSimpleName().equals("long")) {
      return Long.class;
    } else if (primitive.getSimpleName().equals("short")) {
      return Short.class;
    } else if (primitive.getSimpleName().equals("byte")) {
      return Byte.class;
    } else if (primitive.getSimpleName().equals("float")) {
      return Float.class;
    } else if (primitive.getSimpleName().equals("boolean")) {
      return Boolean.class;
    } else if (primitive.getSimpleName().equals("char")) {
      return Character.class;
    } else if (primitive.getSimpleName().equals("double")) {
      return Double.class;
    } else {
      return primitive;
    }
  }

  public static Class<?> getGenericTypeOfParameter(Class<?> clazz, String method,
      int parameterIndex) {
    try {
      Method m = clazz.getMethod(method, new Class<?>[] {Set.class, int.class});
      ParameterizedType type = (ParameterizedType) m.getGenericParameterTypes()[parameterIndex];
      return (Class<?>) type.getActualTypeArguments()[0];
    } catch (Exception e) {
      try {
        Method m = clazz.getMethod(method, new Class<?>[] {HashSet.class, int.class});
        ParameterizedType type = (ParameterizedType) m.getGenericParameterTypes()[parameterIndex];
        return (Class<?>) type.getActualTypeArguments()[0];
      } catch (Exception ex) {
        ex.printStackTrace();
      }
    }

    return null;
  }

  public final static String unescape_perl_string(String oldstr) {

    /*
     * In contrast to fixing Java's broken regex charclasses, this one need be no bigger, as
     * unescaping shrinks the string here, where in the other one, it grows it.
     */

    StringBuffer newstr = new StringBuffer(oldstr.length());

    boolean saw_backslash = false;

    for (int i = 0; i < oldstr.length(); i++) {
      int cp = oldstr.codePointAt(i);
      if (oldstr.codePointAt(i) > Character.MAX_VALUE) {
        i++;
        /**** WE HATES UTF-16! WE HATES IT FOREVERSES!!! ****/
      }

      if (!saw_backslash) {
        if (cp == '\\') {
          saw_backslash = true;
        } else {
          newstr.append(Character.toChars(cp));
        }
        continue; /* switch */
      }

      if (cp == '\\') {
        saw_backslash = false;
        newstr.append('\\');
        newstr.append('\\');
        continue; /* switch */
      }

      switch (cp) {

        case 'r':
          newstr.append('\r');
          break; /* switch */

        case 'n':
          newstr.append('\n');
          break; /* switch */

        case 'f':
          newstr.append('\f');
          break; /* switch */

        /* PASS a \b THROUGH!! */
        case 'b':
          newstr.append("\\b");
          break; /* switch */

        case 't':
          newstr.append('\t');
          break; /* switch */

        case 'a':
          newstr.append('\007');
          break; /* switch */

        case 'e':
          newstr.append('\033');
          break; /* switch */

        /*
         * A "control" character is what you get when you xor its codepoint with '@'==64. This only
         * makes sense for ASCII, and may not yield a "control" character after all.
         * 
         * Strange but true: "\c{" is ";", "\c}" is "=", etc.
         */
        case 'c': {
          if (++i == oldstr.length()) {
            die("trailing \\c");
          }
          cp = oldstr.codePointAt(i);
          /*
           * don't need to grok surrogates, as next line blows them up
           */
          if (cp > 0x7f) {
            die("expected ASCII after \\c");
          }
          newstr.append(Character.toChars(cp ^ 64));
          break; /* switch */
        }

        case '8':
        case '9':
          die("illegal octal digit");
          /* NOTREACHED */

          /*
           * may be 0 to 2 octal digits following this one so back up one for fallthrough to next
           * case; unread this digit and fall through to next case.
           */
        case '1':
        case '2':
        case '3':
        case '4':
        case '5':
        case '6':
        case '7':
          --i;
          /* FALLTHROUGH */

          /*
           * Can have 0, 1, or 2 octal digits following a 0 this permits larger values than octal
           * 377, up to octal 777.
           */
        case '0': {
          if (i + 1 == oldstr.length()) {
            /* found \0 at end of string */
            newstr.append(Character.toChars(0));
            break; /* switch */
          }
          i++;
          int digits = 0;
          int j;
          for (j = 0; j <= 2; j++) {
            if (i + j == oldstr.length()) {
              break; /* for */
            }
            /* safe because will unread surrogate */
            int ch = oldstr.charAt(i + j);
            if (ch < '0' || ch > '7') {
              break; /* for */
            }
            digits++;
          }
          if (digits == 0) {
            --i;
            newstr.append('\0');
            break; /* switch */
          }
          int value = 0;
          try {
            value = Integer.parseInt(oldstr.substring(i, i + digits), 8);
          } catch (NumberFormatException nfe) {
            die("invalid octal value for \\0 escape");
          }
          newstr.append(Character.toChars(value));
          i += digits - 1;
          break; /* switch */
        } /* end case '0' */

        case 'x': {
          if (i + 2 > oldstr.length()) {
            die("string too short for \\x escape");
          }
          i++;
          boolean saw_brace = false;
          if (oldstr.charAt(i) == '{') {
            /* ^^^^^^ ok to ignore surrogates here */
            i++;
            saw_brace = true;
          }
          int j;
          for (j = 0; j < 8; j++) {

            if (!saw_brace && j == 2) {
              break; /* for */
            }

            /*
             * ASCII test also catches surrogates
             */
            int ch = oldstr.charAt(i + j);
            if (ch > 127) {
              die("illegal non-ASCII hex digit in \\x escape");
            }

            if (saw_brace && ch == '}') {
              break; /* for */
            }

            if (!((ch >= '0' && ch <= '9') || (ch >= 'a' && ch <= 'f')
                || (ch >= 'A' && ch <= 'F'))) {
              die(String.format("illegal hex digit #%d '%c' in \\x", ch, ch));
            }

          }
          if (j == 0) {
            die("empty braces in \\x{} escape");
          }
          int value = 0;
          try {
            value = Integer.parseInt(oldstr.substring(i, i + j), 16);
          } catch (NumberFormatException nfe) {
            die("invalid hex value for \\x escape");
          }
          newstr.append(Character.toChars(value));
          if (saw_brace) {
            j++;
          }
          i += j - 1;
          break; /* switch */
        }

        case 'u': {
          if (i + 4 > oldstr.length()) {
            die("string too short for \\u escape");
          }
          i++;
          int j;
          for (j = 0; j < 4; j++) {
            /* this also handles the surrogate issue */
            if (oldstr.charAt(i + j) > 127) {
              die("illegal non-ASCII hex digit in \\u escape");
            }
          }
          int value = 0;
          try {
            value = Integer.parseInt(oldstr.substring(i, i + j), 16);
          } catch (NumberFormatException nfe) {
            die("invalid hex value for \\u escape");
          }
          newstr.append(Character.toChars(value));
          i += j - 1;
          break; /* switch */
        }

        case 'U': {
          if (i + 8 > oldstr.length()) {
            die("string too short for \\U escape");
          }
          i++;
          int j;
          for (j = 0; j < 8; j++) {
            /* this also handles the surrogate issue */
            if (oldstr.charAt(i + j) > 127) {
              die("illegal non-ASCII hex digit in \\U escape");
            }
          }
          int value = 0;
          try {
            value = Integer.parseInt(oldstr.substring(i, i + j), 16);
          } catch (NumberFormatException nfe) {
            die("invalid hex value for \\U escape");
          }
          newstr.append(Character.toChars(value));
          i += j - 1;
          break; /* switch */
        }

        default:
          newstr.append('\\');
          newstr.append(Character.toChars(cp));
          /*
           * say(String.format( "DEFAULT unrecognized escape %c passed through", cp));
           */
          break; /* switch */

      }
      saw_backslash = false;
    }

    /* weird to leave one at the end */
    if (saw_backslash) {
      newstr.append('\\');
    }

    return newstr.toString();
  }

  private static final void die(String foa) {
    throw new IllegalArgumentException(foa);
  }

  /*
   * Return a string "U+XX.XXX.XXXX" etc, where each XX set is the xdigits of the logical Unicode
   * code point. No bloody brain-damaged UTF-16 surrogate crap, just true logical characters.
   */
  public final static String uniplus(String s) {
    if (s.length() == 0) {
      return "";
    }
    /* This is just the minimum; sb will grow as needed. */
    StringBuffer sb = new StringBuffer(2 + 3 * s.length());
    sb.append("U+");
    for (int i = 0; i < s.length(); i++) {
      sb.append(String.format("%X", s.codePointAt(i)));
      if (s.codePointAt(i) > Character.MAX_VALUE) {
        i++;
        /**** WE HATES UTF-16! WE HATES IT FOREVERSES!!! ****/
      }
      if (i + 1 < s.length()) {
        sb.append(".");
      }
    }
    return sb.toString();
  }

  public static BlockFace getCardinalDirection(Location location) {
    double rotation = (location.getYaw() - 90) % 360;
    if (rotation < 0) {
      rotation += 360.0;
    }
    if (0 <= rotation && rotation < 22.5) {
      return BlockFace.NORTH;
    } else if (22.5 <= rotation && rotation < 67.5) {
      return BlockFace.NORTH_EAST;
    } else if (67.5 <= rotation && rotation < 112.5) {
      return BlockFace.EAST;
    } else if (112.5 <= rotation && rotation < 157.5) {
      return BlockFace.SOUTH_EAST;
    } else if (157.5 <= rotation && rotation < 202.5) {
      return BlockFace.SOUTH;
    } else if (202.5 <= rotation && rotation < 247.5) {
      return BlockFace.SOUTH_WEST;
    } else if (247.5 <= rotation && rotation < 292.5) {
      return BlockFace.WEST;
    } else if (292.5 <= rotation && rotation < 337.5) {
      return BlockFace.NORTH_WEST;
    } else if (337.5 <= rotation && rotation < 360.0) {
      return BlockFace.NORTH;
    } else {
      return BlockFace.NORTH;
    }
  }

  public static String getFormattedTime(int time) {
    int hr = 0;
    int min = 0;
    int sec = 0;
    String minStr = "";
    String secStr = "";
    String hrStr = "";

    hr = (int) Math.floor((time / 60) / 60);
    min = ((int) Math.floor((time / 60)) - (hr * 60));
    sec = time % 60;

    hrStr = (hr < 10) ? "0" + String.valueOf(hr) : String.valueOf(hr);
    minStr = (min < 10) ? "0" + String.valueOf(min) : String.valueOf(min);
    secStr = (sec < 10) ? "0" + String.valueOf(sec) : String.valueOf(sec);

    return hrStr + ":" + minStr + ":" + secStr;
  }

  @SuppressWarnings("deprecation")
  public static Material parseMaterial(String material) {
    try {
      if (Utils.isNumber(material)) {
        return Material.getMaterial(Integer.parseInt(material));
      } else {
        return Material.getMaterial(material.toUpperCase());
      }
    } catch (Exception ex) {
      // failed to parse
    }

    return null;
  }

}
