package bet.astral.fluffy.api.events;

import bet.astral.fluffy.FluffyCombat;
import org.bukkit.OfflinePlayer;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * When a player is no longer in combat, this event fires.
 * @see CombatEndEvent
 */
public class CombatFullEndEvent extends Event {
	private final FluffyCombat fluffyCombat;
	private final OfflinePlayer player;

	/**
	 * @param fluffyCombat main
	 * @param player player whose tags ended
	 */
	public CombatFullEndEvent(FluffyCombat fluffyCombat, OfflinePlayer player) {
		super();
		this.fluffyCombat = fluffyCombat;
		this.player = player;
	}
	/**
	 * @param fluffyCombat main
	 * @param player player whose tags ended
	 */
	public CombatFullEndEvent(boolean async, FluffyCombat fluffyCombat, OfflinePlayer player) {
		super(async);
		this.fluffyCombat = fluffyCombat;
		this.player = player;
	}

	/**
	 * Returns the main plugin instance
	 * @return plugin instance
	 */
	public FluffyCombat getFluffyCombat() {
		return fluffyCombat;
	}

	private static final HandlerList HANDLERS = new HandlerList();
	@NotNull
	public static HandlerList getHandlerList(){
		return HANDLERS;
	}
	@Override
	public @NotNull HandlerList getHandlers() {
		return HANDLERS;
	}

	public OfflinePlayer player() {
		return player;
	}
}
