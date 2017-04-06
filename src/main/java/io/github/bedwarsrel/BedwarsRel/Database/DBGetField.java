package io.github.bedwarsrel.BedwarsRel.Database;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)

public @interface DBGetField {

  public boolean autoInc() default false;

  public String dbType();

  public String defaultValue() default "";

  public String name();

  public boolean notNull() default true;

}
