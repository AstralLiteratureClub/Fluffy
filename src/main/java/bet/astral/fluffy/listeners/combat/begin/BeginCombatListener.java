package bet.astral.fluffy.listeners.combat.begin;

import bet.astral.fluffy.FluffyCombat;
import bet.astral.fluffy.api.BlockCombatUser;
import bet.astral.fluffy.api.CombatCause;
import bet.astral.fluffy.api.CombatTag;
import bet.astral.fluffy.api.CombatUser;
import bet.astral.fluffy.events.*;
import bet.astral.fluffy.manager.BlockUserManager;
import bet.astral.fluffy.manager.CombatManager;
import bet.astral.fluffy.messenger.Placeholders;
import bet.astral.fluffy.messenger.Translations;
import bet.astral.messenger.v2.Messenger;
import bet.astral.messenger.v2.placeholder.Placeholder;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.projectiles.BlockProjectileSource;
import org.bukkit.projectiles.ProjectileSource;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import static bet.astral.fluffy.FluffyCombat.*;
import static org.bukkit.event.entity.EntityDamageEvent.DamageCause.*;
import static org.bukkit.persistence.PersistentDataType.BYTE_ARRAY;

/**
 * @author Antritus
 * @since 1.0-SNAPSHOT
 */
@SuppressWarnings("removal")
public class BeginCombatListener implements Listener {
	private final FluffyCombat combat;

	public BeginCombatListener(FluffyCombat combat) {
		this.combat = combat;
	}

	public static boolean isAny(EntityDamageEvent.DamageCause damageCause, EntityDamageEvent.DamageCause... causes) {
		for (EntityDamageEvent.DamageCause cause : causes) {
			if (cause == damageCause) {
				return true;
			}
		}
		return false;
	}

	public static void handle(Player victim, OfflinePlayer attacker, CombatCause combatCause) {
		handle(victim, attacker, combatCause, null);
	}

	public static void handle(Player victim, OfflinePlayer attacker, CombatCause combatCause, ItemStack itemStack) {
		handle(victim, attacker, combatCause, itemStack, false);
	}

	public static boolean cannotEnterCombat(Player player){
		FluffyCombat fluffyCombat = FluffyCombat.getPlugin(FluffyCombat.class);
		boolean can = !fluffyCombat.getRegionManager().canEnterCombat(player, player.getLocation());
		Bukkit.broadcast(Component.text(can));
		return can;
	}

