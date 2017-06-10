package io.github.bedwarsrel.game;

import com.google.common.collect.ImmutableMap;
import io.github.bedwarsrel.BedwarsRel;
import io.github.bedwarsrel.events.BedwarsGameOverEvent;
import io.github.bedwarsrel.events.BedwarsPlayerKilledEvent;
import io.github.bedwarsrel.shop.Specials.RescuePlatform;
import io.github.bedwarsrel.shop.Specials.SpecialItem;
import io.github.bedwarsrel.statistics.PlayerStatistic;
import io.github.bedwarsrel.utils.ChatWriter;
import io.github.bedwarsrel.utils.SoundMachine;
import io.github.bedwarsrel.utils.Utils;
import java.lang.reflect.Method;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.scheduler.BukkitRunnable;

public abstract class GameCycle {

  private boolean endGameRunning = false;
  private Game game = null;

  public GameCycle(Game game) {
    this.game = game;
  }

  public void checkGameOver() {
    if (!BedwarsRel.getInstance().isEnabled()) {
      return;
    }

    Team winner = this.getGame().isOver();
    if (winner != null) {
      if (!this.isEndGameRunning()) {
        this.runGameOver(winner);
      }
    } else {
      if ((this.getGame().getTeamPlayers().size() == 0 || this.getGame().isOverSet())
          && !this.isEndGameRunning()) {
        this.runGameOver(null);
      }
    }
  }

  public Game getGame() {
    return game;
  }

  private Map<String, String> getRewardPlaceholders(Player player) {
    Map<String, String> placeholders = new HashMap<String, String>();

    placeholders.put("{player}", player.getName());
    if (BedwarsRel.getInstance().statisticsEnabled()) {
      PlayerStatistic statistic =
          BedwarsRel.getInstance().getPlayerStatisticManager().getStatistic(player);
      placeholders.put("{score}", String.valueOf(statistic.getCurrentScore()));
    }

    return placeholders;
  }

  public boolean isEndGameRunning() {
    return this.endGameRunning;
  }

  public void setEndGameRunning(boolean running) {
    this.endGameRunning = running;
  }

  public abstract void onGameEnds();

  public abstract void onGameLoaded();

  public abstract void onGameOver(GameOverTask task);

  public abstract void onGameStart();

