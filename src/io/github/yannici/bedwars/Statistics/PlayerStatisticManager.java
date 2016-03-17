package io.github.yannici.bedwars.Statistics;

import java.io.File;
import java.lang.reflect.Method;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import io.github.yannici.bedwars.ChatWriter;
import io.github.yannici.bedwars.Main;
import io.github.yannici.bedwars.Database.DBField;
import io.github.yannici.bedwars.Database.DBGetField;
import io.github.yannici.bedwars.Database.DBSetField;
import io.github.yannici.bedwars.Database.DatabaseManager;
import io.github.yannici.bedwars.Game.Game;

public class PlayerStatisticManager {

	private Map<OfflinePlayer, PlayerStatistic> playerStatistic = null;
	private FileConfiguration fileDatabase = null;
	private File databaseFile = null;

	public PlayerStatisticManager() {
		this.playerStatistic = new HashMap<OfflinePlayer, PlayerStatistic>();
		this.fileDatabase = null;
	}

	public void initialize() {
		if (!Main.getInstance().getBooleanConfig("statistics.enabled", false)) {
			return;
		}

		if (Main.getInstance().getStatisticStorageType() == StorageType.YAML) {
			File file = new File(Main.getInstance().getDataFolder() + "/database/" + DatabaseManager.DBPrefix
					+ PlayerStatistic.tableName + ".yml");
			this.loadYml(file);
		}

		if (Main.getInstance().getStatisticStorageType() == StorageType.DATABASE) {
			this.initializeDatabase();
		}
	}

