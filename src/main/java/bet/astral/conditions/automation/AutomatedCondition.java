package bet.astral.conditions.automation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface AutomatedCondition {
	String name();
	@Deprecated(forRemoval = true)
	Class<?> getType();
}