  public void onPlayerDies(Player player, Player killer) {
    if (this.isEndGameRunning()) {
      return;
    }

    BedwarsPlayerKilledEvent killedEvent =
        new BedwarsPlayerKilledEvent(this.getGame(), player, killer);
    BedwarsRel.getInstance().getServer().getPluginManager().callEvent(killedEvent);

    PlayerStatistic diePlayer = null;
    PlayerStatistic killerPlayer = null;

    Iterator<SpecialItem> itemIterator = this.game.getSpecialItems().iterator();
    while (itemIterator.hasNext()) {
      SpecialItem item = itemIterator.next();
      if (!(item instanceof RescuePlatform)) {
        continue;
      }

      RescuePlatform rescue = (RescuePlatform) item;
      if (rescue.getOwner().equals(player)) {
        itemIterator.remove();
      }
    }

    Team deathTeam = this.getGame().getPlayerTeam(player);
    if (BedwarsRel.getInstance().statisticsEnabled()) {
      diePlayer = BedwarsRel.getInstance().getPlayerStatisticManager().getStatistic(player);

      boolean onlyOnBedDestroy =
          BedwarsRel.getInstance().getBooleanConfig("statistics.bed-destroyed-kills", false);
      boolean teamIsDead = deathTeam.isDead(this.getGame());

      if ((onlyOnBedDestroy && teamIsDead) || !onlyOnBedDestroy) {
        diePlayer.setCurrentDeaths(diePlayer.getCurrentDeaths() + 1);
        diePlayer.setCurrentScore(diePlayer.getCurrentScore() + BedwarsRel
            .getInstance().getIntConfig("statistics.scores.die", 0));
      }

      if (killer != null) {
        if ((onlyOnBedDestroy && teamIsDead) || !onlyOnBedDestroy) {
          killerPlayer = BedwarsRel.getInstance().getPlayerStatisticManager().getStatistic(killer);
          if (killerPlayer != null) {
            killerPlayer.setCurrentKills(killerPlayer.getCurrentKills() + 1);
            killerPlayer.setCurrentScore(killerPlayer.getCurrentScore() + BedwarsRel
                .getInstance().getIntConfig("statistics.scores.kill", 10));
          }
        }
      }

      // dispatch reward commands directly
      if (BedwarsRel.getInstance().getBooleanConfig("rewards.enabled", false) && killer != null
          && ((onlyOnBedDestroy && teamIsDead) || !onlyOnBedDestroy)) {
        List<String> commands = BedwarsRel.getInstance().getConfig()
            .getStringList("rewards.player-kill");
        BedwarsRel.getInstance().dispatchRewardCommands(commands,
            ImmutableMap.of("{player}", killer.getName(), "{score}",
                String
                    .valueOf(BedwarsRel.getInstance().getIntConfig("statistics.scores.kill", 10))));
      }
    }

    if (killer == null) {
      for (Player aPlayer : this.getGame().getPlayers()) {
        if (aPlayer.isOnline()) {
          aPlayer.sendMessage(
              ChatWriter.pluginMessage(
                  ChatColor.GOLD + BedwarsRel._l(aPlayer, "ingame.player.died", ImmutableMap
                      .of("player",
                          Game.getPlayerWithTeamString(player, deathTeam, ChatColor.GOLD)))));
        }
      }

      this.sendTeamDeadMessage(deathTeam);
      this.checkGameOver();
      return;
    }

    Team killerTeam = this.getGame().getPlayerTeam(killer);
    if (killerTeam == null) {
      for (Player aPlayer : this.getGame().getPlayers()) {
        if (aPlayer.isOnline()) {
          aPlayer.sendMessage(
              ChatWriter.pluginMessage(
                  ChatColor.GOLD + BedwarsRel._l(aPlayer, "ingame.player.died", ImmutableMap
                      .of("player",
                          Game.getPlayerWithTeamString(player, deathTeam, ChatColor.GOLD)))));
        }
      }
      this.sendTeamDeadMessage(deathTeam);
      this.checkGameOver();
      return;
    }

    String hearts = "";
    DecimalFormat format = new DecimalFormat("#");
    double health = ((double) killer.getHealth()) / ((double) killer.getMaxHealth())
        * ((double) killer.getHealthScale());
    if (!BedwarsRel.getInstance().getBooleanConfig("hearts-in-halfs", true)) {
      format = new DecimalFormat("#.#");
      health = health / 2;
    }

    if (BedwarsRel.getInstance().getBooleanConfig("hearts-on-death", true)) {
      hearts = "[" + ChatColor.RED + "\u2764" + format.format(health) + ChatColor.GOLD + "]";
    }

    for (Player aPlayer : this.getGame().getPlayers()) {
      if (aPlayer.isOnline()) {
        aPlayer.sendMessage(
            ChatWriter.pluginMessage(ChatColor.GOLD + BedwarsRel._l(aPlayer, "ingame.player.killed",
                ImmutableMap.of("killer",
                    Game.getPlayerWithTeamString(killer, killerTeam, ChatColor.GOLD, hearts),
                    "player",
                    Game.getPlayerWithTeamString(player, deathTeam, ChatColor.GOLD)))));
      }
    }

    if (deathTeam.isDead(this.getGame())) {
      killer.playSound(killer.getLocation(), SoundMachine.get("LEVEL_UP", "ENTITY_PLAYER_LEVELUP"),
          Float.valueOf("1.0"), Float.valueOf("1.0"));
    }
    this.sendTeamDeadMessage(deathTeam);
    this.checkGameOver();
  }

  public abstract boolean onPlayerJoins(Player player);

  public abstract void onPlayerLeave(Player player);

