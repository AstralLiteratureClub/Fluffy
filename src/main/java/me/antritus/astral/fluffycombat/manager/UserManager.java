package me.antritus.astral.fluffycombat.manager;

import me.antritus.astral.fluffycombat.FluffyCombat;
import me.antritus.astral.fluffycombat.api.CombatUser;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

public class UserManager {
	private final FluffyCombat fluffyCombat;
	private final Map<UUID, CombatUser> users = new LinkedHashMap<>();
	private final Constructor<?> constructorCombatUser;

	{
		try {
			constructorCombatUser = CombatUser.class.getDeclaredConstructor(FluffyCombat.class, UUID.class);
			constructorCombatUser.setAccessible(true);
		} catch (NoSuchMethodException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Creates new instance of user manager.
	 * @see FluffyCombat#
	 * @param fluffyCombat main class instance
	 */
	public UserManager(FluffyCombat fluffyCombat) {
		this.fluffyCombat = fluffyCombat;
	}


	/**
	 * Returns the main instance of the plugin
	 * @return main instance
	 */
	@NotNull
	public FluffyCombat getFluffyCombat() {
		return fluffyCombat;
	}

	/**
	 * Returns all users of the server,
	 * which are cached
	 * @return users map
	 */
	@NotNull
	public Map<UUID, CombatUser> getUsers() {
		return users;
	}

	/**
	 * Gets user from users map with given id
	 * @param uuid id
	 * @return combat user, null if not found
	 */
	@Nullable
	public CombatUser getUser(UUID uuid){
		return users.get(uuid);
	}

	/**
	 * Gets user from users map with given player's id
	 * @param player player
	 * @return combat user, null if not found
	 */
	@Nullable
	public CombatUser getUser(Player player){
		return users.get(player.getUniqueId());
	}

	/**
	 * Triggered when player joins the server
	 * @param player player
	 */
	public void onJoin(Player player) {
		if (users.get(player.getUniqueId()) == null){
			try {
				CombatUser user = (CombatUser) constructorCombatUser.newInstance(fluffyCombat, player.getUniqueId());
				users.put(player.getUniqueId(), user);
			} catch (InstantiationException | InvocationTargetException | IllegalAccessException e) {
				throw new RuntimeException(e);
			}
		}
	}

	/**
	 * Triggered when plugin starts
	 */
	public void onEnable() {
		Bukkit.getOnlinePlayers().forEach(this::onJoin);
	}

	/**
	 * Triggered when plugin disables
	 */
	public void onDisable() {
	}
}
