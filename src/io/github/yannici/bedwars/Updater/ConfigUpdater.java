package io.github.yannici.bedwars.Updater;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.Lists;

import io.github.yannici.bedwars.Main;

public class ConfigUpdater {
	
	private List<ConfigItem> updateConfig = null;
	private boolean needUpdate = false;
	
	public ConfigUpdater() {
		super();
		
		this.updateConfig = new ArrayList<ConfigItem>();
	}
	
	public void addConfigs() {
		// 1.1.3
		ConfigItem checkUpdates = new ConfigItem();
		checkUpdates.setName("Check Updates");
		checkUpdates.setType("boolean");
		checkUpdates.addDescLine("Allow check for updates every 30 minutes when server running");
		checkUpdates.addDescLine("or when plugin gets enabled");
		checkUpdates.set("check-updates", true);
		this.addDefault(checkUpdates);
		
		// update if needed
		if(this.needUpdate) {
			try {
				this.update();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	public void addDefault(ConfigItem item) {
		if(Main.getInstance().getConfig().get(item.getKey()) != null
		        && !Main.getInstance().getConfig().equals(item)) {
			return;
		}
		
		this.needUpdate = true;
		this.updateConfig.add(item);
	}
	
	private void update() throws IOException {
		File configFile = new File(Main.getInstance().getDataFolder(), "config.yml");
		
		if(!configFile.exists()) {
			return;
		}
		
		try {
			for(ConfigItem item : this.updateConfig) {
				Thread.sleep(100); // wait for saving file / close writer
				if(item.getKey().contains(".")) {
					this.insertKey(item, configFile);
				} else {
					this.addKey(item, configFile);
				}
			}
		} catch(Exception ex) {
			ex.printStackTrace();
		}
	}
	
	private void addKey(ConfigItem item, File file) throws IOException {
		BufferedWriter writer = new BufferedWriter(new FileWriter(file, true));
		
		// write to config
		writer.newLine();
		if(item.getName() != null) {
			writer.write("# " + item.getName());
			writer.newLine();
		}
		
		if(item.getType() != null) {
			writer.write("# type: " + item.getType());
			writer.newLine();
		}
		
		for(String desc : item.getDescriptionLines()) {
			writer.write("# " + desc);
			writer.newLine();
		}
		
		writer.write(item.saveToString()); 
		writer.close();
	}
	
	private void insertKey(ConfigItem item, File file) throws IOException {
		BufferedReader reader = new BufferedReader(new FileReader(file));
		
		List<String> lines = new ArrayList<String>();
		List<String> reverseDesc = new ArrayList<String>();
		String line = null;
		String currentParent = "";
		String itemKey = item.getKey();
		String[] itemKeyNames = itemKey.split("\\.");
		String itemKeyName = itemKeyNames[itemKeyNames.length-1];
		int parentSpaces = 0;
		int pos = -1;
		int findPos = -1;
		String spaces = "";
		
		while((line = reader.readLine()) != null) {
			pos++;
			lines.add(line);
			
			if(findPos > -1) {
				continue;
			}
			
			String trimmedLine = line.trim();
			if(trimmedLine.length() == 0) {
				continue;
			}
			
			if(trimmedLine.endsWith(":") && !trimmedLine.startsWith("#") && !line.startsWith(" ")) {
				parentSpaces = 0;
				currentParent = trimmedLine.substring(0, trimmedLine.length()-1);
			} else if(!trimmedLine.startsWith("#") && line.startsWith(" ") && trimmedLine.endsWith(":")) {
				currentParent = currentParent + "." + trimmedLine.substring(0, trimmedLine.length()-1);
				parentSpaces = 0;
				
				while(true) {
					parentSpaces++;
					if(line.toCharArray()[parentSpaces] != ' ') {
						break;
					}
				}
			} else if(!trimmedLine.startsWith("#") && !line.startsWith(" ") && !trimmedLine.endsWith(":")) {
				currentParent = "";
				continue;
			} else if(!trimmedLine.startsWith("#") && line.startsWith(" ") && !trimmedLine.endsWith(":")) {
				int currentSpaces = 0;
				while(true) {
					currentSpaces++;
					if(line.toCharArray()[currentSpaces] != ' ') {
						break;
					}
				}
				
				if(currentSpaces <= parentSpaces) {
					parentSpaces = currentSpaces;
					String[] oldParentKeys = currentParent.split("\\.");
					currentParent = "";
					for(int i = 0; i < oldParentKeys.length-2; i++) {
						if(currentParent.equals("")) {
							currentParent = oldParentKeys[i];
						} else {
							currentParent = currentParent + "." + oldParentKeys[i];
						}
					}
				}
				
				continue;
			}
			
			if(trimmedLine.startsWith("#")) {
				continue;
			}
			
			if(itemKey.equals(currentParent + "." + itemKeyName)) {
				findPos = pos+1;
			}
		}
		
		reader.close();
		BufferedWriter writer = new BufferedWriter(new FileWriter(file));
		
		for(int i = 0; i < itemKeyNames.length-1; i++) {
			spaces = spaces + "    ";
		}
		
		lines.add(findPos, spaces);
		
		reverseDesc = Lists.reverse(item.getDescriptionLines());
		lines.add(findPos, spaces + item.saveToString());
		
		for(String desc : reverseDesc) {
			lines.add(findPos, spaces + "# " + desc);
		}
		
		if(item.getType() != null) {
			lines.add(findPos, spaces + "# type: " + item.getType());
		}
		
		if(item.getName() != null) {
			lines.add(findPos, spaces + "# " + item.getName());
		}
		
		for(String outLine : lines) {
			writer.write(outLine);
			writer.newLine();
		}
		
		writer.close();
	}

    public boolean isNeedUpdate() {
        return this.needUpdate;
    }
}
