package io.github.yannici.bedwarsreloaded;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;

import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public final class Utils {

    public static String implode(String glue, ArrayList<String> strings) {
        if(strings.isEmpty()) {
            return "";
        }

        StringBuilder builder = new StringBuilder();
        builder.append(strings.remove(0));

        for(String str : strings) {
            builder.append(glue);
            builder.append(str);
        }

        return builder.toString();
    }
    
    public static boolean isNumber(String numberString) {
        try {
            Integer.parseInt(numberString);
            return true;
        } catch(Exception ex) {
            return false;
        }
    }
    
    public static Method getColorableMethod(Material mat) {
        try {
            ItemStack tempStack = new ItemStack(mat, 1);
            Method method = tempStack.getItemMeta().getClass().getMethod("setColor", new Class[]{Color.class});
            if(method != null) {
                return method;
            }
        } catch(Exception ex) {
            // it's no error
        }
        
        return null;
    }
    
    @SuppressWarnings("unchecked")
	public static String getPlayerLocale(Player player) {
    	try {
    		Method getHandleMethod = Main.getInstance().getCraftBukkitClass("entity.CraftPlayer").getMethod("getHandle", new Class[]{});
        	getHandleMethod.setAccessible(true);
        	Object nmsPlayer = getHandleMethod.invoke(player, new Object[]{});
        	
        	Field localeField = nmsPlayer.getClass().getDeclaredField("locale");
        	localeField.setAccessible(true);
        	return localeField.get(nmsPlayer).toString();
    	} catch(Exception ex) {
    		return Main.getInstance().getFallbackLocale();
    	}
    }
    
    public static boolean checkBungeePlugin()
	  {
	    try
	    {
	      Class.forName("net.md_5.bungee.BungeeCord");
	      return true;
	    }
	    catch (Exception e) {}
	    
	    return false;
	  }

}
