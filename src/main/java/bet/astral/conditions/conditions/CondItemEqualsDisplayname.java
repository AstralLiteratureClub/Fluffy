package bet.astral.conditions.conditions;

import bet.astral.conditions.automation.AutomatedCondition;
import com.google.common.reflect.TypeToken;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.inventory.ItemStack;

@AutomatedCondition(name = "item_displayname", getType = ItemStack.class)
public class CondItemEqualsDisplayname extends ItemCondition<String>{

	@Override
	public boolean check(ItemStack object, String value) {
		if (object == null){
			return false;
		}
		Component displayname = object.displayName();
		if (!object.getItemMeta().hasDisplayName()) {
			return value == null;
		}

		return PlainTextComponentSerializer.plainText().serialize(displayname)
				.equalsIgnoreCase(value);
	}

	@Override
	public TypeToken<String> getValueType() {
		return new TypeToken<>(String.class) {};
	}


	@Override
	public boolean allowsNullValue() {
		return true;
	}
}
