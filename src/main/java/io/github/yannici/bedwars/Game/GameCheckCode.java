package io.github.yannici.bedwars.Game;

import java.util.HashMap;

import com.google.common.collect.ImmutableMap;

import io.github.yannici.bedwars.Main;

public enum GameCheckCode {
	OK(200), LOC_NOT_SET_ERROR(400), TEAM_SIZE_LOW_ERROR(401), NO_RES_SPAWNER_ERROR(402), NO_LOBBY_SET(
			403), TEAMS_WITHOUT_SPAWNS(404), NO_ITEMSHOP_CATEGORIES(405), TEAM_NO_WRONG_BED(406), NO_MAIN_LOBBY_SET(
					407), TEAM_NO_WRONG_TARGET(408);

	private int code;
	public static HashMap<String, String> GameCheckCodeMessages = null;

	GameCheckCode(int code) {
		this.code = code;
	}

	public int getCode() {
		return this.code;
	}

	public String getCodeMessage(ImmutableMap<String, String> map) {
		return Main._l("gamecheck." + this.toString(), map);
	}

	public String getCodeMessage() {
		return Main._l("gamecheck." + this.toString());
	}
}
