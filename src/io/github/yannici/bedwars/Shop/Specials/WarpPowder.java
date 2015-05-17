package io.github.yannici.bedwars.Shop.Specials;

import io.github.yannici.bedwars.Main;
import io.github.yannici.bedwars.Game.Game;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

public class WarpPowder extends SpecialItem {
    
    private BukkitTask teleportingTask = null;
    private int teleportingTime = 6;

    public WarpPowder() {
        super();
    }

    @Override
    public Material getItemMaterial() {
        return Material.SULPHUR;
    }
    
    @Override
    public Material getActivatedMaterial() {
        return Material.GLOWSTONE_DUST;
    }
    
    private boolean playerInteract(PlayerInteractEvent interact) {
        final Player player = interact.getPlayer();
        
        if(player.getLocation().getBlock().getRelative(BlockFace.DOWN).getType() == Material.AIR) {
            return false;
        }
        
        final Game game = Game.getGameOfPlayer(player);
        
        this.teleportingTime = Main.getInstance().getIntConfig("specials.warp-powder.teleport-time", 6);
        this.teleportingTask = new BukkitRunnable() {
            
            @Override
            public void run() {
                player.setLevel(WarpPowder.this.teleportingTime);
                
                if(WarpPowder.this.teleportingTime <= 0) {
                    game.removeRunningTask(this);
                    this.cancel();
                    return;
                }
                WarpPowder.this.teleportingTime--;
            }
        }.runTaskTimer(Main.getInstance(), 0L, 20L);
        game.addRunningTask(this.teleportingTask);
        return true;
    }
    
    private boolean playerMove(PlayerMoveEvent move) {
        return true;
    }
    
    private boolean playerDamage(EntityDamageByEntityEvent damage) {
        return true;
    }
    
    /*private void cancelTeleport() {
        WarpPowder.this.teleportingTime = Main.getInstance().getIntConfig("specials.warp-powder.teleport-time", 6);
        
    }*/

    @Override
    public boolean executeEvent(Event event) {
        if(!(event instanceof PlayerInteractEvent)
                && !(event instanceof PlayerMoveEvent)
                && !(event instanceof EntityDamageByEntityEvent)) {
            return false;
        }
        
        if(event instanceof PlayerInteractEvent) {
            PlayerInteractEvent ev = (PlayerInteractEvent)event;
            if(super.returnPlayerEvent(ev.getPlayer())) {
                return false;
            }
            
            return this.playerInteract(ev);
        } else if(event instanceof PlayerMoveEvent) {
            PlayerMoveEvent ev = (PlayerMoveEvent)event;
            if(super.returnPlayerEvent(ev.getPlayer())) {
                return false;
            }
            
            return this.playerMove(ev);
        } else if(event instanceof EntityDamageByEntityEvent) {
            EntityDamageByEntityEvent ev = (EntityDamageByEntityEvent)event;
            if(ev.getEntityType() != EntityType.PLAYER) {
                return false;
            }
            
            Player player = (Player)ev.getEntity();
            if(super.returnPlayerEvent(player)) {
                return false;
            }
            
            return this.playerDamage(ev);
        }
        
        return false;
    }

    @Override
    public boolean executeEventActivated(Event event) {
        return false;
    }

    @Override
    public List<Class<? extends Event>> getUsedEvents() {
        List<Class<? extends Event>> events = new ArrayList<Class<? extends Event>>();
        events.add(PlayerInteractEvent.class);
        events.add(PlayerMoveEvent.class);
        events.add(EntityDamageByEntityEvent.class);
        
        return events;
    }

}
