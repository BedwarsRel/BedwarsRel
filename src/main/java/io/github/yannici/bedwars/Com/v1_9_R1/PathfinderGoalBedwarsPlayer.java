package io.github.yannici.bedwars.Com.v1_9_R1;

import net.minecraft.server.v1_9_R1.EntityCreature;
import net.minecraft.server.v1_9_R1.PathfinderGoalMeleeAttack;

public class PathfinderGoalBedwarsPlayer extends PathfinderGoalMeleeAttack {

	private EntityCreature creature = null;

	public PathfinderGoalBedwarsPlayer(EntityCreature name, double name3, boolean name4) {
		super(name, name3, name4);
		this.creature = name;
	}

	@Override
	public void e() {
		this.creature.getNavigation().a(this.creature.getGoalTarget());
	}

}
