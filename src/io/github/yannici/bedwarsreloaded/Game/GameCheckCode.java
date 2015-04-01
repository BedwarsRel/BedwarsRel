package io.github.yannici.bedwarsreloaded.Game;

import io.github.yannici.bedwarsreloaded.ChatWriter;
import io.github.yannici.bedwarsreloaded.Main;

import java.util.HashMap;

import org.bukkit.configuration.file.FileConfiguration;

public enum GameCheckCode {
    OK(200),
    LOC_NOT_SET_ERROR(400),
    TEAM_SIZE_LOW_ERROR(401),
    NO_RES_SPAWNER_ERROR(402), 
    NO_LOBBY_SET(403),
    TEAMS_WITHOUT_SPAWNS(404),
    NO_ITEMSHOP_CATEGORIES(405),
    TEAM_NO_WRONG_BED(406);

    private int code;
    public static HashMap<String, String> GameCheckCodeMessages = null;

    GameCheckCode(int code) {
        this.code = code;
    }

    public int getCode() {
        return this.code;
    }

    public String getCodeMessage() {
        if(GameCheckCode.GameCheckCodeMessages == null) {
            try {
                GameCheckCode.GameCheckCodeMessages = new HashMap<String, String>();
                FileConfiguration config = Main.getInstance().getConfig();

                for(GameCheckCode code : GameCheckCode.values()) {
                    if(code == GameCheckCode.OK) {
                        continue;
                    }

                    GameCheckCode.GameCheckCodeMessages.put(code.toString(), config.get("texts.gamecheck." + code.toString()).toString());
                }

            } catch (Exception e) {
                GameCheckCode.GameCheckCodeMessages = null;
                Main.getInstance().getServer().getConsoleSender().sendMessage(ChatWriter.pluginMessage("Game Check Code Messages couldn't be fetched!"));
                return "ERROR";
            }
        }


        return GameCheckCode.GameCheckCodeMessages.get(this.toString()).toString();
    }
}
