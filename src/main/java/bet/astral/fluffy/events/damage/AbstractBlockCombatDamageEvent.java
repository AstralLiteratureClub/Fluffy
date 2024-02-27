package bet.astral.fluffy.events.damage;

import bet.astral.fluffy.FluffyCombat;
import bet.astral.fluffy.api.CombatCause;
import bet.astral.fluffy.api.CombatTag;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

public abstract class AbstractBlockCombatDamageEvent extends AbstractCombatDamageEvent{
	private final BlockState blockState;
	private final Block block;

	public AbstractBlockCombatDamageEvent(FluffyCombat fluffyCombat, CombatTag combatTag, Player entity, OfflinePlayer damager, CombatCause combatCause, BlockState blockState, Block block, ItemStack itemStack) {
		super(fluffyCombat, combatTag, entity, damager, combatCause, itemStack);
		this.blockState = blockState;
		this.block = block;
	}

	public AbstractBlockCombatDamageEvent(boolean isAsync, FluffyCombat fluffyCombat, CombatTag combatTag, Player entity, OfflinePlayer damager, CombatCause combatCause, BlockState blockState, Block block, ItemStack itemStack) {
		super(isAsync, fluffyCombat, combatTag, entity, damager, combatCause, itemStack);
		this.blockState = blockState;
		this.block = block;
	}

	@Nullable
	public BlockState getBlockState() {
		return blockState;
	}

	@Nullable
	public Block getBlock() {
		return block;
	}
}
