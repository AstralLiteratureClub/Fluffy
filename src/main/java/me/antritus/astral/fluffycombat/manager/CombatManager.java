package me.antritus.astral.fluffycombat.manager;

import me.antritus.astral.fluffycombat.FluffyCombat;
import me.antritus.astral.fluffycombat.api.CombatTag;
import me.antritus.astral.fluffycombat.api.CombatUser;
import me.antritus.astral.fluffycombat.api.events.CombatEndEvent;
import org.bukkit.OfflinePlayer;
import org.bukkit.Tag;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Antritus
 * @since 1.0-SNAPSHOT
 */
public final class CombatManager {
	private final Constructor<?> combatTagConstructor;
	{
		try {
			combatTagConstructor = CombatTag.class.getDeclaredConstructor(FluffyCombat.class, CombatUser.class, CombatUser.class);
			combatTagConstructor.setAccessible(true);
		} catch (NoSuchMethodException e) {
			throw new RuntimeException(e);
		}
	}

	private final FluffyCombat main;
	private BukkitTask task;
	private final Map<String, CombatTag> tags = new LinkedHashMap<>();
	private final Map<UUID, CombatTag> latest = new LinkedHashMap<>();

	/**
	 * Creates new CombatManager instance.
	 * @see FluffyCombat#getCombatManager()
	 * @param main main instance
	 */
	public CombatManager(FluffyCombat main) {
		this.main = main;
		task = null;
	}

	/**
	 * Called from FluffyCommand enable() method
	 * @see FluffyCombat#enable()
	 */
	public void onEnable(){
		task = new BukkitRunnable() {
			/**
			 * When an object implementing interface {@code Runnable} is used
			 * to create a thread, starting the thread causes the object's
			 * {@code run} method to be called in that separately executing
			 * thread.
			 * <p>
			 * The general contract of the method {@code run} is that it may
			 * take any action whatsoever.
			 *
			 * @see Thread#run()
			 */
			@Override
			public void run() {
				List<String> deleteList = new ArrayList<>();
				List<String> nullList = new ArrayList<>();
				latest.clear();
 				tags.forEach((key, tag)->{
					if (tag == null){
						nullList.add(key);
					} else {
						latest.putIfAbsent(tag.getVictim().getUniqueId(), tag);
						latest.putIfAbsent(tag.getAttacker().getUniqueId(), tag);
						tag.setTicksLeft(tag.getTicksLeft() - 1);
						if (tag.getTicksLeft() < 0) {
							deleteList.add(key);
						}
						{
							int ticksLeft = tag.getTicksLeft();
							if (latest.get(tag.getVictim().getUniqueId()).getTicksLeft()<ticksLeft){
								latest.put(tag.getVictim().getUniqueId(), tag);
							}
							if (latest.get(tag.getAttacker().getUniqueId()).getTicksLeft()<ticksLeft){
								latest.put(tag.getAttacker().getUniqueId(), tag);
							}

						}
					}
				});
				nullList.forEach(tags::remove);
				deleteList.forEach(key->{
					CombatTag tag = tags.get(key);
					CombatUser victim = tag.getVictim();
					CombatUser attacker = tag.getAttacker();
					tags.remove(key);
					OfflinePlayer victimPlayer = victim.getPlayer();
					OfflinePlayer attackerPlayer = attacker.getPlayer();
					Set<String> keys = tags.keySet();
					if (victimPlayer.isOnline()) {
						if (keys.isEmpty() || keys.stream().noneMatch(id -> id.contains(victimPlayer.getUniqueId().toString()))) {
							main.getMessageManager().message((Player) victimPlayer, "combat-end");
						}
					}
					if (attackerPlayer.isOnline()) {
						if (keys.isEmpty() || keys.stream().noneMatch(id -> id.contains(attackerPlayer.getUniqueId().toString()))) {
							main.getMessageManager().message((Player) attackerPlayer, "combat-end");
						}
					}
					CombatEndEvent event = new CombatEndEvent(main, tag);
					event.callEvent();
				});

			}
		}.runTaskTimerAsynchronously(main, 20, 1);
	}
	/**
	 * Called from FluffyCommand enable() method
	 * @see FluffyCombat#disable()
	 */
	public void onDisable(){
		if (task != null){
			task.cancel();
		}
		tags.clear();
	}

