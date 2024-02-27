package bet.astral.fluffy.events.damage;

import bet.astral.fluffy.FluffyCombat;
import bet.astral.fluffy.api.CombatCause;
import bet.astral.fluffy.api.CombatTag;
import org.bukkit.block.Bed;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CombatDamageUsingBedEvent extends AbstractBlockCombatDamageEvent{
	private static final HandlerList HANDLER_LIST = new HandlerList();

	public CombatDamageUsingBedEvent(FluffyCombat fluffyCombat, CombatTag combatTag, Player entity, Player damager, Bed bed, Block block, ItemStack itemStack) {
		super(fluffyCombat, combatTag, entity, damager, CombatCause.BED, bed, block, itemStack);
	}

	public CombatDamageUsingBedEvent(boolean isAsync, FluffyCombat fluffyCombat, CombatTag combatTag, Player entity, Player damager, Bed bed, Block block, ItemStack itemStack) {
		super(isAsync, fluffyCombat, combatTag, entity, damager, CombatCause.BED, bed, block, itemStack);
	}

	@Override
	public @Nullable Bed getBlockState() {
		return (Bed) super.getBlockState();
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
