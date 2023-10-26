package me.antritus.astral.fluffycombat.api.events;

import me.antritus.astral.fluffycombat.FluffyCombat;
import me.antritus.astral.fluffycombat.api.CombatTag;
import org.bukkit.event.Event;

import java.util.List;

/**
 * Default for many combat events.
 * This is only abstract class, so it won't fire.
 */
public abstract class AbstractListCombatEvent extends Event {
	private final List<CombatTag> combatTags;
	private final FluffyCombat fluffyCombat;

	/**
	 * The default constructor is defined for cleaner code. This constructor
	 * assumes the event is synchronous.
	 * @param fluffyCombat The main plugin instance
	 * @param combatTags tags
	 */
	public AbstractListCombatEvent(FluffyCombat fluffyCombat, List<CombatTag> combatTags) {
		this.fluffyCombat = fluffyCombat;
		this.combatTags = combatTags;
	}

	/**
	 * This constructor is used to explicitly declare an event as synchronous
	 * or asynchronous.
	 *
	 * @param isAsync true indicates the event will fire asynchronously, false
	 *                by default from default constructor
	 * @param fluffyCombat The main plugin instance
	 * @param combatTags tags
	 */
	public AbstractListCombatEvent(boolean isAsync, FluffyCombat fluffyCombat, List<CombatTag> combatTags) {
		super(isAsync);
		this.fluffyCombat = fluffyCombat;
		this.combatTags = combatTags;
	}

	/**
	 * Returns the combat tags in this event
	 * @return tags
	 */
	public List<CombatTag> getCombatTag() {
		return combatTags;
	}

	/**
	 * Returns the main class instance
	 * @return main class instance
	 */
	public FluffyCombat getFluffyCombat() {
		return fluffyCombat;
	}
}
