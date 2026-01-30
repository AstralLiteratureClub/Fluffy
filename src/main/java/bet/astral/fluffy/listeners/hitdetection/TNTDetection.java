package bet.astral.fluffy.listeners.hitdetection;

import bet.astral.fluffy.api.CombatCause;
import bet.astral.fluffy.api.CombatTag;
import bet.astral.fluffy.events.damage.CombatDamageUsingTNTEvent;
import bet.astral.fluffy.listeners.combat.begin.BeginCombatListener;
import bet.astral.fluffy.FluffyCombat;
import bet.astral.more4j.tuples.Pair;
import com.destroystokyo.paper.event.entity.EntityRemoveFromWorldEvent;
import com.jeff_media.morepersistentdatatypes.DataType;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.type.Bed;
import org.bukkit.block.data.type.RespawnAnchor;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.TNTPrimeEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.projectiles.BlockProjectileSource;
import org.bukkit.projectiles.ProjectileSource;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class TNTDetection implements Listener {
    public static final NamespacedKey FLUFFY_TNT_PRIMER_ENTITY = new NamespacedKey("fluffy", "tnt_primer_entity");
    public static final NamespacedKey FLUFFY_TNT_PRIMER_BLOCK = new NamespacedKey("fluffy", "tnt_primer_block");
    public static final NamespacedKey FLUFFY_TNT_LOCATION = new NamespacedKey("fluffy", "tnt_location");
    /**
     * Used to detect TNT lit by CRYSTALS
     */
    protected final Map<UUID, UUID> crystalOwners = new HashMap<>();
    /**
     * Used to detect TNT lit by TNT
     */
    public final Map<TNTPrimed, TNTPrimed> tntOwners = new HashMap<>();
    /**
     * Used to detect TNT lit by ANCHORS
     */
    public final Map<TNTPrimed, UUID> anchorOwners = new HashMap<>();
    /**
     * Used to detect TNT lit by BEDS
     */
    public final Map<TNTPrimed, UUID> bedOwners = new HashMap<>();
    /**
     * Used to detect TNT lit by FIRE
     */
    public final Map<TNTPrimed, UUID> fireOwners = new HashMap<>();
    /**
     * Used internally for detecting TNT primers
     */
    protected final Map<Location, Entity> primers = new HashMap<>();
    /**
     * Used internally for detecting TNT primers
     */
    private final Map<Location, Block> blockPrimers = new HashMap<>();
    /**
     * After TNT primes this map is used to store the actual block from location. Used to find actual block type when using location persistent data type
     */
    private static final Map<Location, Pair<Long, Block>> CACHED_BLOCK_PRIMERS = new HashMap<>();
    private final FluffyCombat fluffy;
    private final CrystalDetection crystalDetection;

    public TNTDetection(FluffyCombat fluffy, CrystalDetection crystalDetection) {
        this.fluffy = fluffy;
        this.crystalDetection = crystalDetection;
    }

    /**
     * Sets the TNT primer to the player
     *
     * @param player player
     * @param primed primed tnt
     */
    public static void setTNTPrimer(@NotNull Player player, @NotNull TNTPrimed primed) {
        // Use persistent data container to split completely from metadata
        PersistentDataContainer container = primed.getPersistentDataContainer();
        container.set(FLUFFY_TNT_PRIMER_ENTITY, DataType.UUID, player.getUniqueId());
    }

    /**
     * Returns null when an entity is a block or a non entity OR entity was not saved
     *
     * @param tntPrimed tnt
     * @return type, else null
     */
    @Nullable
    public static EntityType type(@NotNull TNTPrimed tntPrimed) {
        PersistentDataContainer container = tntPrimed.getPersistentDataContainer();
        UUID who = container.get(FLUFFY_TNT_PRIMER_ENTITY, DataType.UUID);
        if (who == null) {
            return null;
        }
        Entity entity = Bukkit.getEntity(who);
        if (entity == null) {
            return null;
        }
        return entity.getType();
    }

    /**
     * Returns the primer of the tnt. Nullable as the persistent data container values might be null.
     * If no entity is found using the UUID, returns null.
     *
     * @param tntPrimed tnt
     * @return primer
     */
    @Nullable
    private static Object getPrimer(@NotNull TNTPrimed tntPrimed) {
        PersistentDataContainer container = tntPrimed.getPersistentDataContainer();
        UUID who = container.get(FLUFFY_TNT_PRIMER_ENTITY, DataType.UUID);
        if (who == null) {
            // Find location of the primer
            Location location = container.get(FLUFFY_TNT_PRIMER_BLOCK, DataType.LOCATION);
            if (location == null) {
                return null;
            }
            // Check if location exists as cached data
            Pair<Long, Block> cachedType = CACHED_BLOCK_PRIMERS.get(location);
            if (cachedType == null) {
                return null;
            }
            // Old data -> Ignore
            if (System.currentTimeMillis() - cachedType.getFirst() > 8000 /* milliseconds */) {
                CACHED_BLOCK_PRIMERS.remove(location);
                return null;
            }
            return cachedType.getSecond();
        }
        Entity entity = Bukkit.getEntity(who);
        if (entity == null) {
            return null;
        }
        return entity.getType();
    }

    public static @Nullable Block getBlockPrimer(TNTPrimed tntPrimed) {
        Object obj = getPrimer(tntPrimed);
        if (!(obj instanceof Block)) {
            return null;
        }
        return (Block) obj;
    }

    public static @Nullable Entity getEntityPrimer(TNTPrimed tntPrimed) {
        Object obj = getPrimer(tntPrimed);
        if (!(obj instanceof Entity)) {
            return null;
        }
        return (Entity) obj;
    }


    /**
     * Sources all possible primers of all primers who primed given tnt.
     *
     * @param tntPrimed primer
     * @return offline player, player or entity;
     */
    @SuppressWarnings("t")
    @Nullable
    public static Object getStartingEntityPrimer(TNTPrimed tntPrimed) {
        FluffyCombat fluffy = FluffyCombat.getPlugin(FluffyCombat.class);
        TNTDetection tntDetection = fluffy.getTntDetection();
        // Find the primer of the tnt
        Object obj = getPrimer(tntPrimed);
        if (obj instanceof Block block) {
            // Block data to find if it is an explosive
            BlockData blockData = block.getBlockData();
            if (blockData instanceof RespawnAnchor) {
                // Anchor explosion

                UUID anchorOwner = tntDetection.anchorOwners.get(tntPrimed);
                return fluffy.getServer().getOfflinePlayer(anchorOwner);
            } else if (block instanceof Bed) {
                // Bed explosion

                UUID bedOwner = tntDetection.bedOwners.get(tntPrimed);
                return fluffy.getServer().getOfflinePlayer(bedOwner);

            } else if (DetectionHelper.isAny(blockData.getMaterial(), DetectionHelper.FIRE_BLOCKS)) {
                // detect fire blocks

                UUID owner = tntDetection.fireOwners.get(tntPrimed);
                return fluffy.getServer().getOfflinePlayer(owner);
            }
        } else if (obj instanceof Entity entity) {
            switch (entity) {
                case LivingEntity livingEntity -> {
                    // Living entity cannot have other causes, so return the entity
                    return livingEntity;
                    // Living entity cannot have other causes, so return the entity
                }
                case Projectile projectile -> {
                    // Projectile is possible ignition cause (Fire arrows for example)
                    ProjectileSource source = projectile.getShooter();
                    if (source instanceof BlockProjectileSource) {
                        // Ignore block projectile sources for now. Adding way to detect dispensers is an interesting idea. TODO
                        return null;
                    }
                    return source;
                }
                case TNTPrimed tnt -> {
                    // TNT -> Just return the owner
                    return getStartingEntityPrimer(tntDetection.tntOwners.get(tnt));
                }
                case EnderCrystal enderCrystal -> {
                    // Ender crystal

                    CrystalDetection crystalDetect = fluffy.getCrystalDetection();
                    CrystalDetection.CrystalTag detected = crystalDetect.detectionMap.get(enderCrystal);

                    // If no detection is found try to parse using the tnt detection's crystal owners
                    if (detected == null) {
                        Map<UUID, UUID> owners = fluffy.getTntDetection().crystalOwners;
                        UUID owner = owners.get(entity.getUniqueId());
                        Entity ownerEntity = fluffy.getServer().getPlayer(owner);
                        // Check using tnt detection map -> if owner is null check if it is a player
                        if (ownerEntity == null) {
                            ownerEntity = fluffy.getServer().getEntity(owner);
                            if (ownerEntity == null) {
                                OfflinePlayer player = fluffy.getServer().getOfflinePlayer(owner);
                                if (!player.hasPlayedBefore() && !player.isOnline()) {
                                    return null;
                                }
                                return player;
                            }
                        }
                        // Return owner entity instead of null
                        return ownerEntity;
                    }

                    // Source if it is a projectile for obvious reasojs
                    if (detected.entity() instanceof Projectile projectile) {
                        if (projectile.getShooter() instanceof BlockProjectileSource) {
                            // TODO -> Possibly add dispenser detection
                            return null;
                        }
                        return projectile.getShooter();
                    }
                    return detected.entity();
                }
                default -> {
                }
            }
        }
        return null;
    }


    /**
     * Detect all possible ways to prime an TNT
     *
     * @param event event
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    private void onTNTPrime(TNTPrimeEvent event) {
        Location locCenterBlock = event.getBlock().getLocation().toBlockLocation();

        // Explosion = Block explosion && Entity explosion
        if (event.getCause() == TNTPrimeEvent.PrimeCause.EXPLOSION) {
            if (event.getPrimingBlock() != null) {

                // Place block primer
                Block block = event.getPrimingBlock();
                BlockData blockData = block.getBlockData();
                Location location = block.getLocation();
                Location blockLocation = location.toBlockLocation();

                blockPrimers.put(locCenterBlock, block);
                // Remove location after tnt explosion (7 seconds..?)
                DetectionHelper.timedRemovalKeyIfSameValue(fluffy, blockPrimers, locCenterBlock, block, 150);
                // Place block primer
                if (blockData instanceof RespawnAnchor
                        || blockData instanceof Bed) {
                    DetectionHelper.timedRemovalKeyIfSameValue(fluffy, blockPrimers, blockLocation, block, 150);
                    blockPrimers.put(blockLocation, block);
                }
            }

            Entity entity = event.getPrimingEntity();
            switch (entity) {
                case null -> {
                }
                // Ender crystal
                case EnderCrystal crystal -> {
                    primers.put(locCenterBlock, crystal);
                    DetectionHelper.timedRemovalKeyIfSameValue(fluffy, primers, locCenterBlock, crystal, 150);
                }

                // TNT
                case TNTPrimed tnt -> {
                    primers.put(locCenterBlock, tnt);
                    DetectionHelper.timedRemovalKeyIfSameValue(fluffy, primers, locCenterBlock, tnt, 150);
                }

                // Projectile (Fireball)
                case Projectile projectile -> {
                    primers.put(locCenterBlock, (Entity) projectile.getShooter());
                    DetectionHelper.timedRemovalKeyIfSameValue(fluffy, primers, locCenterBlock, (Entity) projectile.getShooter(), 150);
                }
                default -> {
                }
            }
        } else if (event.getCause() == TNTPrimeEvent.PrimeCause.PROJECTILE) {
            primers.put(locCenterBlock, event.getPrimingEntity());
            DetectionHelper.timedRemovalKeyIfSameValue(fluffy, primers, locCenterBlock, event.getPrimingEntity(), 150);
        } else if (event.getCause() == TNTPrimeEvent.PrimeCause.PLAYER) {
            primers.put(locCenterBlock, event.getPrimingEntity());
            DetectionHelper.timedRemovalKeyIfSameValue(fluffy, primers, locCenterBlock, event.getPrimingEntity(), 150);
        } else if (event.getCause() == TNTPrimeEvent.PrimeCause.BLOCK_BREAK) {
            primers.put(locCenterBlock, event.getPrimingEntity());
            DetectionHelper.timedRemovalKeyIfSameValue(fluffy, primers, locCenterBlock, event.getPrimingEntity(), 150);
        } else if (event.getCause() == TNTPrimeEvent.PrimeCause.FIRE) {

            // Fire detection -> Find the block owners using fire detection system
            Block block = event.getPrimingBlock();
            if (block == null) {
                return;
            }
            @Nullable
            UUID owner = DetectionHelper.getBlockOwner(block);
            if (owner == null) {
                return;
            }
            Location location = block.getLocation().toBlockLocation();
            blockPrimers.put(location, block);
            DetectionHelper.timedRemovalKeyIfSameValue(fluffy, blockPrimers, locCenterBlock, block, 150);
        }
    }

    /**
     * Detects when a TNT entity is spawned
     * @param event event
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    private void onTNTSPawn(@NotNull EntitySpawnEvent event) {
        if (event.getEntity() instanceof TNTPrimed tnt) {
            Location location = tnt.getLocation().toBlockLocation();
            PersistentDataContainer container = tnt.getPersistentDataContainer();
            container.set(FLUFFY_TNT_LOCATION, DataType.LOCATION, location);

            Entity entity = primers.get(location);
            switch (entity) {
                case null -> {
                    Block block = blockPrimers.get(location);
                    if (block == null) {
                        return;
                    }
                    BlockData blockData = block.getBlockData();
                    Location mid = block.getLocation().toBlockLocation();
                    // Map anchor owner from detection to tnt detection
                    if (blockData instanceof RespawnAnchor) {
                        AnchorDetection anchorDetection = fluffy.getAnchorDetection();
                        AnchorDetection.AnchorTag owner = anchorDetection.detectionMap.get(mid);
                        anchorOwners.put(tnt, owner.owner().getUniqueId());
                        DetectionHelper.timedRemovalKey(fluffy, anchorOwners, tnt, 150);
                    }
                    // Map bed owner from detection to tnt detection
                    if (blockData instanceof Bed) {
                        blockPrimers.put(mid, block);
                        BedDetection bedDetection = fluffy.getBedDetection();
                        BedDetection.BedTag owner = bedDetection.detectionMap.get(mid);
                        bedOwners.put(tnt, owner.owner().getUniqueId());
                        DetectionHelper.timedRemovalKey(fluffy, bedOwners, tnt, 150);
                    }
                    // Map fire owner from detection to tnt detection
                    if (DetectionHelper.isAny(blockData.getMaterial(), DetectionHelper.FIRE_BLOCKS)) {
                        @Nullable UUID owner = DetectionHelper.getBlockOwner(block);
                        if (owner == null) {
                            return;
                        }
                        fireOwners.put(tnt, owner);
                        DetectionHelper.timedRemovalKey(fluffy, fireOwners, tnt, 150);
                    }
                    blockPrimers.remove(location);
                    container.set(FLUFFY_TNT_PRIMER_BLOCK, DataType.LOCATION, block.getLocation());

                    // Cache block data for later use
                    Pair<Long, Block> data = Pair.immutable(System.currentTimeMillis(), block);
                    CACHED_BLOCK_PRIMERS.put(block.getLocation(), data);
                    DetectionHelper.timedRemovalKeyIfSameValue(fluffy, CACHED_BLOCK_PRIMERS, block.getLocation(), data, 150);

                    // Return so "entity" (null) is not saved
                    return;
                }
                case TNTPrimed tntPrimed -> {
                    tntOwners.put(tnt, tntPrimed);
                    DetectionHelper.timedRemovalKey(fluffy, tntOwners, tnt, 100);
                }
                case EnderCrystal crystal -> {
                    CrystalDetection.CrystalTag owner = crystalDetection.detectionMap.get(crystal);
                    crystalOwners.put(entity.getUniqueId(), owner.entity().getUniqueId());
                    UUID id = entity.getUniqueId();
                    DetectionHelper.timedRemovalKey(fluffy, crystalOwners, id, 100);
                }
                default -> throw new IllegalStateException("Unexpected value: " + entity);
            }
            primers.remove(location);
            container.set(FLUFFY_TNT_PRIMER_ENTITY, DataType.UUID, entity.getUniqueId());
        }
    }

    /**
     * Detects when an tnt is removed from the world so data can be removed
     * @param event event
     */
    @EventHandler
    private void onEntityRemove(@NotNull EntityRemoveFromWorldEvent event) {
        if (event.getEntity() instanceof TNTPrimed tnt) {
            Location location = tnt.getPersistentDataContainer().get(FLUFFY_TNT_LOCATION, DataType.LOCATION);
            if (location != null) {
                blockPrimers.remove(location);
                primers.remove(location);
            }
        }
    }

    /**
     * Detects when tnt does damage to an entity and runs combat calculations
     * @param event event
     */
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    private void onTNTDamage(@NotNull EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player victim)) {
            return;
        }
        if (event.getDamager() instanceof TNTPrimed tnt) {
            PersistentDataContainer container = tnt.getPersistentDataContainer();
            UUID owner = container.get(FLUFFY_TNT_PRIMER_ENTITY, DataType.UUID);
            if (owner != null) {
                Object value = getStartingEntityPrimer(tnt);
                if (value == null) {
                    return;
                }
                if (value instanceof OfflinePlayer attacker) {
                    BeginCombatListener.handle(victim, attacker, CombatCause.TNT, null);
                    CombatTag combatTag = fluffy.getCombatManager().getLatest(victim);
                    if (combatTag == null) {
                        return;
                    }
                    CombatDamageUsingTNTEvent damageEvent = new CombatDamageUsingTNTEvent(
                            fluffy, combatTag, victim, attacker, null, tnt);
                    combatTag.setDamageDealt(attacker, event.getFinalDamage());
                    damageEvent.callEvent();
                }
            }
        }
    }
}
