package io.github.yannici.bedwars.Listener;

import org.bukkit.event.Listener;

import io.github.yannici.bedwars.Main;

public abstract class BaseListener implements Listener {

	public BaseListener() {
		this.registerEvents();
	}

	private void registerEvents() {
		Main.getInstance().getServer().getPluginManager().registerEvents(this, Main.getInstance());
	}

}