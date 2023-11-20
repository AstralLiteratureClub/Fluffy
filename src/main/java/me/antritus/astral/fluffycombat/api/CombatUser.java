package me.antritus.astral.fluffycombat.api;

import lombok.Getter;
import lombok.Setter;
import me.antritus.astral.fluffycombat.FluffyCombat;
import me.antritus.astral.fluffycombat.api.stats.StatisticsField;
import me.antritus.astral.fluffycombat.database.DatabaseField;
import net.citizensnpcs.api.npc.NPC;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

/**
 * @author Antritus
 * @since 1.0-SNAPSHOT
 */
@Getter
@Setter
public class CombatUser {
	@StatisticsField(category = "globals", fieldName = "total-kills")
	@DatabaseField private int totalKills;
	@StatisticsField(category = "globals", fieldName = "combat-kills")
	@DatabaseField private int combatKills;

	@StatisticsField(category = "globals", fieldName = "void-kills")
	@DatabaseField private int voidKills;
	@StatisticsField(category = "globals", fieldName = "fall-damage-kills")
	@DatabaseField private int fallDamageKills;

	@StatisticsField(category = "melee", fieldName = "melee-kills")
	@DatabaseField private int meleeKills;

	@StatisticsField(category = "explosions", fieldName = "respawn-anchor-kills")
	@DatabaseField private int anchorKills;
	@StatisticsField(category = "explosions", fieldName = "tnt-kills")
	@DatabaseField private int tntKills;
	@StatisticsField(category = "explosions", fieldName = "ender-crystal-kills")
	@DatabaseField private int crystalKills;
	@StatisticsField(category = "explosions", fieldName = "bed-kills")
	@DatabaseField private int bedKills;
	@StatisticsField(category = "explosions", fieldName = "firework-kills")
	@DatabaseField private int fireworkKills;

	@StatisticsField(category = "environment", fieldName = "fire-kills")
	@DatabaseField private int fireKills;
	@StatisticsField(category = "environment", fieldName = "lava-kills")
	@DatabaseField private int lavaKills;

	@StatisticsField(category = "globals", fieldName = "total-enchantment-kills")
	@DatabaseField private int enchantmentKills;

	@StatisticsField(category = "enchantments", fieldName = "thorns-kills")
	@DatabaseField private int thornsKills;
	@StatisticsField(category = "enchantments", fieldName = "sweeping-edge-kills")
	@DatabaseField private int sweepingEdgeKills;

	@StatisticsField(category = "globals", fieldName = "total-kills")
	@DatabaseField private int projectileKills;

	@StatisticsField(category = "magic", fieldName = "magic-kills")
	@DatabaseField private int potionKills;
	@StatisticsField(category = "magic", fieldName = "splash-potion-kills")
	@DatabaseField private int splashPotionKills;
	@StatisticsField(category = "magic", fieldName = "lingering-potion-kills")
	@DatabaseField private int lingeringPotionKills;

	@StatisticsField(category = "projectiles", fieldName = "arrow-kills")
	@DatabaseField private int arrowKills;
	@StatisticsField(category = "projectiles", fieldName = "tipped-arrow-kills")
	@DatabaseField private int tippedArrowKills;
	@StatisticsField(category = "projectiles", fieldName = "fireball-kills")
	@DatabaseField private int fireballKills;
	@StatisticsField(category = "projectiles", fieldName = "ender-pearl-kills")
	@DatabaseField private int enderPearlKills;
	@StatisticsField(category = "projectiles", fieldName = "snowball-kills")
	@DatabaseField private int snowballKills;
	@StatisticsField(category = "projectiles", fieldName = "egg-kills")
	@DatabaseField private int eggKills;
	@StatisticsField(category = "projectiles", fieldName = "wither-skull-kills")
	@DatabaseField private int witherSkullKills;
	@StatisticsField(category = "projectiles", fieldName = "thrown-trident-kills")
	@DatabaseField private int thrownTridentKills;
	@StatisticsField(category = "projectiles", fieldName = "shulker-bullet-kills")
	@DatabaseField private int shulkerBulletKills;

	@StatisticsField(category = "globals", fieldName = "killstreak")
	@DatabaseField private int killstreak;

	@StatisticsField(category = "globals", fieldName = "total-deaths")
	@DatabaseField private int totalDeaths;
	@StatisticsField(category = "globals", fieldName = "total-deaths")
	@DatabaseField private int combatDeaths;

	@StatisticsField(category = "globals", fieldName = "total-totems-used")
	@DatabaseField private int totemsUsed;
	@StatisticsField(category = "globals", fieldName = "total-totems-popped")
	@DatabaseField private int totemsPooped;

	@DatabaseField private boolean showGlowingLatest;
	@DatabaseField private boolean showGlowingTagged;
	@DatabaseField private boolean showGlowingTagReLogged;

	private boolean isOffline = false;
	private int rejoinTimer = -1;

	private NPC npc;

	private final Map<String, Object> data = new LinkedHashMap<>();
	private UUID uniqueId;
	private FluffyCombat fluffyCombat;

	/**
	 * Generates new user lol
	 * @param combat main instance
	 * @param uniqueId id
	 */
	public CombatUser(FluffyCombat combat, UUID uniqueId) {
		this.uniqueId = uniqueId;
		this.fluffyCombat = combat;
	}

	public CombatUser() {
		this.uniqueId = null;
		this.fluffyCombat = null;
	}

	/**
	 * Gets offline player using the uniqueId and getServer()#getOfflinePlayer(id)
	 * @see #getUniqueId()
	 * @see org.bukkit.Server#getOfflinePlayer(UUID)
	 * @return server
	 */
	public OfflinePlayer getPlayer(){
		return fluffyCombat.getServer().getOfflinePlayer(uniqueId);
	}

	/**
	 * Returns the uniqueId of the user.
	 * @return uniqueId
	 */
	public UUID getUniqueId() {
		return uniqueId;
	}

	/**
	 * Gets setting by its key
	 * The data in these Properties are not saved!
	 * @param key key
	 * @return data property if found
	 */
	public @Nullable Object get(@NotNull String key) {
		return data.get(key);
	}

	/**
	 * Returns data/setting of this user.
	 * This is not saved data
	 * @return data map
	 */
	public @NotNull Map<String, Object> get() {
		return data;
	}

	/**
	 * Sets value of the settings/data of this user.
	 * Do use this if you do not know what it does!
	 * @param key key
	 * @param value value
	 */
	public void setting(@NotNull String key, @Nullable Object value) {
		data.put(key, value);
	}
}