	public static void handle(Player victim, OfflinePlayer attacker, CombatCause combatCause, ItemStack itemStack, boolean fireTicks) {
		Bukkit.broadcast(Component.text("Hello 3"));
		if (victim.getUniqueId() == attacker.getUniqueId() && !FluffyCombat.debug) {
			return;
		}
		Bukkit.broadcast(Component.text("Hello 2"));
		if (victim.isDead()){
			return;
		}

		Bukkit.broadcast(Component.text("Hello"));
		if (cannotEnterCombat(victim)){
			return;
		}

		FluffyCombat fluffy = FluffyCombat.getPlugin(FluffyCombat.class);
		CombatManager cM = fluffy.getCombatManager();
		Messenger mm = fluffy.getMessenger();
		List<Placeholder> placeholders = new LinkedList<>(Placeholders.combatPlaceholders(victim, attacker, combatCause, itemStack));
		if (!cM.hasTags(victim)) {
			mm.message(victim, Translations.COMBAT_ENTER_VICTIM, placeholders);
		}
		if (!cM.hasTags(attacker)) {
			if (attacker.isOnline()){
			mm.message(attacker, Translations.COMBAT_ENTER_ATTACKER, placeholders);
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
		if (combatCause == CombatCause.FIRE || combatCause == CombatCause.LAVA) {
			CombatUser user = tag.getUser(victim);
			user.setLastFireDamage(attacker.getUniqueId());
		} else if (itemStack != null && (itemStack.containsEnchantment(Enchantment.FIRE_ASPECT) && combatCause == CombatCause.MELEE
				|| itemStack.getType() == Material.BOW && itemStack.containsEnchantment(Enchantment.FLAME))) {
			CombatUser user = tag.getUser(victim);
			user.setLastFireDamage(attacker.getUniqueId());
		} else if (fireTicks) {
			CombatUser user = tag.getUser(victim);
			user.setLastFireDamage(attacker.getUniqueId());
		}
	}

	public static void handle(Player victim, Block attacker, CombatCause combatCause) {
		handle(victim, attacker, combatCause, null);
	}

	public static void handle(Player victim, Block attacker, CombatCause combatCause, @Nullable ItemStack itemStack) {
		if (victim.isDead()){
			return;
		}
		if (!cannotEnterCombat(victim)){
			return;
		}

		FluffyCombat combat = FluffyCombat.getPlugin(FluffyCombat.class);
		BlockUserManager blockUserManager = combat.getBlockUserManager();

		if (blockUserManager.getUser(attacker.getLocation()) == null ||
				!Objects.requireNonNull(blockUserManager.getUser(attacker.getLocation())).isAlive()) {
			blockUserManager.create(attacker);
		}
		BlockCombatUser blockUser = blockUserManager.getUser(attacker.getLocation());
		handle(victim, blockUser, combatCause, itemStack);
	}

	public static void handle(Player victim, BlockCombatUser attacker, CombatCause combatCause) {
		handle(victim, attacker, combatCause, null);
	}

	public static void handle(Player victim, BlockCombatUser attacker, CombatCause combatCause, @Nullable ItemStack itemStack) {
		if (victim.isDead()){
			return;
		}
		if (!cannotEnterCombat(victim)){
			return;
		}

		FluffyCombat fluffy = FluffyCombat.getPlugin(FluffyCombat.class);

		CombatManager cM = fluffy.getCombatManager();
		Messenger mm = fluffy.getMessenger();
		if (!cM.hasTags(victim)) {
			Placeholder[] placeholders = Placeholders.combatPlaceholders(victim, null, combatCause, itemStack).toArray(Placeholder[]::new);
			mm.message(victim, Translations.COMBAT_ENTER_VICTIM, placeholders);
		}
		CombatTag tag = cM.getTag(victim, attacker);
		if (tag == null) {
			tag = fluffy.getCombatManager().create(victim, attacker);
		}
		tag.setVictimCombatCause(combatCause);
		tag.resetTicks();
		CombatEnterEvent enterEvent = new CombatEnterEvent(fluffy, tag);
		enterEvent.callEvent();
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	private void onEntityBlock(EntityDamageEvent event) {
		if (!(event.getEntity() instanceof Player player)) {
			return;
		}
		if (event.getCause() == FIRE) {
			Block nearest = findNearestOwnedBlock(player, Material.FIRE, Material.SOUL_FIRE);
			if (nearest == null) {
				return;
			}
			UUID owner = FluffyCombat.getBlockOwner(nearest);
			if (owner == null) {
				return;
			}
			Material material = nearest.getType();
			if (material == Material.FIRE || material == Material.SOUL_FIRE) {
				OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(owner);
				handle(player, offlinePlayer, CombatCause.FIRE);
			}
		} else if (event.getCause() == LAVA) {
			Block block = FluffyCombat.findNearestOwnedBlock(player, Material.LAVA);
			if (block == null){
				return;
			}
			UUID owner = FluffyCombat.getBlockOwner(block);
			if (owner == null) {
				return;
			}

			OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(owner);
			handle(player, offlinePlayer, CombatCause.FIRE);
		}
	}


	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	private void onEntityDamage(EntityDamageByEntityEvent event) {

		if (!(event.getEntity() instanceof Player victim)) {
			return;
		}
		if (event.getCause() == FIRE_TICK) {
			CombatUser user = combat.getUserManager().getUser(victim);
			if (user.getLastFireDamage() != null) {
				if (!combat.getCombatManager().hasTags(victim)) {
					return;
				}
				handle(victim, Bukkit.getOfflinePlayer(user.getLastFireDamage()), CombatCause.FIRE);
				return;
			}
		}
		// Ignore so we can handle in custom potion effect check
		if (isAny(event.getCause(), MAGIC, WITHER, POISON)) {
			return;
		}
		OfflinePlayer attacker;
		if (!(event.getDamager() instanceof Player player)) {
			if (event.getDamager() instanceof ThrownPotion) {
				return;
			}
			if (event.getDamager() instanceof Projectile projectile) {
				if (projectile.getShooter() != null && projectile.getShooter() instanceof Player player) {
					byte[] bytes = projectile.getPersistentDataContainer().get(PROJECTILE_ITEM_KEY, BYTE_ARRAY);
					ItemStack itemStack = null;
					if (bytes != null) {
						itemStack = ItemStack.deserializeBytes(bytes);
					}

					if (projectile instanceof Arrow arrow) {
						handle(victim, player, CombatCause.PROJECTILE, itemStack, arrow.getFireTicks() > 0);
						return;
					}
					handle(victim, player, CombatCause.PROJECTILE, itemStack);
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
		handle(victim, attacker, CombatCause.MELEE);
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	private void onEntityDamage(ProjectileLaunchEvent event) {
		ProjectileSource source = event.getEntity().getShooter();
		Projectile projectile = event.getEntity();
		if (source instanceof BlockProjectileSource) {
			// todo Add support for projectiles (dispenser)
		} else if (source instanceof LivingEntity entity && entity.getEquipment() != null) {
			ItemStack itemStack = entity.getEquipment().getItemInMainHand();
			if (itemStack.isEmpty()){
				return;
			}
			projectile.getPersistentDataContainer().set(PROJECTILE_ITEM_KEY, BYTE_ARRAY, itemStack.serializeAsBytes());
 		}
	}
}