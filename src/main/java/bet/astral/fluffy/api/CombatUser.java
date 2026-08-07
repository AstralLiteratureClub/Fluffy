package bet.astral.fluffy.api;

import bet.astral.aura.api.color.VanillaGlowColor;
import bet.astral.fluffy.FluffyCombat;
import bet.astral.fluffy.api.setting.Setting;
import bet.astral.fluffy.statistic.Account;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.OfflinePlayer;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static lombok.AccessLevel.NONE;

/**
 * @author Antritus
 * @since 1.0-SNAPSHOT
 */
@Getter
@Setter
public class CombatUser {


	/*
	 * Cached accounts
	 */

	private Account statisticsAccount = null;
	@Setter(value = NONE)
	private Map<UUID, Setting<?>> settings = new LinkedHashMap<>();

	/*
	 * Combat helpers
	 */

	private UUID lastFireDamage = null;

	private BukkitTask taskFlightTimer;
	private int flightTimer;

	private BukkitTask taskTotemTimer;
	private BukkitTask taskRejoinTimer;

	/*
	 * Totem of Undying counting
	 */

	private int totemCounter;
	private int totemResetTimer;

	private boolean isOffline = false;
	private int rejoinTimer = -1;

	private final Map<String, Object> data = new LinkedHashMap<>();
	@Getter
	private UUID uniqueId;
	private FluffyCombat fluffyCombat;

	/*
	 * Glowing
	 */

	private boolean showGlowingLatest;
	private boolean showGlowingTagged;
	private boolean showGlowingTagReLogged;

	// Custom Glowing color support
	@Getter(NONE)
	private VanillaGlowColor latestGlowColor = null;
	@Getter(NONE)
	private VanillaGlowColor taggedGlowColor = null;
	@Getter(NONE)
	private VanillaGlowColor rejoinedGlowColor = null;

	/**
	 * Generates new user lol
	 * @param combat main instance
	 * @param uniqueId id
	 */
	public CombatUser(FluffyCombat combat, UUID uniqueId) {
		this.uniqueId = uniqueId;
		this.fluffyCombat = combat;
		if (uniqueId != null) {
			this.statisticsAccount = fluffyCombat.getStatisticManager().get(uniqueId);
			if (statisticsAccount == null){
				fluffyCombat.getStatisticsDatabase().load(uniqueId).thenAccept(a->statisticsAccount = a);
			}
		}
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

	public Optional<VanillaGlowColor> getLatestGlowColor() {
		return Optional.ofNullable(latestGlowColor);
	}

	public Optional<VanillaGlowColor> getTaggedGlowColor() {
		return Optional.ofNullable(taggedGlowColor);
	}

	public Optional<VanillaGlowColor> getRejoinedGlowColor() {
		return Optional.ofNullable(rejoinedGlowColor);
	}

	public CombatUser setShowGlowingLatest(boolean showGlowingLatest) {
		this.showGlowingLatest = showGlowingLatest;
		return this;
	}

	public CombatUser setShowGlowingTagged(boolean showGlowingTagged) {
		this.showGlowingTagged = showGlowingTagged;
		return this;
	}

	public CombatUser setShowGlowingTagReLogged(boolean showGlowingTagReLogged) {
		this.showGlowingTagReLogged = showGlowingTagReLogged;
		return this;
	}

	public <T> CombatUser setSetting(Setting<T> setting, T value) {
		if (this.settings.get(setting.getUniqueId()) == null) {
			this.settings.put(setting.getUniqueId(), setting.clone());
		}
		((Setting<T>) this.settings.get(setting.getUniqueId())).setValue(value);
		return this;
	}

	public <T> Setting<T> getSetting(Setting<T> setting) {
		if (settings.get(setting.getUniqueId()) == null) {
			setSetting(setting, setting.getValue());
		}
		return (Setting<T>) settings.get(setting.getUniqueId());
	}
}
