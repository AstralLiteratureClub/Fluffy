package me.antritus.astral.fluffycombat.database;

import java.lang.annotation.*;

/**
 * Creates database field based on given field name. Uses the database to automatically save given field.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface DatabaseField {
	String fieldName() default "-default";
	int defaultValue() default 0;
}
