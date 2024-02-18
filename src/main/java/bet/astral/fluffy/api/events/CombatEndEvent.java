package bet.astral.fluffy.api.events;

import bet.astral.fluffy.FluffyCombat;
import bet.astral.fluffy.api.CombatTag;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * When a player's combat tag ends, it will fire this.
 * When a player is no longer in combat, it fires CombatFullEndEvent
 * @see CombatFullEndEvent
 * @author Antritus
 * @since 1.0-SNAPSHOT
 */
public class CombatEndEvent extends AbstractCombatEvent {
	/**
	 *
	 * @param fluffyCombat main
	 * @param combatTag tag
	 */
	public CombatEndEvent(FluffyCombat fluffyCombat, CombatTag combatTag) {
		super(true, fluffyCombat, combatTag);
	}

	private static final HandlerList HANDLERS = new HandlerList();
	@NotNull
	public static HandlerList getHandlerList(){
		return HANDLERS;
	}
	@Override
	public @NotNull HandlerList getHandlers() {
		return HANDLERS;
	}}
