package me.antritus.astral.fluffycombat.manager;

import me.antritus.astral.fluffycombat.FluffyCombat;
import me.antritus.astral.fluffycombat.api.BlockCombatUser;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class BlockUserManager {
	private final FluffyCombat fluffyCombat;
	private final Map<Location, BlockCombatUser> users = new LinkedHashMap<>();
	private final Map<Integer, BlockCombatUser> usersByHash = new HashMap<>();
	private final Constructor<BlockCombatUser> constructorCombatUser;

	{
		try {
			constructorCombatUser = BlockCombatUser.class.getDeclaredConstructor(FluffyCombat.class, Block.class);
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
	public BlockUserManager(FluffyCombat fluffyCombat) {
		this.fluffyCombat = fluffyCombat;

		fluffyCombat.getServer().getAsyncScheduler().runAtFixedRate(fluffyCombat,
				(x) -> {

					if (users.size() == 0) {
						return;
					}
					List<Location> deleteLocations = new LinkedList<>();
					List<Integer> deleteHash = new LinkedList<>();
					users.forEach(((location, blockCombatUser) -> {
						if (!blockCombatUser.isAlive()) {
							deleteHash.add(blockCombatUser.hashCode());
							deleteLocations.add(location);
						}
					}));
					deleteHash.forEach(usersByHash::remove);
					deleteLocations.forEach(users::remove);
				},
				1,
				1,
				TimeUnit.SECONDS);
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
	public Map<Location, BlockCombatUser> getUsers() {
		return users;
	}

	/**
	 * Gets user from users map with given id
	 * @param location location
	 * @return combat user, null if not found
	 */
	@Nullable
	public BlockCombatUser getUser(Location location) {
		location = location.toBlockLocation();
		return users.get(location);
	}

	@Nullable
	public BlockCombatUser getUser(int hash){
		return usersByHash.get(hash);
	}

	/**
	 * Creates new combat user for given block
	 * @param block block
	 */
	public void create(Block block) {
		Location location = block.getLocation().toBlockLocation();
		try {
			BlockCombatUser blockCombatUser = constructorCombatUser.newInstance(fluffyCombat, block);
			users.put(location, blockCombatUser);
			usersByHash.put(blockCombatUser.hashCode(), blockCombatUser);
		} catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Deletes given block from users for given block
 	 * @param block block
	 */
	public void delete(Block block){
		delete(block.getLocation());
	}

	/**
	 * Deletes given block user from given location
	 * @param location location
	 */
	public void delete(Location location){
		location = location.toBlockLocation();
		users.remove(location);
	}
}
