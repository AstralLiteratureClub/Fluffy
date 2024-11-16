package bet.astral.fluffy.manager;

import bet.astral.more4j.tuples.Pair;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.MultipleFacing;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.*;


public abstract class RegionManager {
    public static final RegionManager NONE = new RegionManager() {
        @Override
        public boolean canEnterCombat(Player victim, Location location) {
            return true;
        }

        @Override
        public Color getBarrierColor(Location location) {
            return Color.RED;
        }

        @Override
        public Material getBarrierMaterial(Location location) {
            return Material.BARRIER;
        }
    };
    private final Map<UUID, Set<Location>> barrierLocations = new HashMap<>();
    private final Map<UUID, Map<Location, BlockData>> barrierMaterials = new HashMap<>();
    private final Map<org.bukkit.Material, BlockData> defaultBlockData = new HashMap<>();

    public abstract boolean canEnterCombat(Player victim, Location location);
    public abstract Color getBarrierColor(Location location);
    public abstract Material getBarrierMaterial(Location location);
    public boolean shouldRenderRegionWalls(Player player, Location location){
        return false;
    }
    public org.bukkit.Material getBarrier(Location location){
        return org.bukkit.Material.valueOf(
                getBarrierMaterial(location).materialId
                        .replace("%color%", getBarrierColor(location).name())
        );
    }

    public void clearOldLocations(Player player, Set<Location> locations){
        Set<Location> cloned = new HashSet<>(this.barrierLocations.get(player.getUniqueId()));
        cloned.removeIf(locations::contains);

        cloned.forEach(loc->clearBlockPlacement(loc, player));
    }
    public void checkAndReplaceBarrier(Player player){

    }
    public void clearBlockPlacement(Location location, Player player){
        Block block = location.getBlock();
        player.sendBlockChange(location, block.getBlockData());
    }
    public void handleBlockPlacement(Location location, Player player){
        if (location.getBlock().getType()!= org.bukkit.Material.AIR){
            switch (location.getBlock().getType()){
                case OAK_SAPLING,
                     SPRUCE_SAPLING,
                     BIRCH_SAPLING,
                     JUNGLE_SAPLING,
                     ACACIA_SAPLING,
                     DARK_OAK_SAPLING,
                     MANGROVE_PROPAGULE,
                     CHERRY_SAPLING,
                     BROWN_MUSHROOM,
                     RED_MUSHROOM,
                     CRIMSON_FUNGUS,
                     WARPED_FUNGUS,
                     SHORT_GRASS,
                     FERN,
                     DEAD_BUSH,
                     DANDELION,
                     POPPY,
                     BLUE_ORCHID,
                     ALLIUM,
                     AZURE_BLUET,
                     RED_TULIP,
                     ORANGE_TULIP,
                     WHITE_TULIP,
                     PINK_TULIP,
                     OXEYE_DAISY,
                     CORNFLOWER,
                     LILY_OF_THE_VALLEY,
                     TORCHFLOWER,
                     WITHER_ROSE,
                     PINK_PETALS,
                     SUGAR_CANE,
                     CRIMSON_ROOTS,
                     WARPED_ROOTS,
                     NETHER_SPROUTS,
                     WEEPING_VINES,
                     TWISTING_VINES,
                     CAVE_VINES,
                     TALL_GRASS,
                     LARGE_FERN,
                     SUNFLOWER,
                     LILAC,
                     ROSE_BUSH,
                     PEONY,
                     PITCHER_PLANT,
                     PITCHER_CROP,
                     GLOW_LICHEN,
                     HANGING_ROOTS,
                     FROGSPAWN,
                     WHEAT,
                     PUMPKIN_STEM,
                     MELON_STEM,
                     BEETROOTS,
                     SWEET_BERRIES,
                     NETHER_WART,
                     LILY_PAD,
                     KELP_PLANT,
                     KELP,
                     TUBE_CORAL,
                     TUBE_CORAL_FAN,
                     DEAD_TUBE_CORAL,
                     DEAD_TUBE_CORAL_FAN,
                     BRAIN_CORAL,
                     BRAIN_CORAL_FAN,
                     DEAD_BRAIN_CORAL,
                     DEAD_BRAIN_CORAL_FAN,
                     BUBBLE_CORAL,
                     BUBBLE_CORAL_FAN,
                     DEAD_BUBBLE_CORAL,
                     DEAD_BUBBLE_CORAL_FAN,
                     FIRE_CORAL,
                     FIRE_CORAL_FAN,
                     DEAD_FIRE_CORAL,
                     DEAD_FIRE_CORAL_FAN,
                     HORN_CORAL,
                     HORN_CORAL_FAN,
                     DEAD_HORN_CORAL,
                     DEAD_HORN_CORAL_FAN,
                     SCULK_VEIN,
                     MOSS_CARPET,
                     SNOW,
                     SMALL_AMETHYST_BUD,
                     MEDIUM_AMETHYST_BUD

                        ->{

                }default -> {
                    return;
                }
            }
        }

        org.bukkit.Material material = getBarrier(location);
        BlockData blockData = material.createBlockData();
        if (blockData instanceof MultipleFacing multipleFacing){
            //noinspection unchecked
            Pair<BlockFace, Boolean>[] blockFaces = new Pair[]{
                    isMultiFace(player, location, BlockFace.NORTH),
                    isMultiFace(player, location, BlockFace.WEST),
                    isMultiFace(player, location, BlockFace.EAST),
                    isMultiFace(player, location, BlockFace.SOUTH)
            };

            for (Pair<BlockFace, Boolean> blockFace : blockFaces) {
                multipleFacing.setFace(blockFace.getFirst(), blockFace.getSecond());
            }

            player.sendBlockChange(location, blockData);
        } else {
            player.sendBlockChange(location, getBarrier(location).createBlockData());
        }

        barrierLocations.putIfAbsent(player.getUniqueId(), new HashSet<>());
        barrierLocations.get(player.getUniqueId()).add(location);
    }

