package bet.astral.fluffy.manager;

import bet.astral.fluffy.events.CombatTagEndEvent;
import bet.astral.fluffy.configs.CombatConfig;
import bet.astral.fluffy.events.player.PlayerCombatEndEvent;
import bet.astral.fluffy.FluffyCombat;
import bet.astral.fluffy.api.BlockCombatTag;
import bet.astral.fluffy.api.BlockCombatUser;
import bet.astral.fluffy.api.CombatTag;
import bet.astral.fluffy.api.CombatUser;
import bet.astral.fluffy.events.player.PlayerCombatFullEndEvent;
import bet.astral.fluffy.messenger.Translations;
import bet.astral.shine.Shine;
import bet.astral.shine.ShineColor;
import bet.astral.more4j.tuples.Quartet;
import net.kyori.adventure.util.Ticks;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * @author Antritus
 * @since 1.0-SNAPSHOT
 */
public final class CombatManager {
	private final Constructor<?> combatTagConstructor;
	private final Constructor<BlockCombatTag> blockCombatTagConstructor;
	{
		try {
			combatTagConstructor = CombatTag.class.getDeclaredConstructor(FluffyCombat.class, CombatUser.class, CombatUser.class);
			combatTagConstructor.setAccessible(true);
			blockCombatTagConstructor = BlockCombatTag.class.getDeclaredConstructor(FluffyCombat.class, CombatUser.class, BlockCombatUser.class);
			blockCombatTagConstructor.setAccessible(true);
		} catch (NoSuchMethodException e) {
			throw new RuntimeException(e);
		}
	}

