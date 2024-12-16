package bet.astral.fluffy.utils;

import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.ApiStatus;

@Deprecated
@ApiStatus.ScheduledForRemoval(inVersion = "When paper implements better system for this")
public final class ItemStackUtils {
    public static <P, C> void setValue(ItemStack itemStack, NamespacedKey key, PersistentDataType<P, C> type, C value){
        ItemMeta meta = itemStack.getItemMeta();
        meta.getPersistentDataContainer().set(key, type, value);
        itemStack.setItemMeta(meta);
    }
    public static <P, C> void resetValue(ItemStack itemStack, NamespacedKey key) {
        ItemMeta meta = itemStack.getItemMeta();
        meta.getPersistentDataContainer().remove(key);
        itemStack.setItemMeta(meta);
    }
}
