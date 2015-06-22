package io.github.yannici.bedwars.Com.v1_7_R1;

import java.lang.reflect.Field;
import java.util.ArrayList;

import io.github.yannici.bedwars.Shop.Specials.ITNTSheep;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_7_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_7_R1.entity.CraftEntity;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Sheep;

import net.minecraft.server.v1_7_R1.AttributeInstance;
import net.minecraft.server.v1_7_R1.EntityHuman;
import net.minecraft.server.v1_7_R1.EntitySheep;
import net.minecraft.server.v1_7_R1.GenericAttributes;
import net.minecraft.server.v1_7_R1.Navigation;

public class TNTSheep extends EntitySheep implements ITNTSheep {
	
	private World world = null;
	
	public TNTSheep(World world, Entity target) {
		super(((CraftWorld) world).getHandle());
		
		this.world = world;
		
		try {
			Field b = this.goalSelector.getClass().getDeclaredField("b");
			b.setAccessible(true);
			b.set(this.goalSelector, new ArrayList<>());
			Field field = Navigation.class.getDeclaredField("e");
			field.setAccessible(true);
			AttributeInstance ai = (AttributeInstance) field.get(this.getNavigation());
			ai.setValue(128);
			this.getAttributeInstance(GenericAttributes.b).setValue(128D);
			this.getAttributeInstance(GenericAttributes.d).setValue(0.37D);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		this.goalSelector.a(0, new PathfinderGoalBedwarsPlayer(this, EntityHuman.class, 1D, false));
		this.setTarget(((CraftEntity) target).getHandle());
		((Sheep) this.getBukkitEntity()).setTarget((LivingEntity) target);
	}

	@Override
	public Location getLocation() {
		return new Location(this.world, this.locX, this.locY, this.locZ);
	}

}
