package io.github.yannici.bedwars.Com.v1_8_R1;

import net.minecraft.server.v1_8_R1.Entity;
import net.minecraft.server.v1_8_R1.EntityCreature;
import net.minecraft.server.v1_8_R1.PathfinderGoalMeleeAttack;

public class PathfinderGoalBedwarsPlayer extends PathfinderGoalMeleeAttack {

	private EntityCreature creature = null;

	public PathfinderGoalBedwarsPlayer(EntityCreature name, Class<? extends Entity> name2, double name3,
			boolean name4) {
		super(name, name2, name3, name4);
		this.creature = name;
	}

	public PathfinderGoalBedwarsPlayer(EntityCreature name, double name3, boolean name4) {
		super(name, name3, name4);
		this.creature = name;
	}

	@Override
	public void e() {
		this.creature.getNavigation().a(this.creature.getGoalTarget());
	}

}