  public void onPlayerRespawn(PlayerRespawnEvent pre, Player player) {
    Team team = this.getGame().getPlayerTeam(player);

    // reset damager
    this.getGame().setPlayerDamager(player, null);

    if (this.getGame().isSpectator(player)) {
      Collection<Team> teams = this.getGame().getTeams().values();
      pre.setRespawnLocation(
          ((Team) teams.toArray()[Utils.randInt(0, teams.size() - 1)]).getSpawnLocation());
      return;
    }

    if (team.isDead(this.getGame())) {
      PlayerStorage storage = this.getGame().getPlayerStorage(player);

      if (BedwarsRel.getInstance().statisticsEnabled()) {
        PlayerStatistic statistic =
            BedwarsRel.getInstance().getPlayerStatisticManager().getStatistic(player);
        statistic.setCurrentLoses(statistic.getCurrentLoses() + 1);
      }

      if (BedwarsRel.getInstance().spectationEnabled()) {
        if (storage != null && storage.getLeft() != null) {
          pre.setRespawnLocation(team.getSpawnLocation());
        }

        this.getGame().toSpectator(player);
      } else {
        if (this.game.getCycle() instanceof BungeeGameCycle) {
          this.getGame().playerLeave(player, false);
          return;
        }

        if (!BedwarsRel.getInstance().toMainLobby()) {
          if (storage != null) {
            if (storage.getLeft() != null) {
              pre.setRespawnLocation(storage.getLeft());
            }
          }
        } else {
          if (this.getGame().getMainLobby() != null) {
            pre.setRespawnLocation(this.getGame().getMainLobby());
          } else {
            if (storage != null) {
              if (storage.getLeft() != null) {
                pre.setRespawnLocation(storage.getLeft());
              }
            }
          }
        }

        this.getGame().playerLeave(player, false);
      }

    } else {
      if (BedwarsRel.getInstance().getRespawnProtectionTime() > 0) {
        RespawnProtectionRunnable protection = this.getGame().addProtection(player);
        protection.runProtection();
      }
      pre.setRespawnLocation(team.getSpawnLocation());
    }

    new BukkitRunnable() {

      @Override
      public void run() {
        GameCycle.this.checkGameOver();
      }
    }.runTaskLater(BedwarsRel.getInstance(), 20L);

  }

  @SuppressWarnings("unchecked")
  private void runGameOver(Team winner) {
    BedwarsGameOverEvent overEvent = new BedwarsGameOverEvent(this.getGame(), winner);
    BedwarsRel.getInstance().getServer().getPluginManager().callEvent(overEvent);

    if (overEvent.isCancelled()) {
      return;
    }

    this.getGame().stopWorkers();
    this.setEndGameRunning(true);

    // new record?
    boolean storeRecords = BedwarsRel.getInstance().getBooleanConfig("store-game-records", true);
    boolean storeHolders = BedwarsRel
        .getInstance().getBooleanConfig("store-game-records-holder", true);
    boolean madeRecord = false;
    if (storeRecords && winner != null) {
      madeRecord = this.storeRecords(storeHolders, winner);
    }

    int delay = BedwarsRel.getInstance().getConfig().getInt("gameoverdelay"); // configurable
    // delay

    if (BedwarsRel.getInstance().statisticsEnabled()
        || BedwarsRel.getInstance().getBooleanConfig("rewards.enabled", false)
        || (BedwarsRel.getInstance().getBooleanConfig("titles.win.enabled", true))) {
      if (winner != null) {
        for (Player player : winner.getPlayers()) {
          String title = this
              .winTitleReplace(BedwarsRel._l(player, "ingame.title.win-title"), winner);
          String subtitle = this
              .winTitleReplace(BedwarsRel._l(player, "ingame.title.win-subtitle"), winner);
          if (!"".equals(title) || !"".equals(subtitle)) {
            if (BedwarsRel.getInstance().getBooleanConfig("titles.win.enabled", true)
                && (!"".equals(title) || !"".equals(subtitle))) {
              try {
                Class<?> clazz = Class.forName("io.github.bedwarsrel.com."
                    + BedwarsRel.getInstance().getCurrentVersion().toLowerCase() + ".Title");

                if (!"".equals(title)) {
                  double titleFadeIn =
                      BedwarsRel.getInstance().getConfig()
                          .getDouble("titles.win.title-fade-in", 1.5);
                  double titleStay =
                      BedwarsRel.getInstance().getConfig().getDouble("titles.win.title-stay", 5.0);
                  double titleFadeOut =
                      BedwarsRel
                          .getInstance().getConfig().getDouble("titles.win.title-fade-out", 2.0);
                  Method showTitle = clazz
                      .getDeclaredMethod("showTitle", Player.class, String.class,
                          double.class, double.class, double.class);

                  showTitle.invoke(null, player, title, titleFadeIn, titleStay, titleFadeOut);
                }

                if (!"".equals(subtitle)) {
                  double subTitleFadeIn =
                      BedwarsRel
                          .getInstance().getConfig().getDouble("titles.win.subtitle-fade-in", 1.5);
                  double subTitleStay =
                      BedwarsRel.getInstance().getConfig()
                          .getDouble("titles.win.subtitle-stay", 5.0);
                  double subTitleFadeOut =
                      BedwarsRel
                          .getInstance().getConfig().getDouble("titles.win.subtitle-fade-out", 2.0);
                  Method showSubTitle = clazz.getDeclaredMethod("showSubTitle", Player.class,
                      String.class, double.class, double.class, double.class);

                  showSubTitle.invoke(null, player, subtitle, subTitleFadeIn, subTitleStay,
                      subTitleFadeOut);
                }
              } catch (Exception ex) {
                BedwarsRel.getInstance().getBugsnag().notify(ex);
                ex.printStackTrace();
              }
            }
          }

          if (BedwarsRel.getInstance().getBooleanConfig("rewards.enabled", false)) {
            List<String> commands = new ArrayList<String>();
            commands = (List<String>) BedwarsRel.getInstance().getConfig()
                .getList("rewards.player-win");
            BedwarsRel.getInstance()
                .dispatchRewardCommands(commands, this.getRewardPlaceholders(player));
          }

          if (BedwarsRel.getInstance().statisticsEnabled()) {
            PlayerStatistic statistic =
                BedwarsRel.getInstance().getPlayerStatisticManager().getStatistic(player);
            statistic.setCurrentWins(statistic.getCurrentWins() + 1);
            statistic.setCurrentScore(statistic.getCurrentScore() + BedwarsRel
                .getInstance().getIntConfig("statistics.scores.win", 50));

            if (madeRecord) {
              statistic.setCurrentScore(statistic.getCurrentScore() +
                  BedwarsRel.getInstance().getIntConfig("statistics.scores.record", 100));
            }
          }
        }
      }

      for (Player player : this.game.getPlayers()) {
        if (this.game.isSpectator(player)) {
          continue;
        }

        if (BedwarsRel.getInstance().getBooleanConfig("rewards.enabled", false)) {
          List<String> commands = new ArrayList<String>();
          commands =
              (List<String>) BedwarsRel.getInstance().getConfig()
                  .getList("rewards.player-end-game");
          BedwarsRel.getInstance()
              .dispatchRewardCommands(commands, this.getRewardPlaceholders(player));
        }
      }
    }

    this.getGame().getPlayingTeams().clear();

    GameOverTask gameOver = new GameOverTask(this, delay, winner);
    gameOver.runTaskTimer(BedwarsRel.getInstance(), 0L, 20L);
  }

