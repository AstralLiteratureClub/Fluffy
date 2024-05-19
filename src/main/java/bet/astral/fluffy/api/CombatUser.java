package bet.astral.fluffy.api;

import bet.astral.fluffy.FluffyCombat;
import bet.astral.shine.ShineColor;
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
	 * Glowing
	 */

	private boolean showGlowingLatest;
	private boolean showGlowingTagged;
	private boolean showGlowingTagReLogged;

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

	// Custom Glowing color support
	@Getter(NONE)
	private ShineColor latestGlowColor = null;
	@Getter(NONE)
	private ShineColor taggedGlowColor = null;
	@Getter(NONE)
	private ShineColor rejoinedGlowColor = null;

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

	public Optional<ShineColor> getLatestGlowColor() {
		return Optional.ofNullable(latestGlowColor);
	}

	public Optional<ShineColor> getTaggedGlowColor() {
		return Optional.ofNullable(taggedGlowColor);
	}

	public Optional<ShineColor> getRejoinedGlowColor() {
		return Optional.ofNullable(rejoinedGlowColor);
	}
}
