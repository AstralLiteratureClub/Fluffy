package bet.astral.fluffy.listeners;

import bet.astral.fluffy.FluffyCombat;
import bet.astral.fluffy.api.BlockCombatUser;
import bet.astral.fluffy.api.CombatTag;
import bet.astral.fluffy.manager.CombatManager;
import bet.astral.fluffy.statistic.Account;
import bet.astral.fluffy.statistic.Statistics;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class DeathListener implements Listener {
	private final FluffyCombat fluffy;

	public DeathListener(@NotNull FluffyCombat fluffy) {
		this.fluffy = fluffy;
	}

	@EventHandler(priority = EventPriority.LOWEST)
	private void onDeath(PlayerDeathEvent event) {
		EntityDamageEvent entityDamageEvent = event.getEntity().getLastDamageCause();
		if (entityDamageEvent == null) {
			return;
		}
		final Player player = event.getPlayer();
		Entity attacker = (entityDamageEvent instanceof EntityDamageByEntityEvent entityDamageByEntityEven ? entityDamageByEntityEven.getDamager() : null);
		final EntityDamageEvent.DamageCause cause = entityDamageEvent.getCause();
		final CombatManager combatManager = fluffy.getCombatManager();
		final CombatTag tag = combatManager.getLatest(player);
		final ItemStack weapon;
		boolean isVictim = false;
		boolean isBlock = false;
		if (tag != null) {
			isVictim = tag.getVictim().getUniqueId().equals(player.getUniqueId());
			weapon = isVictim ? tag.getAttackerWeapon() : tag.getVictimWeapon();
			isBlock = isVictim ? tag.getAttacker() instanceof BlockCombatUser : tag.getVictim() instanceof BlockCombatUser;
		} else {
			weapon = null;
		}

		Account victimAcc = fluffy.getStatisticManager().get(player);
		Account attackerAcc = tag != null ? !isBlock ? fluffy.getStatisticManager().get(isVictim ? tag.getAttacker().getUniqueId() : tag.getVictim().getUniqueId()) : null : null;
		victimAcc.increment(Statistics.DEATHS_GLOBAL);
		if (attackerAcc != null){
			attackerAcc.increment(Statistics.KILLS_GLOBAL);
		}

		String messageKey;
		switch (cause) {
			case KILL -> {
				// NONE
			}
			case WORLD_BORDER -> {
				// NONE
			}
			case CONTACT -> {
				/*
				//
				if (tag != null) {
					if (isVictim && tag.getVictimCombatCause() == CombatCause.FALLING_BLOCK) {
					} else if (!isVictim && tag.getAttackerCombatCause() == CombatCause.FALLING_BLOCK) {
					} else if (isVictim && tag.getVictimCombatCause() == CombatCause.CACTUS) {
					} else if (!isVictim && tag.getAttackerCombatCause() == CombatCause.CACTUS) {
					} else if (isVictim && tag.getVictimCombatCause() == CombatCause.DRIPSTONE) {
					} else if (!isVictim && tag.getAttackerCombatCause() == CombatCause.DRIPSTONE) {
					}
				}
				 */
			}
			case ENTITY_ATTACK -> {
				// Handled before (giving 1 to global kills)
			}
			case ENTITY_SWEEP_ATTACK -> {
				// Handled before
			}
			case PROJECTILE -> {
				// Isn't counted
			}
			case SUFFOCATION -> {
				// Isn't counted
			}
			case FALL -> {
				// Isn't counted
			}
			case FIRE -> {
				// Isn't counted
			}
			case FIRE_TICK -> {
				// Isn't counted
			}
			case MELTING -> {
				// Isn't counted
			}
			case LAVA -> {
				// Isn't counted
			}
			case DROWNING -> {
				// Isn't counted
			}
			case BLOCK_EXPLOSION -> {

			}
			case ENTITY_EXPLOSION -> {

			}
			case VOID -> {

			}
			case LIGHTNING -> {

			}
			case SUICIDE -> {

			}
			case STARVATION -> {

			}
			case POISON -> {

			}
			case MAGIC -> {

			}
			case WITHER -> {

			}
			case FALLING_BLOCK -> {

			}
			case THORNS -> {

			}
			case DRAGON_BREATH -> {

			}
			case CUSTOM -> {

			}
			case FLY_INTO_WALL -> {

			}
			case HOT_FLOOR -> {

			}
			case CRAMMING -> {

			}
			case DRYOUT -> {

			}
			case FREEZE -> {

			}
			case SONIC_BOOM -> {

			}
		}
	}
}