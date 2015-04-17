package io.github.yannici.bedwars.Game;

import org.bukkit.scheduler.BukkitRunnable;

public class GameOverTask extends BukkitRunnable {

	private int counter = 10;
	private int counterStart = 10;
	private GameCycle cycle = null;
	private Team winner = null;

	public GameOverTask(GameCycle cycle, int counter, Team winner) {
		this.counterStart = counter;
		this.counter = counter;
		this.cycle = cycle;
		this.winner = winner;
	}

	@Override
	public void run() {
		this.cycle.onGameOver(this);
	}

	public int getStartCount() {
		return this.counterStart;
	}

	public Team getWinner() {
		return this.winner;
	}

	public int getCounter() {
		return this.counter;
	}

	public void setCounter(int counter) {
		this.counter = counter;
	}

	public void decCounter() {
		this.counter--;
	}

	public GameCycle getCycle() {
		return this.cycle;
	}

}
