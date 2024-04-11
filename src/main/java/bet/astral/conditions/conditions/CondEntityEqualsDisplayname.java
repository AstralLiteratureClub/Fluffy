package bet.astral.conditions.conditions;

import bet.astral.conditions.automation.AutomatedCondition;
import com.google.common.reflect.TypeToken;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.entity.Entity;
import org.jetbrains.annotations.Nullable;

@AutomatedCondition(name = "entity_has_displayname", getType = Entity.class)
public class CondEntityEqualsDisplayname extends EntityCondition<String> {

	@Override
	public boolean check(@Nullable Entity object, String s) {
		if (object == null){
			return false;
		}
		Component component = object.customName();
		if (component == null){
			return s == null;
		}
		return PlainTextComponentSerializer.plainText().serialize(component).equalsIgnoreCase(s);
	}

	@Override
	public TypeToken<String> getValueType() {
		return new TypeToken<>(String.class) { };
	}


	@Override
	public boolean allowsNullValue() {
		return true;
	}
}
