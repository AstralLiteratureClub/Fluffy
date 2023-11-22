package me.antritus.astral.fluffycombat.api.events;

import lombok.Getter;
import net.citizensnpcs.api.npc.NPC;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class EntityDamageEntityByBedEvent extends Event implements Cancellable {
	@Getter
	@NotNull
	private final OfflinePlayer entity;
	@Getter
	@NotNull
	private final OfflinePlayer damager;
	@Getter
	@Nullable
	private final NPC npc;
	@Getter
	private final boolean isNPCVictim;
	@Getter
	@Nullable
	private final Block block;
	@Getter
	@NotNull
	private final BlockState state;
	@Getter
	@Nullable
	private final ItemStack item;
	private boolean isCanceled = false;

	public EntityDamageEntityByBedEvent(@NotNull Player victim, @NotNull OfflinePlayer attacker, @Nullable Block block, @NotNull  BlockState state, @Nullable ItemStack itemStack) {
		this.entity = victim;
		this.npc = null;
		this.damager = attacker;
		this.block = block;
		this.state = state;
		this.item = itemStack;
		isNPCVictim = false;
	}

	public EntityDamageEntityByBedEvent(@NotNull NPC victim, @NotNull OfflinePlayer player, @NotNull OfflinePlayer attacker, @Nullable Block block, @NotNull BlockState state, @Nullable ItemStack itemStack) {
		this.entity = player;
		this.npc = victim;
		this.damager = attacker;
		this.block = block;
		this.state = state;
		this.item = itemStack;
		isNPCVictim = true;
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