  private void sendTeamDeadMessage(Team deathTeam) {
    if (deathTeam.getPlayers().size() == 1 && deathTeam.isDead(this.getGame())) {
      for (Player aPlayer : this.getGame().getPlayers()) {
        if (aPlayer.isOnline()) {
          aPlayer.sendMessage(
              ChatWriter.pluginMessage(
                  ChatColor.RED + BedwarsRel._l(aPlayer, "ingame.team-dead", ImmutableMap.of("team",
                      deathTeam.getChatColor() + deathTeam.getDisplayName() + ChatColor.RED))));
        }
      }
    }
  }

  private boolean storeRecords(boolean storeHolders, Team winner) {
    int playTime = this.getGame().getLength() - this.getGame().getTimeLeft();
    boolean throughBed = false;

    if (playTime <= this.getGame().getRecord()) {

      // check for winning through bed destroy
      for (Team team : this.getGame().getPlayingTeams()) {
        if (team.isDead(this.getGame())) {
          throughBed = true;
          break;
        }
      }

      if (!throughBed) {
        for (Player aPlayer : this.getGame().getPlayers()) {
          if (aPlayer.isOnline()) {
            aPlayer.sendMessage(
                ChatWriter.pluginMessage(BedwarsRel._l(aPlayer, "ingame.record-nobeddestroy")));
          }
        }
        return false;
      }

      if (storeHolders) {
        if (playTime < this.getGame().getRecord()) {
          this.getGame().getRecordHolders().clear();
        }

        for (Player player : winner.getPlayers()) {
          this.getGame().addRecordHolder(player.getName());
        }
      }

      this.getGame().setRecord(playTime);
      this.getGame().saveRecord();

      for (Player aPlayer : this.getGame().getPlayers()) {
        if (aPlayer.isOnline()) {
          aPlayer.sendMessage(
              ChatWriter.pluginMessage(BedwarsRel._l(aPlayer, "ingame.newrecord",
                  ImmutableMap.of("record", this.getGame().getFormattedRecord(), "team",
                      winner.getChatColor() + winner.getDisplayName()))));
        }
      }

      return true;
    }

    return false;
  }

  private String winTitleReplace(String str, Team winner) {
    int playTime = this.getGame().getLength() - this.getGame().getTimeLeft();
    String finalStr = str;
    String formattedTime = Utils.getFormattedTime(playTime);

    finalStr = finalStr.replace("$time$", formattedTime);

    if (winner == null) {
      return finalStr;
    }

    finalStr = finalStr.replace("$team$", winner.getChatColor() + winner.getDisplayName());
    return finalStr;
  }

}
