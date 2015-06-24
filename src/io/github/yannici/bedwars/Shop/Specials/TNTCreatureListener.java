package io.github.yannici.bedwars.Shop.Specials;

import io.github.yannici.bedwars.Main;
import io.github.yannici.bedwars.Game.Game;
import io.github.yannici.bedwars.Game.GameState;

import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

public class TNTCreatureListener implements Listener {
	
	public TNTCreatureListener() {
		try {
			// register entities
			Class<?> tntRegisterClass = Main.getInstance().getVersionRelatedClass("TNTCreatureRegister");
			ITNTCreatureRegister register = (ITNTCreatureRegister) tntRegisterClass.newInstance();
			register.registerEntities(Main.getInstance().getIntConfig("specials.tntcreature.entity-id", 91));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void onInteract(PlayerInteractEvent event) {
		if(event.getPlayer() == null) {
			return;
		}
		
		if(event.getPlayer().getItemInHand() == null) {
			return;
		}
		
		Player player = event.getPlayer();
		TNTCreature creature = new TNTCreature();
		ItemStack inHand = player.getItemInHand();
		
		if(inHand.getType() != creature.getItemMaterial()) {
			return;
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
		
		creature.setPlayer(player);
		creature.setGame(game);
		creature.run();
	}

}
