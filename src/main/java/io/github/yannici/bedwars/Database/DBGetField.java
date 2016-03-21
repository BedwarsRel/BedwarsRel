package io.github.yannici.bedwars.Database;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)

public @interface DBGetField {

	public String name();

	public String dbType();

	public String defaultValue() default "";

	public boolean notNull() default true;

	public boolean autoInc() default false;

}
