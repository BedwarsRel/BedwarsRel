package io.github.yannici.bedwars.Database;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.bukkit.ChatColor;

import com.mchange.v2.c3p0.ComboPooledDataSource;
import com.mchange.v2.c3p0.DataSources;

import io.github.yannici.bedwars.ChatWriter;
import io.github.yannici.bedwars.Main;

public class DatabaseManager {

	private String host = null;
	private int port = 3306;
	private String user = null;
	private String password = null;
	private String database = null;
	private ComboPooledDataSource dataSource = null;

	private static DatabaseManager instance = null;

	public static String DBPrefix = "bw_";

	public DatabaseManager(String host, int port, String user, String password, String database) {
		this.host = host;
		this.port = port;
		this.user = user;
		this.password = password;
		this.database = database;
	}

	public void initialize() {
		this.initializePooledDataSource(this.getMinPoolSizeConfig(), this.getMaxPoolSizeConfig());
		DatabaseManager.instance = this;
	}

	public static DatabaseManager getInstance() {
		return DatabaseManager.instance;
	}

	private int getMinPoolSizeConfig() {
		return Main.getInstance().getIntConfig("database.connection-pooling.min-pool-size", 3);
	}

	private int getMaxPoolSizeConfig() {
		return Main.getInstance().getIntConfig("database.connection-pooling.max-pool-size", 15);
	}

	private void initializePooledDataSource(int minPoolSize, int maxPoolSize) {
		try {
			this.dataSource = new ComboPooledDataSource();

			// currently only mysql is supported
			this.dataSource.setDriverClass("com.mysql.jdbc.Driver");
			this.dataSource
					.setJdbcUrl("jdbc:mysql://" + this.host + ":" + String.valueOf(this.port) + "/" + this.database);

			this.dataSource.setUser(this.user);
			this.dataSource.setPassword(this.password);

			// connection pool configuration
			this.dataSource.setMaxIdleTime(600);
			this.dataSource.setMinPoolSize(minPoolSize);
			this.dataSource.setMaxPoolSize(maxPoolSize);
		} catch (Exception ex) {
			Main.getInstance().getServer().getConsoleSender().sendMessage(
					ChatWriter.pluginMessage(ChatColor.RED + "Couldn't create pooled datasource: " + ex.getMessage()));
		}
	}

	public Connection getDataSourceConnection() {
		try {
			return this.dataSource.getConnection();
		} catch (SQLException e) {
			Main.getInstance().getServer().getConsoleSender().sendMessage(
					ChatWriter.pluginMessage(ChatColor.RED + "Couldn't get a pooled connection: " + e.getMessage()));
		}

		return null;
	}

	public static Connection getConnection() {
		return DatabaseManager.instance.getDataSourceConnection();
	}

	public void cleanUp() {
		if (this.dataSource != null) {
			try {
				this.dataSource.setMinPoolSize(0);
				this.dataSource.setInitialPoolSize(0);
				DataSources.destroy(this.dataSource);
			} catch (SQLException e) {
				// just shutdown
			}
		}
	}

	public void clean(Connection dbConnection) {
		try {
			if (dbConnection == null) {
				return;
			}

			if (!dbConnection.isClosed()) {
				dbConnection.close();
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public void cleanStatement(Statement statement) {
		try {
			if (statement == null) {
				return;
			}

			if (!statement.isClosed()) {
				statement.close();
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public void cleanResult(ResultSet result) {
		try {
			if (result == null) {
				return;
			}

			if (!result.isClosed()) {
				result.close();
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public void execute(String... sqls) throws SQLException {
		Connection con = null;
		Statement statement = null;

		if (sqls.length == 0) {
			return;
		}

		try {
			con = this.getDataSourceConnection();
			statement = con.createStatement();

			if (sqls.length == 1) {
				statement.execute(sqls[0]);
			} else {
				for (String sql : sqls) {
					statement.addBatch(sql);
				}

				statement.executeBatch();
			}
		} finally {
			this.clean(con);
		}
	}

	public ResultSet query(String sql) {
		Connection con = null;
		Statement statement = null;
		ResultSet result = null;

		try {
			con = this.getDataSourceConnection();
			statement = con.createStatement();
			result = statement.executeQuery(sql);

			return result;
		} catch (Exception ex) {
			ex.printStackTrace();
			this.clean(con);
		}

		return null;
	}

	public int getRowCount(ResultSet result) {
		int size = 0;
		try {
			result.last();
			size = result.getRow();
			result.beforeFirst();

			return size;
		} catch (Exception ex) {
			return 0;
		}
	}

	public void update(String sql) {
		Connection con = null;
		Statement statement = null;

		try {
			con = this.getDataSourceConnection();
			statement = con.createStatement();

			statement.executeUpdate(sql);
		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			this.clean(con);
			this.cleanStatement(statement);
		}
	}

	public void insert(String sql) {
		this.update(sql);
	}

	public void delete(String sql) {
		this.update(sql);
	}
}
