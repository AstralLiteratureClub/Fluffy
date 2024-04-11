package bet.astral.conditions.conditions;

import com.google.common.reflect.TypeToken;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

public class CondItemIs extends ItemCondition<NamespacedKey> {
	@Override
	public boolean check(@Nullable ItemStack object, @Nullable NamespacedKey key) {
		assert object != null;
		return object.getType().getKey().equals(key);
	}

	@Override
	public TypeToken<NamespacedKey> getValueType() {
		return new TypeToken<>(NamespacedKey.class) {};
	}
}
