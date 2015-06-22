package io.github.yannici.bedwars.Shop.Specials;

import io.github.yannici.bedwars.ChatWriter;
import io.github.yannici.bedwars.Main;
import io.github.yannici.bedwars.Game.Game;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public class TNTSheep extends SpecialItem {
	
	private Player player = null;
	private Game game = null;
	private TNTSheep entity = null;

	@Override
	public Material getItemMaterial() {
		return Material.MONSTER_EGG;
	}
	
	public int getEntityTypeId() {
		return 91; // Currently sheep only
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
	
	public TNTSheep getEntity() {
		return this.entity;
	}

	public void run() {
		Player target = this.findTargetPlayer();
		if(target == null) {
			this.player.sendMessage(ChatWriter.pluginMessage(ChatColor.RED + Main._l("ingame.specials.tntsheep.no-target-found")));
			return;
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
