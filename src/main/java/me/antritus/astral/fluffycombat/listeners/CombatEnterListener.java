package me.antritus.astral.fluffycombat.listeners;

import me.antritus.astral.fluffycombat.FluffyCombat;
import me.antritus.astral.fluffycombat.antsfactions.MessageManager;
import me.antritus.astral.fluffycombat.api.BlockCombatUser;
import me.antritus.astral.fluffycombat.api.CombatTag;
import me.antritus.astral.fluffycombat.api.events.*;
import me.antritus.astral.fluffycombat.manager.BlockUserManager;
import me.antritus.astral.fluffycombat.manager.CombatManager;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;

import java.util.*;

import static org.bukkit.event.entity.EntityDamageEvent.DamageCause.*;

/**
 * @author Antritus
 * @since 1.0-SNAPSHOT
 */
@SuppressWarnings("removal")
public class CombatEnterListener implements Listener {
	private final FluffyCombat combat;

	public CombatEnterListener(FluffyCombat combat) {
		this.combat = combat;
	}

	public static boolean isAny(EntityDamageEvent.DamageCause damageCause, EntityDamageEvent.DamageCause... causes){
		for (EntityDamageEvent.DamageCause cause : causes) {
			if (cause == damageCause){
				return true;
			}
		}
		return false;
	}

	public static void handle(Player victim, OfflinePlayer attacker) {
		if (victim.getUniqueId() == attacker.getUniqueId() && !FluffyCombat.debug) {
			return;
		}
		FluffyCombat combat = FluffyCombat.getPlugin(FluffyCombat.class);
		CombatManager cM = combat.getCombatManager();
		MessageManager mm = combat.getMessageManager();
		if (!cM.hasTags(victim)) {
			mm.message(victim, "combat-enter.victim", "%attacker%=" + attacker.getName());
		}
		if (!cM.hasTags(attacker)) {
			if (attacker instanceof Player attackerPlayer)
				mm.message(attackerPlayer, "combat-enter.attacker", "%victim%=" + victim.getName());
		}

		CombatTag tag = cM.getTag(victim, attacker);
		if (tag == null) {
			tag = combat.getCombatManager().create(victim, attacker);
		}
		tag.resetTicks();
		CombatEnterEvent enterEvent = new CombatEnterEvent(combat, tag);
		enterEvent.callEvent();
	}

	public static void handle(Player victim, Block attacker) {
		FluffyCombat combat = FluffyCombat.getPlugin(FluffyCombat.class);
		BlockUserManager blockUserManager = combat.getBlockUserManager();

		if (blockUserManager.getUser(attacker.getLocation())==null ||
				!Objects.requireNonNull(blockUserManager.getUser(attacker.getLocation())).isAlive()){
			blockUserManager.create(attacker);
		}
		BlockCombatUser blockUser = blockUserManager.getUser(attacker.getLocation());


		CombatManager cM = combat.getCombatManager();
		MessageManager mm = combat.getMessageManager();
		if (!cM.hasTags(victim)) {
			mm.message(victim, "combat-enter.victim", "%attacker%=" + attacker.getType());
		}
		CombatTag tag = cM.getTag(victim, blockUser);
		if (tag == null) {
			tag = combat.getCombatManager().create(victim, blockUser);
		}
		tag.resetTicks();
		CombatEnterEvent enterEvent = new CombatEnterEvent(combat, tag);
		enterEvent.callEvent();
	}

	public static void handle(Player victim, BlockCombatUser attacker) {
		FluffyCombat combat = FluffyCombat.getPlugin(FluffyCombat.class);
		BlockUserManager blockUserManager = combat.getBlockUserManager();

		CombatManager cM = combat.getCombatManager();
		MessageManager mm = combat.getMessageManager();
		if (!cM.hasTags(victim)) {
			mm.message(victim, "combat-enter.victim", "%attacker%=" + attacker.getBlock().getType());
		}
		CombatTag tag = cM.getTag(victim, attacker);
		if (tag == null) {
			tag = combat.getCombatManager().create(victim, attacker);
		}
		tag.resetTicks();
		CombatEnterEvent enterEvent = new CombatEnterEvent(combat, tag);
		enterEvent.callEvent();
	}


	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onEntityDamage(EntityDamageByEntityEvent event) {

		if (!(event.getEntity() instanceof Player victim)) {
			return;
		}
		// Ignore so we can handle in custom potion effect check
		if (isAny(event.getCause(), MAGIC, WITHER, POISON)) {
			return;
		}
		OfflinePlayer attacker;
		if (!(event.getDamager() instanceof Player player)) {
			if (event.getDamager() instanceof ThrownPotion){
				return;
			}
			if (event.getDamager() instanceof Projectile projectile) {
				if (projectile.getShooter() != null && projectile.getShooter() instanceof Player player) {
					attacker = player;
				} else {
					return;
				}
			} else {
				return;
			}
		} else {
			attacker = player;
		}
		handle(victim, attacker);
	}

	// TODO Test ALL methods of getting knocked in to combat
	@EventHandler
	public void onTnTHit(EntityDamageEntityByTNTEvent event){
		if (!combat.getCombatConfig().isTNTDetection()){
			return;
		}
		if (!(event.getEntity() instanceof Player player)){
			return;
		}
		if (!(event.attacker() instanceof OfflinePlayer attacker)){
			return;
		}
		handle(player, attacker);
	}

	// TODO Fix anchor hit detection
	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
	public void onAnchorHit(EntityDamageEntityByRespawnAnchorEvent event) {
		if (!combat.getCombatConfig().isAnchorDetection()){
			return;
		}
		if (!(event.getEntity() instanceof Player victim)){
			return;
		}
		// those entities are players as mobs can't click on anchors
		Player attacker = (Player) event.getDamager();
		handle(victim, attacker);
	}

	// TODO Fix anchor hit detection
	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
	public void onBedHit(EntityDamageEntityByBedEvent event) {
		if (!combat.getCombatConfig().isBedDetection()){
			return;
		}
		if (!(event.getEntity() instanceof Player victim)){
			return;
		}
		// those entities are players as mobs can't click on anchors
		Player attacker = (Player) event.getDamager();
		handle(victim, attacker);
	}



	// TODO Fix anchor hit detection
	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
	public void onCrystalHit(EntityDamageEntityByEnderCrystalEvent event) {
		if (!combat.getCombatConfig().isCrystalDetection()){
			return;
		}
		if (!(event.getEntity() instanceof Player player)){
			return;
		}
		if (!(event.attacker() instanceof Player attacker)){
			return;
		}
		handle(player, attacker);
	}
}