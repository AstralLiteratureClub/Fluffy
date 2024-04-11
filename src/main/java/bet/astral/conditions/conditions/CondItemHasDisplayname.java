package bet.astral.conditions.conditions;

import bet.astral.conditions.automation.AutomatedCondition;
import com.google.common.reflect.TypeToken;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

@AutomatedCondition(name = "item_has_[display]name", getType = ItemStack.class)
public class CondItemHasDisplayname extends ItemCondition<Boolean> {
	@Override
	public boolean check(ItemStack object, @NotNull Boolean value) {
		if (object == null){
			return false;
		}
		boolean bool = value;
		return bool && object.getItemMeta().hasDisplayName();
	}

	@Override
	public TypeToken<Boolean> getValueType() {
		return new TypeToken<>(Boolean.class) {};
	}

}
