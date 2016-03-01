package io.github.yannici.bedwars.Updater;

import java.util.ArrayList;
import java.util.List;

import io.github.yannici.bedwars.Main;
import io.github.yannici.bedwars.Database.DatabaseManager;
import io.github.yannici.bedwars.Statistics.PlayerStatistic;

public class DatabaseUpdater {

	private List<DatabaseUpdate> updates = null;

	public DatabaseUpdater() {
		super();

		this.updates = new ArrayList<DatabaseUpdate>();
	}

	public void execute() {
		this.updates.add(new DatabaseUpdate("ALTER TABLE `" + DatabaseManager.DBPrefix + PlayerStatistic.tableName
				+ "` ADD `name` VARCHAR(255) NOT NULL FIRST;"));
		this.updates.add(new DatabaseUpdate("ALTER TABLE `" + DatabaseManager.DBPrefix + PlayerStatistic.tableName
				+ "` ADD `kd` DOUBLE NOT NULL;"));
		this.updates.add(new DatabaseUpdate("ALTER TABLE `" + DatabaseManager.DBPrefix + PlayerStatistic.tableName
				+ "` ADD `games` INT(11) NOT NULL;"));

		this.executeUpdates();
	}

	private void executeUpdates() {
		for (DatabaseUpdate update : this.updates) {
			try {
				Main.getInstance().getDatabaseManager().execute(update.getSql());
			} catch (Exception ex) {
				// nothing ;)
			}
		}
	}

}
