package io.github.yannici.bedwars.Database;

public abstract class DatabaseObject {

	private long id = 0;

	public DatabaseObject() {
		this.id = 0;
	}

	@DBGetField(name = "id", dbType = "INT(10) UNSIGNED", autoInc = true)
	public long getId() {
		return this.id;
	}

	@DBSetField(name = "id")
	public void setId(long id) {
		this.id = id;
	}

	public boolean isNew() {
		return (this.id == 0);
	}

}