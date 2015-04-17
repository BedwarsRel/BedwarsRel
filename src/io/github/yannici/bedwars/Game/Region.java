package io.github.yannici.bedwars.Game;

import io.github.yannici.bedwars.ChatWriter;
import io.github.yannici.bedwars.Main;
import io.github.yannici.bedwars.Utils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;

public class Region {

	private Location minCorner = null;
	private Location maxCorner = null;
	private World world = null;
	private ArrayList<Block> blocks = null;

	public Region(Location pos1, Location pos2) {
		if (pos1 == null || pos2 == null) {
			return;
		}

		if (!pos1.getWorld().getName().equals(pos2.getWorld().getName())) {
			return;
		}

		this.world = pos1.getWorld();
		this.setMinMax(pos1, pos2);
	}

	public Region(World w, int x1, int y1, int z1, int x2, int y2, int z2) {
		this(new Location(w, x1, y1, z1), new Location(w, x2, y2, z2));
	}

	public boolean check() {
		return (this.minCorner != null && this.maxCorner != null && this.world != null);
	}

	private void setMinMax(Location pos1, Location pos2) {
		this.minCorner = this.getMinimumCorner(pos1, pos2);
		this.maxCorner = this.getMaximumCorner(pos1, pos2);
	}

	private Location getMinimumCorner(Location pos1, Location pos2) {
		return new Location(this.world, Math.min(pos1.getBlockX(),
				pos2.getBlockX()),
				Math.min(pos1.getBlockY(), pos2.getBlockY()), Math.min(
						pos1.getBlockZ(), pos2.getBlockZ()));
	}

	private Location getMaximumCorner(Location pos1, Location pos2) {
		return new Location(this.world, Math.max(pos1.getBlockX(),
				pos2.getBlockX()),
				Math.max(pos1.getBlockY(), pos2.getBlockY()), Math.max(
						pos1.getBlockZ(), pos2.getBlockZ()));
	}

	public ArrayList<Block> getBlocks(boolean withAir) {
		if (this.minCorner == null || this.maxCorner == null) {
			return new ArrayList<Block>();
		}

		if (!withAir && this.blocks != null) {
			return this.blocks;
		}

		ArrayList<Block> blocks = new ArrayList<>();

		for (int x = this.minCorner.getBlockX(); x <= this.maxCorner
				.getBlockX(); ++x) {
			for (int y = this.minCorner.getBlockY(); y <= this.maxCorner
					.getBlockY(); ++y) {
				for (int z = this.minCorner.getBlockZ(); z <= this.maxCorner
						.getBlockZ(); ++z) {
					Block block = this.world.getBlockAt(x, y, z);
					if ((block.getType().isBlock() && !block.getType().equals(
							Material.AIR))
							|| withAir) {
						blocks.add(this.world.getBlockAt(x, y, z));
					}
				}
			}
		}

		if (!withAir) {
			this.blocks = blocks;
		}

		return blocks;
	}

	public boolean isInRegion(Location location) {
		return (location.getBlockX() >= this.minCorner.getBlockX()
				&& location.getBlockX() <= this.maxCorner.getBlockX()
				&& location.getBlockY() >= this.minCorner.getBlockY()
				&& location.getBlockY() <= this.maxCorner.getBlockY()
				&& location.getBlockZ() >= this.minCorner.getBlockZ() && location
				.getBlockZ() <= this.maxCorner.getBlockZ());
	}

	@SuppressWarnings("deprecation")
	public void save(File file, boolean directSave) throws IOException {
		FileConfiguration cfg = YamlConfiguration.loadConfiguration(file);

		if (file.exists()) {
			file.delete();
		}

		if (directSave) {
			this.blocks = null;
		}

		for (Block b : this.getBlocks(false)) {
			ArrayList<String> config = new ArrayList<>();
			int x = b.getX();
			int y = b.getY();
			int z = b.getZ();

			config.add(Integer.toString(x));
			config.add(Integer.toString(y));
			config.add(Integer.toString(z));
			config.add(Integer.toString(b.getTypeId()));
			config.add(Byte.toString(b.getData()));

			cfg.set(Utils.implode(";", config), Boolean.toString(true));
		}

		cfg.save(file);
	}

	@SuppressWarnings("deprecation")
	public void reset(File file) {
		if (!file.exists()) {
			Main.getInstance()
					.getServer()
					.getConsoleSender()
					.sendMessage(
							ChatWriter.pluginMessage(ChatColor.RED
									+ Main._l("errors.regionnotfound")));
			return;
		}

		if (this.minCorner == null || this.maxCorner == null) {
			return;
		}

		FileConfiguration cfg = YamlConfiguration.loadConfiguration(file);
		Set<String> data = cfg.getKeys(false);
		Iterator<String> it = data.iterator();
		String[] currentData = it.next().split(";");

		for (int x = this.minCorner.getBlockX(); x <= this.maxCorner
				.getBlockX(); ++x) {
			for (int y = this.minCorner.getBlockY(); y <= this.maxCorner
					.getBlockY(); ++y) {
				for (int z = this.minCorner.getBlockZ(); z <= this.maxCorner
						.getBlockZ(); ++z) {
					Block block = this.world.getBlockAt(x, y, z);

					if (this.serializedIsAt(currentData, x, y, z)) {
						block.setTypeId(Integer.parseInt(currentData[3]));
						block.setData(Byte.parseByte(currentData[4]));
						if (it.hasNext()) {
							currentData = it.next().split(";");
						}

						continue;
					}

					block.setType(Material.AIR);
					block.setData(Byte.parseByte("0"));
				}
			}
		}

		this.blocks = this.getBlocks(false);

		Iterator<Entity> entityIterator = this.world.getEntities().iterator();
		while (entityIterator.hasNext()) {
			Entity e = entityIterator.next();
			if (e instanceof Item) {
				e.remove();
			}

			if (e instanceof LivingEntity) {
				LivingEntity le = (LivingEntity) e;
				le.setRemoveWhenFarAway(false);
			}
		}
	}

	private boolean serializedIsAt(String[] data, int x, int y, int z) {
		return (Integer.parseInt(data[0]) == x
				&& Integer.parseInt(data[1]) == y && Integer.parseInt(data[2]) == z);
	}

	@SuppressWarnings("deprecation")
	public void setBlock(Location location, int typeId, byte data,
			boolean checkContains) {
		if (checkContains && !this.isInRegion(location)) {
			return;
		}

		this.world.getBlockAt(location).setTypeIdAndData(typeId, data, false);
	}

	public World getWorld() {
		return this.minCorner.getWorld();
	}

}
