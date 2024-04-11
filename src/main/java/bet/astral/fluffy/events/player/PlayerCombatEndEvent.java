package bet.astral.fluffy.events.player;

import bet.astral.fluffy.FluffyCombat;
import bet.astral.fluffy.api.CombatTag;
import bet.astral.fluffy.events.CombatTagEndEvent;
import lombok.Getter;
import org.bukkit.OfflinePlayer;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

@Getter
public class PlayerCombatEndEvent extends CombatTagEndEvent {
	private static final HandlerList HANDLER_LIST = new HandlerList();
	private final OfflinePlayer player;

	/**
	 * @param fluffyCombat main
	 * @param combatTag    tag
	 */
	public PlayerCombatEndEvent(FluffyCombat fluffyCombat, CombatTag combatTag, OfflinePlayer player) {
		super(fluffyCombat, combatTag);
		this.player = player;
	}

	@Override
	public @NotNull HandlerList getHandlers() {
		return HANDLER_LIST;
	}

	public @NotNull static HandlerList getHandlerList(){
		return HANDLER_LIST;
	}
}
