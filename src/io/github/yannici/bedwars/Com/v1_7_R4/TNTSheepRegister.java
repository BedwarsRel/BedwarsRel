package io.github.yannici.bedwars.Com.v1_7_R4;

import java.lang.reflect.Field;
import java.util.HashMap;

import net.minecraft.server.v1_7_R4.EntityTypes;
import net.minecraft.server.v1_7_R4.World;

import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.entity.Sheep;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.scheduler.BukkitRunnable;

import io.github.yannici.bedwars.Main;
import io.github.yannici.bedwars.Shop.Specials.ITNTSheep;
import io.github.yannici.bedwars.Shop.Specials.ITNTSheepRegister;

public class TNTSheepRegister implements ITNTSheepRegister {

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public void registerEntities() {
		try {
			Class<?> entityTypeClass = EntityTypes.class;

			Field c = entityTypeClass.getDeclaredField("c");
			c.setAccessible(true);
			HashMap c_map = (HashMap) c.get(null);
			c_map.put("TNTSheep", TNTSheep.class);

			Field d = entityTypeClass.getDeclaredField("d");
			d.setAccessible(true);
			HashMap d_map = (HashMap) d.get(null);
			d_map.put(TNTSheep.class, "TNTSheep");

			Field e = entityTypeClass.getDeclaredField("e");
			e.setAccessible(true);
			HashMap e_map = (HashMap) e.get(null);
			e_map.put(Integer.valueOf(91), TNTSheep.class);

			Field f = entityTypeClass.getDeclaredField("f");
			f.setAccessible(true);
			HashMap f_map = (HashMap) f.get(null);
			f_map.put(TNTSheep.class, Integer.valueOf(91));

			Field g = entityTypeClass.getDeclaredField("g");
			g.setAccessible(true);
			HashMap g_map = (HashMap) g.get(null);
			g_map.put("TNTSheep", Integer.valueOf(91));
			
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	@Override
	public ITNTSheep spawnSheep(final Location location, Player owner, Player target, final DyeColor color) {
		final TNTSheep sheep = new TNTSheep(location.getWorld(), target);
		
		new BukkitRunnable() {
			
			@Override
			public void run() {
				
				
				((World) location.getWorld()).addEntity(sheep, SpawnReason.CUSTOM);
				((Sheep) sheep.getBukkitEntity()).setColor(color);
				sheep.setPosition(location.getX(), location.getY(), location.getZ());
			}
		}.runTask(Main.getInstance());
		
		return sheep;
	}

}
