package bet.astral.fluffy.listeners.hitdetection;

import bet.astral.fluffy.FluffyCombat;
import bet.astral.fluffy.api.BlockCombatUser;
import bet.astral.fluffy.api.CombatCause;
import bet.astral.fluffy.configs.CombatConfig;
import bet.astral.fluffy.listeners.combat.begin.BeginCombatListener;
import bet.astral.fluffy.manager.BlockUserManager;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.block.data.type.Dispenser;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.AreaEffectCloudApplyEvent;
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

	private static void handleEffectCloud(AreaEffectCloud areaEffectCloud, LivingEntity entity) {
		Player player = (Player) entity;
		CombatConfig combatConfig = FluffyCombat.getPlugin(FluffyCombat.class).getCombatConfig();
		if (!combatConfig.isLingeringPotionDetection()) {
			return;
		}
		ProjectileSource source = areaEffectCloud.getSource();
		if (source == null) {
			return;
		}

		List<PotionEffectType> types = effects(areaEffectCloud);
		List<PotionEffectType> combat = combatConfig.getPotionsToBeginCombat();;
		boolean isCombatEnabling = isCombat(types, combat);
		if (!isCombatEnabling) {
			return;
		}
		if (source instanceof BlockProjectileSource blockProjectileSource) {
			Block block = blockProjectileSource.getBlock();
			if (block.getType() == Material.DISPENSER) {
				if (!combatConfig.isDispenserLingeringPotionCombat()) {
					return;
				}
				BeginCombatListener.handle(player, block, CombatCause.LINGERING_POTION); // Creates combat user for the block
				BlockCombatUser blockCombatUser = FluffyCombat.getPlugin(FluffyCombat.class).getBlockUserManager().getUser(block.getLocation());
				assert blockCombatUser != null;
				handleEffects(blockCombatUser.hashCode(), player, types);
			}
			return;
		}
		LivingEntity livingEntity = (LivingEntity) source;
		handleEffects(livingEntity, player, types);
		if (source instanceof Player attacker) {
			BeginCombatListener.handle(player, attacker, CombatCause.LINGERING_POTION);
		}
	}
	private static boolean isCombat(List<PotionEffectType> types, List<PotionEffectType> combatPotions){
		for (PotionEffectType type : types) {
			for (PotionEffectType other : combatPotions){
				if (other.getName().equals(type.getName())){
					return true;
				}
			}
		}
		return false;
	}
	private static List<PotionEffectType> effects(ThrownPotion potion) {
		return combine(potion.getPotionMeta().getBasePotionType().getPotionEffects(),
				potion.getPotionMeta().getCustomEffects());
	}
	private static List<PotionEffectType> effects(AreaEffectCloud potion) {
		potion.getBasePotionType().getPotionEffects();
		return combine(potion.getBasePotionType().getPotionEffects(),
				potion.getCustomEffects());
	}
	private static List<PotionEffectType> combine(@NotNull List<@NotNull PotionEffect> effects, @NotNull List<@NotNull PotionEffect> effects2){
		List<PotionEffectType> types = new LinkedList<>();
		for (@NotNull PotionEffect type : effects) {
			types.add(type.getType());
		}
		for (@NotNull PotionEffect type : effects2) {
			if (types.contains(type.getType())){
				continue;
			}
			types.add(type.getType());
		}
		return types;
	}
	private static void handleSplash(ThrownPotion potion, LivingEntity entity) {
		Player player = (Player) entity;
		CombatConfig combatConfig = FluffyCombat.getPlugin(FluffyCombat.class).getCombatConfig();
		ProjectileSource source = potion.getShooter();
		if (source instanceof BlockProjectileSource && !combatConfig.isDispenserSplashPotionCombat()) {
			return;
		}
		List<PotionEffectType> types = effects(potion);
		boolean isCombatEnabling = isCombat(types, FluffyCombat.getPlugin(FluffyCombat.class).getCombatConfig().getPotionsToBeginCombat());
		if (!isCombatEnabling) {
			return;
		}
		if (source instanceof BlockProjectileSource projectileSource) {
			Block block = projectileSource.getBlock();
			BeginCombatListener.handle(player, block, CombatCause.SPLASH_POTION);
			BlockCombatUser blockCombatUser = FluffyCombat.getPlugin(FluffyCombat.class).getBlockUserManager().getUser(block.getLocation());
			assert blockCombatUser != null;
			handleEffects(blockCombatUser.hashCode(), player, types);
		} else if (source instanceof Player attacker) {
			BeginCombatListener.handle(player, attacker, CombatCause.SPLASH_POTION);
			handleEffects(attacker, player, types);
		}
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
		for (LivingEntity livingEntity : event.getAffectedEntities().stream().filter(entity -> entity instanceof Player player && player.getGameMode() != GameMode.CREATIVE && player.getGameMode() != GameMode.SPECTATOR ).toList()) {
			handleSplash(event.getPotion(), livingEntity);
		}
	}

	@EventHandler
	private void onAreaEffect(AreaEffectCloudApplyEvent event){
		AreaEffectCloud cloud = event.getEntity();
		for (LivingEntity livingEntity : event.getAffectedEntities().stream().filter(entity -> entity instanceof Player player && player.getGameMode() != GameMode.CREATIVE && player.getGameMode() != GameMode.SPECTATOR).toList()) {
			handleEffectCloud(cloud, livingEntity);
		}
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
			case MAGIC -> effectType = PotionEffectType.INSTANT_DAMAGE;
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

		if (whoGave.get(PotionEffectType.INSTANT_DAMAGE).get(event.getEntity().getUniqueId()) == null) {
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
			BeginCombatListener.handle(victim, blockUser, CombatCause.EFFECT_STATUS);
		} else if (whoGave instanceof UUID uuid) {
			OfflinePlayer player = fluffy.getServer().getOfflinePlayer(uuid);
			BeginCombatListener.handle(victim, player, CombatCause.EFFECT_STATUS);
		}
	}

	private void onMagicDamage(EntityDamageByEntityEvent event){
		if (!(event.getEntity() instanceof Player player)){
			return;
		}
		CombatConfig combatConfig = fluffy.getCombatConfig();
		if (event.getDamager() instanceof AreaEffectCloud areaEffectCloud){
			handleEffectCloud(areaEffectCloud, player);

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
					BeginCombatListener.handle(player, block, CombatCause.SPLASH_POTION); // Creates combat user for the block
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
					BeginCombatListener.handle(victim, attacker, CombatCause.SPLASH_POTION);
					return;
				}
			}
		}
	}
}