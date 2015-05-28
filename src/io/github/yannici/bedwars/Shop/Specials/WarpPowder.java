package io.github.yannici.bedwars.Shop.Specials;

import io.github.yannici.bedwars.Main;
import io.github.yannici.bedwars.Utils;
import io.github.yannici.bedwars.Game.Game;
import io.github.yannici.bedwars.Game.Team;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
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
    
    public void cancelTeleport(boolean removeSpecial) {
    	this.teleportingTask.cancel();
        this.teleportingTime = (double) Main.getInstance().getIntConfig("specials.warp-powder.teleport-time", 6);
        this.game.removeRunningTask(this.teleportingTask);
        
        if(removeSpecial) {
        	this.game.removeSpecialItem(this);
        }
    }
    
    public void runTask() {
    	final int circles = 15;
    	final int circleElements = 20;
    	final double radius = 1.0;
    	final double height = 2.0;
    	
    	final String particle = Main.getInstance().getStringConfig("specials.warp-powder.particle", "fireworksSpark");
    	
        this.teleportingTime = (double) Main.getInstance().getIntConfig("specials.warp-powder.teleport-time", 6);
        this.teleportingTask = new BukkitRunnable() {
        	
        	private double through = 0.0;
            
            @Override
            public void run() {
                WarpPowder.this.teleportingTime -= (WarpPowder.this.fullTeleportingTime/circles);
                Team team = WarpPowder.this.game.getPlayerTeam(WarpPowder.this.player);
                Location tLoc = team.getSpawnLocation();
                
                if(WarpPowder.this.teleportingTime <= 0.0) {
                	WarpPowder.this.player.teleport(team.getSpawnLocation());
                	WarpPowder.this.cancelTeleport(true);
                	return;
                }
                
                Location loc = WarpPowder.this.player.getLocation();
                
                double y = (height/circles)*through;
                double yTarget = height-((height/circles)*through);
                
                for(int i = 0; i < 20; i++) {
                	double alpha = (360.0/circleElements)*i;
                	double x = radius * Math.sin(Math.toRadians(alpha));
                	double z = radius * Math.cos(Math.toRadians(alpha));
                	
                	Location particleFrom = new Location(loc.getWorld(), loc.getX()+x, loc.getY()+y, loc.getZ()+z);
                	Utils.createParticleInGame(game, particle, particleFrom);
                	
                	Location particleTo = new Location(tLoc.getWorld(), tLoc.getX()+x, tLoc.getY()+yTarget, tLoc.getZ()+z);
                	Utils.createParticleInGame(game, particle, particleTo);
                }
                
                this.through += 1.0;
            }
        }.runTaskTimer(Main.getInstance(), 0L, (long) Math.ceil((height/circles)*((this.fullTeleportingTime*20)/circles)));
        this.game.addRunningTask(this.teleportingTask);
        this.game.addSpecialItem(this);
    }

    public void setPlayer(Player player) {
        this.player = player;
    }

    public void setGame(Game game) {
        this.game = game;
    }
}
