package me.antritus.astral.fluffycombat.configs;

import lombok.Getter;
import me.antritus.astral.fluffycombat.EnumUtils;
import me.antritus.astral.fluffycombat.FluffyCombat;
import me.antritus.astral.fluffycombat.NamedTextColorUtils;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.file.FileConfiguration;
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

	private boolean isCombatLogBroadcast;
	private CombatLogAction combatLogAction;
	private boolean isCombatLogRejoinBroadcast;
	private boolean isCombatLogRejoinPrivateMessage;
	private boolean isCombatLogDiscordEnabled;
	private String combatLogDiscordNPCSpawn;
	private String combatLogDiscordNPCDeath;
	private String combatLogDiscordNPCSurvive;
	private String combatLogDiscordKillMessage;
	private String combatLogDiscordNoneMessage;
	private boolean isCombatLogKillTotemBypass;
	private int combatLogKillTotemBypassAmount;
	private boolean isCombatLogKillKeepItem;
	private boolean isCombatLogKillKeepExp;
	private double combatLogKillKeepExpPercentage;

	private boolean isCombatLogNPCArmor;
	private boolean isCombatLogNPCAttackAI;
	private boolean isCombatLogNPCKnockback;
	private boolean isCombatLogNPCDamageCombatReset;
	private boolean isCombatLogNPCDeathBroadcast;
	private boolean isCombatLogNPCDeathUseMessage;
	private boolean isCombatLogNPCDeathKeepItems;
	private boolean isCombatLogNPCDeathKeepExperience;
	private double combatLogNPCDeathKeepExperiencePercentage;
	private boolean isCombatGlow;
	private boolean isCombatGlowLatest;
	private boolean isCombatGlowAllTagged;
	private boolean isCombatGlowCombatLogRejoin;
	private NamedTextColorUtils combatGlowLatest;
	private NamedTextColorUtils combatGlowAllTagged;
	private NamedTextColorUtils combatGlowTagRejoin;

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

	public void reload(@NotNull FileConfiguration configuration){
		isCustomCooldowns = configuration.getBoolean("cooldowns.enabled", true);
		isResetCooldownsOnDeath = configuration.getBoolean("cooldowns.reset-on-combat-end", true);


		combatLogAction = EnumUtils.valueOf(configuration.getString("combat-log.quit.action"), CombatLogAction.NOTHING);
		isCombatLogRejoinBroadcast = configuration.getBoolean("combat-log.join.broadcast");
		isCombatLogRejoinPrivateMessage = configuration.getBoolean("combat-log.join.player-message");

		isCombatLogBroadcast = configuration.getBoolean("combat-log.quit.broadcast");

		isCombatLogDiscordEnabled = configuration.getBoolean("combat-log.quit.discord-dms.enabled");
		combatLogDiscordNPCSpawn = configuration.getString("combat-log.quit.discord-dms.npc.spawn");
		combatLogDiscordNPCDeath = configuration.getString("combat-log.quit.discord-dms.npc.death");
		combatLogDiscordNPCSurvive = configuration.getString("combat-log.quit.discord-dms.npc.survive");
		combatLogDiscordKillMessage = configuration.getString("combat-log.quit.discord-dms.death");
		combatLogDiscordNoneMessage = configuration.getString("combat-log.quit.discord-dms.none");

		isCombatLogKillTotemBypass = configuration.getBoolean("combat-log.quit.kill.totem-bypass");
		combatLogKillTotemBypassAmount = configuration.getInt("combat-log.quit.kill.totems-to-bypass");
		isCombatLogKillKeepItem = configuration.getBoolean("combat-log.quit.kill.keep-items");
		isCombatLogKillKeepExp = configuration.getBoolean("combat-log.quit.kill.keep-experience");
		combatLogKillKeepExpPercentage = configuration.getDouble("combat-log.quit.kill.keep-experience-percentage");

		isCombatLogNPCArmor = configuration.getBoolean("combat-log.quit.npc.equip-armor");
		isCombatLogNPCAttackAI = configuration.getBoolean("combat-log.quit.npc.attack-others");
		isCombatLogNPCKnockback =  configuration.getBoolean("combat-log.quit.npc.knockback");
		isCombatLogNPCDamageCombatReset = configuration.getBoolean("combat-log.quit.npc.damage.restart-combat");
		isCombatLogNPCDeathBroadcast = configuration.getBoolean("combat-log.quit.npc.death.broadcast");
		isCombatLogNPCDeathUseMessage = configuration.getBoolean("combat-log.quit.npc.death.death-message");
		isCombatLogNPCDeathKeepItems = configuration.getBoolean("combat-log.quit.npc.death.keep-items");
		isCombatLogNPCDeathKeepExperience = configuration.getBoolean("combat-log.quit.npc.death.keep-experience");
		combatLogNPCDeathKeepExperiencePercentage = configuration.getDouble("combat-log.quit.npc.death.keep-experience-percentage");

		isCombatGlow = configuration.getBoolean("glowing.enabled");
		isCombatGlowLatest = configuration.getBoolean("glowing.latest.enabled");
		isCombatGlowAllTagged = configuration.getBoolean("glowing.regular.enabled");
		isCombatGlowCombatLogRejoin = configuration.getBoolean("glowing.combat-log.enabled");
		combatGlowLatest = NamedTextColorUtils.getByName(configuration.getString("glowing.latest.color"), NamedTextColorUtils.RED);
		combatGlowAllTagged = NamedTextColorUtils.getByName(configuration.getString("glowing.regular.color"), NamedTextColorUtils.GOLD);
		combatGlowTagRejoin = NamedTextColorUtils.getByName(configuration.getString("glowing.combat-log.color"), NamedTextColorUtils.BLUE);

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

	public enum CombatLogAction {
		KILL,
		NOTHING,
		SPAWN_NPC
	}
}
