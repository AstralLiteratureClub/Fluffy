package bet.astral.fluffy.configs;

import bet.astral.fluffy.database.AbstractDatabase;
import lombok.Getter;
import bet.astral.fluffy.FluffyCombat;
import org.apache.commons.lang3.EnumUtils;
import org.bukkit.ChatColor;
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


	private CombatLogAction combatLogAction;

	private int combatLogRejoinTicks;

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

	private boolean isCombatLogNPCArmor;
	private boolean isCombatLogNPCAttackAI;
	private boolean isCombatLogNPCKnockback;
	private boolean isCombatLogNPCDamageCombatReset;
	private boolean isCombatLogNPCDeathKeepItems;
	private boolean isCombatLogNPCDeathKeepExperience;

	private boolean isCombatGlow;
	private boolean isCombatGlowLatest;
	private boolean isCombatGlowAllTagged;
	private boolean isCombatGlowCombatLogRejoin;
	private ChatColor combatGlowLatest;
	private ChatColor combatGlowAllTagged;
	private ChatColor combatGlowTagRejoin;

	private ElytraMode elytraMode;
	private boolean isElytraBoostAllowed;

	private boolean isTridentBoostAllowed;

	private boolean isArmorChangeAllowed;
	private boolean isArmorHotSwapAllowed;
	private boolean isArmorHotBarEquipAllowed;

	private boolean isAnchorDetection;
	private boolean isBedDetection;
	private boolean isCrystalDetection;
	private boolean isTNTDetection;
	private boolean isLingeringPotionDetection;
	private boolean isSplashPotionDetection;

	private FlightMode flightMode;
	private int flightTicks;

	private AbstractDatabase.DBType statisticDatabaseType;

	public CombatConfig(FluffyCombat fluffy){
		this.fluffy = fluffy;
		reload(fluffy.getConfig());
	}

	public void reload(@NotNull FileConfiguration configuration){
		isCustomCooldowns = configuration.getBoolean("cooldowns.enabled", true);
		isResetCooldownsOnDeath = configuration.getBoolean("cooldowns.reset-on-combat-end", true);


		combatLogAction = EnumUtils.getEnumIgnoreCase(CombatLogAction.class, configuration.getString("combat-log.quit.action"), CombatLogAction.NOTHING);

		combatLogRejoinTicks = configuration.getInt("combat-log.join.rejoin-ticks");

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

		isCombatLogNPCArmor = configuration.getBoolean("combat-log.quit.npc.equip-armor");
		isCombatLogNPCAttackAI = configuration.getBoolean("combat-log.quit.npc.attack-others");
		isCombatLogNPCKnockback =  configuration.getBoolean("combat-log.quit.npc.knockback");
		isCombatLogNPCDamageCombatReset = configuration.getBoolean("combat-log.quit.npc.damage.restart-combat");
		isCombatLogNPCDeathKeepItems = configuration.getBoolean("combat-log.quit.npc.death.keep-items");
		isCombatLogNPCDeathKeepExperience = configuration.getBoolean("combat-log.quit.npc.death.keep-experience");

		isCombatGlow = configuration.getBoolean("glowing.enabled");
		isCombatGlowLatest = configuration.getBoolean("glowing.latest.enabled");
		isCombatGlowAllTagged = configuration.getBoolean("glowing.regular.enabled");
		isCombatGlowCombatLogRejoin = configuration.getBoolean("glowing.combat-log.enabled");
		combatGlowLatest = EnumUtils.getEnumIgnoreCase(ChatColor.class, configuration.getString("glowing.latest.color"), ChatColor.RED);
		combatGlowAllTagged = EnumUtils.getEnumIgnoreCase(ChatColor.class, configuration.getString("glowing.regular.color"), ChatColor.GOLD);
		combatGlowTagRejoin = EnumUtils.getEnumIgnoreCase(ChatColor.class, configuration.getString("glowing.combat-log.color"), ChatColor.BLUE);

		isCommandsDisabled = configuration.getBoolean("commands.combat.enabled", true);
		commandsToDisable = configuration.getStringList("commands.combat.disabled-list");

		elytraMode = EnumUtils.getEnumIgnoreCase(ElytraMode.class, configuration.getString("elytra.allow-in-combat"), ElytraMode.DENY_CHESTPLATE);
		isElytraBoostAllowed = configuration.getBoolean("elytra.rocket-boost.allow-in-combat", true);

		isTridentBoostAllowed = configuration.getBoolean("trident.riptide.allow-in-combat", true);

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

		flightMode = EnumUtils.getEnumIgnoreCase(FlightMode.class, configuration.getString("flight.flight-mode"), FlightMode.DENY);
		flightTicks = configuration.getInt("flight.allow-flight-time", 0);

		isArmorChangeAllowed = configuration.getBoolean("armor-change.allow-armor-change", true);
		isArmorHotSwapAllowed = configuration.getBoolean("armor-change.allow-hotbar-swap", true);
		isArmorHotBarEquipAllowed = configuration.getBoolean("armor-change.allow-hotbar-equip", true);


		statisticDatabaseType = EnumUtils.getEnumIgnoreCase(AbstractDatabase.DBType.class, "database.statistic.type");
		if (statisticDatabaseType == null){
			throw new RuntimeException("Couldn't find a correct database type for database database.statistic.type");
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
	public enum FlightMode {
		DENY,
		ALLOW,
		ALLOW_TICKS
	}

	public enum ElytraMode {
		ALLOW,
		DENY_ELYTRA,
		DENY_CHESTPLATE
	}
}
