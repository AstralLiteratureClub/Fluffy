package bet.astral.fluffy.api.events;

import bet.astral.fluffy.FluffyCombat;
import bet.astral.fluffy.api.CombatTag;
import org.bukkit.event.Event;

/**
 * Default for many combat events.
 * This is only abstract class, so it won't fire.
 */
public abstract class AbstractCombatEvent extends Event {
	private final CombatTag combatTag;
	private final FluffyCombat fluffyCombat;

	/**
	 * The default constructor is defined for cleaner code. This constructor
	 * assumes the event is synchronous.
	 */
	public AbstractCombatEvent(FluffyCombat fluffyCombat, CombatTag combatTag) {
		this.fluffyCombat = fluffyCombat;
		this.combatTag = combatTag;
	}

	/**
	 * This constructor is used to explicitly declare an event as synchronous
	 * or asynchronous.
	 *
	 * @param isAsync true indicates the event will fire asynchronously, false
	 *                by default from default constructor
	 */
	public AbstractCombatEvent(boolean isAsync, FluffyCombat fluffyCombat, CombatTag combatTag) {
		super(isAsync);
		this.fluffyCombat = fluffyCombat;
		this.combatTag = combatTag;
	}

	/**
	 * Returns the combat tag in this event
	 * @return tag
	 */

	public CombatTag getCombatTag() {
		return combatTag;
	}

	/**
	 * Returns the main class instance
	 * @return main class instance
	 */
	public FluffyCombat getFluffyCombat() {
		return fluffyCombat;
	}
}
