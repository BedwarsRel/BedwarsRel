package io.github.bedwarsrel.BedwarsRel.Com.v1_11_R1;

import java.lang.reflect.Field;
import java.util.HashMap;

import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_11_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_11_R1.entity.CraftLivingEntity;
import org.bukkit.craftbukkit.v1_11_R1.entity.CraftSheep;
import org.bukkit.craftbukkit.v1_11_R1.entity.CraftTNTPrimed;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.scheduler.BukkitRunnable;

import io.github.bedwarsrel.BedwarsRel.Main;
import io.github.bedwarsrel.BedwarsRel.Shop.Specials.ITNTSheep;
import io.github.bedwarsrel.BedwarsRel.Shop.Specials.ITNTSheepRegister;
import net.minecraft.server.v1_11_R1.EntityTNTPrimed;

public class TNTSheepRegister implements ITNTSheepRegister {

  @Override
  public void registerEntities(int entityId) {
    CustomEntityRegistry.addCustomEntity(entityId, "TNTSheep", TNTSheep.class);
  }

  @Override
  public ITNTSheep spawnCreature(final io.github.bedwarsrel.BedwarsRel.Shop.Specials.TNTSheep specialItem,
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
          Main.getInstance().getBugsnag().notify(ex);
          ex.printStackTrace();
        }
        sheep.getTNT().setYield((float) (sheep.getTNT().getYield()
            * Main.getInstance().getConfig().getDouble("specials.tntsheep.explosion-factor", 1.0)));
        sheep.getTNT().setFuseTicks((int) Math.round(
            Main.getInstance().getConfig().getDouble("specials.tntsheep.fuse-time", 8) * 20));
        sheep.getTNT().setIsIncendiary(false);
        specialItem.getGame().getRegion().addRemovingEntity(sheep.getTNT());
        specialItem.getGame().getRegion().addRemovingEntity(sheep.getBukkitEntity());
        specialItem.updateTNT();
      }
    }.runTaskLater(Main.getInstance(), 5L);

    return sheep;
  }

}
