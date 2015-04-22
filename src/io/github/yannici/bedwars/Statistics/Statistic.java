package io.github.yannici.bedwars.Statistics;

import java.io.File;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import io.github.yannici.bedwars.ChatWriter;
import io.github.yannici.bedwars.Main;
import io.github.yannici.bedwars.Database.DBField;
import io.github.yannici.bedwars.Database.DBGetField;
import io.github.yannici.bedwars.Database.DBSetField;

public abstract class Statistic {

	private Map<String, DBField> fields = null;

	public Statistic() {
		this.loadFields();
	}

	private void loadFields() {
		this.fields.clear();
		
		for(Method method : this.getClass().getMethods()) {
			DBGetField getAnnotation = method.getAnnotation(DBGetField.class);
			DBSetField setAnnotation = method.getAnnotation(DBSetField.class);
			
			if(getAnnotation == null && setAnnotation == null) {
				continue;
			}
			
			String fieldName = (getAnnotation != null) ? getAnnotation.name() : setAnnotation.name();
			
			if(this.fields.containsKey(fieldName)) {
				DBField field = this.fields.get(fieldName);
				
				if(getAnnotation == null) {
					field.setSetter(method);
				} else {
					field.setGetter(method);
				}
			} else {
				DBField field = new DBField();
				
				if(getAnnotation == null) {
					field.setSetter(method);
				} else {
					field.setGetter(method);
				}
				
				this.fields.put(fieldName, field);
			}
		}
	}
	
	public Map<String, DBField> getFields() {
		return this.fields;
	}

	public abstract String getTableName();

	public abstract String getKeyField();

	public void load() {
		if(Main.getInstance().getStatisticStorageType() == StorageType.YAML) {
			
		}
	}
	
	public void load(String key) {
		
	}
	
	public static Map<OfflinePlayer, Statistic> loadAll(Class<? extends Statistic> clazz, File ymlFile) {
		try {
			YamlConfiguration config = null;
			Map<OfflinePlayer, Statistic> map = new HashMap<OfflinePlayer, Statistic>();
			
			if(!ymlFile.exists()) {
				config = new YamlConfiguration();
				config.createSection("data");
				config.save(ymlFile);
			} else {
				config = YamlConfiguration.loadConfiguration(ymlFile);
			}
			
			ConfigurationSection dataSection = config.getConfigurationSection("data");
			
			for(String key : dataSection.getKeys(false)) {
				Statistic statistic = clazz.newInstance();
				ConfigurationSection dataEntry = dataSection.getConfigurationSection(key);
				
				for(String field : statistic.getFields().keySet()) {
					Object value = dataEntry.get(field);
					statistic.setValue(field, value);
				}
			}
			
			return map;
		} catch(Exception ex) {
			ex.printStackTrace();
		}
		
		return new HashMap<OfflinePlayer, Statistic>();
	}

	public Object getValue(String field) {
		try {
			Method getter = this.fields.get(field).getGetter();
			
			getter.setAccessible(true);
			return getter.invoke(this, new Object[]{});
		} catch(Exception ex) {
			Main.getInstance().getServer().getConsoleSender().sendMessage(ChatWriter.pluginMessage("Couldn't fetch value of field: " + field));
		}
		
		return null;
	}
	
	public void setValue(String field, Object value) {
		try {
			Method setter = this.fields.get(field).getSetter();
			
			setter.setAccessible(true);
			setter.invoke(this, new Object[]{value});
		} catch(Exception ex) {
			Main.getInstance().getServer().getConsoleSender().sendMessage(ChatWriter.pluginMessage("Couldn't set value of field: " + field));
		}
	}
}
