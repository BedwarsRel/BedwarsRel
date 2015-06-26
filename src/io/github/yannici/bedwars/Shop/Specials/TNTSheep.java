package io.github.yannici.bedwars.Shop.Specials;

import io.github.yannici.bedwars.ChatWriter;
import io.github.yannici.bedwars.Main;
import io.github.yannici.bedwars.Game.Game;
import io.github.yannici.bedwars.Game.GameState;
import io.github.yannici.bedwars.Game.Team;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

public class TNTSheep extends SpecialItem {
	
	private Player player = null;
	private Game game = null;
	private ITNTSheep creature = null;

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
	
	public ITNTSheep getCreature() {
		return this.creature;
	}

	public void run(final Location start) {
		ItemStack usedStack = this.player.getItemInHand().clone();
		usedStack.setAmount(1);
		this.player.getInventory().remove(usedStack);
		
		final Player target = this.findTargetPlayer();
		if(target == null) {
			this.player.sendMessage(ChatWriter.pluginMessage(ChatColor.RED + Main._l("ingame.specials.tntsheep.no-target-found")));
			return;
		}
		
		// as task
		new BukkitRunnable() {
			
			@Override
			public void run() {
				final TNTSheep that = TNTSheep.this;
				Team playerTeam = TNTSheep.this.game.getPlayerTeam(TNTSheep.this.player);
				
				try {
					// register entity
					Class<?> tntRegisterClass = Main.getInstance().getVersionRelatedClass("TNTSheepRegister");
					ITNTSheepRegister register = (ITNTSheepRegister) tntRegisterClass.newInstance();
					TNTSheep.this.creature = register.spawnCreature(that, start, TNTSheep.this.player, target, playerTeam.getColor().getDyeColor());
					
					BukkitTask task = new BukkitRunnable() {
						
						@Override
						public void run() {
							that.getGame().removeRunningTask(this);
							that.getGame().getRegion().removeRemovingEntity(that.getCreature().getTNT());
							that.getGame().getRegion().removeRemovingEntity(that.getCreature().getTNT().getVehicle());
						}
					}.runTaskLater(Main.getInstance(), (Main.getInstance().getIntConfig("specials.tntsheep.fuse-time", 8)*20)-5);
					
					BukkitTask taskEnd = new BukkitRunnable() {
						
						@Override
						public void run() {
							that.getCreature().getTNT().getVehicle().remove();
							that.getCreature().getTNT().remove();
							that.getGame().removeRunningTask(this);
						}
					}.runTaskLater(Main.getInstance(), (Main.getInstance().getIntConfig("specials.tntsheep.fuse-time", 8)*20)+1);
					
					TNTSheep.this.game.addRunningTask(task);
					TNTSheep.this.game.addRunningTask(taskEnd);
					TNTSheep.this.game.addSpecialItem(that);
				} catch(Exception ex) {
					ex.printStackTrace();
				}
			}
		}.runTask(Main.getInstance());
	}
	
	public void updateTNT() {
	    new BukkitRunnable() {
            
	        @Override
	        public void run() {
	            TNTSheep that = TNTSheep.this;
	            
	            if(that.game.isStopping()
	    				|| that.game.getState() != GameState.RUNNING) {
	    			return;
	    		}
	            
	            if(that.creature == null) {
	                return;
	            }
	            
	            if(that.creature.getTNT() == null) {
	                return;
	            }
	            
	            TNTPrimed old = that.creature.getTNT();
	            int fuse = old.getFuseTicks();
	            Location oldLoc = old.getLocation();
	            old.leaveVehicle();
	            old.remove();
	            
				TNTPrimed primed = (TNTPrimed) that.game.getRegion().getWorld().spawnEntity(oldLoc, EntityType.PRIMED_TNT);
	            primed.setFuseTicks(fuse);
	            primed.setIsIncendiary(false);
	            that.creature.setPassenger(primed);
	            
	            if(primed.getFuseTicks() >= 60) {
	            	that.updateTNT();
	            }
	        }
	        
	    }.runTaskLater(Main.getInstance(), 60L);
	}
	
	private Player findTargetPlayer() {
		Player foundPlayer = null;
		double distance = Double.MAX_VALUE;
		
		for(Player p : this.game.getTeamPlayers()) {
			double dist = this.player.getLocation().distance(p.getLocation());
			if(dist < distance
			        && p != this.player) {
				foundPlayer = p;
			}
		}
		
		return foundPlayer;
	}

}
