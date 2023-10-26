package me.antritus.astral.fluffycombat.api;

import me.antritus.astral.fluffycombat.FluffyCombat;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

public class EntityAPI {
	private static final FluffyCombat fluffyCombat = FluffyCombat.getPlugin(FluffyCombat.class);
	private static final String ITEM_KEY = "fluffycombat;";
	/**
	 * Makes this api private and allows only static methods.
	 */
	private EntityAPI(){}

	/**
	 * Stores ItemStack to the entity's metadata.
	 * @param entity entity
	 * @param itemStack item
	 * @see EntityAPI#getItem(Entity, String)
	 */
	public static void setItem(@NotNull Entity entity, @NotNull ItemStack itemStack, @NotNull String name){
		entity.removeMetadata(ITEM_KEY+name, fluffyCombat);
		entity.setMetadata(ITEM_KEY+name, new FixedMetadataValue(fluffyCombat, itemStack.serializeAsBytes()));
	}

	/**
	 * Returns the item stored in the entity.
	 * @param entity entity
	 * @return item if found, else null
	 * @see EntityAPI#setItem(Entity, ItemStack, String)
	 */
	@Nullable
	public static ItemStack getItem(@NotNull Entity entity, @NotNull String name){
		List<MetadataValue> metadataValues = entity.getMetadata(ITEM_KEY+name);
		if (metadataValues.isEmpty()){
			return null;
		}
		Optional<MetadataValue> metadataValueOptional = metadataValues.stream().filter(filter->filter.getOwningPlugin() != null).filter(plugin->plugin.getOwningPlugin().getName().equalsIgnoreCase(fluffyCombat.getName())).findAny();
		MetadataValue value = metadataValueOptional.orElse(null);
		if (value==null){
			return null;
		}
		return ItemStack.deserializeBytes((byte[]) value.value());
	}

	/**
	 * Sets the given object to key: fluffycombat;%name%.
	 *
	 * @param entity entity
	 * @param object object
	 * @param name name
	 */
	public static void set(@NotNull Entity entity, @NotNull Object object, @NotNull String name){
		entity.removeMetadata(ITEM_KEY+name, fluffyCombat);
		entity.setMetadata(ITEM_KEY+name, new FixedMetadataValue(fluffyCombat, object));
	}

	/**
	 * Sets gets given object with key fluffycombat;%name%
	 * @param entity entity
	 * @param name name
	 * @return object, or null
	 */
	public static Object get(@NotNull Entity entity, @NotNull String name){
		List<MetadataValue> metadataValues = entity.getMetadata(ITEM_KEY+name);
		if (metadataValues.isEmpty()){
			return null;
		}
		Optional<MetadataValue> metadataValueOptional = metadataValues.stream().filter(filter->filter.getOwningPlugin() != null).filter(plugin->plugin.getOwningPlugin().getName().equalsIgnoreCase(fluffyCombat.getName())).findAny();
		MetadataValue value = metadataValueOptional.orElse(null);
		if (value==null){
			return null;
		}
		return value.value();
	}
}
