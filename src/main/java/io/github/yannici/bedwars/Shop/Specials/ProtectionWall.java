package io.github.yannici.bedwars.Shop.Specials;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import com.google.common.collect.ImmutableMap;

import io.github.yannici.bedwars.ChatWriter;
import io.github.yannici.bedwars.Main;
import io.github.yannici.bedwars.Utils;
import io.github.yannici.bedwars.Game.Game;

public class ProtectionWall extends SpecialItem {

	private List<Block> wallBlocks = null;
	private Player owner = null;
	private Game game = null;
	private int livingTime = 0;
	private BukkitTask task = null;

	public ProtectionWall() {
		super();
		this.wallBlocks = new ArrayList<Block>();
		this.owner = null;
		this.game = null;
	}

	@Override
	public Material getItemMaterial() {
		return Utils.getMaterialByConfig("specials.protection-wall.item", Material.BRICK);
	}

	public List<Block> getWallBlocks() {
		return this.wallBlocks;
	}

	@Override
	public Material getActivatedMaterial() {
		return null;
	}

	public Player getOwner() {
		return this.owner;
	}

	public Game getGame() {
		return this.game;
	}

	public int getLivingTime() {
		return this.livingTime;
	}

	public void create(Player player, Game game) {
		this.owner = player;
		this.game = game;

		int breakTime = Main.getInstance().getIntConfig("specials.protection-wall.break-time", 0);
		int waitTime = Main.getInstance().getIntConfig("specials.protection-wall.wait-time", 20);
		int width = Main.getInstance().getIntConfig("specials.protection-wall.width", 4);
		int height = Main.getInstance().getIntConfig("specials.protection-wall.height", 4);
		int distance = Main.getInstance().getIntConfig("specials.protection-wall.distance", 2);
		boolean canBreak = Main.getInstance().getBooleanConfig("specials.protection-wall.can-break", true);
		Material blockMaterial = Utils.getMaterialByConfig("specials.protection-wall.block", Material.SANDSTONE);

		if (width % 2 == 0) {
			try {
				throw new IllegalArgumentException("The width of a protection block has to be odd!");
			} catch (IllegalArgumentException ex) {
				ex.printStackTrace();
			}

			return;
		}

		if (player.getEyeLocation().getBlock().getType() != Material.AIR) {
			player.sendMessage(ChatWriter.pluginMessage(Main._l("ingame.specials.protection-wall.not-usable-here")));
			return;
		}

		if (waitTime > 0) {
			ProtectionWall livingWall = this.getLivingWall();
			if (livingWall != null) {
				int waitLeft = waitTime - livingWall.getLivingTime();
				player.sendMessage(ChatWriter.pluginMessage(Main._l("ingame.specials.protection-wall.left",
						ImmutableMap.of("time", String.valueOf(waitLeft)))));
				return;
			}
		}

		Location wallLocation = Utils.getDirectionLocation(player.getLocation(), distance);
		ItemStack usedStack = player.getInventory().getItemInHand();
		usedStack.setAmount(usedStack.getAmount() - 1);
		player.getInventory().setItem(player.getInventory().getHeldItemSlot(), usedStack);
		player.updateInventory();

		BlockFace face = Utils.getCardinalDirection(player.getLocation());
		int widthStart = (int) Math.floor(((double) width) / 2.0);

		for (int w = widthStart * (-1); w < width - widthStart; w++) {
			for (int h = 0; h < height; h++) {
				Location wallBlock = wallLocation.clone();

				switch (face) {
				case SOUTH:
				case NORTH:
				case SELF:
					wallBlock.add(0, h, w);
					break;
				case WEST:
				case EAST:
					wallBlock.add(w, h, 0);
					break;
				case SOUTH_EAST:
					wallBlock.add(w, h, w);
					break;
				case SOUTH_WEST:
					wallBlock.add(w, h, w * (-1));
					break;
				case NORTH_EAST:
					wallBlock.add(w * (-1), h, w);
					break;
				case NORTH_WEST:
					wallBlock.add(w * (-1), h, w * (-1));
					break;
				default:
					continue;
				}

				Block block = wallBlock.getBlock();
				if (!block.getType().equals(Material.AIR)) {
					continue;
				}

				block.setType(blockMaterial);
				if (!canBreak) {
					game.getRegion().addPlacedUnbreakableBlock(wallBlock.getBlock(), null);
				} else {
					game.getRegion().addPlacedBlock(wallBlock.getBlock(), null);
				}
				this.wallBlocks.add(block);
			}
		}

		if (breakTime > 0 || waitTime > 0) {
			this.createTask(breakTime, waitTime);
			game.addSpecialItem(this);
		}
	}

	private void createTask(final int breakTime, final int waitTime) {
		this.task = new BukkitRunnable() {

			@Override
			public void run() {
				ProtectionWall.this.livingTime++;

				if (breakTime > 0) {
					if (ProtectionWall.this.livingTime == breakTime) {
						for (Block block : ProtectionWall.this.wallBlocks) {
							block.getChunk().load(true);
							block.setType(Material.AIR);
							ProtectionWall.this.game.getRegion().removePlacedUnbreakableBlock(block);
						}
					}
				}

				if (ProtectionWall.this.livingTime >= waitTime && waitTime > 0) {
					ProtectionWall.this.game.removeRunningTask(this);
					ProtectionWall.this.game.removeSpecialItem(ProtectionWall.this);
					ProtectionWall.this.task = null;
					this.cancel();
					return;
				}

				if (breakTime > 0) {
					if (waitTime <= 0 && ProtectionWall.this.livingTime >= breakTime) {
						ProtectionWall.this.game.removeRunningTask(this);
						ProtectionWall.this.game.removeSpecialItem(ProtectionWall.this);
						ProtectionWall.this.task = null;
						this.cancel();
						return;
					}
				}
			}
		}.runTaskTimer(Main.getInstance(), 20L, 20L);
		this.game.addRunningTask(this.task);
	}

	private ProtectionWall getLivingWall() {
		for (SpecialItem item : game.getSpecialItems()) {
			if (!(item instanceof ProtectionWall)) {
				continue;
			}

			ProtectionWall wall = (ProtectionWall) item;
			if (!wall.getOwner().equals(this.getOwner())) {
				continue;
			}

			return wall;
		}

		return null;
	}

}
