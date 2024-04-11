package bet.astral.conditions.conditions;

import com.google.common.reflect.TypeToken;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

public class CondKeyContainsKey implements Condition<Collection<String>, String> {
	@Override
	public boolean check(@Nullable Collection<String> object, @Nullable String s) {
		assert object != null;
		return object.contains(s);
	}

	@Override
	public TypeToken<Collection<String>> getType() {
		return new TypeToken<Collection<String>>() { };
	}

	@Override
	public TypeToken<String> getValueType() {
		return new TypeToken<>(String.class) {};
	}

}