package me.antritus.astral.fluffycombat.api.events;

import me.antritus.astral.fluffycombat.FluffyCombat;
import me.antritus.astral.fluffycombat.api.CombatTag;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * CombatEnterEvent is triggered when the player's
 *  combat ends.
 *
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
