package io.github.bedwarsrel.BedwarsRel.Com.v1_8_R2;

import net.minecraft.server.v1_8_R2.Entity;
import net.minecraft.server.v1_8_R2.EntityCreature;
import net.minecraft.server.v1_8_R2.PathfinderGoalMeleeAttack;

public class PathfinderGoalBedwarsPlayer extends PathfinderGoalMeleeAttack {

  private EntityCreature creature = null;

  public PathfinderGoalBedwarsPlayer(EntityCreature name, Class<? extends Entity> name2,
      double name3, boolean name4) {
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
