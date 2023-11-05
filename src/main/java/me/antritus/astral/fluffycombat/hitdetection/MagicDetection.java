package me.antritus.astral.fluffycombat.hitdetection;

import me.antritus.astral.fluffycombat.FluffyCombat;
import me.antritus.astral.fluffycombat.api.BlockCombatUser;
import me.antritus.astral.fluffycombat.configs.CombatConfig;
import me.antritus.astral.fluffycombat.listeners.CombatEnterListener;
import me.antritus.astral.fluffycombat.manager.BlockUserManager;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.block.data.type.Dispenser;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PotionSplashEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.projectiles.BlockProjectileSource;
import org.bukkit.projectiles.ProjectileSource;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class MagicDetection implements Listener {
	private final FluffyCombat fluffy;
	protected final Map<PotionEffectType, Map<UUID, Object>> whoGave = new HashMap<>();

	public MagicDetection(FluffyCombat fluffyCombat) {
		this.fluffy = fluffyCombat;
	}

	public static void handleEffects(@Nullable LivingEntity whoPlaced, @NotNull Entity to, @NotNull List<@NotNull PotionEffectType> effectList){
		MagicDetection magicDetection = FluffyCombat.getPlugin(FluffyCombat.class).getMagicDetection();
		effectList.forEach(effect->{
			magicDetection.whoGave.putIfAbsent(effect, new HashMap<>());
			if (whoPlaced != null) {
				magicDetection.whoGave.get(effect).put(to.getUniqueId(), whoPlaced.getUniqueId());
			} else {
				magicDetection.whoGave.get(effect).put(to.getUniqueId(), null);
			}
		});
	}
	public static void handleEffects(int hashCode, @NotNull Entity to, @NotNull List<@NotNull PotionEffectType> effectList){
		MagicDetection magicDetection = FluffyCombat.getPlugin(FluffyCombat.class).getMagicDetection();
		effectList.forEach(effect->{
			magicDetection.whoGave.putIfAbsent(effect, new HashMap<>());
			magicDetection.whoGave.get(effect).put(to.getUniqueId(), hashCode);
		});
	}

	@EventHandler
	private void onPotionSplash(PotionSplashEvent event){
		CombatConfig combatConfig = fluffy.getCombatConfig();
		if (!combatConfig.isSplashPotionDetection()){
			return;
		}

		if (event.getAffectedEntities().size()==0){
			return;
		}

		ProjectileSource source = event.getPotion().getShooter();
		if (source instanceof BlockProjectileSource && !combatConfig.isDispenserSplashPotionCombat()){
			return;
		}
		boolean isCombatEnabling = false;
		List<PotionEffectType> types = new LinkedList<>();
		for (PotionEffect potionEffect : event.getPotion().getEffects()){
			types.add(potionEffect.getType());
			if (combatConfig.getPotionsToBeginCombat().stream().anyMatch(potion->potion.getKey().equals(potionEffect.getType().getKey()))){
				isCombatEnabling = true;
			}
		}
		if (!isCombatEnabling){
			return;
		}
		event.getAffectedEntities().stream().filter(entity->entity instanceof Player).forEach(entity->{
			Player player = (Player) entity;
			if (source instanceof BlockProjectileSource projectileSource){
				Block block = projectileSource.getBlock();
				CombatEnterListener.handle(player, block);
				BlockCombatUser blockCombatUser = fluffy.getBlockUserManager().getUser(block.getLocation());
				assert blockCombatUser != null;
				handleEffects(blockCombatUser.hashCode(), player, types);
			} else if (source instanceof Player attacker){
				CombatEnterListener.handle(player, attacker);
				handleEffects(attacker, player, types);
			}
		});
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	private void onMagicDamageMore(EntityDamageByEntityEvent event) {
		if (event.getDamager() instanceof AreaEffectCloud || event.getDamager() instanceof ThrownPotion) {
			return;
		}
		if (!(event.getEntity() instanceof Player victim)) {
			return;
		}
		PotionEffectType effectType;
		switch (event.getCause()) {
			case POISON -> effectType = PotionEffectType.POISON;
			case MAGIC -> effectType = PotionEffectType.HARM;
			case WITHER -> effectType = PotionEffectType.WITHER;
			default -> effectType = null;
		}
		if (effectType == null) {
			return;
		}
		CombatConfig combatConfig = fluffy.getCombatConfig();
		if (combatConfig.getPotionsToBeginCombat().stream().noneMatch(potion->potion.getKey().equals(effectType.getKey()))){
			return;
		}

		if (whoGave.get(PotionEffectType.HARM).get(event.getEntity().getUniqueId()) == null) {
			return;
		}
		Object whoGave = this.whoGave.get(effectType).get(event.getEntity().getUniqueId());
		if (whoGave instanceof Integer hash) {
			BlockUserManager blockUserManager = fluffy.getBlockUserManager();
			BlockCombatUser blockUser = blockUserManager.getUser(hash);
			if (blockUser == null) {
				return;
			}
			if (!blockUser.isAlive()) {
				return;
			}
			CombatEnterListener.handle(victim, blockUser);
		} else if (whoGave instanceof UUID uuid) {
			OfflinePlayer player = fluffy.getServer().getOfflinePlayer(uuid);
			CombatEnterListener.handle(victim, player);
		}
	}


	@EventHandler(priority = EventPriority.NORMAL)
	private void onMagicDamage(EntityDamageByEntityEvent event){
		if (!(event.getEntity() instanceof Player player)){
			return;
		}
		CombatConfig combatConfig = fluffy.getCombatConfig();
		if (event.getDamager() instanceof AreaEffectCloud areaEffectCloud){
			if (!combatConfig.isLingeringPotionDetection()){
				return;
			}
			ProjectileSource source = areaEffectCloud.getSource();
			if (source == null){
				return;
			}
			boolean isCombatEnabling = false;
			List<PotionEffectType> types = new LinkedList<>();
			for (PotionEffect potionEffect : areaEffectCloud.getCustomEffects()){
				types.add(potionEffect.getType());
				if (combatConfig.getPotionsToBeginCombat().stream().anyMatch(potion->potion.getKey().equals(potionEffect.getType().getKey()))){
					isCombatEnabling = true;
				}
			}
			player.sendMessage("4");
			if (!isCombatEnabling){
				return;
			}

			player.sendMessage("5");
			if (source instanceof BlockProjectileSource blockProjectileSource){
				Block block = blockProjectileSource.getBlock();
				if (block.getBlockData() instanceof Dispenser){
					player.sendMessage("6");
					if (!combatConfig.isDispenserLingeringPotionCombat()){
						return;
					}
					player.sendMessage("7");
					CombatEnterListener.handle(player, block); // Creates combat user for the block
					BlockCombatUser blockCombatUser = fluffy.getBlockUserManager().getUser(block.getLocation());
					assert blockCombatUser != null;

					handleEffects(blockCombatUser.hashCode(), player, types);
					player.sendMessage("8");
					return;
				} else {
					return;
				}
			}
			player.sendMessage("9");
			LivingEntity livingEntity = (LivingEntity) source;
			handleEffects(livingEntity, player, types);
			if (source instanceof Player attacker){
				player.sendMessage("10");
				if (event.getEntity() instanceof Player victim) {
					player.sendMessage("11");
					CombatEnterListener.handle(victim, attacker);
					return;
				}
			}

		}else if (event.getDamager() instanceof ThrownPotion potion){
			if (!combatConfig.isSplashPotionDetection()){
				return;
			}
			ProjectileSource source = potion.getShooter();
			List<PotionEffectType> types = new LinkedList<>();
			boolean isCombatEnabling = false;
			for (PotionEffect potionEffect : potion.getPotionMeta().getCustomEffects()){
				types.add(potionEffect.getType());
				if (combatConfig.getPotionsToBeginCombat().stream().anyMatch(potionType->potionType.getKey().equals(potionEffect.getType().getKey()))){
					isCombatEnabling = true;
				}
			}
			if (!isCombatEnabling){
				return;
			}
			if (source instanceof BlockProjectileSource projectileSource){
				Block block = projectileSource.getBlock();
				if (block.getBlockData() instanceof Dispenser){
					if (combatConfig.isDispenserSplashPotionCombat()){
						return;
					}
					CombatEnterListener.handle(player, block); // Creates combat user for the block
					BlockCombatUser blockCombatUser = fluffy.getBlockUserManager().getUser(block.getLocation());
					assert blockCombatUser != null;

					handleEffects(blockCombatUser.hashCode(), player, types);
				}
				return;
			}
			LivingEntity livingEntity = (LivingEntity) source;
			handleEffects(livingEntity, player, types);
			if (source instanceof Player attacker){
				if (event.getEntity() instanceof Player victim) {
					CombatEnterListener.handle(victim, attacker);
					return;
				}
			}
		}
	}
}