	private final FluffyCombat main;
	private BukkitTask task;
	private final Map<String, CombatTag> tags = new LinkedHashMap<>();
	private final Map<UUID, CombatTag> latest = new HashMap<>();
	private final Map<UUID, Boolean> alreadyEnded = new HashMap<>();

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
	 * @see FluffyCombat#onEnable()
	 */
	public void onEnable() {
		task = new BukkitRunnable() {
			final Set<UUID> ended = new HashSet<>();
			/**
			 * Maybe switching to a thread per player would be possible, but it's not something in my mind currently.
			 */
			@Override
			public void run() {
				try {
					for (UUID uniqueId : List.copyOf(ended)) {
						if (!hasTags(Bukkit.getOfflinePlayer(uniqueId)) && (alreadyEnded.get(uniqueId)==null||!alreadyEnded.get(uniqueId))) {
							OfflinePlayer player = main.getServer().getOfflinePlayer(uniqueId);
							PlayerCombatFullEndEvent event = new PlayerCombatFullEndEvent(true, main, player);
							event.callEvent();
							alreadyEnded.put(uniqueId, true);
							ended.remove(uniqueId);
						}
					}

					CombatConfig config = main.getCombatConfig();
					List<String> deleteList = new ArrayList<>();
					List<String> nullList = new ArrayList<>();
					latest.clear();
					Map<String, List<CombatTag>> userTags = new HashMap<>();
					Map<CombatTag, Quartet<UUID, Boolean, UUID, Boolean>> combatEnded = new HashMap<>();

					tags.forEach((key, tag) -> {
						if (tag == null) {
							nullList.add(key);
						} else {
							String[] ids = splitId(key);
							userTags.putIfAbsent(ids[0], new ArrayList<>());
							userTags.putIfAbsent(ids[1], new ArrayList<>());

							tag.setVictimTicksLeft(tag.getVictimTicksLeft() - 5);
							tag.setAttackerTicksLeft(tag.getAttackerTicksLeft() - 5);
							if (tag.getVictimTicksLeft() < 0 && tag.getAttackerTicksLeft() < 0) {
								deleteList.add(key);
							} else {
								userTags.get(ids[0]).add(tag);
								userTags.get(ids[1]).add(tag);

								// Checking combat timers
								combatEnded.put(tag, Quartet.immutable(tag.getVictim().getUniqueId(), tag.getVictimTicksLeft() < 0,
										tag.getAttacker() instanceof BlockCombatUser ? null : tag.getAttacker().getUniqueId(), tag.getAttackerTicksLeft() < 0));

								// Setting the victim's latest tag
								CombatUser combatUser = tag.getVictim();
								UUID uniqueId = combatUser.getUniqueId();
								latest.putIfAbsent(uniqueId, tag);
								int ticksLeft = tag.getVictimTicksLeft();
								// Checking if the latest tag is less than current tag
								if (latest.get(uniqueId).getTicksLeft(combatUser) < ticksLeft) {
									latest.put(uniqueId, tag);
								}

								// Checking if the tag is a combat tag
								if (!(tag.getAttacker() instanceof BlockCombatUser blockCombatUser)) {
									// Player attacker
									combatUser = tag.getAttacker();
									uniqueId = combatUser.getUniqueId();
									latest.putIfAbsent(uniqueId, tag);
									{
										if (latest.get(uniqueId).getTicksLeft(combatUser) < ticksLeft) {
											latest.put(uniqueId, tag);
										}
									}

									// Block attacker, doesn't need a latest
								} else {
									// Checking if dead, and removing the tag if the block is dead
									if (!blockCombatUser.isAlive()) {
										tag.setAttackerTicksLeft(-1);
										tag.setVictimTicksLeft(-1);
										deleteList.add(key);
									}
								}
							}
						}
					});
					deleteList.forEach(key -> {
						CombatTag tag = tags.get(key);
						if (tag.getAttacker() instanceof BlockCombatUser blockCombatUser) {
							OfflinePlayer victimOP = tag.getVictim().getPlayer();
							if (victimOP.isOnline()) {
								Player victim = (Player) victimOP;
								Block block = blockCombatUser.getBlock();
								removeGlow(main, victim, block);
							}
						} else {
							OfflinePlayer victimOP = tag.getVictim().getPlayer();
							OfflinePlayer attackerOP = tag.getAttacker().getPlayer();
							if (victimOP.isOnline() && attackerOP.isOnline()) {
								removeGlow(main, attackerOP.getPlayer(), victimOP.getPlayer());
								removeGlow(main, victimOP.getPlayer(), attackerOP.getPlayer());
							}
						}
					});
					nullList.forEach(tags::remove);
					deleteList.forEach(key -> {
						CombatTag tag = tags.get(key);
						CombatUser victim = tag.getVictim();
						CombatUser attacker = tag.getAttacker();
						tags.remove(key);
						OfflinePlayer victimPlayer = victim.getPlayer();
						Set<String> keys = tags.keySet();
						if (victimPlayer.isOnline()) {
							if (keys.isEmpty() || keys.stream().noneMatch(id -> id.contains(victimPlayer.getUniqueId().toString()))) {
								main.getMessenger().message(victimPlayer, Translations.COMBAT_END);
							}
						}
						if (!(attacker instanceof BlockCombatUser)) {
							OfflinePlayer attackerPlayer = attacker.getPlayer();
							if (attackerPlayer.isOnline()) {
								if (keys.isEmpty() || keys.stream().noneMatch(id -> id.contains(attackerPlayer.getUniqueId().toString()))) {
									main.getMessenger().message(attackerPlayer, Translations.COMBAT_END);
								}
							}
						}
						CombatTagEndEvent event = new CombatTagEndEvent(main, tag);
						event.callEvent();
					});

					combatEnded.forEach((key, value) -> {
						UUID victim = value.getFirst();
						UUID attacker = value.getThird();
						if (value.getSecond()) {
							OfflinePlayer player = main.getServer().getOfflinePlayer(victim);
							PlayerCombatEndEvent event = new PlayerCombatEndEvent(main, key, player);
							event.callEvent();
						}
						if (attacker != null) {
							if (value.getFourth()) {
								OfflinePlayer player = main.getServer().getOfflinePlayer(attacker);
								PlayerCombatEndEvent event = new PlayerCombatEndEvent(main, key, player);
								event.callEvent();
							}
							ended.add(value.getThird());
						}
						ended.add(value.getFirst());
					});

					latest.forEach((key, tag) -> {
						if (tag.getAttacker() instanceof BlockCombatUser blockCombatUser) {
							OfflinePlayer victimOP = tag.getVictim().getPlayer();
							if (victimOP.isOnline()) {
								Player victim = victimOP.getPlayer();
								if (victim != null) {
									makeGlow(main, victim, victim, tag);
								}
							}
						} else {
							OfflinePlayer victimOP = tag.getVictim().getPlayer();
							OfflinePlayer attackerOP = tag.getAttacker().getPlayer();
							if (config.isCombatGlow() && config.isCombatGlowLatest()
									&& victimOP.isOnline() && attackerOP.isOnline()) {
								makeGlow(main, victimOP.getPlayer(), attackerOP.getPlayer(), tag);
								if (!victimOP.equals(attackerOP)) {
									makeGlow(main, attackerOP.getPlayer(), victimOP.getPlayer(), tag);
								}
							}

							if (victimOP.isOnline() && config.isCombatGlow()
									&& (config.isCombatGlowAllTagged() || config.isCombatGlowCombatLogRejoin())) {
								List<CombatTag> tags = getTags(victimOP);
								for (CombatTag cTag : tags) {
									makeGlow(main, (Player) victimOP, attackerOP, cTag);
								}
							}

							if (attackerOP.isOnline() && config.isCombatGlow()
									&& (config.isCombatGlowAllTagged() || config.isCombatGlowCombatLogRejoin())) {
								List<CombatTag> tags = getTags(attackerOP);
								for (CombatTag cTag : tags) {
									makeGlow(main, (Player) attackerOP, victimOP, cTag);
								}
							}
						}
					});
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}.runTaskTimerAsynchronously(main, 20, 10);
	}

	public static void makeGlow(@NotNull FluffyCombat fluffy, @NotNull Player whoSees, @NotNull OfflinePlayer ignore, @NotNull CombatTag tag){
		CombatConfig combatConfig = fluffy.getCombatConfig();
		if (!combatConfig.isCombatGlow()){
			return;
		}

		final Shine shine = fluffy.getShine();
		final boolean isLatest = fluffy.getCombatManager().isLatest(whoSees, tag);
		final boolean isVictim = tag.getVictim().getUniqueId().equals(whoSees.getUniqueId());
		final CombatUser attacker = isVictim ? tag.getAttacker() : tag.getVictim();
		final CombatUser victim = isVictim ? tag.getVictim() : tag.getAttacker();
		if (attacker.getUniqueId().equals(ignore.getUniqueId())){
			return;
		}
		if (tag instanceof BlockCombatTag){
			Block block = ((BlockCombatUser) tag.getAttacker()).getBlock();
			ShineColor shineColor = victim.getTaggedGlowColor().orElse(combatConfig.getCombatGlowAllTagged());
			if (isLatest && combatConfig.isCombatGlowLatest()){
				shineColor = victim.getLatestGlowColor().orElse(combatConfig.getCombatGlowLatest());
			}
			try {
				shine.setGlowing(whoSees, block, shineColor);
			} catch (ReflectiveOperationException e) {
				throw new RuntimeException(e);
			}
		} else {
			OfflinePlayer attackerPlayer = attacker.getPlayer();
			if (attackerPlayer instanceof Player attackerOnline) {
				ShineColor color = victim.getTaggedGlowColor().orElse(combatConfig.getCombatGlowAllTagged());
				boolean isRejoin = attacker.getRejoinTimer()>0;
				if (isRejoin && combatConfig.isCombatGlowCombatLogRejoin()){
					color = victim.getRejoinedGlowColor().orElse(combatConfig.getCombatGlowTagRejoin());
				} else if (isLatest && combatConfig.isCombatGlowLatest()){
					color = victim.getLatestGlowColor().orElse(combatConfig.getCombatGlowLatest());
				}
				try {
					shine.setGlowing(whoSees, attackerOnline, color);
				} catch (ReflectiveOperationException e) {
					throw new RuntimeException(e);
				}
			}
		}
	}
	public static void removeGlow(@NotNull FluffyCombat fluffyCombat, Player whoSees, Entity entity){
		Shine shine = fluffyCombat.getShine();
		try {
			shine.removeGlowing(whoSees, entity);
		} catch (ReflectiveOperationException e) {
			throw new RuntimeException(e);
		}
	}
	public static void removeGlow(@NotNull FluffyCombat fluffyCombat, Player whoSees, Block block){
		Shine shine = fluffyCombat.getShine();
		try {
			shine.removeGlowing(whoSees, block);
		} catch (ReflectiveOperationException e) {
			throw new RuntimeException(e);
		}
	}


	/**
	 * Called from FluffyCommand enable() method
	 * @see FluffyCombat#onDisable()
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

	public boolean isLatest(@NotNull Player whoSees, @NotNull CombatTag tag) {
		if (getLatest(whoSees) == null){
			return false;
		}
		return getLatest(whoSees).equals(tag);
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

	/**
	 * Returns true if the given players have no combat tag.
	 * This is check by using getTag(..., ...)
	 * @see #getTag(OfflinePlayer, OfflinePlayer)
	 * @param player player
	 * @param block block
	 * @return is tag from getTag() null or not
	 */
	public synchronized boolean hasTag(OfflinePlayer player, BlockCombatUser block){
		return getTag(player, block) != null;
	}
	public synchronized boolean isActive(CombatTag tag, OfflinePlayer player){
		return isActive(tag, player.getUniqueId());
	}
	public synchronized boolean isActive(CombatTag tag, UUID playerId){
		return tag.isActive(playerId);
	}

	public synchronized boolean isActive(BlockCombatTag tag, BlockCombatUser block){
		return tag.isActive(block);
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
	public synchronized CombatTag getTag(OfflinePlayer player, OfflinePlayer player2){
		CombatTag tag = tags.get(toId(player, player2));
		if (tag == null){
			tag = tags.get(toId(player2, player));
		}
		return tag;
	}

	/**
	 * Checks using player-block.hashCode() if tag exists,
	 * If no tag is found will return null.
	 * @param player player
	 * @param block block
	 * @return combat tag, null if no tag is found.
	 */
	@Nullable
	public synchronized CombatTag getTag(OfflinePlayer player, BlockCombatUser block){
		return tags.get(toId(player, block));
	}

	/**
	 * Creates a new instance of combat tag and stores it to the combat tags map.
	 * @param playerVictim victim
	 * @param playerAttacker attacker
	 * @return instance of the tag
	 */
	@NotNull
	public synchronized CombatTag create(Player playerVictim, OfflinePlayer playerAttacker){
		@NotNull
		CombatUser combatUserVictim = Objects.requireNonNull(main.getUserManager().getUser(playerVictim));
		@Nullable
		CombatUser combatUserAttacker = main.getUserManager().getUser(playerAttacker.getUniqueId());
		if (combatUserAttacker == null) {
			combatUserAttacker = main.getUserManager().createAndLoadASync(playerAttacker);
		}

		alreadyEnded.remove(playerVictim.getUniqueId());
		alreadyEnded.remove(playerAttacker.getUniqueId());

		try {
			CombatTag tag = (CombatTag) combatTagConstructor.newInstance(main, combatUserVictim, combatUserAttacker);
			tags.remove(toId(playerAttacker, playerVictim));
			tags.remove(toId(playerVictim, playerAttacker));
			tags.put(toId(playerVictim, playerAttacker), tag);
			return tag;
		} catch (InstantiationException | InvocationTargetException | IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Creates a new instance of combat tag and stores it to the combat tags map.
	 * @param playerVictim victim
	 * @param block attacker
	 * @return instance of the tag
	 */
	@NotNull
	public synchronized CombatTag create(Player playerVictim, BlockCombatUser block){
		alreadyEnded.remove(playerVictim.getUniqueId());
		CombatUser combatUserVictim = main.getUserManager().getUser(playerVictim);
		try {
			BlockCombatTag tag = blockCombatTagConstructor.newInstance(main, combatUserVictim, block);
			tags.remove(toId(playerVictim, block));
			tags.put(toId(playerVictim, block), tag);
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
	public synchronized List<CombatTag> getTags(OfflinePlayer player){
		List<CombatTag> returnList = new ArrayList<>();
		tags.forEach((key, tag)->{
			if (key.contains(player.getUniqueId().toString())){
				returnList.add(tag);
			}
		});
		return returnList;
	}
	/**
	 * Returns all tags where player is part of.
	 * @param player player
	 * @return empty list if none found, else all tags where player is in the key
	 */
	public synchronized List<CombatTag> getTags(UUID player){
		List<CombatTag> returnList = new ArrayList<>();
		tags.forEach((key, tag)->{
			if (key.contains(player.toString())){
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
	public synchronized boolean hasTags(UUID player){
		List<String> keys = tags.keySet().stream().filter(key->key.contains(player.toString())).toList();
		for (String key : keys){
			CombatTag tag = this.tags.get(key);
			if (isActive(tag, player)){
				return true;
			}
		}
		return false;
	}

	/**
	 * Checks keys and tries to find any key that contains the id of the player
	 * @param player player
	 * @return found tags
	 */
	public synchronized boolean hasTags(OfflinePlayer player){
		List<String> keys = tags.keySet().stream().filter(key->key.contains(player.getUniqueId().toString())).toList();
		for (String key : keys){
			CombatTag tag = this.tags.get(key);
			if (isActive(tag, player)){
				return true;
			}
		}
		return false;
	}

	public synchronized boolean hasTags(BlockCombatUser user){
		List<String> keys = tags.keySet().stream().filter(key->key.contains(String.valueOf(user.hashCode()))).toList();
		for (String key : keys){
			BlockCombatTag tag = (BlockCombatTag) this.tags.get(key);
			if (isActive(tag, user)){
				return true;
			}
		}
		return false;
	}

	/**
	 * Generates players uniqueIds to new the format of tags string id.
	 * @param player player
	 * @param player2 player 2
	 * @return id format
	 */
	private synchronized String toId(OfflinePlayer player, OfflinePlayer player2){
		return "{"+player.getUniqueId()+"}-{"+player2.getUniqueId()+"}";
	}
	/**
	 * Generates uniqueIds to new the format of tags string id.
	 * @param uuid unique id
	 * @param uuid2 unique id 2
	 * @return id format
	 */
	private synchronized String toId(UUID uuid, UUID uuid2){
		return "{"+uuid+"}-{"+uuid2+"}";
	}


	/**
	 * Generates players uniqueIds to new the format of tags string id.
	 * @param player player
	 * @param block block
	 * @return id format
	 */
	private synchronized String toId(OfflinePlayer player, BlockCombatUser block){
		return toId(player.getUniqueId(), block);
	}
	/**
	 * Generates uniqueIds to new the format of tags string id.
	 * @param uuid unique id
	 * @param blockCombatUser combatUser
	 * @return id format
	 */
	private synchronized String toId(UUID uuid, BlockCombatUser blockCombatUser){
		return "{"+uuid+"}-{"+blockCombatUser.hashCode()+"}";
	}

	private synchronized String[] splitId(String key) {
		key = key.replace("{", "");
		String[] ids = key.split("}-");
		ids[0] = ids[0].replace("}", "").replace("{", "");
		ids[1] = ids[1].replace("}", "").replace("{", "");
		return ids;
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
	 * Extends the combat of a given player.
	 * @param uniqueId player
	 * @param extendCombat time to extend tag with
	 * @param timeUnit time unit
	 */
	public void extendAllTags(@NotNull UUID uniqueId, int extendCombat, @NotNull TimeUnit timeUnit) {
		List<CombatTag> tags = getTags(uniqueId);

		for (CombatTag tag : tags) {
			if (tag.isActive(uniqueId)){
				tag.setTicksLeft(uniqueId, (int) (timeUnit.toMillis(extendCombat)/Ticks.SINGLE_TICK_DURATION_MS));
			}
		}
	}
	/**
	 * Extends the combat of a given player.
	 * @param user player
	 * @param extendCombat time to extend tag with
	 * @param timeUnit time unit
	 */
	public void extendAllTags(@NotNull Player user, int extendCombat, @NotNull TimeUnit timeUnit) {
		extendAllTags(user.getUniqueId(), extendCombat, timeUnit);
	}
	/**
	 * Extends the combat of a given player.
	 * @param user player
	 * @param extendCombat time to extend tag with
	 * @param timeUnit time unit
	 */
	public void extendAllTags(@NotNull CombatUser user, int extendCombat, @NotNull TimeUnit timeUnit) {
		extendAllTags(user.getUniqueId(), extendCombat, timeUnit);
	}
}
