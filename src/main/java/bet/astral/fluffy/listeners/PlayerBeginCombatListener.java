package bet.astral.fluffy.listeners;

import bet.astral.fluffy.api.CombatCause;
import bet.astral.fluffy.api.events.*;
import bet.astral.fluffy.configs.CombatConfig;
import bet.astral.fluffy.manager.BlockUserManager;
import bet.astral.fluffy.messenger.Placeholders;
import bet.astral.messenger.Messenger;
import bet.astral.messenger.placeholder.Placeholder;
import bet.astral.fluffy.FluffyCombat;
import bet.astral.fluffy.api.BlockCombatUser;
import bet.astral.fluffy.api.CombatTag;
import bet.astral.fluffy.manager.CombatManager;
import fr.skytasul.glowingentities.GlowingBlocks;
import fr.skytasul.glowingentities.GlowingEntities;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.ThrownPotion;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.UUID;

import static org.bukkit.event.entity.EntityDamageEvent.DamageCause.*;

/**
 * @author Antritus
 * @since 1.0-SNAPSHOT
 */
@SuppressWarnings("removal")
public class PlayerBeginCombatListener implements Listener {
	private final FluffyCombat combat;

	public PlayerBeginCombatListener(FluffyCombat combat) {
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

	public static void handle(Player victim, OfflinePlayer attacker, CombatCause combatCause) {
		handle(victim, attacker, combatCause, null);
	}
	public static void handle(Player victim, OfflinePlayer attacker, CombatCause combatCause, ItemStack itemStack) {
		if (victim.getUniqueId() == attacker.getUniqueId() && !FluffyCombat.debug) {
			return;
		}
		FluffyCombat fluffy = FluffyCombat.getPlugin(FluffyCombat.class);
		CombatManager cM = fluffy.getCombatManager();
		Messenger<?> mm = fluffy.getMessageManager();
		if (!cM.hasTags(victim)) {
			Placeholder[] placeholders = Placeholders.combatPlaceholders(victim, attacker, combatCause, itemStack).toArray(Placeholder[]::new);
			mm.message(victim, "combat-enter.victim", placeholders);
		}
		if (!cM.hasTags(attacker)) {
			if (attacker instanceof Player attackerPlayer) {
				Placeholder[] placeholders = Placeholders.combatPlaceholders(victim, attackerPlayer, combatCause, itemStack).toArray(Placeholder[]::new);
				mm.message(attackerPlayer, "combat-enter.attacker", placeholders);
			}
		}

		CombatTag tag = cM.getTag(victim, attacker);
		if (tag == null) {
			tag = fluffy.getCombatManager().create(victim, attacker);
			CombatEnterEvent enterEvent = new CombatEnterEvent(fluffy, tag);
			enterEvent.callEvent();
		}
		if (victim.getUniqueId().equals(tag.getVictim().getUniqueId())) {
			tag.setVictimCombatCause(combatCause);
		} else {
			tag.setAttackerCombatCause(combatCause);
		}
		tag.resetTicks();
		if (attacker.getUniqueId() == tag.getAttacker().getUniqueId()) {
			tag.setAttackerWeapon(itemStack);
		} else {
			tag.setVictimWeapon(itemStack);
		}
		CombatConfig combatConfig = fluffy.getCombatConfig();
		if (combatConfig.isCombatGlow() && combatConfig.isCombatGlowLatest()) {
			GlowingEntities glowingEntities = fluffy.getGlowingEntities();
			try {
				if (attacker instanceof Player aPlayer) {
					glowingEntities.setGlowing(aPlayer, victim, combatConfig.getCombatGlowLatest());
					glowingEntities.setGlowing(victim, aPlayer, combatConfig.getCombatGlowLatest());
				}
			} catch (ReflectiveOperationException e) {
				throw new RuntimeException(e);
			}
		}
	}
	public static void handle(Player victim, Block attacker, CombatCause combatCause) {
		handle(victim, attacker, combatCause, null);
	}

	public static void handle(Player victim, Block attacker, CombatCause combatCause, @Nullable ItemStack itemStack) {
		FluffyCombat combat = FluffyCombat.getPlugin(FluffyCombat.class);
		BlockUserManager blockUserManager = combat.getBlockUserManager();

		if (blockUserManager.getUser(attacker.getLocation())==null ||
				!Objects.requireNonNull(blockUserManager.getUser(attacker.getLocation())).isAlive()){
			blockUserManager.create(attacker);
		}
		BlockCombatUser blockUser = blockUserManager.getUser(attacker.getLocation());
		handle(victim, blockUser, combatCause, itemStack);
	}
	public static void handle(Player victim, BlockCombatUser attacker, CombatCause combatCause) {
		handle(victim, attacker, combatCause, null);
	}

	public static void handle(Player victim, BlockCombatUser attacker, CombatCause combatCause, @Nullable ItemStack itemStack) {
		FluffyCombat fluffy = FluffyCombat.getPlugin(FluffyCombat.class);

		CombatManager cM = fluffy.getCombatManager();
		Messenger<?> mm = fluffy.getMessageManager();
		if (!cM.hasTags(victim)) {
			Placeholder[] placeholders = Placeholders.combatPlaceholders(victim, null, combatCause, itemStack).toArray(Placeholder[]::new);
			mm.message(victim, "combat-enter.victim", placeholders);
		}
		CombatTag tag = cM.getTag(victim, attacker);
		if (tag == null) {
			tag = fluffy.getCombatManager().create(victim, attacker);
		}
		tag.setVictimCombatCause(combatCause);
		tag.resetTicks();
		CombatEnterEvent enterEvent = new CombatEnterEvent(fluffy, tag);
		enterEvent.callEvent();

		CombatConfig combatConfig = fluffy.getCombatConfig();
		if (combatConfig.isCombatGlow() && combatConfig.isCombatGlowLatest()) {
			GlowingBlocks glowingBlocks = fluffy.getGlowingBlocks();
			try {
				glowingBlocks.setGlowing(attacker.getBlock(), victim, combatConfig.getCombatGlowLatest());
			} catch (ReflectiveOperationException e) {
				throw new RuntimeException(e);
			}
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onEntityBlock(EntityDamageEvent event) {
		if (!(event.getEntity() instanceof Player player)) {
			return;
		}
		player.sendRichMessage("Oh no help!!!");
		Block block = event.getEntity().getLocation().getBlock();
		player.sendRichMessage(block.getType().name());
		if (event.getCause() == FIRE) {
			player.sendRichMessage("Hello!");
			Material material = block.getType();
			if (material == Material.FIRE || material == Material.SOUL_FIRE) {
				@Nullable UUID owner = FluffyCombat.getBlockOwner(block);
				if (owner == null) {
					return;
				}
				player.sendRichMessage("Heyllo!");
				OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(owner);
				handle(player, offlinePlayer, CombatCause.FIRE);
			}
		} else if (event.getCause() == LAVA) {
			player.sendRichMessage("Hi!");
			@Nullable UUID owner = FluffyCombat.getBlockOwner(block);
			if (owner == null) {
				return;
			}
			player.sendRichMessage("Hiollo!");
			OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(owner);
			handle(player, offlinePlayer, CombatCause.FIRE);
		}
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
					handle(victim, player, CombatCause.PROJECTILE);
					return;
				} else {
					return;
				}
			} else {
				return;
			}
		} else {
			attacker = player;
		}
		handle(victim, attacker,  CombatCause.MELEE);
	}

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
		handle(player, attacker, CombatCause.TNT);
	}

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
		handle(victim, attacker, CombatCause.RESPAWN_ANCHOR, event.getItem());
	}

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
		handle(victim, attacker, CombatCause.BED, event.getItem());
	}



	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
	public void onCrystalHit(EntityDamageEntityByEnderCrystalEvent event) {
		if (!combat.getCombatConfig().isCrystalDetection()){
			return;
		}
		if (!(event.getEntity() instanceof Player player)){
			return;
		}
		if (!(event.getDamager() instanceof Player attacker)){
			return;
		}
		handle(player, attacker, CombatCause.ENDER_CRYSTAL, event.getItem());
	}
}