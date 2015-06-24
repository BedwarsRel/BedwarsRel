package io.github.yannici.bedwars.Com.v1_7_R2;

import java.lang.reflect.Field;
import java.util.HashMap;

import net.minecraft.server.v1_7_R2.EntityTypes;
import net.minecraft.server.v1_7_R2.World;

import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.scheduler.BukkitRunnable;

import io.github.yannici.bedwars.Main;
import io.github.yannici.bedwars.Shop.Specials.ITNTCreature;
import io.github.yannici.bedwars.Shop.Specials.ITNTCreatureRegister;

public class TNTCreatureRegister implements ITNTCreatureRegister {

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public void registerEntities(int entityId) {
		try {
			Class<?> entityTypeClass = EntityTypes.class;

			Field c = entityTypeClass.getDeclaredField("c");
			c.setAccessible(true);
			HashMap c_map = (HashMap) c.get(null);
			c_map.put("TNTCreature", TNTCreature.class);

			Field d = entityTypeClass.getDeclaredField("d");
			d.setAccessible(true);
			HashMap d_map = (HashMap) d.get(null);
			d_map.put(TNTCreature.class, "TNTCreature");

			Field e = entityTypeClass.getDeclaredField("e");
			e.setAccessible(true);
			HashMap e_map = (HashMap) e.get(null);
			e_map.put(Integer.valueOf(entityId), TNTCreature.class);

			Field f = entityTypeClass.getDeclaredField("f");
			f.setAccessible(true);
			HashMap f_map = (HashMap) f.get(null);
			f_map.put(TNTCreature.class, Integer.valueOf(entityId));

			Field g = entityTypeClass.getDeclaredField("g");
			g.setAccessible(true);
			HashMap g_map = (HashMap) g.get(null);
			g_map.put("TNTCreature", Integer.valueOf(entityId));
			
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	@Override
	public ITNTCreature spawnCreature(final Location location, Player owner, Player target, final DyeColor color) {
		final TNTCreature sheep = new TNTCreature(location.getWorld(), target);
		
		new BukkitRunnable() {
			
			@Override
			public void run() {
				((World) location.getWorld()).addEntity(sheep, SpawnReason.CUSTOM);
				//((Sheep) sheep.getBukkitEntity()).setColor(color);
				sheep.setPosition(location.getX(), location.getY(), location.getZ());
			}
		}.runTask(Main.getInstance());
		
		return sheep;
	}

}
