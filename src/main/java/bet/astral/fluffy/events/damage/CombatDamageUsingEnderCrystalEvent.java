package bet.astral.fluffy.events.damage;

import bet.astral.fluffy.FluffyCombat;
import bet.astral.fluffy.api.CombatCause;
import bet.astral.fluffy.api.CombatTag;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.EnderCrystal;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CombatDamageUsingEnderCrystalEvent extends AbstractEntityCombatDamageEvent{

	private static final HandlerList HANDLER_LIST = new HandlerList();

	public CombatDamageUsingEnderCrystalEvent(FluffyCombat fluffyCombat, CombatTag combatTag, @NotNull Player entity, @NotNull OfflinePlayer damager, @Nullable ItemStack itemStack, @NotNull EnderCrystal usedEntity) {
		super(fluffyCombat, combatTag, entity, damager, CombatCause.ENDER_CRYSTAL, itemStack, usedEntity);
	}

	public CombatDamageUsingEnderCrystalEvent(boolean isAsync, FluffyCombat fluffyCombat, CombatTag combatTag, @NotNull Player entity, @NotNull OfflinePlayer damager, @Nullable ItemStack itemStack, @NotNull EnderCrystal usedEntity) {
		super(isAsync, fluffyCombat, combatTag, entity, damager, CombatCause.ENDER_CRYSTAL, itemStack, usedEntity);
	}

	@Override
	public @NotNull EnderCrystal getUsedEntity() {
		return (EnderCrystal) super.getUsedEntity();
	}

	@Override
	public @NotNull HandlerList getHandlers() {
		return HANDLER_LIST;
	}
	public static HandlerList getHandlerList(){
		return HANDLER_LIST;
	}
}
