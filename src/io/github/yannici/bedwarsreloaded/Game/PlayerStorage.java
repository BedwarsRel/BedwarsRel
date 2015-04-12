package io.github.yannici.bedwarsreloaded.Game;

import io.github.yannici.bedwarsreloaded.Main;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;

public class PlayerStorage {

    private Player player = null;

    private ItemStack[] inventory = null;
    private ItemStack[] armor = null;
    private float xp = 0.0F;
    private Collection<PotionEffect> effects = null;
    private GameMode mode = null;
    private Location left = null;
    private int level = 0;

    public PlayerStorage(Player p) {
        super();

        this.player = p;
    }

    public void store() {
        this.inventory = this.player.getInventory().getContents();
        this.armor = this.player.getInventory().getArmorContents();
        this.xp = Float.valueOf(this.player.getExp());
        this.effects = this.player.getActivePotionEffects();
        this.mode = this.player.getGameMode();
        this.left = this.player.getLocation();
        this.level = this.player.getLevel();
    }

    public void clean() {

        PlayerInventory inv = this.player.getInventory();
        inv.setArmorContents(new ItemStack[4]);
        inv.setContents(new ItemStack[]{});

        this.player.setAllowFlight(false);
        this.player.setFlying(false);
        this.player.setExp(0.0F);
        this.player.setLevel(0);
        this.player.setSneaking(false);
        this.player.setSprinting(false);
        this.player.setFoodLevel(20);
        this.player.setMaxHealth(20.0D);
        this.player.setHealth(20.0D);
        this.player.setFireTicks(0);

        if (this.player.isInsideVehicle()) {
            this.player.leaveVehicle();
        }

        for (PotionEffect e : this.player.getActivePotionEffects()) {
            this.player.removePotionEffect(e.getType());
        }

        this.player.updateInventory();
    }

    public void restore() {
        this.player.getInventory().setContents(this.inventory);
        this.player.getInventory().setArmorContents(this.armor);
        this.player.setGameMode(this.mode);

        if (this.mode == GameMode.CREATIVE) {
            this.player.setAllowFlight(true);
        }

        this.player.addPotionEffects(this.effects);
        this.player.setExp(this.xp);
        this.player.setLevel(this.level);
    }
    
    public Location getLeft() {
    	return this.left;
    }

    public void loadLobbyInventory() {
    	// Choose team (Wool)
        ItemStack teamSelection = new ItemStack(Material.BED, 1);
        ItemMeta im = teamSelection.getItemMeta();
        im.setDisplayName(Main._l("lobby.chooseteam"));
        teamSelection.setItemMeta(im);
        this.player.getInventory().addItem(teamSelection);
        
        // Leave Game (Slimball)
        ItemStack leaveGame = new ItemStack(Material.SLIME_BALL, 1);
        im = leaveGame.getItemMeta();
        im.setDisplayName(Main._l("lobby.leavegame"));
        leaveGame.setItemMeta(im);
        this.player.getInventory().setItem(8, leaveGame);

        if(this.player.hasPermission("bw.setup")) {
        	// Force start game (Diamond)
            ItemStack startGame = new ItemStack(Material.DIAMOND, 1);
            im = startGame.getItemMeta();
            im.setDisplayName(Main._l("lobby.startgame"));
            startGame.setItemMeta(im);
            this.player.getInventory().addItem(startGame);
        }

        this.player.updateInventory();
    }

    @SuppressWarnings("deprecation")
    public void openTeamSelection(Game game) {
        HashMap<String, Team> teams = game.getTeams();

        Inventory inv = Bukkit.createInventory(this.player, (teams.size()-teams.size()%9)+9, Main._l("lobby.chooseteam"));
        for(Team team : teams.values()) {
            ArrayList<Player> players = team.getPlayers();
            if(players.size() >= team.getMaxPlayers()) {
                continue;
            }

            ItemStack is = new ItemStack(Material.WOOL, 1, team.getColor().getDyeColor().getData());
            ItemMeta im = is.getItemMeta();
            im.setDisplayName(team.getChatColor() + team.getName());
            ArrayList<String> teamplayers = new ArrayList<>();
            
            int teamPlayerSize = team.getPlayers().size();
            int maxPlayers = team.getMaxPlayers();
            
            String current = "0";
            if (teamPlayerSize >= maxPlayers) {
                current = ChatColor.RED + String.valueOf(teamPlayerSize);
            } else {
                current = ChatColor.YELLOW + String.valueOf(teamPlayerSize);
            }
            
            teamplayers.add(ChatColor.GRAY + "(" + current + ChatColor.GRAY + "/" + ChatColor.YELLOW + String.valueOf(maxPlayers) + ChatColor.GRAY + ")");
            teamplayers.add(ChatColor.WHITE + "---------");
            
            for(Player teamPlayer : players) {
                teamplayers.add(team.getChatColor() + teamPlayer.getName());
            }

            im.setLore(teamplayers);
            is.setItemMeta(im);
            inv.addItem(is);
        }

        this.player.openInventory(inv);
    }

}
