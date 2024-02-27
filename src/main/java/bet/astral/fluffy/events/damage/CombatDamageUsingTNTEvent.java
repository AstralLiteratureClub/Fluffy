package bet.astral.fluffy.events.damage;

import bet.astral.fluffy.FluffyCombat;
import bet.astral.fluffy.api.CombatCause;
import bet.astral.fluffy.api.CombatTag;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CombatDamageUsingTNTEvent extends AbstractEntityCombatDamageEvent{
	private static final HandlerList HANDLER_LIST = new HandlerList();

	public CombatDamageUsingTNTEvent(FluffyCombat fluffyCombat, CombatTag combatTag, @NotNull Player entity, @NotNull OfflinePlayer damager, @Nullable ItemStack itemStack, @NotNull TNTPrimed usedEntity) {
		super(fluffyCombat, combatTag, entity, damager, CombatCause.TNT, itemStack, usedEntity);
	}

	public CombatDamageUsingTNTEvent(boolean isAsync, FluffyCombat fluffyCombat, CombatTag combatTag, @NotNull Player entity, @NotNull OfflinePlayer damager, @Nullable ItemStack itemStack, @NotNull TNTPrimed usedEntity) {
		super(isAsync, fluffyCombat, combatTag, entity, damager, CombatCause.TNT, itemStack, usedEntity);
	}

	@Override
	public @NotNull TNTPrimed getUsedEntity() {
		return (TNTPrimed) super.getUsedEntity();
	}

	@Override
	public @NotNull HandlerList getHandlers() {
		return HANDLER_LIST;
	}
	public static HandlerList getHandlerList(){
		return HANDLER_LIST;
	}
}
