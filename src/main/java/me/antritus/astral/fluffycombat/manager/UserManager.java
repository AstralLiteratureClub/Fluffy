package me.antritus.astral.fluffycombat.manager;

import me.antritus.astral.fluffycombat.FluffyCombat;
import me.antritus.astral.fluffycombat.api.CombatUser;
import me.antritus.astral.fluffycombat.database.StatisticDatabase;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class UserManager {
	private final FluffyCombat fluffyCombat;
	private final Map<UUID, CombatUser> users = new LinkedHashMap<>();

	/**
	 * Creates new instance of user manager.
	 * @see FluffyCombat#getUserManager()
	 * @param fluffyCombat fluffy
	 */
	public UserManager(FluffyCombat fluffyCombat) {
		this.fluffyCombat = fluffyCombat;
		fluffyCombat.getServer().getScheduler().runTaskTimerAsynchronously(fluffyCombat,
				(x) -> {
					List<UUID> removeList = new LinkedList<>();
					for (CombatUser user : users.values()) {
						// Might be null in testing. Database isn't working atm so this fixes it
						StatisticDatabase database = fluffyCombat.getStatisticDatabase();
						if (database != null) {
							fluffyCombat.getStatisticDatabase().updateDatabase(user);
						}
						if (!user.getPlayer().isOnline()) {
							removeList.add(user.getUniqueId());
						}
					}
					for (UUID uuid : removeList) {
						users.remove(uuid);
					}
				}, 20, 300);
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
			fluffyCombat.getServer().getAsyncScheduler().runNow(fluffyCombat, (x)->{
				StatisticDatabase database = fluffyCombat.getStatisticDatabase();
				// Might be null in testing. Database isn't working atm so this fixes it
				if (database == null){
					users.put(player.getUniqueId(), new CombatUser(fluffyCombat, player.getUniqueId()));
					return;
				}
				CombatUser user = database.loadFromDatabase(player.getUniqueId());
				users.put(player.getUniqueId(), user);
			});
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
