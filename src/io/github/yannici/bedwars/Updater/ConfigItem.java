package io.github.yannici.bedwars.Updater;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.bukkit.configuration.file.YamlConfiguration;

public class ConfigItem extends YamlConfiguration {
	
	private String name = null;
	private String type = null;
	private String parentKey = null;
	private List<String> descriptionLines = null;
	
	public ConfigItem() {
		super();
		
		this.descriptionLines = new ArrayList<String>();
	}
	
	public void addDescLine(String line) {
		this.descriptionLines.add(line);
	}
	
	public void clearDesc() {
		this.descriptionLines.clear();
	}
	
	public void setType(String type) {
		this.type = type;
	}
	
	public String getType() {
		return this.type;
	}

	public List<String> getDescriptionLines() {
		return descriptionLines;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getKey() {
		Set<String> keys = this.getKeys(false);
		
		if(keys.size() > 0) {
			if(this.parentKey == null) {
				return (String)keys.toArray()[0];
			} else {
				return this.parentKey + "." +  (String)keys.toArray()[0];
			}
		} else {
			return null;
		}
	}

	public String getParentKey() {
		return this.parentKey;
	}

	public void setParentKey(String parentKey) {
		this.parentKey = parentKey;
	}
	
}
