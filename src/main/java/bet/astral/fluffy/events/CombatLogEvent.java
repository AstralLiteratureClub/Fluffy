package bet.astral.fluffy.events;

import bet.astral.fluffy.FluffyCombat;
import bet.astral.fluffy.api.CombatTag;
import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * CombatLogEvent is triggered when the player
 *  is ion combat and quits from the server.
 *
 * @author Antritus
 * @since 1.0-SNAPSHOT
 */
public class CombatLogEvent extends AbstractListCombatEvent implements Cancellable {

	/**
	 * -- GETTER --
	 *  Returns the player who left the server
	 *
	 */
	@Getter
	private final Player who;
	private boolean cancel;

	/**
	 * @param fluffyCombat main
	 * @param who Who just combat logged
	 * @param combatTag tag
	 */
	public CombatLogEvent(FluffyCombat fluffyCombat, Player who, List<CombatTag> combatTag) {
		super(fluffyCombat, combatTag);
		this.who = who;
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

	@Override
	public boolean isCancelled() {
		return cancel;
	}

	@Override
	public void setCancelled(boolean cancel) {
		this.cancel = cancel;
	}
}
