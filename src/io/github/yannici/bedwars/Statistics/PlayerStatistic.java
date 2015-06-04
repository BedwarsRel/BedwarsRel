package io.github.yannici.bedwars.Statistics;

import java.math.BigDecimal;
import java.util.UUID;

import io.github.yannici.bedwars.Main;
import io.github.yannici.bedwars.UUIDFetcher;
import io.github.yannici.bedwars.Database.DBGetField;
import io.github.yannici.bedwars.Database.DBSetField;

import org.bukkit.OfflinePlayer;

public class PlayerStatistic extends Statistic {
	
	public static final String tableName = "stats_players";
	
	private OfflinePlayer player = null;
	private UUID uuid = null;
	
	// Statistics
	private int kills = 0;
	private int deaths = 0;
	private int destroyedBeds = 0;
	private int wins = 0;
	private int loses = 0;
	private int score = 0;
	private int currentScore = 0;
	
	private boolean once = false;
	
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
	
	public int getCurrentScore() {
		return this.currentScore;
	}
	
	public void addCurrentScore(int score) {
		this.currentScore += score;
	}
	
	public void setCurrentScore(int score) {
		this.currentScore = score;
	}
	
	@DBGetField(name = "uuid", dbType = "VARCHAR(255)")
	public String getUUID() throws Exception {
	    if(this.uuid == null) {
	        try {
	            if(this.player.isOnline()) {
	                this.uuid = this.player.getPlayer().getUniqueId();
	            } else {
	                this.uuid = this.player.getUniqueId();
	            }
	        } catch(Exception ex) {
	            this.uuid = UUIDFetcher.getUUIDOf(this.player.getName());
	        }
	    }
	    
		return this.uuid.toString();
	}

	@DBGetField(name = "kills", dbType = "INT(11)", defaultValue = "0")
	@StatField(name = "kills", order = 10)
	public int getKills() {
		return kills;
	}

	@DBSetField(name = "kills")
	public void setKills(int kills) {
		this.kills = kills;
	}
	
	@DBGetField(name = "deaths", dbType = "INT(11)", defaultValue = "0")
	@StatField(name = "deaths", order = 20)
	public int getDeaths() {
		return deaths;
	}
	
	@StatField(name = "kd", order = 25)
	public String getKD() {
	    double kd = 0.0;
        if(this.getDeaths() == 0) {
            kd = this.getKills();
        } else if(this.getKills() == 0) {
            kd = 0.0;
        } else {
            kd = ((double)this.getKills())/((double)this.getDeaths());
        }
        
        BigDecimal bd = BigDecimal.valueOf(kd).setScale(2, BigDecimal.ROUND_HALF_UP);
        return bd.toPlainString();
	}

	@DBSetField(name = "deaths")
	public void setDeaths(int deaths) {
		this.deaths = deaths;
	}

	@DBGetField(name = "destroyedBeds", dbType = "INT(11)", defaultValue = "0")
	@StatField(name = "destroyedBeds", order = 30)
	public int getDestroyedBeds() {
		return destroyedBeds;
	}

	@DBSetField(name = "destroyedBeds")
	public void setDestroyedBeds(int destroyedBeds) {
		this.destroyedBeds = destroyedBeds;
	}

	@DBGetField(name = "wins", dbType = "INT(11)", defaultValue = "0")
	@StatField(name = "wins", order = 40)
	public int getWins() {
		return wins;
	}

	@DBSetField(name = "wins")
	public void setWins(int wins) {
		this.wins = wins;
	}

	@DBGetField(name = "loses", dbType = "INT(11)", defaultValue = "0")
	@StatField(name = "loses", order = 50)
	public int getLoses() {
		return loses;
	}

	@DBSetField(name = "loses")
	public void setLoses(int loses) {
		this.loses = loses;
	}
	
	@StatField(name = "games", order = 60)
	public int getGames() {
		return this.wins+this.loses;
	}

	@DBGetField(name = "score", dbType = "INT(11)", defaultValue = "0")
	@StatField(name = "score", order = 70)
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
    
    public void setOnce(boolean once) {
    	this.once = once;
    }
    
    public boolean isOnce() {
    	return this.once;
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
		this.loses = 0;
		this.wins = 0;
		this.score = 0;
	}

	@Override
	public String getTableName() {
		return "stats_players";
	}
	
}
