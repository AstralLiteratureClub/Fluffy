package me.antritus.astral.fluffycombat.api.events;

import me.antritus.astral.fluffycombat.FluffyCombat;
import me.antritus.astral.fluffycombat.api.CombatTag;
import org.bukkit.event.Event;

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
	 * Returns the main class instance
	 * @return main class instance
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
