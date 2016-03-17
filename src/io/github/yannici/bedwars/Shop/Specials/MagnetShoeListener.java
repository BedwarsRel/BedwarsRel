package io.github.yannici.bedwars.Shop.Specials;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;

import io.github.yannici.bedwars.Main;
import io.github.yannici.bedwars.Utils;
import io.github.yannici.bedwars.Game.Game;
import io.github.yannici.bedwars.Game.GameState;

public class MagnetShoeListener implements Listener {

	public MagnetShoeListener() {
		super();
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onDamage(EntityDamageByEntityEvent ev) {
		if (ev.isCancelled()) {
			return;
		}

		if (!(ev.getEntity() instanceof Player)) {
			return;
		}

		Game game = Main.getInstance().getGameManager().getGameOfPlayer((Player) ev.getEntity());
		if (game == null) {
			return;
		}

		if (game.getState() != GameState.RUNNING) {
			return;
		}

		Player player = (Player) ev.getEntity();
		ItemStack boots = player.getInventory().getBoots();

		if (boots == null) {
			return;
		}

		MagnetShoe shoe = new MagnetShoe();
		if (boots.getType() != shoe.getItemMaterial()) {
			return;
		}

		if (this.rollKnockbackDice()) {
			ev.setCancelled(true);
			player.damage(ev.getDamage());
		}
	}

	private boolean rollKnockbackDice() {
		int target = Main.getInstance().getIntConfig("specials.magnetshoe.probability", 75);
		int roll = Utils.randInt(0, 100);

		return (roll <= target);
	}

}
