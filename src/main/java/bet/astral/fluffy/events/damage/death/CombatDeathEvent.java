package bet.astral.fluffy.events.damage.death;

import bet.astral.fluffy.FluffyCombat;
import bet.astral.fluffy.api.CombatCause;
import bet.astral.fluffy.api.CombatTag;
import bet.astral.fluffy.events.damage.AbstractCombatDamageEvent;
import lombok.Getter;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Getter
public class CombatDeathEvent extends AbstractCombatDamageEvent {
	private static final HandlerList HANDLER_LIST = new HandlerList();
	private final CombatCause combatCause;
	private final double victimDamageDealt;
	private final double attackerDamageDealt;
	private final Entity damagerEntity;
	private final Block damagerBlock;
	private final BlockState damagerBlockState;

	public CombatDeathEvent(FluffyCombat fluffyCombat, CombatTag combatTag, Player entity, OfflinePlayer damager, CombatCause combatCause, Entity damagerEntity, Block damagerBlock, BlockState damagerBlockState, ItemStack itemStack) {
		super(fluffyCombat, combatTag, entity, damager, combatCause, itemStack);
		this.combatCause = combatCause;
		this.damagerEntity = damagerEntity;
		this.damagerBlock = damagerBlock;
		this.damagerBlockState = damagerBlockState;
		this.victimDamageDealt = combatTag.getDamageDealt(entity);
		this.attackerDamageDealt = combatTag.getDamageDealt(damager);
	}

	@Override
	public @NotNull HandlerList getHandlers() {
		return HANDLER_LIST;
	}

	@NotNull
	public static HandlerList getHandlerList(){
		return HANDLER_LIST;
	}

	@NotNull
	public CombatCause getCombatCause() {
		return combatCause;
	}
	@Nullable
	public Block getDamagerBlock() {
		return damagerBlock;
	}

	@Nullable
	public BlockState getDamagerBlockState() {
		return damagerBlockState;
	}

	@Nullable
	public Entity getDamagerEntity() {
		return damagerEntity;
	}
}
