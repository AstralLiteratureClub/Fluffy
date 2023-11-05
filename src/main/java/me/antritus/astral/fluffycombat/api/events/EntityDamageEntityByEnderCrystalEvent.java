package me.antritus.astral.fluffycombat.api.events;

import org.bukkit.entity.EnderCrystal;
import org.bukkit.entity.Entity;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.entity.EntityEvent;
import org.jetbrains.annotations.NotNull;

public class EntityDamageEntityByEnderCrystalEvent extends EntityEvent implements Cancellable {
	private final EnderCrystal enderCrystal;
	private final Entity attacker;
	private final double damage;
	private boolean cancel;

	public EntityDamageEntityByEnderCrystalEvent(@NotNull final Entity damagee, @NotNull final Entity attacker, @NotNull final EnderCrystal enderCrystal, final double damage) {
		super(damagee);
		this.enderCrystal = enderCrystal;
		this.attacker = attacker;
		this.damage = damage;
	}

	public EnderCrystal enderCrystal() {
		return enderCrystal;
	}

	public Entity attacker() {
		return attacker;
	}

	public double damage() {
		return damage;
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
		return cancel;
	}

	@Override
	public void setCancelled(boolean cancel) {
		this.cancel = cancel;
	}
}
