package io.github.bedwarsrel.BedwarsRel.Statistics;

import io.github.bedwarsrel.BedwarsRel.Main;
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

  @Getter(AccessLevel.NONE)
  @Setter(AccessLevel.NONE)
  private UUID uuid;
  private int deaths = 0;
  private int destroyedBeds = 0;
  private int games = 0;
  private int kills = 0;
  private int loses = 0;
  private int score = 0;
  private int wins = 0;
  private String name = "";


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

  public double getKD() {
    double kd = 0.0;
    if (this.getDeaths() == 0) {
      kd = this.getKills();
    } else if (this.getKills() == 0) {
      kd = 0.0;
    } else {
      kd = ((double) this.getKills()) / ((double) this.getDeaths());
    }

    return kd;
  }

  public UUID getId() {
    return this.uuid;
  }

  public void setId(UUID uuid) {
    this.uuid = uuid;
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

  @Override
  public Map<String, Object> serialize() {
    HashMap<String, Object> playerStatistic = new HashMap<>();
    playerStatistic.put("deaths", this.deaths);
    playerStatistic.put("destroyedBeds", this.destroyedBeds);
    playerStatistic.put("kills", this.kills);
    playerStatistic.put("loses", this.loses);
    playerStatistic.put("score", this.score);
    playerStatistic.put("wins", this.wins);
    playerStatistic.put("name", this.name);
    return playerStatistic;
  }
}
