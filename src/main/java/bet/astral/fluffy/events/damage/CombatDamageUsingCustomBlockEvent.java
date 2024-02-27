package bet.astral.fluffy.events.damage;

import bet.astral.fluffy.FluffyCombat;
import bet.astral.fluffy.api.CombatCause;
import bet.astral.fluffy.api.CombatTag;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public abstract class CombatDamageUsingCustomBlockEvent extends AbstractBlockCombatDamageEvent{
	private static final HandlerList HANDLER_LIST = new HandlerList();

	public CombatDamageUsingCustomBlockEvent(FluffyCombat fluffyCombat, CombatTag combatTag, Player entity, OfflinePlayer damager, CombatCause combatCause, BlockState blockState, Block block, ItemStack itemStack) {
		super(fluffyCombat, combatTag, entity, damager, combatCause, blockState, block, itemStack);
	}

	public CombatDamageUsingCustomBlockEvent(boolean isAsync, FluffyCombat fluffyCombat, CombatTag combatTag, Player entity, OfflinePlayer damager, CombatCause combatCause, BlockState blockState, Block block, ItemStack itemStack) {
		super(isAsync, fluffyCombat, combatTag, entity, damager, combatCause, blockState, block, itemStack);
	}

	@Override
	public @NotNull HandlerList getHandlers() {
		return HANDLER_LIST;
	}
	public static HandlerList getHandlerList(){
		return HANDLER_LIST;
	}
}
