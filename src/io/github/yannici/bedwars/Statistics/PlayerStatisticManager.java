package io.github.yannici.bedwars.Statistics;

import io.github.yannici.bedwars.ChatWriter;
import io.github.yannici.bedwars.Main;
import io.github.yannici.bedwars.Database.DatabaseManager;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

public class PlayerStatisticManager {
    
    public static String tableName = "stats_players";
    
    private Map<OfflinePlayer, PlayerStatistic> playerStatistic = null;
    private FileConfiguration fileDatabase = null;
    private File databaseFile = null;

    public PlayerStatisticManager() {
        this.playerStatistic = new HashMap<OfflinePlayer, PlayerStatistic>();
        this.fileDatabase = null;
    }
    
    public void initialize()  {
        if(!Main.getInstance().getBooleanConfig("statistics.enabled", false)) {
            return;
        }
        
        if(Main.getInstance().getStatisticStorageType() == StorageType.YAML) {
            File file = new File(Main.getInstance().getDataFolder() + "/database/" + DatabaseManager.DBPrefix + PlayerStatisticManager.tableName + ".yml");
            this.loadYml(file);
        }
    }
    
    public void storeStatistic(PlayerStatistic statistic) {
        if(Main.getInstance().getStatisticStorageType() == StorageType.YAML) {
            this.storeYamlStatistic(statistic);
        } else {
            this.storeDatabaseStatistic(statistic);
        }
    }
    
    private synchronized void storeYamlStatistic(PlayerStatistic statistic) {
        String keyValue = String.valueOf(statistic.getValue(statistic.getKeyField()));
        
        try {
            for(String field : statistic.getFields().keySet()) {
                this.fileDatabase.set("data." + keyValue + "." + field, statistic.getValue(field));
            }
            
            this.fileDatabase.save(this.databaseFile);
        } catch(Exception ex) {
            Main.getInstance().getServer().getConsoleSender().sendMessage(ChatWriter.pluginMessage(ChatColor.RED + "Couldn't store statistic data for player with uuid: " + keyValue));
        }
    }
    
    private void storeDatabaseStatistic(PlayerStatistic statistic) {
        
    }
    
    public void loadStatistic(PlayerStatistic statistic) {
        if(Main.getInstance().getStatisticStorageType() == StorageType.YAML) {
            this.loadYamlStatistic(statistic);
        } else {
            this.loadDatabaseStatistic(statistic);
        }
    }
    
    private void loadYamlStatistic(PlayerStatistic statistic) {
        
    }
    
    private void loadDatabaseStatistic(PlayerStatistic statistic) {
        
    }
    
    public PlayerStatistic getStatistic(OfflinePlayer player) { 
        return this.playerStatistic.get(player);
    }
    
    public FileConfiguration getDatabaseFile() {
        return this.fileDatabase;
    }
    
    private void loadYml(File ymlFile) {
        try {
            YamlConfiguration config = null;
            Map<OfflinePlayer, PlayerStatistic> map = new HashMap<OfflinePlayer, PlayerStatistic>();
            
            this.databaseFile = ymlFile;
            
            if(!ymlFile.exists()) {
                config = new YamlConfiguration();
                config.createSection("data");
                config.save(ymlFile);
            } else {
                config = YamlConfiguration.loadConfiguration(ymlFile);
            }
            
            this.fileDatabase = config;
            
            ConfigurationSection dataSection = config.getConfigurationSection("data");
            for(String key : dataSection.getKeys(false)) {
                PlayerStatistic statistic = new PlayerStatistic();
                ConfigurationSection dataEntry = dataSection.getConfigurationSection(key);
                
                for(String field : statistic.getFields().keySet()) {
                    Object value = dataEntry.get(field);
                    statistic.setValue(field, value);
                }
                
                map.put(Main.getInstance().getServer().getOfflinePlayer(UUID.fromString(key)), statistic);
            }
            
            this.playerStatistic = map;
        } catch(Exception ex) {
            ex.printStackTrace();
        }
    }

}
