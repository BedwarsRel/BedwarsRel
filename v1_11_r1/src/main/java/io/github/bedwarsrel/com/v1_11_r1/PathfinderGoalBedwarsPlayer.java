package io.github.bedwarsrel.com.v1_11_r1;

import net.minecraft.server.v1_11_R1.EntityCreature;
import net.minecraft.server.v1_11_R1.PathfinderGoalMeleeAttack;

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
