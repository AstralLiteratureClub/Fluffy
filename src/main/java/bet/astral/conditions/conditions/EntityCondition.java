package bet.astral.conditions.conditions;

import com.google.common.reflect.TypeToken;
import org.bukkit.entity.Entity;

public abstract class EntityCondition<Value> implements Condition<Entity, Value> {
	@Override
	public TypeToken<Entity> getType() {
		return new TypeToken<>(Entity.class) {};
	}

}
