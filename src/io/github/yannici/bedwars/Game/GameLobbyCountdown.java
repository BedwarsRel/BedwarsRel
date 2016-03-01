package io.github.yannici.bedwars.Game;

import java.lang.reflect.Method;
import java.util.ArrayList;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import com.google.common.collect.ImmutableMap;

import io.github.yannici.bedwars.Main;
import io.github.yannici.bedwars.SoundMachine;

public class GameLobbyCountdown extends BukkitRunnable {

	private Game game = null;
	private int counter = 0;
	private int lobbytime;
	private int lobbytimeWhenFull;
	private GameLobbyCountdownRule rule = null;

	public GameLobbyCountdown(Game game) {
		this.game = game;
		this.counter = Main.getInstance().getConfig().getInt("lobbytime");
		this.rule = Main.getInstance().getLobbyCountdownRule();
		this.lobbytime = this.counter;
		this.lobbytimeWhenFull = Main.getInstance().getConfig().getInt("lobbytime-full");
	}

	public void setRule(GameLobbyCountdownRule rule) {
		this.rule = rule;
	}

	@Override
	public void run() {
		ArrayList<Player> players = this.game.getPlayers();
		float xpPerLevel = 1.0F / this.lobbytime;

		if (this.game.getState() != GameState.WAITING) {
			this.game.setGameLobbyCountdown(null);
			this.cancel();
			return;
		}

		if (this.counter > this.lobbytimeWhenFull && this.game.getPlayerAmount() == this.game.getMaxPlayers()) {
			this.counter = this.lobbytimeWhenFull;
			this.game.broadcast(
					ChatColor.YELLOW + Main._l("lobby.countdown",
							ImmutableMap.of("sec", ChatColor.RED.toString() + this.counter + ChatColor.YELLOW)),
					players);
		}

		for (Player p : players) {
			p.setLevel(this.counter);
			if (this.counter == this.lobbytime) {
				p.setExp(1.0F);
			} else {
				p.setExp(1.0F - (xpPerLevel * (this.lobbytime - this.counter)));
			}

		}

		if (this.counter == this.lobbytime) {
			this.game.broadcast(
					ChatColor.YELLOW + Main._l("lobby.countdown",
							ImmutableMap.of("sec", ChatColor.RED.toString() + this.counter + ChatColor.YELLOW)),
					players);
		}

		if (!this.rule.isRuleMet(this.game)) {
			this.game.broadcast(ChatColor.RED + Main._l("lobby.cancelcountdown." + this.rule.name()), players);
			this.counter = this.lobbytime;
			for (Player p : players) {
				p.setLevel(0);
				p.setExp(0.0F);
			}

			this.game.setGameLobbyCountdown(null);
			this.cancel();
		}

		if (this.counter <= 10 && this.counter > 0) {
			this.game.broadcast(
					ChatColor.YELLOW + Main._l("lobby.countdown",
							ImmutableMap.of("sec", ChatColor.RED.toString() + this.counter + ChatColor.YELLOW)),
					players);

			Class<?> titleClass = null;
			Method showTitle = null;
			String title = ChatColor.translateAlternateColorCodes('&',
					Main.getInstance().getStringConfig("titles.countdown.format", "&3{countdown}"));
			title = title.replace("{countdown}", String.valueOf(this.counter));

			if (Main.getInstance().getBooleanConfig("titles.countdown.enabled", true)) {
				try {
					titleClass = Main.getInstance().getVersionRelatedClass("Title");
					showTitle = titleClass.getMethod("showTitle", Player.class, String.class, double.class,
							double.class, double.class);
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}

			for (Player player : players) {
				player.playSound(player.getLocation(), SoundMachine.get("CLICK", "UI_BUTTON_CLICK"), 20.0F, 20.0F);

				if (titleClass == null) {
					continue;
				}

				try {
					showTitle.invoke(null, player, title, 0.2, 0.6, 0.2);
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		}

		if (this.counter == 0) {
			this.game.setGameLobbyCountdown(null);
			this.cancel();
			for (Player player : players) {
				player.playSound(player.getLocation(), SoundMachine.get("LEVEL_UP", "ENTITY_PLAYER_LEVELUP"), 20.0F, 20.0F);
				player.setLevel(0);
				player.setExp(0.0F);
			}

			this.game.start(Main.getInstance().getServer().getConsoleSender());
			return;
		}

		this.counter--;
	}

}
