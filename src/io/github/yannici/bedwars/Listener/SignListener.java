package io.github.yannici.bedwars.Listener;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.SignChangeEvent;

import io.github.yannici.bedwars.Main;
import io.github.yannici.bedwars.Game.Game;

public class SignListener extends BaseListener {

	@EventHandler
	public void onSignChange(SignChangeEvent sce) {
		String firstLine = sce.getLine(0).trim();
		if (!firstLine.equals("[bw]")) {
			return;
		}

		Player player = sce.getPlayer();
		if (!player.hasPermission("bw.setup")) {
			return;
		}

		String gameName = sce.getLine(1).trim();
		Game game = Main.getInstance().getGameManager().getGame(gameName);

		if (game == null) {
			String notfound = Main._l("errors.gamenotfoundsimple");
			if (notfound.length() > 16) {
				String[] splitted = notfound.split(" ", 4);
				for (int i = 0; i < splitted.length; i++) {
					sce.setLine(i, ChatColor.RED + splitted[i]);
				}
			} else {
				sce.setLine(0, ChatColor.RED + notfound);
				sce.setLine(1, "");
				sce.setLine(2, "");
				sce.setLine(3, "");
			}

			return;
		}

		sce.setCancelled(true);
		game.addJoinSign(sce.getBlock().getLocation());
		game.updateSigns();
	}

}
