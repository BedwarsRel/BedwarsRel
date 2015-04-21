package io.github.yannici.bedwars.Database;

import java.sql.Connection;
import java.sql.SQLException;

import org.bukkit.ChatColor;

import io.github.yannici.bedwars.ChatWriter;
import io.github.yannici.bedwars.Main;

import com.mchange.v2.c3p0.ComboPooledDataSource;

public class DatabaseManager {

	private String host = null;
	private int port = 3306;
	private String user = null;
	private String password = null;
	private String database = null;
	private ComboPooledDataSource dataSource = null;
	
	private static DatabaseManager instance = null;

	public DatabaseManager(String host, int port, String user,
			String password, String database) {
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
			dataSource.setDriverClass("com.mysql.jdbc.Driver");
			dataSource.setJdbcUrl("jdbc:mysql://" + this.host + ":"
					+ String.valueOf(this.port) + "/" + this.database);
			
			dataSource.setUser(this.user);
			dataSource.setPassword(this.password);
			
			// connection pool configuration
			dataSource.setMinPoolSize(minPoolSize);
			dataSource.setMaxPoolSize(maxPoolSize);
		} catch(Exception ex) {
			Main.getInstance().getServer().getConsoleSender().sendMessage(ChatWriter.pluginMessage(ChatColor.RED + "Couldn't create pooled datasource: " + ex.getMessage()));
		}
	}
	
	public Connection getDataSourceConnection() {
		try {
			return this.dataSource.getConnection();
		} catch (SQLException e) {
			Main.getInstance().getServer().getConsoleSender().sendMessage(ChatWriter.pluginMessage(ChatColor.RED + "Couldn't get a pooled connection: " + e.getMessage()));
		}
		
		return null;
	}
	
	public static Connection getConnection() {
		return DatabaseManager.instance.getDataSourceConnection();
	}
	
	public void cleanUp() {
		if(this.dataSource != null) {
			this.dataSource.close();
		}
	}
}
