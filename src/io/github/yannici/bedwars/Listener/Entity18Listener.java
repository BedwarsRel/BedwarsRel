package io.github.yannici.bedwars.Listener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;

public class Entity18Listener extends BaseListener {
    
    private EntityListener entityListener = null;

    public Entity18Listener(EntityListener listener) {
        this.entityListener = listener;
    }
    
    @EventHandler(priority = EventPriority.HIGH)
    public void onInteractAtEntity(PlayerInteractAtEntityEvent event) {
        this.entityListener.onInteractEntity(event);
    }

}
