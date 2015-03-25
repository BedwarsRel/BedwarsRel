package io.github.yannici.bedwarsreloaded.Listener;

import io.github.yannici.bedwarsreloaded.Main;

import org.bukkit.event.Listener;

public abstract class BaseListener implements Listener {
	
	public BaseListener() {
		this.registerEvents();
	}
	
	private void registerEvents() {
	    Main.getInstance().getServer().getPluginManager().registerEvents(this, Main.getInstance());
	}

}