package bet.astral.fluffy.events.player;

import bet.astral.fluffy.FluffyCombat;
import bet.astral.fluffy.events.CombatTagEndEvent;
import lombok.Getter;
import org.bukkit.OfflinePlayer;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * When a player is no longer in combat, this event fires.
 * @see CombatTagEndEvent
 */
public class PlayerCombatFullEndEvent extends Event {
	/**
	 * -- GETTER --
	 *  Returns the main plugin instance
	 *
	 * @return plugin instance
	 */
	@Getter
	private final FluffyCombat fluffyCombat;
	private final OfflinePlayer player;

	/**
	 * @param fluffyCombat main
	 * @param player player whose tags ended
	 */
	public PlayerCombatFullEndEvent(FluffyCombat fluffyCombat, OfflinePlayer player) {
		super();
		this.fluffyCombat = fluffyCombat;
		this.player = player;
	}
	/**
	 * @param fluffyCombat main
	 * @param player player whose tags ended
	 */
	public PlayerCombatFullEndEvent(boolean async, FluffyCombat fluffyCombat, OfflinePlayer player) {
		super(async);
		this.fluffyCombat = fluffyCombat;
		this.player = player;
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
