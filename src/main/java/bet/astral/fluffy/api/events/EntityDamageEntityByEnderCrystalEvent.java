package bet.astral.fluffy.api.events;

import lombok.Getter;
import org.bukkit.entity.EnderCrystal;
import org.bukkit.entity.Entity;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.entity.EntityEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class EntityDamageEntityByEnderCrystalEvent extends EntityEvent implements Cancellable {
	@Getter
	@NotNull
	private final EnderCrystal enderCrystal;
	@NotNull
	@Getter
	private final Entity damager;
	@Getter
	private final double damage;
	@Nullable
	@Getter
	private final ItemStack item;
	private boolean cancel;

	public EntityDamageEntityByEnderCrystalEvent(@NotNull final Entity damagee, @NotNull final Entity attacker, @NotNull final EnderCrystal enderCrystal, final double damage, ItemStack itemStack) {
		super(damagee);
		this.enderCrystal = enderCrystal;
		this.damager = attacker;
		this.damage = damage;
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
		return cancel;
	}

	@Override
	public void setCancelled(boolean cancel) {
		this.cancel = cancel;
	}
}
