package bet.astral.fluffy.listeners;

import bet.astral.fluffy.FluffyCombat;
import bet.astral.fluffy.api.BlockCombatUser;
import bet.astral.fluffy.api.CombatCause;
import bet.astral.fluffy.api.CombatTag;
import bet.astral.fluffy.api.CombatUser;
import bet.astral.fluffy.manager.CombatManager;
import bet.astral.fluffy.messenger.DeathTranslations;
import bet.astral.fluffy.statistic.Account;
import bet.astral.fluffy.statistic.Statistics;
import bet.astral.messenger.v2.placeholder.collection.PlaceholderMap;
import bet.astral.messenger.v2.translation.TranslationKey;
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

		PlaceholderMap placeholders = new PlaceholderMap();

		TranslationKey deathMessage = null;
		if (tag != null) {
			if (attackerAcc != null) {
				attackerAcc.increment(Statistics.KILLS_GLOBAL);
			}

			final CombatUser victimUser = isVictim ? tag.getVictim() : tag.getAttacker();
			final CombatUser attackerUser = !isVictim ? tag.getAttacker() : tag.getVictim();
			final CombatCause lastCombatCause = isVictim ? tag.getVictimCombatCause() : tag.getAttackerCombatCause();

			boolean pushedDown = false;

			switch (cause) {
				case KILL -> {
				}
				case WORLD_BORDER -> {
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
					deathMessage = DeathTranslations.DEATH_VOID;
				}
				case FIRE -> {
					// Isn't counted
				}
				case FIRE_TICK -> {
					// Isn't counted
				}
				case LAVA -> {
					// Isn't counted
				}
				case MELTING -> {
					// Isn't counted
				}
				case CAMPFIRE -> {

				}
				case DROWNING -> {
					// Isn't counted
				}
				case BLOCK_EXPLOSION -> {
					switch (lastCombatCause) {
						case BED -> {
							victimAcc.increment(Statistics.DEATHS_BED);
							deathMessage = DeathTranslations.DEATH_BED;
							if (attackerAcc != null) {
								attackerAcc.increment(Statistics.KILLS_BED);
								deathMessage = DeathTranslations.DEATH_BED_ATTACKER;
							}
						}
						case RESPAWN_ANCHOR -> {
							victimAcc.increment(Statistics.DEATHS_ANCHOR);
							deathMessage = DeathTranslations.DEATH_ANCHOR;
							if (attackerAcc != null) {
								attackerAcc.increment(Statistics.KILLS_ANCHOR);
								deathMessage = DeathTranslations.DEATH_ANCHOR_ATTACKER;
							}
						}
						case TNT -> {
							// Not sure
						}
					}
				}
				case ENTITY_EXPLOSION -> {
					switch (lastCombatCause) {
						case TNT -> {
							victimAcc.increment(Statistics.DEATHS_TNT);
							deathMessage = DeathTranslations.DEATH_TNT;
							if (attackerAcc != null) {
								attackerAcc.increment(Statistics.KILLS_TNT);
								deathMessage = DeathTranslations.DEATH_TNT_ATTACKER;
							}
						}
						case ENDER_CRYSTAL -> {
							victimAcc.increment(Statistics.DEATHS_CRYSTAL);
							deathMessage = DeathTranslations.DEATH_CRYSTAL;
							if (attackerAcc != null) {
								attackerAcc.increment(Statistics.KILLS_CRYSTAL);
								deathMessage = DeathTranslations.DEATH_CRYSTAL_ATTACKER;
							}
						}
					}
				}
				case VOID -> {
					deathMessage = DeathTranslations.DEATH_VOID;
					if (attackerAcc != null) {
						if (pushedDown) {
							deathMessage = DeathTranslations.DEATH_VOID_ATTACKER;
						} else {
							deathMessage = DeathTranslations.DEATH_VOID_ATTACKER_FELL_DOWN;
						}
					}
				}
				case LIGHTNING -> {

				}
				case SUICIDE -> {

				}
				case STARVATION -> {

				}
				case POISON -> {
					if (attackerAcc != null) {
						deathMessage = DeathTranslations.DEATH_POISON_ATTACKER;
						switch (lastCombatCause) {
							case LINGERING_POTION -> {
								deathMessage = DeathTranslations.DEATH_POISON_ATTACKER_LINGERING;
							}
							case SPLASH_POTION -> {
								deathMessage = DeathTranslations.DEATH_POISON_ATTACKER_SPLASH;
							}
						}
					}
				}
				case MAGIC -> {
					if (attackerAcc != null) {
						deathMessage = DeathTranslations.DEATH_INSTANT_DAMAGE_ATTACKER;
						switch (lastCombatCause) {
							case LINGERING_POTION -> {
								deathMessage = DeathTranslations.DEATH_INSTANT_DAMAGE_ATTACKER_LINGERING;
							}
							case SPLASH_POTION -> {
								deathMessage = DeathTranslations.DEATH_INSTANT_DAMAGE_ATTACKER_SPLASH;
							}
						}
					}
				}
				case WITHER -> {
					if (attackerAcc != null) {
						deathMessage = DeathTranslations.DEATH_WITHER_ATTACKER;
						switch (lastCombatCause) {
							case LINGERING_POTION -> {
								deathMessage = DeathTranslations.DEATH_WITHER_ATTACKER_LINGERING;
							}
							case SPLASH_POTION -> {
								deathMessage = DeathTranslations.DEATH_WITHER_ATTACKER_SPLASH;
							}
						}
					}
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
		} else {
			switch (cause) {
				case KILL -> {
				}
				case WORLD_BORDER -> {
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
					deathMessage = DeathTranslations.DEATH_VOID;
				}
				case FIRE -> {
					// Isn't counted
				}
				case FIRE_TICK -> {
					// Isn't counted
				}
				case LAVA -> {
					// Isn't counted
				}
				case MELTING -> {
					// Isn't counted
				}
				case CAMPFIRE -> {

				}
				case DROWNING -> {
					// Isn't counted
				}
				case BLOCK_EXPLOSION -> {

				}
				case ENTITY_EXPLOSION -> {
				}
				case VOID -> {
					deathMessage = DeathTranslations.DEATH_VOID;

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
}