package io.github.yannici.bedwars.Com.v1_7_R3;

import java.lang.reflect.Field;
import java.util.ArrayList;

import io.github.yannici.bedwars.Shop.Specials.ITNTCreature;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_7_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_7_R3.entity.CraftEntity;
import org.bukkit.entity.Creature;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;

import net.minecraft.server.v1_7_R3.AttributeInstance;
import net.minecraft.server.v1_7_R3.EntityCreature;
import net.minecraft.server.v1_7_R3.EntityHuman;
import net.minecraft.server.v1_7_R3.GenericAttributes;
import net.minecraft.server.v1_7_R3.Navigation;

public class TNTCreature extends EntityCreature implements ITNTCreature {
	
	private World world = null;
	private TNTPrimed primedTnt = null;
	
	public TNTCreature(World world, Player target) {
		super(((CraftWorld) world).getHandle());
		
		this.world = world;
		this.locX = target.getLocation().getX();
		this.locY = target.getLocation().getY();
		this.locZ = target.getLocation().getZ();
		
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
		((Creature) this.getBukkitEntity()).setTarget((LivingEntity) target);
	}

	@Override
	public Location getLocation() {
		return new Location(this.world, this.locX, this.locY, this.locZ);
	}
	
	@Override
	public void setTNT(TNTPrimed tnt) {
		this.primedTnt = tnt;
	}
	
	@Override
	public TNTPrimed getTNT() {
		return this.primedTnt;
	}

}