	public void initializeDatabase() {
		Main.getInstance().getServer().getConsoleSender()
				.sendMessage(ChatWriter.pluginMessage(ChatColor.GREEN + "Loading Statistics from Database ..."));

		// create table if not exists
		String sql = this.getTableSql(new PlayerStatistic());

		try {
			Main.getInstance().getDatabaseManager().execute(sql);
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		Main.getInstance().getServer().getConsoleSender()
				.sendMessage(ChatWriter.pluginMessage(ChatColor.GREEN + "Done."));
	}

	private String getTableSql(StoringTable statistic) {
		StringBuilder builder = new StringBuilder("CREATE TABLE IF NOT EXISTS ");
		String tablename = DatabaseManager.DBPrefix + statistic.getTableName();

		builder.append("`" + tablename + "` (");
		for (DBField field : statistic.getFields().values()) {
			Method getter = field.getGetter();

			DBGetField fieldAno = getter.getAnnotation(DBGetField.class);
			if (fieldAno == null) {
				continue;
			}

			builder.append("`" + fieldAno.name() + "` ");
			builder.append(fieldAno.dbType() + " ");
			builder.append((fieldAno.notNull()) ? "NOT NULL" : "NULL");

			if (!fieldAno.defaultValue().equals("")) {
				builder.append(" DEFAULT '" + fieldAno.defaultValue() + "' ");
			}

			if (fieldAno.autoInc()) {
				builder.append(" AUTO_INCREMENT");
			}

			builder.append(",");
		}

		if (statistic.getKeyField() != null) {
			builder.append("UNIQUE (" + statistic.getKeyField() + "),");
		}

		builder.append("PRIMARY KEY (id)");
		builder.append(");");

		return builder.toString();
	}

	public void storeStatistic(PlayerStatistic statistic) {
		if (Main.getInstance().getStatisticStorageType() == StorageType.YAML) {
			this.storeYamlStatistic(statistic);
		} else {
			this.storeDatabaseStatistic(statistic);
		}
	}

	private synchronized void storeYamlStatistic(PlayerStatistic statistic) {
		String keyValue = String.valueOf(statistic.getValue(statistic.getKeyField()));
		if (keyValue == null || keyValue.equals("null")) {
			return;
		}

		try {
			for (String field : statistic.getFields().keySet()) {
				this.fileDatabase.set("data." + keyValue + "." + field, statistic.getValue(field));
			}

			this.fileDatabase.save(this.databaseFile);
		} catch (Exception ex) {
			Main.getInstance().getServer().getConsoleSender().sendMessage(ChatWriter
					.pluginMessage(ChatColor.RED + "Couldn't store statistic data for player with uuid: " + keyValue));
		}
	}

	private void storeDatabaseStatistic(PlayerStatistic statistic) {
		if (!this.playerStatistic.containsKey(statistic.getPlayer())) {
			return;
		}

		// duplicate entry fix
		if (statistic.isNew()) {
			ResultSet result = null;

			try {
				// check if is it really new ;)
				String uuid = statistic.getUUID();
				String sql = "SELECT id FROM `" + DatabaseManager.DBPrefix + statistic.getTableName()
						+ "` WHERE `uuid` = '" + uuid + "'";

				result = Main.getInstance().getDatabaseManager().query(sql);
				int num = Main.getInstance().getDatabaseManager().getRowCount(result);

				if (num > 0) {
					result.first();
					statistic.setId(result.getLong(0));
				}
			} catch (Exception ex) {
				// couldn't check, try to store anyway
			} finally {
				if (result != null) {
					try {
						Main.getInstance().getDatabaseManager().clean(result.getStatement().getConnection());
					} catch (Exception ex) {
						ex.printStackTrace();
					}
				}
			}
		}

		String updateSql = this.getStoreSQL(statistic);
		Main.getInstance().getDatabaseManager().update(updateSql);
	}

	private String getStoreSQL(StoringTable statistic) {
		StringBuilder sql = new StringBuilder();

		if (statistic.isNew()) {
			StringBuilder insertFields = new StringBuilder();
			StringBuilder fieldValues = new StringBuilder();

			sql.append("INSERT INTO `" + DatabaseManager.DBPrefix + statistic.getTableName() + "` (");
			for (DBField field : statistic.getFields().values()) {
				DBGetField anoGet = field.getGetter().getAnnotation(DBGetField.class);

				if (anoGet == null) {
					continue;
				}

				if (anoGet.name().equals("id")) {
					continue;
				}

				insertFields.append("`" + anoGet.name() + "`,");
				fieldValues.append("'" + statistic.getValue(anoGet.name()) + "',");
			}

			String ifields = insertFields.toString();
			String fvalues = fieldValues.toString();

			ifields = ifields.trim().substring(0, ifields.length() - 1);
			fvalues = fvalues.trim().substring(0, fvalues.length() - 1);

			sql.append(ifields + ") VALUES (");
			sql.append(fvalues + ")");
		} else {
			StringBuilder updateString = new StringBuilder();

			sql.append("UPDATE `" + DatabaseManager.DBPrefix + statistic.getTableName() + "` SET ");
			for (DBField field : statistic.getFields().values()) {
				DBGetField anoGet = field.getGetter().getAnnotation(DBGetField.class);

				if (anoGet == null) {
					continue;
				}

				if (anoGet.name().equals("id") || anoGet.name().equals(statistic.getKeyField())) {
					continue;
				}

				updateString.append("`" + anoGet.name() + "` = '" + statistic.getValue(anoGet.name()) + "',");
			}

			String update = updateString.toString().trim();
			update = update.substring(0, update.length() - 1);

			sql.append(update + " WHERE `" + statistic.getKeyField() + "` = '"
					+ statistic.getValue(statistic.getKeyField()) + "'");
		}

		return sql.toString();
	}

	public void loadStatistic(PlayerStatistic statistic) {
		if (Main.getInstance().getStatisticStorageType() == StorageType.YAML) {
			this.loadYamlStatistic(statistic);
		} else {
			this.loadDatabaseStatistic(statistic);
		}
	}

	private void loadYamlStatistic(PlayerStatistic statistic) {
		if (this.playerStatistic.containsKey(statistic.getPlayer())) {
			PlayerStatistic existing = this.playerStatistic.get(statistic.getPlayer());
			for (String field : statistic.getFields().keySet()) {
				if (field.equalsIgnoreCase("id")) {
					continue;
				}

				statistic.setValue(field, existing.getValue(field));
			}

			statistic.setId(1);
			return;
		}

		String keyValue = statistic.getValue(statistic.getKeyField()).toString();
		statistic.setDefault();

		if (this.fileDatabase == null) {
			this.playerStatistic.put(statistic.getPlayer(), statistic);
			return;
		}

		if (!this.fileDatabase.contains("data." + keyValue)) {
			this.playerStatistic.put(statistic.getPlayer(), statistic);
			return;
		}

		for (String field : statistic.getFields().keySet()) {
			if (field.equalsIgnoreCase("id")) {
				continue;
			}

			if (!this.fileDatabase.contains("data." + keyValue + "." + field)) {
				continue;
			}

			statistic.setValue(field, this.fileDatabase.get("data." + keyValue + "." + field));
		}

		statistic.setId(1);
		this.playerStatistic.put(statistic.getPlayer(), statistic);
	}

	public void unloadStatistic(OfflinePlayer player) {
		if (Main.getInstance().getStatisticStorageType() != StorageType.YAML) {
			this.playerStatistic.remove(player);
		}
	}

	private void loadDatabaseStatistic(PlayerStatistic statistic) {
		if (this.playerStatistic.containsKey(statistic.getPlayer())) {
			return;
		}

		ResultSet playerStatistic = null;
		Game game = null;

		try {
			playerStatistic = Main.getInstance().getDatabaseManager()
					.query("SELECT * FROM" + " `" + DatabaseManager.DBPrefix + statistic.getTableName() + "`"
							+ " WHERE `" + statistic.getKeyField() + "` = '"
							+ statistic.getValue(statistic.getKeyField()) + "'");

			// get size
			if (Main.getInstance().getDatabaseManager().getRowCount(playerStatistic) == 0) {
				statistic.setDefault();
				this.playerStatistic.put(statistic.getPlayer(), statistic);
				return;
			}

			playerStatistic.first();
			for (DBField field : statistic.getFields().values()) {
				if (field.getSetter() == null) {
					continue;
				}

				DBSetField setField = field.getSetter().getAnnotation(DBSetField.class);

				if (setField == null) {
					continue;
				}

				if (field.getSetter().getParameterTypes().length == 0) {
					continue;
				}

				// Class<?> setterType =
				// field.getSetter().getParameterTypes()[0];
				statistic.setValue(setField.name(), playerStatistic.getObject(setField.name()));
			}
		} catch (Exception ex) {
			ex.printStackTrace();
			return;
		} finally {
			try {
				Main.getInstance().getDatabaseManager().clean(playerStatistic.getStatement().getConnection());
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}

		if (statistic.getPlayer().isOnline()) {
			Player p = statistic.getPlayer().getPlayer();
			game = Main.getInstance().getGameManager().getGameOfPlayer(p);
		}

		if (game == null) {
			statistic.setOnce(true);
			return;
		}

		this.playerStatistic.put(statistic.getPlayer(), statistic);
	}

	public PlayerStatistic getStatistic(OfflinePlayer player) {
		if (player == null) {
			return null;
		}

		if (!this.playerStatistic.containsKey(player)) {
			PlayerStatistic statistic = new PlayerStatistic(player);
			statistic.load();
			if (statistic.isOnce()) {
				return statistic;
			}
		}

		return this.playerStatistic.get(player);
	}

	public FileConfiguration getDatabaseFile() {
		return this.fileDatabase;
	}

	private void loadYml(File ymlFile) {
		try {
			Main.getInstance().getServer().getConsoleSender()
					.sendMessage(ChatWriter.pluginMessage(ChatColor.GREEN + "Loading statistics from YAML-File ..."));

			YamlConfiguration config = null;
			Map<OfflinePlayer, PlayerStatistic> map = new HashMap<OfflinePlayer, PlayerStatistic>();

			this.databaseFile = ymlFile;

			if (!ymlFile.exists()) {
				ymlFile.getParentFile().mkdirs();
				ymlFile.createNewFile();

				config = new YamlConfiguration();
				config.createSection("data");
				config.save(ymlFile);
			} else {
				config = YamlConfiguration.loadConfiguration(ymlFile);
			}

			this.fileDatabase = config;

			ConfigurationSection dataSection = config.getConfigurationSection("data");
			for (String key : dataSection.getKeys(false)) {
				PlayerStatistic statistic = new PlayerStatistic();
				ConfigurationSection dataEntry = dataSection.getConfigurationSection(key);

				for (String field : statistic.getFields().keySet()) {
					if (field.equalsIgnoreCase("id")) {
						continue;
					}

					if (!dataEntry.contains(field)) {
						continue;
					}

					Object value = dataEntry.get(field);
					statistic.setValue(field, value);
				}

				statistic.setId(1);
				map.put(Main.getInstance().getServer().getOfflinePlayer(UUID.fromString(key)), statistic);
			}

			this.playerStatistic = map;
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		Main.getInstance().getServer().getConsoleSender()
				.sendMessage(ChatWriter.pluginMessage(ChatColor.GREEN + "Done!"));
	}

}
