package bet.astral.fluffy.events.damage;

import bet.astral.fluffy.FluffyCombat;
import bet.astral.fluffy.api.CombatCause;
import bet.astral.fluffy.api.CombatTag;
import lombok.Getter;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Getter
public abstract class AbstractEntityCombatDamageEvent extends AbstractCombatDamageEvent{
	@NotNull
	private final Entity usedEntity;
	public AbstractEntityCombatDamageEvent(FluffyCombat fluffyCombat, CombatTag combatTag, @NotNull Player entity, @NotNull OfflinePlayer damager, @NotNull CombatCause combatCause, @Nullable ItemStack itemStack, @NotNull Entity usedEntity) {
		super(fluffyCombat, combatTag, entity, damager, combatCause, itemStack);
		this.usedEntity = usedEntity;
	}

	public AbstractEntityCombatDamageEvent(boolean isAsync, FluffyCombat fluffyCombat, CombatTag combatTag, @NotNull Player entity, @NotNull OfflinePlayer damager, @NotNull CombatCause combatCause, @Nullable ItemStack itemStack, @NotNull Entity usedEntity) {
		super(isAsync, fluffyCombat, combatTag, entity, damager, combatCause, itemStack);
		this.usedEntity = usedEntity;
	}
}
