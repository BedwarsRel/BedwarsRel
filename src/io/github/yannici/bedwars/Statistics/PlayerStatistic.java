package io.github.yannici.bedwars.Statistics;

import io.github.yannici.bedwars.Main;
import io.github.yannici.bedwars.Database.DBGetField;
import io.github.yannici.bedwars.Database.DBSetField;

import org.bukkit.OfflinePlayer;

public class PlayerStatistic extends Statistic {

	private OfflinePlayer player = null;
	
	// Statistics
	private int kills = 0;
	private int deaths = 0;
	private int destroyedBeds = 0;
	private int wins = 0;
	private int loses = 0;
	private int games = 0;
	private int score = 0;
	
	public PlayerStatistic() {
		super();
	}
	
	public PlayerStatistic(OfflinePlayer player) {
		super();
		
		this.player = player;
	}
	
	public OfflinePlayer getPlayer() {
		return this.player;
	}
	
	@DBGetField(name = "uuid")
	public String getUUID() {
		return this.player.getUniqueId().toString();
	}

	@DBGetField(name = "kills")
	public int getKills() {
		return kills;
	}

	@DBSetField(name = "kills")
	public void setKills(int kills) {
		this.kills = kills;
	}
	
	@DBGetField(name = "deaths")
	public int getDeaths() {
		return deaths;
	}

	@DBSetField(name = "deaths")
	public void setDeaths(int deaths) {
		this.deaths = deaths;
	}

	@DBGetField(name = "destroyedBeds")
	public int getDestroyedBeds() {
		return destroyedBeds;
	}

	@DBSetField(name = "destroyedBeds")
	public void setDestroyedBeds(int destroyedBeds) {
		this.destroyedBeds = destroyedBeds;
	}

	@DBGetField(name = "wins")
	public int getWins() {
		return wins;
	}

	@DBSetField(name = "wins")
	public void setWins(int wins) {
		this.wins = wins;
	}

	@DBGetField(name = "loses")
	public int getLoses() {
		return loses;
	}

	@DBSetField(name = "loses")
	public void setLoses(int loses) {
		this.loses = loses;
	}

	@DBGetField(name = "games")
	public int getGames() {
		return games;
	}

	@DBSetField(name = "games")
	public void setGames(int games) {
		this.games = games;
	}

	@DBGetField(name = "score")
	public int getScore() {
		return score;
	}

	@DBSetField(name = "score")
	public void setScore(int score) {
		this.score = score;
	}

	@Override
	public String getKeyField() {
		return "uuid";
	}

    @Override
    public void load() {
        Main.getInstance().getPlayerStatisticManager().loadStatistic(this);
    }

    @Override
    public void store() {
        Main.getInstance().getPlayerStatisticManager().storeStatistic(this);
    }

    @Override
	public void setDefault() {
		this.kills = 0;
		this.deaths = 0;
		this.destroyedBeds = 0;
		this.games = 0;
		this.loses = 0;
		this.wins = 0;
		this.score = 0;
	}
	
}
