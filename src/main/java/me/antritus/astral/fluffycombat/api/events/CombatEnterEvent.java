package me.antritus.astral.fluffycombat.api.events;

import me.antritus.astral.fluffycombat.FluffyCombat;
import me.antritus.astral.fluffycombat.api.CombatTag;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * CombatEnterEvent is triggered when the player
 *  enters combat and the event is not canceled.
 *
 * @author Antritus
 * @since 1.0-SNAPSHOT
 */
public class CombatEnterEvent extends AbstractCombatEvent {
	/**
	 *
	 * @param fluffyCombat main
	 * @param combatTag tag
	 */
	public CombatEnterEvent(FluffyCombat fluffyCombat, CombatTag combatTag) {
		super(fluffyCombat, combatTag);
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
