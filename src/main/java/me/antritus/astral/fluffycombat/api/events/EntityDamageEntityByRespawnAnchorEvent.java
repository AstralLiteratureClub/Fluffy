package me.antritus.astral.fluffycombat.api.events;


import lombok.Getter;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.entity.EntityEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class EntityDamageEntityByRespawnAnchorEvent extends EntityEvent implements Cancellable {
	@Getter
	private final OfflinePlayer damager;
	@Getter
	private final Block block;
	@Getter
	private final BlockState state;
	@Getter
	private final ItemStack item;
	private boolean isCanceled = false;

	public EntityDamageEntityByRespawnAnchorEvent(Player victim, OfflinePlayer attacker, Block block, BlockState state, ItemStack itemStack) {
		super(victim);
		this.damager = attacker;
		this.block = block;
		this.state = state;
		this.item = itemStack;
	}


	private static final HandlerList HANDLERS = new HandlerList();
	@NotNull
	public static HandlerList getHandlerList(){
		return HANDLERS;
	}
	@Override
	public @NotNull HandlerList getHandlers() {
		return HANDLERS;
	}

	@Override
	public boolean isCancelled() {
		return isCanceled;
	}

	@Override
	public void setCancelled(boolean cancel) {
		isCanceled = cancel;
	}
}
