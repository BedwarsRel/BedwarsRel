package io.github.yannici.bedwars.Com.v1_8_R3;

import java.lang.reflect.Field;
import java.util.HashMap;

import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftLivingEntity;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftSheep;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftTNTPrimed;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.scheduler.BukkitRunnable;

import io.github.yannici.bedwars.Main;
import io.github.yannici.bedwars.Shop.Specials.ITNTSheep;
import io.github.yannici.bedwars.Shop.Specials.ITNTSheepRegister;
import net.minecraft.server.v1_8_R3.EntityTNTPrimed;
import net.minecraft.server.v1_8_R3.EntityTypes;

public class TNTSheepRegister implements ITNTSheepRegister {

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public void registerEntities(int entityId) {
		try {
			Class<?> entityTypeClass = EntityTypes.class;

			/*
			 * try { Method a = EntityTypes.class.getDeclaredMethod("a",
			 * Class.class, String.class, int.class); a.setAccessible(true);
			 * a.invoke(a, TNTCreature.class, "TNTCreature", entityId); } catch
			 * (Exception ignored) { // Do some cleanup and error-handling here.
			 * }
			 */

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
			e_map.put(Integer.valueOf(entityId), TNTSheep.class);

			Field f = entityTypeClass.getDeclaredField("f");
			f.setAccessible(true);
			HashMap f_map = (HashMap) f.get(null);
			f_map.put(TNTSheep.class, Integer.valueOf(entityId));

			Field g = entityTypeClass.getDeclaredField("g");
			g.setAccessible(true);
			HashMap g_map = (HashMap) g.get(null);
			g_map.put("TNTSheep", Integer.valueOf(entityId));

		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	@Override
	public ITNTSheep spawnCreature(final io.github.yannici.bedwars.Shop.Specials.TNTSheep specialItem,
			final Location location, final Player owner, Player target, final DyeColor color) {
		final TNTSheep sheep = new TNTSheep(location, target);

		((CraftWorld) location.getWorld()).getHandle().addEntity(sheep, SpawnReason.NATURAL);
		sheep.setPosition(location.getX(), location.getY(), location.getZ());
		((CraftSheep) sheep.getBukkitEntity()).setColor(color);

		new BukkitRunnable() {

			@Override
			public void run() {
				TNTPrimed primedTnt = (TNTPrimed) location.getWorld().spawnEntity(location.add(0.0, 1.0, 0.0),
						EntityType.PRIMED_TNT);
				((CraftSheep) sheep.getBukkitEntity()).setPassenger(primedTnt);
				sheep.setTNT(primedTnt);

				try {
					Field sourceField = EntityTNTPrimed.class.getDeclaredField("source");
					sourceField.setAccessible(true);
					sourceField.set(((CraftTNTPrimed) primedTnt).getHandle(), ((CraftLivingEntity) owner).getHandle());
				} catch (Exception ex) {
					ex.printStackTrace();
				}

				sheep.getTNT().setYield((float) (sheep.getTNT().getYield()
						* Main.getInstance().getConfig().getDouble("specials.tntsheep.explosion-factor", 1.0)));
				sheep.getTNT().setFuseTicks((int) Math
						.round(Main.getInstance().getConfig().getDouble("specials.tntsheep.fuse-time", 8) * 20));
				sheep.getTNT().setIsIncendiary(false);
				specialItem.getGame().getRegion().addRemovingEntity(sheep.getTNT());
				specialItem.getGame().getRegion().addRemovingEntity(sheep.getBukkitEntity());
				specialItem.updateTNT();
			}
		}.runTaskLater(Main.getInstance(), 5L);

		return sheep;
	}

}
