package bet.astral.conditions.conditions;

import bet.astral.conditions.automation.AutomatedCondition;
import com.google.common.reflect.TypeToken;
import org.bukkit.entity.Entity;
import org.jetbrains.annotations.Nullable;

@AutomatedCondition(name = "entity_has_name", getType = Entity.class)
public class CondEntityHasDisplayname extends EntityCondition<Boolean>{
	@Override
	public boolean check(@Nullable Entity object, Boolean aBoolean) {
		assert aBoolean != null;
		if (object == null){
			return false;
		}
		return aBoolean && object.customName() != null;
	}

	@Override
	public TypeToken<Boolean> getValueType() {
		return new TypeToken<>(Boolean.class) { };
	}
}
