package bet.astral.fluffy.api.events;

import bet.astral.fluffy.api.CombatTag;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Projectile;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class BetterDeathEvent extends Event {
	@NotNull
	private final LivingEntity entity;
	@NotNull
	private final List<CombatTag> combatTags;
	@Nullable
	private final LivingEntity attacker;
	@Nullable
	private final Projectile projectile;
	@Nullable
	private final ItemStack weapon;
	@NotNull
	private final EntityDamageEvent.DamageCause cause;

	public BetterDeathEvent(@NotNull LivingEntity entity, @NotNull List<CombatTag> combatTags, @Nullable LivingEntity attacker, @Nullable Projectile projectile, @Nullable ItemStack weapon, EntityDamageEvent.@NotNull DamageCause cause) {
		this.entity = entity;
		this.combatTags = combatTags;
		this.attacker = attacker;
		this.projectile = projectile;
		this.weapon = weapon;
		this.cause = cause;
	}
	public BetterDeathEvent(boolean async, @NotNull LivingEntity entity, @NotNull List<CombatTag> combatTags, @Nullable LivingEntity attacker, @Nullable Projectile projectile, @Nullable ItemStack weapon, EntityDamageEvent.@NotNull DamageCause cause) {
		this.entity = entity;
		this.combatTags = combatTags;
		this.attacker = attacker;
		this.projectile = projectile;
		this.weapon = weapon;
		this.cause = cause;
	}

	public @NotNull LivingEntity getEntity() {
		return entity;
	}

	public @NotNull List<CombatTag> getCombatTags() {
		return combatTags;
	}

	public @Nullable LivingEntity getAttacker() {
		return attacker;
	}

	public @Nullable Projectile getProjectile() {
		return projectile;
	}

	public @Nullable ItemStack getWeapon() {
		return weapon;
	}

	public EntityDamageEvent.@NotNull DamageCause getCause() {
		return cause;
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

	public enum BetterDamageCause {
		PROJECTILE,
		PROJECTILE_WEAPON,
	}
}
