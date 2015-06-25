package io.github.yannici.bedwars.Shop.Specials;

import io.github.yannici.bedwars.ChatWriter;
import io.github.yannici.bedwars.Main;
import io.github.yannici.bedwars.Game.Game;
import io.github.yannici.bedwars.Game.Team;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

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

	public void run(final Location start) {
		// as task
		new BukkitRunnable() {
			
			@Override
			public void run() {
				Player target = TNTCreature.this.findTargetPlayer();
				if(target == null) {
					TNTCreature.this.player.sendMessage(ChatWriter.pluginMessage(ChatColor.RED + Main._l("ingame.specials.tntsheep.no-target-found")));
					return;
				}
				
				Team playerTeam = TNTCreature.this.game.getPlayerTeam(TNTCreature.this.player);
				
				try {
					// register entity
					Class<?> tntRegisterClass = Main.getInstance().getVersionRelatedClass("TNTCreatureRegister");
					ITNTCreatureRegister register = (ITNTCreatureRegister) tntRegisterClass.newInstance();
					ITNTCreature creature = register.spawnCreature(start, TNTCreature.this.player, target, playerTeam.getColor().getDyeColor());
					creature.getTNT().setFuseTicks(Main.getInstance().getIntConfig("specials.tntcreature.fuse-time", 8)*20);
					creature.getTNT().setIsIncendiary(false);
					creature.getTNT().setTicksLived(5);
				} catch(Exception ex) {
					ex.printStackTrace();
				}
			}
		}.runTask(Main.getInstance());
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
