package bet.astral.fluffy.messenger;

import bet.astral.messenger.Messenger;
import bet.astral.messenger.placeholder.Placeholder;
import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;

import java.lang.annotation.*;
import java.lang.reflect.Field;
import java.util.Map;

public final class MessageKey
{
	/**
	 * Loads all the messages inside this class. This uses reflections so it may throw out errors.
	 * @param messenger messenger to load the messages from
	 */
	public static void loadMessages(Messenger<?> messenger) {
		for (Field field : MessageKey.class.getFields()){
			try {
				if (!field.isAnnotationPresent(KeyType.class)){
					return;
				}
				KeyType type = field.getAnnotation(KeyType.class);
				if (!type.autoLoad()){
					continue;
				}
				field.setAccessible(true);
				Object value = field.get(null);
				if (value instanceof String key){
					switch (type.value()){
						case PLACEHOLDER -> {
							Map<String, Placeholder> placeholders = messenger.loadPlaceholders(key);
							if (placeholders.isEmpty()){
								continue;
							}
							placeholders.forEach(messenger::overrideDefaultPlaceholder);
						}
						case MESSAGE -> {
							messenger.loadMessage(key);
						}
					}
					messenger.loadMessage(key);
				}
			} catch (IllegalAccessException e) {
				throw new RuntimeException(e);
			}

		}
	}
	@KeyType(Type.PLACEHOLDER)
	public static final String DEFAULT_PLACEHOLDER = "placeholders";

	@KeyType(Type.MESSAGE)
	public static final String COMBAT_ILLEGAL_COMMAND = "combat-execute-illegal-command";
	@KeyType(Type.MESSAGE)
	public static final String COMBAT_END = "combat-end";
	/*
	 * Rejoin - NPC
	 */
	@KeyType(Type.MESSAGE)
	public static final String COMBAT_REJOIN_NPC_REPLACEMENT_ALIVE_BROADCAST = "combat-logged-rejoin-npc-replacement-alive-broadcast";
	@KeyType(Type.MESSAGE)
	public static final String COMBAT_REJOIN_NPC_REPLACEMENT_ALIVE = "combat-logged-rejoin-npc-replacement-alive-player";
	@KeyType(Type.MESSAGE)
	public static final String COMBAT_REJOIN_NPC_REPLACEMENT_DEAD_BROADCAST = "combat-logged-rejoin-npc-replacement-alive-broadcast";
	@KeyType(Type.MESSAGE)
	public static final String COMBAT_REJOIN_NPC_REPLACEMENT_DEAD = "combat-logged-rejoin-npc-replacement-alive-player";
	/*
	 * Rejoin
	 */
	@KeyType(Type.MESSAGE)
	public static final String COMBAT_REJOIN_BROADCAST = "combat-logged-rejoin-broadcast";
	@KeyType(Type.MESSAGE)
	public static final String COMBAT_REJOIN_PLAYER = "combat-logged-rejoin-player";
	/*
	 * Combat LOG OUT
	 */
	@KeyType(Type.MESSAGE)
	public static final String COMBAT_LOG_BROADCAST = "combat-logged-quit-npc-spawn-broadcast";
	@KeyType(Type.MESSAGE)
	public static final String COMBAT_LOG_NPC_SPAWN_BROADCAST = "combat-logged-quit-broadcast";
	/*
	 * Combat tagging
	 */
	@KeyType(Type.MESSAGE)
	public static final String COMBAT_ENTER_VICTIM = "combat-enter-victim";
	@KeyType(Type.MESSAGE)
	public static final String COMBAT_ENTER_ATTACKER = "combat-enter-attacker";


	/*
	 * 1 second messages
	 */
	@KeyType(Type.MESSAGE)
	public static final String COMBAT_1_SECOND_MESSAGE = "combat-every-1-second";
	/*
	 * Item restrictions
	 */
	@KeyType(Type.MESSAGE)
	public static final String COMBAT_USE_ITEM_ELYTRA_GLIDE = "combat-use-item-elytra-glide";
	@KeyType(Type.MESSAGE)
	public static final String COMBAT_USE_ITEM_ELYTRA_ROCKET_BOOST = "combat-use-item-elytra-rocket-boost";
	@KeyType(Type.MESSAGE)
	public static final String COMBAT_USE_ITEM_TRIDENT_RIPTIDE = "combat-use-item-trident-riptide";
	/*
	 * Cooldowns - Item
	 */
	@KeyType(Type.MESSAGE)
	public static final String COMBAT_COOLDOWN_DEFAULT = "combat-item-cooldown-default";
	@KeyType(value = Type.MESSAGE, autoLoad = false)
	public static final String COMBAT_COOLDOWN_ITEM_SPECIFIC = "combat-item-cooldown-%item%";

	@NotNull
	public static String itemSpecificItemCooldown(@NotNull Material material){
		return COMBAT_COOLDOWN_ITEM_SPECIFIC.replace("%item%", material.name().toLowerCase());
	}

	/*
	 * Cooldowns - Command
	 */


	@KeyType(Type.MESSAGE)
	public static final String COMBAT_COOLDOWN_COMMAND = "combat-command-cooldown";

	@Documented
	@Target(ElementType.FIELD)
	@Retention(RetentionPolicy.RUNTIME)
	public @interface KeyType {
		Type value() default Type.MESSAGE;
		boolean autoLoad() default true;
	}

	public enum Type {
		PLACEHOLDER,
		MESSAGE,
	}
}
