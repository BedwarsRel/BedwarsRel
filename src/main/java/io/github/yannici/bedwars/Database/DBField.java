package io.github.yannici.bedwars.Database;

import java.lang.reflect.Method;

public class DBField {

	private Method getter = null;
	private Method setter = null;

	public DBField() {
		this.getter = null;
		this.setter = null;
	}

	public DBField(Method getter, Method setter) {
		this.getter = getter;
		this.setter = setter;
	}

	public Method getGetter() {
		return this.getter;
	}

	public Method getSetter() {
		return this.setter;
	}

	public void setGetter(Method getter) {
		this.getter = getter;
	}

	public void setSetter(Method setter) {
		this.setter = setter;
	}

}
