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
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

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
	
	public static Block getBedNeighbor(Block head) {
		if(Utils.isBedBlock(head.getRelative(BlockFace.EAST))) {
			return head.getRelative(BlockFace.EAST);
		} else if(Utils.isBedBlock(head.getRelative(BlockFace.WEST))) {
			return head.getRelative(BlockFace.WEST);
		} else if(Utils.isBedBlock(head.getRelative(BlockFace.SOUTH))) {
			return head.getRelative(BlockFace.SOUTH);
		} else {
			return head.getRelative(BlockFace.NORTH);
		}
	}
	
	public static boolean isBedBlock(Block isBed) {
	    if(isBed == null) {
	        return false;
	    }
	    
		return (isBed.getType() == Material.BED 
				|| isBed.getType() == Material.BED_BLOCK);
	}

	public static Object getCraftPlayer(Player player) {
		try {
			Class<?> craftPlayerClass = Main.getInstance().getCraftBukkitClass(
					"entity.CraftPlayer");
			Method getHandle = craftPlayerClass.getMethod("getHandle",
					new Class[] {});
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
			Method method = tempStack.getItemMeta().getClass()
					.getMethod("setColor", new Class[] { Color.class });
			if (method != null) {
				return method;
			}
		} catch (Exception ex) {
			// it's no error
		}

		return null;
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
			 * In case of a jar file, we can't actually find a directory. Have
			 * to assume the same jar as clazz.
			 */
			String me = clazz.getName().replace(".", "/") + ".class";
			dirURL = clazz.getClassLoader().getResource(me);
		}

		if (dirURL.getProtocol().equals("jar")) {
			/* A JAR path */
			String jarPath = dirURL.getPath().substring(5,
					dirURL.getPath().indexOf("!")); // strip out only the JAR
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

		throw new UnsupportedOperationException("Cannot list files for URL "
				+ dirURL);
	}

	public static int randInt(int min, int max) {
		Random rand = new Random();
		int randomNum = rand.nextInt((max - min) + 1) + min;

		return randomNum;
	}
	
	public static Class<?> getPrimitiveWrapper(Class<?> primitive) {
		if(!primitive.isPrimitive()) {
			return primitive;
		}
		
		if(primitive.getSimpleName().equals("int")) {
			return Integer.class;
		} else if(primitive.getSimpleName().equals("long")) {
			return Long.class;
		} else if(primitive.getSimpleName().equals("short")) {
			return Short.class;
		} else if(primitive.getSimpleName().equals("byte")) {
			return Byte.class;
		} else if(primitive.getSimpleName().equals("float")) {
			return Float.class;
		} else if(primitive.getSimpleName().equals("boolean")) {
			return Boolean.class;
		} else if(primitive.getSimpleName().equals("char")) {
			return Character.class;
		} else if(primitive.getSimpleName().equals("double")) {
			return Double.class;
		} else {
			return primitive;
		}
	}

	public static Class<?> getGenericTypeOfParameter(Class<?> clazz,
			String method, int parameterIndex) {
		try {
			Method m = clazz.getMethod(method, new Class<?>[]{Set.class, int.class});
			ParameterizedType type = (ParameterizedType) m.getGenericParameterTypes()[parameterIndex];
			return (Class<?>) type.getActualTypeArguments()[0];
		} catch (Exception e) {
			try {
				Method m = clazz.getMethod(method, new Class<?>[]{HashSet.class, int.class});
				ParameterizedType type = (ParameterizedType) m.getGenericParameterTypes()[parameterIndex];
				return (Class<?>) type.getActualTypeArguments()[0];
			} catch(Exception ex) {
				ex.printStackTrace();
			}
		}
		
		return null;
	}

}
