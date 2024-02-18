package bet.astral.fluffy.death_messages;

import bet.astral.fluffy.Compatibility;
import bet.astral.fluffy.api.*;
import bet.astral.fluffy.hitdetection.AnchorDetection;
import bet.astral.messagemanager.Message;
import bet.astral.messagemanager.MessageManager;
import bet.astral.fluffy.messenger.Placeholders;
import bet.astral.messagemanager.placeholder.Placeholder;
import bet.astral.fluffy.FluffyCombat;
import me.antritus.astral.fluffycombat.api.*;
import bet.astral.fluffy.hitdetection.BedDetection;
import bet.astral.fluffy.manager.CombatManager;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.BlockState;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByBlockEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class ExplosionDeathMessageListener implements DeathListener {
	private final FluffyCombat fluffy;

	public ExplosionDeathMessageListener(FluffyCombat fluffy) {
		this.fluffy = fluffy;
	}

	@EventHandler
	public void onDeath(PlayerDeathEvent event){
		MessageManager<?, ?, ?> messageManager = fluffy.getMessageManager();
		Player player = event.getEntity();
		EntityDamageEvent entityDamageEvent = player.getLastDamageCause();
		if (entityDamageEvent == null){
			return;
		}
		Component message = null;
		if (entityDamageEvent.getCause() == EntityDamageEvent.DamageCause.BLOCK_EXPLOSION){
			CombatManager combatManager = fluffy.getCombatManager();
			CombatTag tag = combatManager.getLatest(player);
			if (tag == null){
				EntityDamageByBlockEvent blockDamageEvent = (EntityDamageByBlockEvent) entityDamageEvent;
				if (Compatibility.DAMAGER_BLOCK_STATE.isCompatible()){
					OfflinePlayer attacker = null;
					UUID owner;
					String messageKey;
					BlockState state = blockDamageEvent.getDamagerBlockState();
					if (state == null){
						Message msg = messageManager.getMessage("deaths.block_explosion.random.random");
						List<Placeholder> placeholders = defaults("victim", player);
						message = messageManager.parse(player, Objects.requireNonNull(msg), Message.Type.CHAT, placeholders.toArray(Placeholder[]::new));
					} else {
						Material material = state.getType();
						if (material.name().endsWith("_BED")) {
							String name = material.name().toLowerCase();
							BedDetection bedDetection = fluffy.getBedDetection();
							BedDetection.BedTag bedTag = bedDetection.detectionMap.get(state.getLocation().toBlockLocation());
							if (bedTag == null){
								messageKey = "deaths.block_explosion.random." + name;
							} else {
								owner = bedTag.owner().getUniqueId();
								messageKey = "deaths.block_explosion.combat." + name;
								attacker = fluffy.getServer().getOfflinePlayer(owner);
							}
						} else if (material == Material.RESPAWN_ANCHOR) {
							AnchorDetection anchorDetection = fluffy.getAnchorDetection();
							AnchorDetection.AnchorTag anchorTag = anchorDetection.detectionMap.get(state.getLocation().toBlockLocation());
							if (anchorTag == null){
								messageKey = "deaths.block_explosion.random.respawn_anchor";
							} else {
								messageKey = "deaths.block_explosion.combat.respawn_anchor";
								owner = anchorTag.owner().getUniqueId();
								attacker = fluffy.getServer().getOfflinePlayer(owner);
							}
						} else {
							owner = null;
							messageKey = "deaths.block_explosion.random.random";
						}
						Message msg = messageManager.getMessage(messageKey);
						if (msg==null){
							msg = messageManager.getMessage("deaths.block_explosion.random.random");
						}
						List<Placeholder> placeholders = defaults(player, attacker, new ItemStack(material));
						message = messageManager.parse(player, Objects.requireNonNull(msg), Message.Type.CHAT, placeholders.toArray(Placeholder[]::new));
					}
				} else {
					Message msg = messageManager.getMessage("deaths.block_explosion.random.random");
					List<Placeholder> placeholders = defaults("victim", player);
					message = messageManager.parse(player, Objects.requireNonNull(msg), Message.Type.CHAT, placeholders.toArray(Placeholder[]::new));
				}
			} else if (!(tag instanceof BlockCombatTag)) {
				String messageKey = null;

				boolean isVictim = tag.getVictim().getUniqueId().equals(player.getUniqueId());
				CombatUser userAttacker = isVictim ? tag.getAttacker() : tag.getVictim();
				CombatCause combatCause = isVictim ? tag.getVictimCombatCause() : tag.getAttackerCombatCause();
				OfflinePlayer attacker = fluffy.getServer().getOfflinePlayer(userAttacker.getUniqueId());
				ItemStack itemStack = isVictim ? tag.getAttackerWeapon() : tag.getVictimWeapon();
				if (itemStack == null){
					itemStack = new ItemStack(Material.AIR);
				}
				if (combatCause==CombatCause.BED){
					messageKey = "deaths.block_explosion.combat."+itemStack.getType().name().toLowerCase();
				} else if (combatCause==CombatCause.RESPAWN_ANCHOR) {
					messageKey = "deaths.block_explosion.combat.respawn_anchor";
				}
				List<Placeholder> placeholders = defaults(player, attacker, itemStack);
				Message msg = messageManager.getMessage(messageKey);
				if (msg == null){
					msg = messageManager.getMessage("deaths.block_explosion.combat.respawn_anchor");
				}
				message = messageManager.parse(player, Objects.requireNonNull(msg), Message.Type.CHAT, placeholders.toArray(Placeholder[]::new));
			}

			if (message == null){
				return;
			}
			event.deathMessage(message);
		} else if (entityDamageEvent.getCause() == EntityDamageEvent.DamageCause.ENTITY_EXPLOSION){
			String key;
			List<Placeholder> placeholders;
			placeholders = Placeholders.playerPlaceholders("victim", player);
			EntityDamageByEntityEvent entityAttackEvent = (EntityDamageByEntityEvent) entityDamageEvent;
			Entity attacker = entityAttackEvent.getDamager();
			CombatTag combatTag = fluffy.getCombatManager().getLatest(player);
			if (attacker instanceof Creeper creeper){
				Message msg = messageManager.getMessage("deaths.entity_explosion.random.creeper");
				placeholders.addAll(Placeholders.entityPlaceholders("attacker", creeper));
			} else if (combatTag != null){
				boolean isVictim = combatTag.getVictim().getUniqueId().equals(player.getUniqueId());
				CombatUser userAttacker = isVictim ? combatTag.getAttacker() : combatTag.getVictim();
				if (userAttacker instanceof BlockCombatUser user){
					return;
				}
				OfflinePlayer attackerPlayer = fluffy.getServer().getOfflinePlayer(userAttacker.getUniqueId());
				ItemStack itemStack = isVictim ? combatTag.getAttackerWeapon() : combatTag.getVictimWeapon();
				if (itemStack == null){
					itemStack = new ItemStack(Material.AIR);
				}
				placeholders.clear();
				placeholders = Placeholders.combatPlaceholders(player, combatTag.getAttacker().getPlayer(), CombatCause.ENDER_CRYSTAL, itemStack);
				Placeholder[] placeholderArray = placeholders.toArray(Placeholder[]::new);
				if (attacker instanceof EnderCrystal crystal){
					key = "deaths.entity_explosion.combat.ender_crystal";
				} else if (attacker instanceof TNTPrimed){
					key = "deaths.entity_explosion.combat.primed_tnt";
				} else if (attacker instanceof Firework firework) {
					key = "deaths.entity_explosion.combat.firework";
				} else {
					key = "deaths.entity_explosion.combat.random";
				}
			}
			else {
				if (attacker instanceof EnderCrystal crystal){
					key = "deaths.entity_explosion.random.ender_crystal";
				} else if (attacker instanceof TNTPrimed){
					key = "deaths.entity_explosion.random.primed_tnt";
				} else if (attacker instanceof Firework firework) {
					key = "deaths.entity_explosion.random.firework";
				} else {
					key = "deaths.entity_explosion.random.random";
				}
			}
		}
	}


}
