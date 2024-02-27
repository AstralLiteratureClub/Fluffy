package bet.astral.fluffy.events.damage;

import bet.astral.fluffy.FluffyCombat;
import bet.astral.fluffy.api.CombatCause;
import bet.astral.fluffy.api.CombatTag;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class CombatDamageUsingRespawnAnchorEvent extends AbstractBlockCombatDamageEvent{
	private static final HandlerList HANDLER_LIST = new HandlerList();

	public CombatDamageUsingRespawnAnchorEvent(FluffyCombat fluffyCombat, CombatTag combatTag, Player entity, Player damager, BlockState blockState, Block block, ItemStack itemStack) {
		super(fluffyCombat, combatTag, entity, damager, CombatCause.RESPAWN_ANCHOR, blockState, block, itemStack);
	}

	public CombatDamageUsingRespawnAnchorEvent(boolean isAsync, FluffyCombat fluffyCombat, CombatTag combatTag, Player entity, Player damager, BlockState blockState, Block block, ItemStack itemStack) {
		super(isAsync, fluffyCombat, combatTag, entity, damager, CombatCause.RESPAWN_ANCHOR, blockState, block, itemStack);
	}

	@Override
	public @NotNull Player getDamager() {
		return (Player) super.getDamager();
	}

	@Override
	public @NotNull HandlerList getHandlers() {
		return HANDLER_LIST;
	}
	public static HandlerList getHandlerList(){
		return HANDLER_LIST;
	}
}
