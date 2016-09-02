package io.github.bedwarsrel.BedwarsRel.Commands;

import java.util.ArrayList;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import io.github.bedwarsrel.BedwarsRel.ChatWriter;
import io.github.bedwarsrel.BedwarsRel.Main;
import io.github.bedwarsrel.BedwarsRel.Events.BedwarsOpenShopEvent;
import io.github.bedwarsrel.BedwarsRel.Game.Game;
import io.github.bedwarsrel.BedwarsRel.Game.GameState;
import io.github.bedwarsrel.BedwarsRel.Shop.NewItemShop;
import io.github.bedwarsrel.BedwarsRel.Villager.MerchantCategory;

public class OpenShopCommand extends BaseCommand {

  public OpenShopCommand(Main plugin) {
    super(plugin);
  }

  @Override
  public String getCommand() {
    return "shop";
  }

  @Override
  public String getName() {
    return Main._l("commands.shop.name");
  }

  @Override
  public String getDescription() {
    return Main._l("commands.shop.desc");
  }

  @Override
  public String[] getArguments() {
    return new String[] {};
  }

  @Override
  public boolean execute(CommandSender sender, ArrayList<String> args) {
    if (!super.hasPermission(sender) && !sender.isOp()) {
      return false;
    }

    Player player = (Player) sender;
    Game game = this.getPlugin().getGameManager().getGameOfPlayer(player);

    if (game.getState() != GameState.RUNNING) {
      return true;
    }

    if (!Main.getInstance().getBooleanConfig("use-build-in-shop", true)) {
      sender
          .sendMessage(ChatWriter.pluginMessage(ChatColor.RED + Main._l("errors.opencommandshop")));
      return true;
    }

    BedwarsOpenShopEvent openShopEvent =
        new BedwarsOpenShopEvent(game, player, game.getItemShopCategories(), null);
    Main.getInstance().getServer().getPluginManager().callEvent(openShopEvent);

    if (openShopEvent.isCancelled()) {
      return true;
    }

    if (game.getPlayerSettings(player).useOldShop()) {
      MerchantCategory.openCategorySelection(player, game);
    } else {
      NewItemShop itemShop = game.getNewItemShop(player);
      if (itemShop == null) {
        itemShop = game.openNewItemShop(player);
      }

      itemShop.setCurrentCategory(null);
      itemShop.openCategoryInventory(player);
    }
    return true;
  }

  @Override
  public String getPermission() {
    return "vip.commandshop";
  }

}
