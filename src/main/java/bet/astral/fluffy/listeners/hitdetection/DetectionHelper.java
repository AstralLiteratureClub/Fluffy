package bet.astral.fluffy.listeners.hitdetection;

import bet.astral.more4j.tuples.Pair;
import org.apache.commons.lang3.Validate;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockFadeEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;

public final class DetectionHelper implements Listener {
    public static Material[] FIRE_BLOCKS = {Material.SOUL_FIRE, Material.FIRE, Material.FIRE};
    private static Map<Chunk, Map<Location, Pair<UUID, Material>>> blockOwners = new HashMap<>();

    /**
     * Times a removal of the key from the map.
     *
     * @param plugin plugin
     * @param map    map
     * @param key    key
     * @param ticks  ticks to remove
     * @param <K>    key type
     * @param <V>    value type
     */
    public static <K, V> void timedRemovalKey(@NotNull Plugin plugin, Map<K, V> map, K key, int ticks) {
        plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, () -> {
            if (key == null) {
                return;
            }
            map.remove(key);
        }, ticks);
    }

    /**
     * Times a removal of the key from the map. If the value in the map is not the same as the value provided ignores.
     *
     * @param plugin plugin
     * @param map    map
     * @param key    key
     * @param value  value to match
     * @param ticks  ticks to remove
     * @param <K>    key type
     * @param <V>    value type
     */
    public static <K, V> void timedRemovalKeyIfSameValue(@NotNull Plugin plugin, Map<K, V> map, K key, V value, int ticks) {
        plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, () -> {
            if (key == null) {
                return;
            }
            if (value == null) {
                map.remove(key);
            } else {
                V valueFromMap = map.get(key);
                if (valueFromMap == value) {
                    map.remove(key);
                }
            }
        }, ticks);
    }

    /**
     * Checks if the materials array contains the material provided in first param
     *
     * @param material  material to find
     * @param materials materials to search in
     * @return true if is in the array
     */
    @Contract(pure = true)
    public static boolean isAny(@NotNull Material material, Material @NotNull ... materials) {
        for (Material m : materials) {
            if (m == material) return true;
        }
        return false;
    }

    /**
     * Returns the block owner data for the given block
     *
     * @param block block to find it from
     * @return data
     */
    public static @Nullable Pair<UUID, Material> getBlockData(@NotNull Block block) {
        if (blockOwners.get(block.getChunk()) == null) {
            return null;
        }

        Pair<UUID, Material> data = blockOwners.get(block.getChunk()).get(block.getLocation());
        if (data == null) {
            return null;
        }
        if (data.getSecond() != block.getType()) {
            return null;
        }
        return data;
    }

    /**
     * Returns true if the block has a block owner
     *
     * @param block block to find owner for
     * @return true if block owner data is not null
     */
    public static boolean isOwned(Block block) {
        return getBlockData(block) != null;
    }

    /**
     * Returns the block's owner
     *
     * @param block block
     * @return owner, nullable
     */
    public static @Nullable UUID getBlockOwner(Block block) {
        Pair<UUID, Material> blockData = getBlockData(block);
        if (blockData == null) {
            return null;
        }
        if (blockData.getSecond() != block.getType()) {
            return null;
        }
        return blockData.getFirst();
    }

    /**
     * Finds the nearest block owned by a player from the player's location
     *
     * @param player           player
     * @param allowedMaterials allowed materials to be searched for
     * @return nearest block owned by a player
     */
    public static @Nullable Block findNearestOwnedBlock(@NotNull Player player, Material... allowedMaterials) {
        Validate.notNull(player, "Player cannot be null.");

        Location playerLocation = player.getLocation();
        List<Block> surroundingBlocks = getSurroundingBlocks(playerLocation, allowedMaterials);

        return getNearestOwnedBlock(player, surroundingBlocks);
    }

    /**
     * Finds the nearest block owned by a player from the player's location
     *
     * @param location  location
     * @param materials allowed materials to be searched for
     * @return nearest block owned by a player
     */
    private static @NotNull List<Block> getSurroundingBlocks(Location location, Material... materials) {
        return getSurroundingBlocks(location, 1, materials);
    }

    /**
     * Finds the nearest block owned by a player from the player's location
     *
     * @param location  location
     * @param radius    radius to search for
     * @param materials allowed materials to be searched for
     * @return nearest block owned by a player
     */
    private static @NotNull List<Block> getSurroundingBlocks(Location location, int radius, Material... materials) {
        Set<Material> set = Arrays.stream(materials).collect(Collectors.toSet());
        ;
        List<Block> surroundingBlocks = new ArrayList<>();
        for (int x = -radius; x <= radius; x++) {
            for (int y = -radius; y <= radius; y++) {
                for (int z = -radius; z <= radius; z++) {
                    Block block = location.clone().add(x, y, z).getBlock();
                    if (set.contains(block.getType())) {
                        surroundingBlocks.add(block);
                    }
                }
            }
        }
        return surroundingBlocks;
    }

    /**
     * Finds the nearest owned block from player's location.
     * @param player player
     * @param blocks blocks to search for
     * @return owned block, nullable
     */
    public static @Nullable Block getNearestOwnedBlock(Player player, @NotNull List<Block> blocks) {
        Block nearestBlock = null;
        double shortestDistance = Double.MAX_VALUE;
        for (Block block : blocks) {
            if (isOwned(block)) {
                double distance = block.getLocation().distanceSquared(player.getLocation());
                if (distance < shortestDistance) {
                    shortestDistance = distance;
                    nearestBlock = block;
                }
            }
        }
        return nearestBlock;
    }

    /**
     * Clears block data from the given location
     * @param chunk chunk
     * @param location location
     */
    public static void clearBlockData(Chunk chunk, Location location) {
        if (blockOwners.get(chunk) != null) {
            blockOwners.get(chunk).remove(location);
        }
    }
    /**
     * Clears block data from the given location
     * @param location location
     */
    public static void clearBlockData(Location location) {
        Chunk chunk = location.getChunk();
        clearBlockData(chunk, location);
    }

    /**
     * Clears all incorrect block data from the block
     * @param chunk chunk to clear
     */
    public static void clearIncorrectBlockData(Chunk chunk) {
        List<Block> remove = new ArrayList<>();
        if (blockOwners.get(chunk) != null) {
            for (Location location : blockOwners.get(chunk).keySet()) {
                Material ownedMaterial = blockOwners.get(chunk).get(location).getSecond();
                Material materialBlock = location.getBlock().getType();
                if (ownedMaterial != materialBlock) {

                    // Not sure what this is for...? Most probable cause for it being when fire changes from soul fire to normal fire
                    if (ownedMaterial == Material.FIRE && materialBlock == Material.SOUL_FIRE) {
                        continue;
                    }
                    if (ownedMaterial == Material.SOUL_FIRE && materialBlock == Material.FIRE) {
                        continue;
                    }

                    remove.add(location.getBlock());
                }
            }
        }
        for (Block block : remove) {
            blockOwners.get(block.getChunk()).remove(block.getLocation());
        }
    }

    /**
     * Sets the block to be owned by the given player
     * @param player owned by player
     * @param block block
     */
    public static void setBlockOwner(@NotNull OfflinePlayer player, @NotNull Block block) {
        blockOwners.putIfAbsent(block.getChunk(), new HashMap<>());
        blockOwners.get(block.getChunk()).put(block.getLocation(), Pair.immutable(player.getUniqueId(), block.getType()));
    }

    /**
     * Sets the block to be owned by the given player
     * @param player owned by player
     * @param block block
     * @param material Material to set as saved block type
     */
    public static void setBlockOwner(@NotNull OfflinePlayer player, @NotNull Block block, Material material) {
        blockOwners.putIfAbsent(block.getChunk(), new HashMap<>());
        blockOwners.get(block.getChunk()).put(block.getLocation(), Pair.immutable(player.getUniqueId(), material));
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onChunkLoad(@NotNull ChunkLoadEvent event) {
        DetectionHelper.clearIncorrectBlockData(event.getChunk());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    private void blockBreak(@NotNull BlockBreakEvent event) {
        DetectionHelper.clearBlockData(event.getBlock().getChunk(), event.getPlayer().getLocation());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void bucketFill(@NotNull PlayerBucketFillEvent event) {
        DetectionHelper.clearBlockData(event.getBlock().getChunk(), event.getBlock().getLocation());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void blockFade(@NotNull BlockFadeEvent event) {
        DetectionHelper.clearBlockData(event.getBlock().getChunk(), event.getBlock().getLocation());
    }
}
