package io.github.yannici.bedwars.Shop.Specials;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import io.github.yannici.bedwars.Main;
import io.github.yannici.bedwars.Game.Game;
import io.github.yannici.bedwars.Game.GameState;

public abstract class SpecialItem {

	private static List<Class<? extends SpecialItem>> availableSpecials = new ArrayList<Class<? extends SpecialItem>>();

	public SpecialItem() {
		super();
	}

	public abstract Material getItemMaterial();

	public abstract Material getActivatedMaterial();

	public boolean returnPlayerEvent(Player player) {
		if (!player.getItemInHand().getType().equals(this.getItemMaterial())
				&& (!player.getItemInHand().getType().equals(this.getActivatedMaterial())
						&& this.getActivatedMaterial() != null)) {
			return true;
		}

		Game game = Main.getInstance().getGameManager().getGameOfPlayer(player);

		if (game == null) {
			return true;
		}

		if (game.getState() != GameState.RUNNING) {
			return true;
		}

		if (game.isSpectator(player)) {
			return true;
		}

		return false;
	}

	public static void loadSpecials() {
		SpecialItem.availableSpecials.add(RescuePlatform.class);
		SpecialItem.availableSpecials.add(Trap.class);
		SpecialItem.availableSpecials.add(MagnetShoe.class);
		SpecialItem.availableSpecials.add(ProtectionWall.class);
		SpecialItem.availableSpecials.add(WarpPowder.class);
		SpecialItem.availableSpecials.add(TNTSheep.class);
		SpecialItem.availableSpecials.add(Tracker.class);
		Main.getInstance().getServer().getPluginManager().registerEvents(new RescuePlatformListener(),
				Main.getInstance());
		Main.getInstance().getServer().getPluginManager().registerEvents(new TrapListener(), Main.getInstance());
		Main.getInstance().getServer().getPluginManager().registerEvents(new MagnetShoeListener(), Main.getInstance());
		Main.getInstance().getServer().getPluginManager().registerEvents(new ProtectionWallListener(),
				Main.getInstance());
		Main.getInstance().getServer().getPluginManager().registerEvents(new WarpPowderListener(), Main.getInstance());
		Main.getInstance().getServer().getPluginManager().registerEvents(new TNTSheepListener(), Main.getInstance());
		Main.getInstance().getServer().getPluginManager().registerEvents(new TrackerListener(), Main.getInstance());
	}

	public static List<Class<? extends SpecialItem>> getSpecials() {
		return SpecialItem.availableSpecials;
	}

}
