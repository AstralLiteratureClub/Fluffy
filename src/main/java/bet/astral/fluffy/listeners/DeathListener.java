package bet.astral.fluffy.listeners;

import bet.astral.fluffy.FluffyCombat;
import bet.astral.fluffy.api.BlockCombatUser;
import bet.astral.fluffy.api.CombatCause;
import bet.astral.fluffy.api.CombatTag;
import bet.astral.fluffy.api.CombatUser;
import bet.astral.fluffy.database.CombatLogDB;
import bet.astral.fluffy.events.player.PlayerNewHighStreakEvent;
import bet.astral.fluffy.manager.CombatManager;
import bet.astral.fluffy.messenger.DeathTranslations;
import bet.astral.fluffy.statistic.Account;
import bet.astral.fluffy.statistic.Statistic;
import bet.astral.fluffy.statistic.Statistics;
import bet.astral.messenger.v2.placeholder.collection.PlaceholderMap;
import bet.astral.messenger.v2.translation.TranslationKey;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.damage.DamageSource;
import org.bukkit.damage.DamageType;
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

import java.util.Objects;
import java.util.UUID;

import static org.bukkit.damage.DamageType.*;

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
		DamageSource damageSource = event.getDamageSource();


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
		Account attackerAcc = tag != null ? !isBlock ? isVictim ? tag.getAttacker().getStatisticsAccount() : tag.getVictim().getStatisticsAccount() : null : null;

		victimAcc.increment(Statistics.DEATHS_GLOBAL);
		victimAcc.reset(Statistics.STREAK_DEATHS);
		incrementStreak(player, victimAcc, Statistics.STREAK_DEATHS, Statistics.STREAK_DEATHS_HIGHEST);

		PlaceholderMap placeholders = new PlaceholderMap();

		TranslationKey deathMessage = null;
		if (tag != null) {
			if (attackerAcc != null) {
				attackerAcc.increment(Statistics.KILLS_GLOBAL);
				OfflinePlayer attackerPlayer = Bukkit.getOfflinePlayer(tag.getAttacker().getUniqueId());
				incrementStreak(attackerPlayer, attackerAcc, Statistics.STREAK_KILLS, Statistics.STREAK_KILLS_HIGHEST);
				attackerAcc.reset(Statistics.STREAK_DEATHS);
			}

			final CombatUser victimUser = isVictim ? tag.getVictim() : tag.getAttacker();
			final CombatUser attackerUser = !isVictim ? tag.getAttacker() : tag.getVictim();
			final CombatCause lastCombatCause = isVictim ? tag.getVictimCombatCause() : tag.getAttackerCombatCause();


			boolean touchedGroundBeforeDying = false;

			if (damageSource.getDamageType() == DamageType.MOB_ATTACK ||
					damageSource.getDamageType() == DamageType.MOB_ATTACK_NO_AGGRO ||
					damageSource.getDamageType() == DamageType.PLAYER_ATTACK) {
				// Empty block
			} else if (damageSource.getDamageType() == DamageType.OUTSIDE_BORDER) {
				// Empty block
			} else if (damageSource.getDamageType() == DamageType.FALLING_BLOCK ||
					damageSource.getDamageType() == DamageType.FALLING_ANVIL ||
					damageSource.getDamageType() == DamageType.FALLING_STALACTITE) {
				// TODO Could do some cool stuff with these
    /*
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
			} else if (damageSource.getDamageType() == DamageType.MOB_PROJECTILE ||
					damageSource.getDamageType() == DamageType.ARROW ||
					damageSource.getDamageType() == DamageType.TRIDENT ||
					damageSource.getDamageType() == DamageType.SPIT ||
					damageSource.getDamageType() == DamageType.FIREWORKS ||
					damageSource.getDamageType() == DamageType.FIREBALL ||
					damageSource.getDamageType() == DamageType.UNATTRIBUTED_FIREBALL ||
					damageSource.getDamageType() == DamageType.WITHER_SKULL ||
					damageSource.getDamageType() == DamageType.THROWN) {
				// Empty block
			} else if (damageSource.getDamageType() == DamageType.FALL) {
				deathMessage = DeathTranslations.DEATH_FELL;
				if (attackerAcc != null) {
					if (!touchedGroundBeforeDying) {
						deathMessage = DeathTranslations.DEATH_FELL_ATTACKER;
					} else {
						deathMessage = DeathTranslations.DEATH_FELL_ATTACKER_FELL_DOWN;
					}
				}
			} else if (damageSource.getDamageType() == DamageType.IN_WALL) {
				// Suffocate
			} else if (damageSource.getDamageType() == DamageType.IN_FIRE ||
					damageSource.getDamageType() == DamageType.CAMPFIRE ||
					damageSource.getDamageType() == DamageType.LIGHTNING_BOLT ||
					damageSource.getDamageType() == DamageType.LAVA) {
				// Empty block
			} else if (damageSource.getDamageType() == DamageType.CAMPFIRE) {
				// Empty block
			} else if (damageSource.getDamageType() == DamageType.DROWN) {
				// Isn't counted
			} else if (damageSource.getDamageType() == DamageType.EXPLOSION) {
				if (lastCombatCause == CombatCause.BED) {
					victimAcc.increment(Statistics.DEATHS_BED);
					deathMessage = DeathTranslations.DEATH_BED;
					if (attackerAcc != null) {
						attackerAcc.increment(Statistics.KILLS_BED);
						deathMessage = DeathTranslations.DEATH_BED_ATTACKER;
					}
				} else if (lastCombatCause == CombatCause.RESPAWN_ANCHOR) {
					victimAcc.increment(Statistics.DEATHS_ANCHOR);
					deathMessage = DeathTranslations.DEATH_ANCHOR;
					if (attackerAcc != null) {
						attackerAcc.increment(Statistics.KILLS_ANCHOR);
						deathMessage = DeathTranslations.DEATH_ANCHOR_ATTACKER;
					}
				} else if (lastCombatCause == CombatCause.TNT /*TODO , TNT_MINECART */) {
					victimAcc.increment(Statistics.DEATHS_TNT);
					deathMessage = DeathTranslations.DEATH_TNT;
					if (attackerAcc != null) {
						attackerAcc.increment(Statistics.KILLS_TNT);
						deathMessage = DeathTranslations.DEATH_TNT_ATTACKER;
						attackerAcc.increment(Statistics.STREAK_KILLS);
					}
				} else if (lastCombatCause == CombatCause.ENDER_CRYSTAL) {
					victimAcc.increment(Statistics.DEATHS_CRYSTAL);
					deathMessage = DeathTranslations.DEATH_CRYSTAL;
					if (attackerAcc != null) {
						attackerAcc.increment(Statistics.KILLS_CRYSTAL);
						deathMessage = DeathTranslations.DEATH_CRYSTAL_ATTACKER;
					}
				}
			} else if (damageSource.getDamageType() == DamageType.OUT_OF_WORLD) {
				deathMessage = DeathTranslations.DEATH_VOID;
				if (attackerAcc != null) {
					if (!touchedGroundBeforeDying) {
						deathMessage = DeathTranslations.DEATH_VOID_ATTACKER;
					} else {
						deathMessage = DeathTranslations.DEATH_VOID_ATTACKER_FELL_DOWN;
					}
				}
			} else if (damageSource.getDamageType() == DamageType.LIGHTNING_BOLT) {
				// Empty block
			} else if (damageSource.getDamageType() == DamageType.STARVE) {
				// Empty block
			} else if (damageSource.getDamageType() == DamageType.MAGIC) {
				if (damageSource.getCausingEntity() == null) {
					if (attackerAcc != null) {
						deathMessage = DeathTranslations.DEATH_POISON_ATTACKER;
						if (lastCombatCause == CombatCause.LINGERING_POTION) {
							deathMessage = DeathTranslations.DEATH_POISON_ATTACKER_LINGERING;
						} else if (lastCombatCause == CombatCause.SPLASH_POTION) {
							deathMessage = DeathTranslations.DEATH_POISON_ATTACKER_SPLASH;
						}
					}
				}
			} else if (damageSource.getDamageType() == DamageType.WITHER) {
				if (attackerAcc != null) {
					deathMessage = DeathTranslations.DEATH_WITHER_ATTACKER;
					if (lastCombatCause == CombatCause.LINGERING_POTION) {
						deathMessage = DeathTranslations.DEATH_WITHER_ATTACKER_LINGERING;
					} else if (lastCombatCause == CombatCause.SPLASH_POTION) {
						deathMessage = DeathTranslations.DEATH_WITHER_ATTACKER_SPLASH;
					}
				}
			} else if (damageSource.getDamageType() == DamageType.FALLING_BLOCK) {
				// Empty block
			} else if (damageSource.getDamageType() == DamageType.THORNS) {
				// Empty block
			} else if (damageSource.getDamageType() == DamageType.DRAGON_BREATH) {
				// Empty block
			} else if (damageSource.getDamageType() == DamageType.FLY_INTO_WALL) {
				// Empty block
			} else if (damageSource.getDamageType() == DamageType.HOT_FLOOR) {
				// Empty block
			} else if (damageSource.getDamageType() == DamageType.CRAMMING) {
				// Empty block
			} else if (damageSource.getDamageType() == DamageType.DRY_OUT) {
				// Empty block
			} else if (damageSource.getDamageType() == DamageType.FREEZE) {
				// Empty block
			} else if (damageSource.getDamageType() == DamageType.SONIC_BOOM) {
				// Empty block
			}


			if (fluffy.getNpcManager().isFluffyNPC(player)) {
				UUID owner = fluffy.getNpcManager().getUniqueId(player);
				if (owner == null) {
					return;
				}

				CombatLogDB combatLogDB = fluffy.getCombatLogDB();
				Objects.requireNonNull(combatLogDB.getLog(player.getUniqueId())).thenAccept((log) -> {
					if (log != null) {
						combatLogDB.save(owner);
						if (attackerAcc != null) {
							combatLogDB.update(owner, attackerAcc.getId());
						}
					}
				});


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
		if (fluffy.getNpcManager().isFluffyNPC(player)) {
			UUID owner = fluffy.getNpcManager().getUniqueId(player);
			if (owner == null) {
				return;
			}

			CombatLogDB combatLogDB = fluffy.getCombatLogDB();
			Objects.requireNonNull(combatLogDB.getLog(player.getUniqueId())).thenAccept((log) -> {
				if (log != null) {
					combatLogDB.save(owner);
					if (attackerAcc != null) {
						combatLogDB.update(owner, null);
					}
				}
			});

		}
	}

	public void incrementStreak(OfflinePlayer player, Account account, Statistic streak, Statistic highestStreak){
		account.increment(streak);
		if (account.getStatistic(streak) > account.getStatistic(highestStreak)) {
			account.set(streak, account.getStatistic(highestStreak));
			PlayerNewHighStreakEvent event = new PlayerNewHighStreakEvent(player, account, streak, highestStreak);
			event.callEvent();
		}
	}
}