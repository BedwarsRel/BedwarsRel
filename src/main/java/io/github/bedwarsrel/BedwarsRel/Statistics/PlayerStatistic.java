package io.github.bedwarsrel.BedwarsRel.Statistics;

import io.github.bedwarsrel.BedwarsRel.Main;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.entity.Player;

@Getter
@Setter
public class PlayerStatistic implements ConfigurationSerializable {

  private int currentDeaths = 0;
  private int currentDestroyedBeds = 0;
  private int currentGames = 0;
  private int currentKills = 0;
  private int currentLoses = 0;
  private int currentScore = 0;
  private int currentWins = 0;
  @Setter(AccessLevel.NONE)
  private int deaths = 0;
  @Setter(AccessLevel.NONE)
  private int destroyedBeds = 0;
  @Setter(AccessLevel.NONE)
  private int games = 0;
  @Setter(AccessLevel.NONE)
  private int kills = 0;
  @Setter(AccessLevel.NONE)
  private int loses = 0;
  private String name = "";
  @Setter(AccessLevel.NONE)
  private int score = 0;
  @Getter(AccessLevel.NONE)
  @Setter(AccessLevel.NONE)
  private UUID uuid;
  @Setter(AccessLevel.NONE)
  private int wins = 0;


  public PlayerStatistic(UUID uuid) {
    this.uuid = uuid;

    Player player = Main.getInstance().getServer().getPlayer(uuid);
    if (player != null && !this.name.equals(player.getName())) {
      this.name = player.getName();
    }
  }

  public PlayerStatistic(OfflinePlayer player) {
    this.uuid = player.getUniqueId();
    this.name = player.getName();
  }

  public PlayerStatistic() {

  }

  public PlayerStatistic(Map<String, Object> deserialize) {
    if (deserialize.containsKey("deaths")) {
      this.deaths = (int) deserialize.get("deaths");
    }
    if (deserialize.containsKey("deaths")) {
      this.deaths = (int) deserialize.get("deaths");
    }
    if (deserialize.containsKey("destroyedBeds")) {
      this.destroyedBeds = (int) deserialize.get("destroyedBeds");
    }
    if (deserialize.containsKey("kills")) {
      this.kills = (int) deserialize.get("kills");
    }
    if (deserialize.containsKey("loses")) {
      this.loses = (int) deserialize.get("loses");
    }
    if (deserialize.containsKey("score")) {
      this.score = (int) deserialize.get("score");
    }
    if (deserialize.containsKey("wins")) {
      this.wins = (int) deserialize.get("wins");
    }
    if (deserialize.containsKey("name")) {
      this.name = (String) deserialize.get("name");
    }
    if (deserialize.containsKey("uuid")) {
      this.uuid = UUID.fromString((String) deserialize.get("uuid"));
    }
  }

  public void addCurrentValues() {
    this.deaths = this.deaths + this.currentDeaths;
    this.currentDeaths = 0;
    this.destroyedBeds = this.destroyedBeds + this.currentDestroyedBeds;
    this.currentDestroyedBeds = 0;
    this.games = this.games + this.currentGames;
    this.currentGames = 0;
    this.kills = this.kills + this.currentKills;
    this.currentKills = 0;
    this.loses = this.loses + this.currentLoses;
    this.currentLoses = 0;
    this.score = this.score + this.currentScore;
    this.currentScore = 0;
    this.wins = this.wins + this.currentWins;
    this.currentWins = 0;

  }

  public double getCurrentKD() {
    double kd = 0.0;
    if (this.getDeaths() + this.getCurrentDeaths() == 0) {
      kd = this.getKills();
    } else if (this.getKills() + this.getCurrentKills() == 0) {
      kd = 0.0;
    } else {
      kd = ((double) this.getKills() + this.getCurrentKills()) / ((double) this.getDeaths() + this
          .getCurrentDeaths());
    }
    DecimalFormat df = new DecimalFormat("#.##");
    kd = Double.valueOf(df.format(kd));

    return kd;
  }

  public UUID getId() {
    return this.uuid;
  }

  public void setId(UUID uuid) {
    this.uuid = uuid;
  }

  public double getKD() {
    double kd = 0.0;
    if (this.getDeaths() == 0) {
      kd = this.getKills();
    } else if (this.getKills() == 0) {
      kd = 0.0;
    } else {
      kd = ((double) this.getKills()) / ((double) this.getDeaths());
    }
    DecimalFormat df = new DecimalFormat("#.##");
    kd = Double.valueOf(df.format(kd));

    return kd;
  }

  @Override
  public Map<String, Object> serialize() {
    HashMap<String, Object> playerStatistic = new HashMap<>();
    playerStatistic.put("deaths", this.deaths);
    playerStatistic.put("destroyedBeds", this.destroyedBeds);
    playerStatistic.put("games", this.games);
    playerStatistic.put("kills", this.kills);
    playerStatistic.put("loses", this.loses);
    playerStatistic.put("score", this.score);
    playerStatistic.put("wins", this.wins);
    playerStatistic.put("name", this.name);
    return playerStatistic;
  }
}
