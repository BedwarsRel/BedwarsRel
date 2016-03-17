package io.github.yannici.bedwars.Statistics;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import io.github.yannici.bedwars.ChatWriter;
import io.github.yannici.bedwars.Main;
import io.github.yannici.bedwars.Database.DBField;
import io.github.yannici.bedwars.Database.DBGetField;
import io.github.yannici.bedwars.Database.DBSetField;
import io.github.yannici.bedwars.Database.DatabaseObject;

public abstract class StoringTable extends DatabaseObject {

	private Map<String, DBField> fields = null;

	public StoringTable() {
		this.fields = new HashMap<String, DBField>();
		this.loadFields();
	}

	private void loadFields() {
		this.fields.clear();

		for (Method method : this.getClass().getMethods()) {
			DBGetField getAnnotation = method.getAnnotation(DBGetField.class);
			DBSetField setAnnotation = method.getAnnotation(DBSetField.class);

			if (getAnnotation == null && setAnnotation == null) {
				continue;
			}

			String fieldName = (getAnnotation != null) ? getAnnotation.name() : setAnnotation.name();

			if (this.fields.containsKey(fieldName)) {
				DBField field = this.fields.get(fieldName);

				if (getAnnotation == null) {
					field.setSetter(method);
				} else {
					field.setGetter(method);
				}
			} else {
				DBField field = new DBField();

				if (getAnnotation == null) {
					field.setSetter(method);
				} else {
					field.setGetter(method);
				}

				this.fields.put(fieldName, field);
			}
		}
	}

	public Map<String, DBField> getFields() {
		return this.fields;
	}

	public abstract String getTableName();

	public abstract String getKeyField();

	public abstract void load();

	public abstract void store();

	public abstract void setDefault();

	public Object getValue(String field) {
		try {
			Method getter = this.fields.get(field).getGetter();

			getter.setAccessible(true);
			return getter.invoke(this, new Object[] {});
		} catch (Exception ex) {
			Main.getInstance().getServer().getConsoleSender()
					.sendMessage(ChatWriter.pluginMessage("Couldn't fetch value of field: " + field));
		}

		return null;
	}

	public void setValue(String field, Object value) {
		try {
			Method setter = this.fields.get(field).getSetter();

			if (setter == null) {
				return;
			}

			setter.setAccessible(true);

			Class<?> paramType = setter.getParameterTypes()[0];

			try {
				if (value instanceof Number) {
					String classname = value.getClass().getSimpleName().toLowerCase();
					if (value.getClass().equals(Integer.class)) {
						classname = "int";
					}

					Method castNumber = value.getClass().getMethod(classname + "Value", new Class<?>[] {});
					castNumber.setAccessible(true);

					Class<?> returningNumberType = castNumber.getReturnType();
					if (!paramType.equals(returningNumberType)) {
						setter.invoke(this, new Object[] { paramType.cast(castNumber.invoke(value, new Object[] {})) });
						return;
					}

					setter.invoke(this, new Object[] { castNumber.invoke(value, new Object[] {}) });
					return;
				}

				value = paramType.cast(value);
				setter.invoke(this, new Object[] { value });
			} catch (Exception ex) {
				Main.getInstance().getServer().getConsoleSender()
						.sendMessage(ChatWriter.pluginMessage("Couldn't cast value for field: " + field));
			}
		} catch (Exception ex) {
			Main.getInstance().getServer().getConsoleSender()
					.sendMessage(ChatWriter.pluginMessage("Couldn't set value of field: " + field));
		}
	}
}
