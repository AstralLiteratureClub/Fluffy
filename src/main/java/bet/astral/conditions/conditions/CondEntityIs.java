package bet.astral.conditions.conditions;

import com.google.common.reflect.TypeToken;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Entity;
import org.jetbrains.annotations.Nullable;

public class CondEntityIs extends EntityCondition<NamespacedKey>{
	@Override
	public boolean check(@Nullable Entity object, @Nullable NamespacedKey s) {
		assert object != null;
		return object.getType().getKey().equals(s);
	}

	@Override
	public TypeToken<NamespacedKey> getValueType() {
		return new TypeToken<NamespacedKey>(NamespacedKey.class) {};
	}
}
