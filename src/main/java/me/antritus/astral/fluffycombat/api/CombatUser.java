package me.antritus.astral.fluffycombat.api;

import me.antritus.astral.fluffycombat.FluffyCombat;
import me.antritus.astral.fluffycombat.antsfactions.IUser;
import me.antritus.astral.fluffycombat.antsfactions.Property;
import me.antritus.astral.fluffycombat.antsfactions.SimpleProperty;
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
public class CombatUser implements IUser {
	private final Map<String, SimpleProperty<?>> data = new LinkedHashMap<>();
	private final UUID uniqueId;
	private final FluffyCombat fluffyCombat;

	/**
	 * Generates new user lol
	 * @param combat main instance
	 * @param uniqueId id
	 */
	private CombatUser(FluffyCombat combat, UUID uniqueId) {
		this.uniqueId = uniqueId;
		this.fluffyCombat = combat;
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
	@Override
	public @Nullable Property<String, ?> get(@NotNull String key) {
		return data.get(key);
	}

	/**
	 * Returns data/setting of this user.
	 * This is not saved data
	 * @return data map
	 */
	@Override
	public @NotNull Map<String, SimpleProperty<?>> get() {
		return data;
	}

	/**
	 * Sets value of the settings/data of this user.
	 * Do use this if you do not know what it does!
	 * @param key key
	 * @param value value
	 */
	@Override
	public void setting(@NotNull String key, @Nullable Object value) {
		data.putIfAbsent(key, new SimpleProperty<>(key, value));
		data.get(key).setValueObj(value);
	}
}
