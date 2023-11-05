package me.antritus.astral.fluffycombat.configs;

import lombok.Getter;
import me.antritus.astral.fluffycombat.FluffyCombat;
import me.antritus.astral.fluffycombat.astrolminiapi.Configuration;
import org.bukkit.NamespacedKey;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedList;
import java.util.List;

@Getter
public class CombatConfig {
	private final FluffyCombat fluffy;

	private final List<PotionEffectType> potionsToBeginCombat = new LinkedList<>();
	private boolean isPotionStartCombat;
	private boolean isDispenserLingeringPotionCombat;
	private boolean isDispenserSplashPotionCombat;

	private List<String> commandsToDisable = new LinkedList<>();
	private boolean isCommandsDisabled;
	private boolean isCustomCooldowns;

	private boolean isResetCooldownsOnDeath;

	private boolean isCombatLog;
	private boolean isCombatLogKill;
	private boolean isCombatLogBroadcast;
	private boolean isCombatLogRejoinEnabled;
	private boolean isCombatLogRejoinBroadcast;
	private boolean isCombatLogRejoinPrivateMessage;


	private boolean isElytraAllowed;
	private boolean isElytraMessage;
	private boolean isElytraBoostAllowed;
	private boolean isElytraBoostMessage;

	private boolean isTridentBoostAllowed;
	private boolean isTridentBoostMessage;


	private boolean isAnchorDetection;
	private boolean isBedDetection;
	private boolean isCrystalDetection;
	private boolean isTNTDetection;
	private boolean isLingeringPotionDetection;
	private boolean isSplashPotionDetection;


	public CombatConfig(FluffyCombat fluffy){
		this.fluffy = fluffy;
		reload(fluffy.getConfig());
	}

	public void reload(@NotNull Configuration configuration){
		isCustomCooldowns = configuration.getBoolean("cooldowns.enabled", true);
		isResetCooldownsOnDeath = configuration.getBoolean("cooldowns.reset-on-combat-end", true);

		isCombatLog = configuration.getBoolean("combat-log.enabled", true);
		isCombatLogKill = configuration.getBoolean("combat-log.kill.enabled", true);
		isCombatLogBroadcast = configuration.getBoolean("combat-log.kill.broadcast");
		isCombatLogRejoinEnabled = configuration.getBoolean("combat-log.rejoin.enabled");
		isCombatLogRejoinBroadcast = configuration.getBoolean("combat-log.rejoin.broadcast");
		isCombatLogRejoinPrivateMessage = configuration.getBoolean("combat-log.rejoin.message");


		isCommandsDisabled = configuration.getBoolean("commands.combat.enabled", true);
		commandsToDisable = configuration.getStringList("commands.combat.disabled-list");

		isElytraAllowed = configuration.getBoolean("elytra.allow-in-combat", true);
		isElytraMessage = configuration.getBoolean("elytra.message", true);
		isElytraBoostAllowed = configuration.getBoolean("elytra.rocket-boost.allow-in-combat", true);
		isElytraBoostMessage = configuration.getBoolean("elytra.rocket-boost.message", true);

		isTridentBoostAllowed = configuration.getBoolean("trident.riptide.allow-in-combat", true);
		isTridentBoostMessage = configuration.getBoolean("trident.riptide.message", true);

		isAnchorDetection = configuration.getBoolean("anchors.begin-combat", true);
		isBedDetection = configuration.getBoolean("beds.begin-combat", true);
		isCrystalDetection = configuration.getBoolean("crystals.begin-combat", true);
		isTNTDetection = configuration.getBoolean("tnt.begin-combat", true);

		isPotionStartCombat = configuration.getBoolean("potions.begin-combat.enabled", true);
		isSplashPotionDetection = configuration.getBoolean("potions.begin-combat.splash", true);
		isLingeringPotionDetection = configuration.getBoolean("potions.begin-combat.lingering", true);
		isDispenserLingeringPotionCombat = configuration.getBoolean("potions.begin-combat.block-sources.dispenser.lingering", true);
		isDispenserSplashPotionCombat = configuration.getBoolean("potions.begin-combat.block-sources.dispenser.splash", true);
		if (!isSplashPotionDetection){
			isDispenserSplashPotionCombat = false;
		}
		if (!isLingeringPotionDetection){
			isDispenserLingeringPotionCombat = false;
		}
		potionsToBeginCombat.clear();
		for (String potionString : configuration.getStringList("potions.begin-combat.types")) {
			potionString = potionString.toLowerCase();
			NamespacedKey key;
			if (potionString.contains(":")){
				String[] split = potionString.split(":");
				if (split[0].contentEquals("minecraft")){
					key = NamespacedKey.minecraft(split[1]);
				} else {
					key = new NamespacedKey(split[0], split[1]);
				}
			} else {
				key = NamespacedKey.minecraft(potionString);
			}
			@Nullable
			PotionEffectType type = PotionEffectType.getByKey(key);
			if (type == null) {
				continue;
			}
			potionsToBeginCombat.add(type);
		}
	}

	public FluffyCombat fluffy() {
		return fluffy;
	}
}
