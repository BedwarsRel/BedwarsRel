package io.github.yannici.bedwars.Shop.Specials;

import io.github.yannici.bedwars.Main;
import io.github.yannici.bedwars.Game.Game;
import io.github.yannici.bedwars.Game.GameState;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.SpawnEgg;

public class TNTSheepListener implements Listener {
	
	@SuppressWarnings("deprecation")
	public void onInteract(PlayerInteractEvent event) {
		if(event.getPlayer() == null) {
			return;
		}
		
		if(event.getPlayer().getItemInHand() == null) {
			return;
		}
		
		Player player = event.getPlayer();
		TNTSheep sheep = new TNTSheep();
		ItemStack inHand = player.getItemInHand();
		
		if(inHand.getType() != sheep.getItemMaterial()) {
			return;
		}
		
		if(inHand.getData() instanceof SpawnEgg) {
			if(((SpawnEgg) inHand.getData()).getSpawnedType() != EntityType.fromId(sheep.getEntityTypeId())) {
				return;
			}
		}
		
		Game game = Main.getInstance().getGameManager().getGameOfPlayer(player);
		
		if(game == null) {
			return;
		}
		
		if(game.getState() != GameState.RUNNING) {
			return;
		}
		
		if(game.isSpectator(player)) {
			return;
		}
		
		sheep.setPlayer(player);
		sheep.setGame(game);
		sheep.run();
	}

}
