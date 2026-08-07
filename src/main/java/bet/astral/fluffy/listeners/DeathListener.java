package bet.astral.fluffy.listeners;

import bet.astral.fluffy.FluffyCombat;
import bet.astral.fluffy.api.*;
import bet.astral.fluffy.database.ICombatLogDatabase;
import bet.astral.fluffy.events.damage.death.CombatDeathEvent;
import bet.astral.fluffy.manager.CombatManager;
import bet.astral.fluffy.statistic.*;
import bet.astral.messenger.v2.placeholder.collection.PlaceholderMap;
import bet.astral.messenger.v2.translation.TranslationKey;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityResurrectEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static bet.astral.fluffy.statistic.Statistic.incrementStreak;

public class DeathListener implements Listener {
    private final FluffyCombat fluffy;

    public DeathListener(@NotNull FluffyCombat fluffy) {
        this.fluffy = fluffy;
    }

    @EventHandler(ignoreCancelled = true)
    public void onEntityResurrect(@NotNull EntityResurrectEvent event) {
        if (event.getEntity() instanceof Player player) {
            Account account = fluffy.getStatisticManager().get(player);

            account.increment(Statistics.DEATHS_TOTEM);
            account.reset(Statistics.STREAK_KILLS_TOTEM);
            Statistic.incrementStreak(player, account, Statistics.STREAK_DEATHS_TOTEM, Statistics.STREAK_DEATHS_TOTEM_HIGHEST);
            if (!fluffy.getCombatManager().hasTags(player.getUniqueId())) {
                return;
            }
            CombatTag tag = fluffy.getCombatManager().getLatest(player);
            if (tag instanceof BlockCombatTag) {
                return;
            }
            if (tag == null) {
                return;
            }
            Account attackerAccount = tag.getOpposite(player).getStatisticsAccount();
            if (player.getUniqueId().equals(attackerAccount.getId())) {
                return;
            }
            Statistic.incrementStreak(tag.getOpposite(player).getPlayer(), attackerAccount, Statistics.STREAK_KILLS_TOTEM, Statistics.STREAK_KILLS_TOTEM_HIGHEST);
            attackerAccount.increment(Statistics.KILLS_TOTEM);
            attackerAccount.reset(Statistics.STREAK_DEATHS); // Not sure if this fits here
            attackerAccount.reset(Statistics.STREAK_DEATHS_TOTEM);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    private void onDeath(@NotNull PlayerDeathEvent event) {
        EntityDamageEvent entityDamageEvent = event.getEntity().getLastDamageCause();
        if (entityDamageEvent == null) {
            return;
        }
        fluffy.printDebug("Damage Event: " + entityDamageEvent.getEventName());
        if (fluffy.getNpcManager().isNPC(event.getPlayer())) {
            fluffy.printDebug("Ignoring combat death as victim is an npc...");
            return;
        }


        final Player player = event.getPlayer();
        final CombatManager combatManager = fluffy.getCombatManager();
        final CombatTag tag = combatManager.getLatest(player);

        final ItemStack weapon;
        boolean isVictim = false;
        boolean isBlock = false;
        Block lastBlockDamage = null;

        fluffy.printDebug("Done setting default variables.");
        if (tag != null) {
            fluffy.printDebug("Combat tag is not null");
            isVictim = tag.getVictim().getUniqueId().equals(player.getUniqueId());
            weapon = isVictim ? tag.getAttackerWeapon() : tag.getVictimWeapon();
            isBlock = isVictim ? tag.getAttacker() instanceof BlockCombatUser : tag.getVictim() instanceof BlockCombatUser;
            lastBlockDamage = isVictim ? tag.getLastVictimBlockDamage() : tag.getLastAttackerBlockDamage();

            // Call death event
            Entity damager = event.getDamageSource().getCausingEntity();
            CombatDeathEvent combatDeathEvent = new CombatDeathEvent(
                    fluffy,
                    tag,
                    event.getEntity(),
                    isVictim ? tag.getAttacker().getPlayer() : tag.getVictim().getPlayer(),
                    isVictim ? tag.getVictimCombatCause() : tag.getAttackerCombatCause(),
                    damager,
                    lastBlockDamage,
                    lastBlockDamage != null ? lastBlockDamage.getState() : null,
                    weapon
            );
            fluffy.printDebug("Calling combat death event");
            fluffy.getServer().getPluginManager().callEvent(combatDeathEvent);
        } else {
            weapon = null;
        }
        fluffy.printDebug("Updating victim account");
        Account victimAcc = fluffy.getStatisticManager().get(player);
        if (fluffy.getNpcManager().isNPC(player)) {
            fluffy.printDebug("Victim is an NPC");
            OfflinePlayer owner = fluffy.getNpcManager().getOwnerFromNPC(player);
            victimAcc = fluffy.getStatisticManager().get(owner.getUniqueId());
            if (victimAcc == null) {
                victimAcc = new PlaceholderAccount(fluffy, owner.getUniqueId());
                fluffy.getStatisticManager().load(owner);
            }
        }
        fluffy.printDebug("Starting to update victim account");
        incrementStreak(player, victimAcc, Statistics.STREAK_DEATHS, Statistics.STREAK_DEATHS_HIGHEST);
        victimAcc.increment(Statistics.DEATHS_GLOBAL);
        victimAcc.reset(StatisticType.KILL_STREAKS);
//		victimAcc.reset(Statistics.STREAK_COMBAT_LOGS);
        victimAcc.save();

        PlaceholderMap placeholders = new PlaceholderMap();

        TranslationKey deathMessage = null;


        if (tag != null) {
            fluffy.printDebug("Starting attacker account incrementation");
            Account attackerAcc = tag != null ? !isBlock ? isVictim ? tag.getAttacker().getStatisticsAccount() : tag.getVictim().getStatisticsAccount() : null : null;
            fluffy.printDebug("Attack account: " + (attackerAcc != null ? attackerAcc.getId() : "NULL"));
            fluffy.printDebug("Attack account class: " + attackerAcc);
            if (attackerAcc != null) {
                fluffy.printDebug("Checking...!");
                if (!FluffyCombat.debug && attackerAcc.getId().equals(player.getUniqueId())) {
                } else {
                    fluffy.printDebug("Trying to manage stats of the attacker!");
                    attackerAcc.increment(Statistics.KILLS_GLOBAL);
                    OfflinePlayer attackerPlayer = Bukkit.getOfflinePlayer(tag.getAttacker().getUniqueId());
                    incrementStreak(attackerPlayer, attackerAcc, Statistics.STREAK_KILLS, Statistics.STREAK_KILLS_HIGHEST);
                    attackerAcc.reset(StatisticType.DEATH_STREAKS);
                    attackerAcc.reset(Statistics.STREAK_DEATHS_TOTEM);
                    attackerAcc.reset(Statistics.STREAK_COMBAT_LOGS);
                    attackerAcc.save();
                }
            }

            final CombatUser victimUser = isVictim ? tag.getVictim() : tag.getAttacker();
            final CombatUser attackerUser = !isVictim ? tag.getAttacker() : tag.getVictim();
            final CombatCause lastCombatCause = isVictim ? tag.getVictimCombatCause() : tag.getAttackerCombatCause();


            fluffy.printDebug("Starting NPC management");
            if (fluffy.getNpcManager().isFluffyNPC(player)) {
                UUID owner = fluffy.getNpcManager().getUniqueId(player);
                if (owner != null) {
                    fluffy.printDebug("NPC found to have an owner");
                    ICombatLogDatabase combatLogDB = fluffy.getCombatLogDatabase();
                    Objects.requireNonNull(combatLogDB.getLog(player.getUniqueId())).thenAccept((log) -> {
                        if (log != null) {
                            combatLogDB.save(owner);
                            if (attackerAcc != null) {
                                fluffy.printDebug("Updated combat logs");
                                combatLogDB.update(owner, attackerAcc.getId());
                            }
                        }
                    });
                }
            }
        }

        if (victimAcc instanceof PlaceholderAccount placeholderAccount) {
            Account realAccount = fluffy.getStatisticManager().get(placeholderAccount.getId());
            if (realAccount == null) {
                Bukkit.getAsyncScheduler().runDelayed(fluffy, t -> placeholderAccount.apply(null), 20, TimeUnit.MILLISECONDS);
            }
            placeholderAccount.apply(realAccount);
            victimAcc.save();
        } else {
            victimAcc.save();
        }

        // Delete all the tags last to allow tags to work properly.

        // Cancel all combat tags of victim
        fluffy.printDebug("Start deletion of the combat tag!");
        if (combatManager.hasTags(player)) {
            fluffy.printDebug("Found combat tags");
            List<CombatTag> tags = combatManager.getTags(player);
            tags.forEach(cyclingTag -> {
                fluffy.printDebug("Deleting combat tag");
                // Set the tag ticks to -1 as it's instantly removed from the player
                cyclingTag.setAttackerTicksLeft(-1);
                cyclingTag.setVictimTicksLeft(-1);
                if (cyclingTag.getAttacker().getUniqueId().equals(player.getUniqueId())) {
                    cyclingTag.setDeadAttacker(true);
                } else {
                    cyclingTag.setDeadVictim(true);
                }
            });
        }
    }
}