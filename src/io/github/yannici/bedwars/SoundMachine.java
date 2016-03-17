package io.github.yannici.bedwars;

import org.bukkit.Sound;

public class SoundMachine {
	
	public static Sound get(String v18, String v19) {
		Sound finalSound = null;
		
		try {
			if(Main.getInstance().getCurrentVersion().startsWith("v1_8")) {
				finalSound = Sound.valueOf(v18);
			} else {
				finalSound = Sound.valueOf(v19);
			}
		} catch(Exception ex) {
			// just compatibility
		}
		
		return finalSound;
	}

}
