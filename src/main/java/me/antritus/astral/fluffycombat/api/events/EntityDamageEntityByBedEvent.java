package me.antritus.astral.fluffycombat.api.events;

import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.entity.EntityEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class EntityDamageEntityByBedEvent extends EntityEvent implements Cancellable {
	private final OfflinePlayer attacker;
	private final Block block;
	private final BlockState state;
	private boolean isCanceled = false;

	public EntityDamageEntityByBedEvent(Player victim, OfflinePlayer attacker, Block block, BlockState state) {
		super(victim);
		this.attacker = attacker;
		this.block = block;
		this.state = state;
	}


	public @Nullable Block getBlock(){
		return block;
	}
	public @Nullable BlockState getState(){return state;}
	public @NotNull OfflinePlayer getDamager() {
		return attacker;
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
