package bet.astral.fluffy.events.damage;

import bet.astral.fluffy.FluffyCombat;
import bet.astral.fluffy.api.CombatCause;
import bet.astral.fluffy.api.CombatTag;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CombatDamageUsingCustomEntityEvent extends AbstractEntityCombatDamageEvent{
	private static final HandlerList HANDLER_LIST = new HandlerList();

	public CombatDamageUsingCustomEntityEvent(FluffyCombat fluffyCombat, CombatTag combatTag, @NotNull Player entity, @NotNull OfflinePlayer damager, @NotNull CombatCause combatCause, @Nullable ItemStack itemStack, @NotNull Entity usedEntity) {
		super(fluffyCombat, combatTag, entity, damager, combatCause, itemStack, usedEntity);
	}

	public CombatDamageUsingCustomEntityEvent(boolean isAsync, FluffyCombat fluffyCombat, CombatTag combatTag, @NotNull Player entity, @NotNull OfflinePlayer damager, @NotNull CombatCause combatCause, @Nullable ItemStack itemStack, @NotNull Entity usedEntity) {
		super(isAsync, fluffyCombat, combatTag, entity, damager, combatCause, itemStack, usedEntity);
	}

	@Override
	public @NotNull HandlerList getHandlers() {
		return HANDLER_LIST;
	}
	public static HandlerList getHandlerList(){
		return HANDLER_LIST;
	}
}
