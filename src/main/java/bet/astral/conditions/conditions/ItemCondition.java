package bet.astral.conditions.conditions;

import com.google.common.reflect.TypeToken;
import org.bukkit.inventory.ItemStack;

public abstract class ItemCondition<Value> implements Condition<ItemStack, Value> {
	@Override
	public TypeToken<ItemStack> getType() {
		return new TypeToken<>(ItemStack.class) {};
	}
}