	@Nullable
	public CombatTag getLatest(OfflinePlayer player){
		return latest.get(player.getUniqueId());
	}

	/**
	 * Returns true if the given players have no combat tag.
	 * This is check by using getTag(..., ...)
	 * @see #getTag(OfflinePlayer, OfflinePlayer)
	 * @param player player
	 * @param player2 player 2
	 * @return is tag from getTag() null or not
	 */
	public boolean hasTag(OfflinePlayer player, OfflinePlayer player2){
		return getTag(player, player2) != null;
	}

	public boolean isActive(CombatTag tag, OfflinePlayer player){
		if (tag.getAttacker().getUniqueId().equals(player.getUniqueId())){
			return !tag.isDeadAttacker();
		} else {
			return !tag.isDeadVictim();
		}
	}
	public boolean isActive(CombatTag tag, UUID playerId){
		if (tag.getAttacker().getUniqueId().equals(playerId)){
			return !tag.isDeadAttacker();
		} else {
			return !tag.isDeadVictim();
		}
	}


	/**
	 * Checks using player-player2 if tag exists,
	 * if not it flips the players around to see.
	 * If no tag is found will return null.
	 * @param player player
	 * @param player2 player-2
	 * @return combat tag, null if no tag is found.
	 */
	@Nullable
	public CombatTag getTag(OfflinePlayer player, OfflinePlayer player2){
		CombatTag tag = tags.get(toId(player, player2));
		if (tag == null){
			tag = tags.get(toId(player2, player));
		}
		return tag;
	}

	/**
	 * Creates a new instance of combat tag and stores it to the combat tags map.
	 * @param playerVictim victim
	 * @param playerAttacker attacker
	 * @return instance of the tag
	 */
	@NotNull
	public CombatTag create(Player playerVictim, Player playerAttacker){
		CombatUser combatUserVictim = main.getUserManager().getUser(playerVictim);
		CombatUser combatUserAttacker = main.getUserManager().getUser(playerAttacker);
		try {
			CombatTag tag = (CombatTag) combatTagConstructor.newInstance(main, combatUserVictim, combatUserAttacker);
			tags.remove(toId(playerAttacker, playerVictim));
			tags.put(toId(playerVictim, playerAttacker), tag);
			return tag;
		} catch (InstantiationException | InvocationTargetException | IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}


	/**
	 * Returns all tags where player is part of.
	 * @param player player
	 * @return empty list if none found, else all tags where player is in the key
	 */
	public List<CombatTag> getTags(OfflinePlayer player){
		List<CombatTag> returnList = new ArrayList<>();
		tags.forEach((key, tag)->{
			if (key.contains(player.getUniqueId().toString())){
				returnList.add(tag);
			}
		});
		return returnList;
	}

	/**
	 * Checks keys and tries to find any key that contains the id of the player
	 * @param player player
	 * @return found tags
	 */
	public boolean hasTags(OfflinePlayer player){
		List<String> keys = tags.keySet().stream().filter(key->key.contains(player.getUniqueId().toString())).toList();
		List<CombatTag> tags = new ArrayList<>();
		for (String key : keys){
			CombatTag tag = this.tags.get(key);
			if (isActive(tag, player)){
				tags.add(this.tags.get(key));
			}
		}
		return tags.size()>0;
	}

	/**
	 * Generates players uniqueIds to new the format of tags string id.
	 * @param player player
	 * @param player2 player 2
	 * @return id format
	 */
	private String toId(OfflinePlayer player, OfflinePlayer player2){
		return "{"+player.getUniqueId()+"}-{"+player2.getUniqueId()+"}";
	}
	/**
	 * Generates uniqueIds to new the format of tags string id.
	 * @param uuid unique id
	 * @param uuid2 unique id 2
	 * @return id format
	 */
	private String toId(UUID uuid, UUID uuid2){
		return "{"+uuid+"}-{"+uuid2+"}";
	}



	/**
	 * Returns the main class instance of the plugin.
	 * @return main class instance
	 */
	@NotNull
	public FluffyCombat getMain() {
		return main;
	}

	/**
	 * Triggered when player joins the server
	 * @param player player
	 */
	@ApiStatus.Internal
	@ApiStatus.NonExtendable
	public void onJoin(Player player) {
		if (hasTags(player)){
			main.getMessageManager().broadcast("log-join", "%player%="+player.getUniqueId());
		}
	}
}
