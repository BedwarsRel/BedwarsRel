package io.github.yannici.bedwars.Shop.Specials;

import io.github.yannici.bedwars.ChatWriter;
import io.github.yannici.bedwars.Main;
import io.github.yannici.bedwars.Game.Game;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public class TNTCreature extends SpecialItem {
	
	private Player player = null;
	private Game game = null;
	private ITNTCreature creature = null;

	@Override
	public Material getItemMaterial() {
		return Material.MONSTER_EGG;
	}
	
	public int getEntityTypeId() {
		return Main.getInstance().getIntConfig("specials.tntcreature.entity-id", 91);
	}

	@Override
	public Material getActivatedMaterial() {
		return null;
	}
	
	public void setPlayer(Player player) {
		this.player = player;
	}
	
	public void setGame(Game game) {
		this.game = game;
	}
	
	public Game getGame() {
		return this.game;
	}
	
	public Player getPlayer() {
		return this.player;
	}
	
	public ITNTCreature getCreature() {
		return this.creature;
	}

	public void run() {
		Player target = this.findTargetPlayer();
		if(target == null) {
			this.player.sendMessage(ChatWriter.pluginMessage(ChatColor.RED + Main._l("ingame.specials.tntsheep.no-target-found")));
			return;
		}
		
		try {
			// register entity
			//Class<?> tntRegisterClass = Main.getInstance().getVersionRelatedClass("TNTCreatureRegister");
			//ITNTCreatureRegister register = (ITNTCreatureRegister) tntRegisterClass.newInstance();
			
		} catch(Exception ex) {
			ex.printStackTrace();
		}
		
	}
	
	private Player findTargetPlayer() {
		Player foundPlayer = null;
		double distance = Double.MAX_VALUE;
		
		for(Player p : this.game.getTeamPlayers()) {
			double dist = this.player.getLocation().distance(p.getLocation());
			if(dist < distance) {
				foundPlayer = p;
			}
		}
		
		return foundPlayer;
	}

}
