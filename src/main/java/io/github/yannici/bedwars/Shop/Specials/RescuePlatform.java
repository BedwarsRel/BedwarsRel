package io.github.yannici.bedwars.Shop.Specials;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import io.github.yannici.bedwars.Main;
import io.github.yannici.bedwars.Utils;
import io.github.yannici.bedwars.Game.Game;

public class RescuePlatform extends SpecialItem {

	private int livingTime = 0;
	private Player activatedPlayer = null;
	private List<Block> platformBlocks = null;
	private BukkitTask task = null;
	private Game game = null;

	public RescuePlatform() {
		super();

		this.platformBlocks = new ArrayList<Block>();
	}

	@Override
	public Material getItemMaterial() {
		return Utils.getMaterialByConfig("specials.rescue-platform.item", Material.BLAZE_ROD);
	}

	public int getLivingTime() {
		return this.livingTime;
	}

	public Player getPlayer() {
		return this.activatedPlayer;
	}

	public void addPlatformBlock(Block block) {
		this.platformBlocks.add(block);
	}

	public void runTask() {
		if (Main.getInstance().getConfig().getInt("specials.rescue-platform.break-time", 10) == 0
				&& Main.getInstance().getConfig().getInt("specials.rescue-platform.using-wait-time", 20) == 0) {
			// no break and no wait time ;)
			return;
		}

		this.task = new BukkitRunnable() {

			@Override
			public void run() {
				RescuePlatform.this.livingTime++;
				if (RescuePlatform.this.livingTime == Main.getInstance().getConfig()
						.getInt("specials.rescue-platform.break-time", 10)) {
					for (Block block : RescuePlatform.this.platformBlocks) {
						block.getChunk().load(true);
						block.setType(Material.AIR);
						RescuePlatform.this.game.getRegion().removePlacedUnbreakableBlock(block);
					}
				}

				int wait = Main.getInstance().getConfig().getInt("specials.rescue-platform.using-wait-time", 20);
				if (RescuePlatform.this.livingTime >= wait && wait > 0) {
					RescuePlatform.this.game.removeRunningTask(this);
					RescuePlatform.this.game.removeSpecialItem(RescuePlatform.this);
					RescuePlatform.this.task = null;
					this.cancel();
					return;
				}

				if (wait <= 0 && RescuePlatform.this.livingTime >= Main.getInstance().getConfig()
						.getInt("specials.rescue-platform.break-time", 10)) {
					RescuePlatform.this.game.removeRunningTask(this);
					RescuePlatform.this.game.removeSpecialItem(RescuePlatform.this);
					RescuePlatform.this.task = null;
					this.cancel();
					return;
				}
			}
		}.runTaskTimer(Main.getInstance(), 20L, 20L);
		this.game.addRunningTask(this.task);
	}

	@Override
	public Material getActivatedMaterial() {
		// not needed
		return null;
	}

	public void setActivatedPlayer(Player player) {
		this.activatedPlayer = player;
	}

	public void setGame(Game game) {
		this.game = game;
	}

}