    public Pair<BlockFace, Boolean> isMultiFace(Player player, Location location, BlockFace blockFace){
        if (barrierMaterials.get(player.getUniqueId()) == null || barrierMaterials.get(player.getUniqueId()).isEmpty()){
            return Pair.immutable(blockFace, false);
        }

        Location relative = location.getBlock().getRelative(blockFace).getLocation();
        BlockData materialData = barrierMaterials.get(player.getUniqueId()).get(relative);

        if (materialData == null){
            org.bukkit.Material material = location.getBlock().getType();
            if (material.isEmpty()){
                return Pair.immutable(blockFace, false);
            }

            BlockData blockData = defaultBlockData.get(material);
            if (blockData == null){
                blockData = material.createBlockData();
                defaultBlockData.put(material, blockData);
            }
            return Pair.immutable(blockFace, blockData instanceof MultipleFacing);
        }

        org.bukkit.Material material = materialData.getMaterial();
        BlockData blockData = defaultBlockData.get(material);
        if (blockData == null){
            defaultBlockData.put(material, material.createBlockData());
            blockData = defaultBlockData.get(material);
        }

        return Pair.immutable(blockFace, blockData instanceof MultipleFacing);
    }

    public List<Location> getSphere(Location location, int radius, boolean empty) {
        List<Location> locations = new ArrayList<>();

        int bx = location.getBlockX();
        int by = location.getBlockY();
        int bz = location.getBlockZ();

        for (int x = bx - radius; x <= bx + radius; x++) {
            for (int y = by - radius; y <= by + radius; y++) {
                for (int z = bz - radius; z <= bz + radius; z++) {
                    double distance = ((bx - x) * (bx - x) + (bz - z) * (bz - z) + (by - y) * (by - y));
                    if (distance < radius * radius && (!empty && distance < (radius - 1) * (radius - 1))) {
                        locations.add(new Location(location.getWorld(), x, y, z));
                    }
                }
            }
        }

        return locations;
    }

    public List<Location> getSphereVisible(Player player, int radius) {
        Location head = player.getEyeLocation();
        Vector playerDirection = head.getDirection().normalize();
        float fov = 180F; // Field of view in degrees
        double halfFov = Math.toRadians(fov / 2); // Convert FOV to radians and divide by 2 for calculations

        List<Location> locations = new LinkedList<>();
        List<Location> allLocations = getSphere(head, radius, false); // Get all locations in the sphere

        for (Location loc : allLocations) {
            Vector toLocation = loc.toVector().subtract(head.toVector()).normalize(); // Vector from player to location
            double angle = playerDirection.angle(toLocation); // Angle between the player's direction and the location

            if (angle <= halfFov) { // If the angle is within the FOV
                locations.add(loc);
            }
        }

        return locations;
    }

    public enum Color {
        RED,
        ORANGE,
        YELLOW,
        LIME,
        GREEN,
        LIGHT_BLUE,
        CYAN,
        BLUE,
        PURPLE,
        MAGENTA,
        PINK,
        BLACK,
        GRAY,
        LIGHT_GRAY,
        WHITE,
        BROWN
    }

    public enum Material {
        GLASS("%color%_STAINED_GLASS"),
//        GLASS_PANE("%color%_STAINED_GLASS_PANE"),
        BARRIER("BARRIER")
        ;

        private final String materialId;

        Material(String materialId) {
            this.materialId = materialId;
        }
    }
}
