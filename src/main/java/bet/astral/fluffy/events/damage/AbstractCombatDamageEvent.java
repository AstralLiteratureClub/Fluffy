package bet.astral.fluffy.events.damage;

import bet.astral.fluffy.FluffyCombat;
import bet.astral.fluffy.api.CombatCause;
import bet.astral.fluffy.api.CombatTag;
import bet.astral.fluffy.events.AbstractCombatEvent;
import lombok.Getter;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Getter
public abstract class AbstractCombatDamageEvent extends AbstractCombatEvent {
	@NotNull
	private final Player victim;
	@NotNull
	private final OfflinePlayer damager;
	@NotNull
	private final CombatCause combatCause;
	@Nullable
	private final ItemStack itemStack;
	public AbstractCombatDamageEvent(FluffyCombat fluffyCombat, CombatTag combatTag, @NotNull Player victim, @NotNull OfflinePlayer damager, @NotNull CombatCause combatCause, @Nullable ItemStack itemStack) {
		super(fluffyCombat, combatTag);
		this.victim = victim;
		this.damager = damager;
		this.combatCause = combatCause;
		this.itemStack = itemStack;
	}

	public AbstractCombatDamageEvent(boolean isAsync, FluffyCombat fluffyCombat, CombatTag combatTag, @NotNull Player victim, @NotNull OfflinePlayer damager, @NotNull CombatCause combatCause, @Nullable ItemStack itemStack) {
		super(isAsync, fluffyCombat, combatTag);
		this.victim = victim;
		this.damager = damager;
		this.combatCause = combatCause;
		this.itemStack = itemStack;
	}
}
