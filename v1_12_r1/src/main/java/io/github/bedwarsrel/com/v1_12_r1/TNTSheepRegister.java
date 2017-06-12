package io.github.bedwarsrel.com.v1_12_r1;

import io.github.bedwarsrel.BedwarsRel;
import io.github.bedwarsrel.shop.Specials.ITNTSheep;
import io.github.bedwarsrel.shop.Specials.ITNTSheepRegister;
import java.lang.reflect.Field;
import net.minecraft.server.v1_12_R1.EntityTNTPrimed;
import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_12_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftLivingEntity;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftSheep;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftTNTPrimed;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.scheduler.BukkitRunnable;

public class TNTSheepRegister implements ITNTSheepRegister {

  @Override
  public void registerEntities(int entityId) {
    CustomEntityRegistry.addCustomEntity(entityId, "TNTSheep", TNTSheep.class);
  }

  @Override
  public ITNTSheep spawnCreature(
      final io.github.bedwarsrel.shop.Specials.TNTSheep specialItem,
      final Location location, final Player owner, Player target, final DyeColor color) {
    final TNTSheep sheep = new TNTSheep(location, target);

    ((CraftWorld) location.getWorld()).getHandle().addEntity(sheep, SpawnReason.CUSTOM);
    sheep.setPosition(location.getX(), location.getY(), location.getZ());
    ((CraftSheep) sheep.getBukkitEntity()).setColor(color);
    new BukkitRunnable() {

      @Override
      public void run() {

        TNTPrimed primedTnt = (TNTPrimed) location.getWorld()
            .spawnEntity(location.add(0.0, 1.0, 0.0), EntityType.PRIMED_TNT);
        ((CraftSheep) sheep.getBukkitEntity()).setPassenger(primedTnt);
        sheep.setTNT(primedTnt);
        try {
          Field sourceField = EntityTNTPrimed.class.getDeclaredField("source");
          sourceField.setAccessible(true);
          sourceField.set(((CraftTNTPrimed) primedTnt).getHandle(),
              ((CraftLivingEntity) owner).getHandle());
        } catch (Exception ex) {
          BedwarsRel.getInstance().getBugsnag().notify(ex);
          ex.printStackTrace();
        }
        sheep.getTNT().setYield((float) (sheep.getTNT().getYield()
            * BedwarsRel
            .getInstance().getConfig().getDouble("specials.tntsheep.explosion-factor", 1.0)));
        sheep.getTNT().setFuseTicks((int) Math.round(
            BedwarsRel.getInstance().getConfig().getDouble("specials.tntsheep.fuse-time", 8) * 20));
        sheep.getTNT().setIsIncendiary(false);
        specialItem.getGame().getRegion().addRemovingEntity(sheep.getTNT());
        specialItem.getGame().getRegion().addRemovingEntity(sheep.getBukkitEntity());
        specialItem.updateTNT();
      }
    }.runTaskLater(BedwarsRel.getInstance(), 5L);

    return sheep;
  }

}
