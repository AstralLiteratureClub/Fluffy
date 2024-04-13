package bet.astral.fluffy.messenger;

import bet.astral.messenger.Messenger;
import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;

import java.lang.annotation.*;
import java.lang.reflect.Field;

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
				if (value instanceof String key) {
					messenger.loadMessage(key);
				}
			} catch (IllegalAccessException e) {
				throw new RuntimeException(e);
			}

		}
	}

	@KeyType()
	public static final String COMBAT_ILLEGAL_COMMAND = "combat-execute-illegal-command";
	@KeyType()
	public static final String COMBAT_END = "combat-end";
	/*
	 * Rejoin - NPC
	 */
	@KeyType()
	public static final String COMBAT_REJOIN_NPC_REPLACEMENT_ALIVE_BROADCAST = "combat-logged-rejoin-npc-replacement-alive-broadcast";
	@KeyType()
	public static final String COMBAT_REJOIN_NPC_REPLACEMENT_ALIVE = "combat-logged-rejoin-npc-replacement-alive-player";
	@KeyType()
	public static final String COMBAT_REJOIN_NPC_REPLACEMENT_DEAD_BROADCAST = "combat-logged-rejoin-npc-replacement-alive-broadcast";
	@KeyType()
	public static final String COMBAT_REJOIN_NPC_REPLACEMENT_DEAD = "combat-logged-rejoin-npc-replacement-alive-player";
	/*
	 * Rejoin
	 */
	@KeyType()
	public static final String COMBAT_REJOIN_BROADCAST = "combat-logged-rejoin-broadcast";
	@KeyType()
	public static final String COMBAT_REJOIN_PLAYER = "combat-logged-rejoin-player";
	/*
	 * Combat LOG OUT
	 */
	@KeyType()
	public static final String COMBAT_LOG_BROADCAST = "combat-logged-quit-broadcast";
	@KeyType()
	public static final String COMBAT_LOG_NPC_SPAWN_BROADCAST = "combat-logged-quit-npc-spawn-broadcast";
	/*
	 * Combat tagging
	 */
	@KeyType()
	public static final String COMBAT_ENTER_VICTIM = "combat-enter-victim";
	@KeyType()
	public static final String COMBAT_ENTER_ATTACKER = "combat-enter-attacker";


	/*
	 * 1 second messages
	 */
	@KeyType()
	public static final String COMBAT_1_SECOND_MESSAGE = "combat-every-1-second";
	/*
	 * Item restrictions
	 */
	@KeyType()
	public static final String COMBAT_USE_ITEM_ELYTRA_GLIDE = "combat-use-item-elytra-glide";
	@KeyType()
	public static final String COMBAT_USE_ITEM_ELYTRA_ROCKET_BOOST = "combat-use-item-elytra-rocket-boost";
	@KeyType()
	public static final String COMBAT_USE_ITEM_TRIDENT_RIPTIDE = "combat-use-item-trident-riptide";
	/*
	 * Cooldowns - Item
	 */
	@KeyType()
	public static final String COMBAT_COOLDOWN_DEFAULT = "combat-item-cooldown-default";
	@KeyType(autoLoad = false)
	public static final String COMBAT_COOLDOWN_ITEM_SPECIFIC = "combat-item-cooldown-%item%";

	@NotNull
	public static String itemSpecificItemCooldown(@NotNull Material material){
		return COMBAT_COOLDOWN_ITEM_SPECIFIC.replace("%item%", material.name().toLowerCase());
	}

	/*
	 * Cooldowns - Command
	 */


	@KeyType()
	public static final String COMBAT_COOLDOWN_COMMAND = "combat-command-cooldown";




	/*
	 * Deaths
	 */


	@KeyType(autoLoad = false)
	public static final String DEATH_ENTITY_EXPLOSION_COMBAT = "entity_explosion.combat.*";

	/*
	 * Commands
	 */
	public static final String STATS_CONSOLE = "statistics.usage";
	public static final String STATS_OTHER = "statistics.other";
	public static final String STATS_SELF = "statistics.self";

	public static final String EDIT_STATS_RESET = "editstatistics.reset";
	public static final String EDIT_STATS_SET = "editstatistics.reset";
	public static final String EDIT_STATS_ADD = "editstatistics.reset";
	public static final String EDIT_STATS_REMOVE = "editstatistics.reset";

	public static final String EDIT_STATS_HELP = "editstatistics.help.main";
	public static final String EDIT_STATS_HELP_RESET = "editstatistics.help.reset";
	public static final String EDIT_STATS_HELP_SET = "editstatistics.help.set";
	public static final String EDIT_STATS_HELP_ADD = "editstatistics.help.add";
	public static final String EDIT_STATS_HELP_REMOVE = "editstatistics.help.remove";




	@Documented
	@Target(ElementType.FIELD)
	@Retention(RetentionPolicy.RUNTIME)
	public @interface KeyType {
		boolean autoLoad() default true;
	}
}
