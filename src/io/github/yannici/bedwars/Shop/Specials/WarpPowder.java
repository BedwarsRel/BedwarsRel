package io.github.yannici.bedwars.Shop.Specials;

import io.github.yannici.bedwars.Main;
import io.github.yannici.bedwars.Game.Game;

import org.bukkit.Effect;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

public class WarpPowder extends SpecialItem {
    
    private BukkitTask teleportingTask = null;
    private double teleportingTime = 6.0;
    private Player player = null;
    private Game game = null;
    private int fullTeleportingTime = 6;

    public WarpPowder() {
        super();
        
        this.fullTeleportingTime = Main.getInstance().getIntConfig("specials.warp-powder.teleport-time", 6);
    }

    @Override
    public Material getItemMaterial() {
        return Material.SULPHUR;
    }
    
    @Override
    public Material getActivatedMaterial() {
        return Material.GLOWSTONE_DUST;
    }
    
    public Player getPlayer() {
        return this.player;
    }
    
    private void cancelTeleport() {
        this.teleportingTime = Main.getInstance().getIntConfig("specials.warp-powder.teleport-time", 6);
        this.teleportingTask.cancel();
        this.game.removeRunningTask(this.teleportingTask);
    }

    public boolean executeEventActivated(Event event) {
        return false;
    }
    
    public void runTask() {
        this.teleportingTime = Main.getInstance().getIntConfig("specials.warp-powder.teleport-time", 6);
        this.teleportingTask = new BukkitRunnable() {
            
            @Override
            public void run() {
                WarpPowder.this.player.setLevel((int)Math.ceil(WarpPowder.this.teleportingTime));
                
                if(WarpPowder.this.teleportingTime <= 0.0) {
                    WarpPowder.this.cancelTeleport();
                    return;
                }
                
                World world = WarpPowder.this.game.getRegion().getWorld();
                double size = 1.0;
                double vertical = 2.0*(WarpPowder.this.teleportingTime/WarpPowder.this.fullTeleportingTime);
                
                for(int i = 0; i < 30; i++) {
                    double alpha = i*12;
                    world.playEffect(WarpPowder.this.player.getLocation().add(size * Math.cos(Math.toRadians(alpha)), vertical, size * Math.sin(Math.toRadians(alpha))), Effect.SMOKE, 31);
                }
                
                WarpPowder.this.teleportingTime -= 0.5;
            }
            
        }.runTaskTimer(Main.getInstance(), 0L, 10L);
        this.game.addRunningTask(this.teleportingTask);
    }

    public void setPlayer(Player player) {
        this.player = player;
    }

    public void setGame(Game game) {
        this.game = game;
    }
}
