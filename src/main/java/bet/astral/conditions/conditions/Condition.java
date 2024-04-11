package bet.astral.conditions.conditions;

import com.google.common.reflect.TypeToken;
import org.jetbrains.annotations.Nullable;

public interface Condition<Object, Value> {
	boolean check(@Nullable Object object, @Nullable Value value);

	TypeToken<Object> getType();

	TypeToken<Value> getValueType();

	default boolean allowsNullValue() {
		return false;
	}

	default boolean checkInternal(java.lang.Object object, java.lang.Object required) {
		if (!getType().isSubtypeOf(object.getClass())) {
			return false;
		} else if (!getValueType().isSubtypeOf(required.getClass())){
			return false;
		}
		//noinspection unchecked
		return check((Object) object, (Value) required);
	}
}
