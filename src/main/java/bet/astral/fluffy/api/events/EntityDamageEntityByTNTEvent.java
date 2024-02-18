package bet.astral.fluffy.api.events;

import net.minecraft.world.entity.item.PrimedTnt;
import org.bukkit.block.Block;
import org.bukkit.entity.EnderCrystal;
import org.bukkit.entity.Entity;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.entity.EntityEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class EntityDamageEntityByTNTEvent extends EntityEvent implements Cancellable {
	private final TNTPrimed tnt;
	private final Object attacker;
	private boolean cancel;

	public EntityDamageEntityByTNTEvent(@NotNull final Entity damagee, @Nullable final Object attacker, @NotNull final TNTPrimed tnt) {
		super(damagee);
		this.attacker = attacker;
		this.tnt = tnt;
	}

	@NotNull
	public TNTPrimed tnt() {
		return tnt;
	}

	@Nullable
	public Object attacker() {
		return attacker;
	}

	public boolean cancel() {
		return cancel;
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
