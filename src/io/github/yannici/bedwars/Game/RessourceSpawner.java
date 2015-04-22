package io.github.yannici.bedwars.Game;

import io.github.yannici.bedwars.Utils;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Location;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.SerializableAs;
import org.bukkit.entity.Item;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

@SerializableAs("RessourceSpawner")
public class RessourceSpawner implements Runnable, ConfigurationSerializable {

	private Game game = null;
	private Location location = null;
	private int interval = 1000;
	private ItemStack itemstack;

	public RessourceSpawner(Map<String, Object> deserialize) {
		this.location = (Location) deserialize.get("location");
		this.itemstack = (ItemStack) deserialize.get("itemstack");
		this.interval = Integer
				.parseInt(deserialize.get("interval").toString());
	}

	public RessourceSpawner(Game game, int interval, Location location,
			ItemStack itemstack) {
		this.game = game;
		this.interval = interval;
		this.location = location;
		this.itemstack = itemstack;
	}

	public int getInterval() {
		return this.interval;
	}

	public void setGame(Game game) {
		this.game = game;
	}

	@Override
	public void run() {
		Location dropLocation = this.location.getBlock().getRelative(BlockFace.UP).getLocation();
		Item item = this.game.getRegion().getWorld()
				.dropItemNaturally(dropLocation, this.itemstack);
		
		double vectorX = 0.08*(Utils.randInt(-1, 1));
		double vectorZ = 0.08*(Utils.randInt(-1, 1));
		
		item.teleport(dropLocation);
		item.setVelocity(new Vector(vectorX, 0.1, vectorZ));
	}

	@Override
	public Map<String, Object> serialize() {
		HashMap<String, Object> rs = new HashMap<>();

		rs.put("location", this.location);
		rs.put("interval", this.interval);
		rs.put("itemstack", this.itemstack);
		return rs;
	}

}