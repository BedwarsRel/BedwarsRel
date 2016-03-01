package io.github.yannici.bedwars.Listener;

import org.bukkit.entity.Hanging;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.hanging.HangingBreakEvent;
import org.bukkit.event.hanging.HangingBreakEvent.RemoveCause;

public class HangingListener extends BaseListener {

	public HangingListener() {
		super();
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onHangingBreak(HangingBreakEvent event) {
		Hanging hanging = event.getEntity();
		if (event.getCause().equals(RemoveCause.OBSTRUCTION)) {
			hanging.getLocation().getBlock().breakNaturally();
			event.setCancelled(true);
		} else if (event.getCause().equals(RemoveCause.EXPLOSION)) {
			event.setCancelled(true);
		}

	}
}
