package bet.astral.fluffy.hooks.worldguard;

import bet.astral.fluffy.FluffyCombat;
import bet.astral.fluffy.manager.RegionManager;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.world.World;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.internal.platform.WorldGuardPlatform;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class WGRegionManager extends RegionManager {
    private final WorldGuardHook worldGuardHook;

    public WGRegionManager(FluffyCombat fluffyCombat, WorldGuardHook worldGuardHook) {
        super(fluffyCombat);
        this.worldGuardHook = worldGuardHook;
    }

    @Override
    public boolean canEnterCombat(Player victim, @NotNull Location location) {
        World world = BukkitAdapter.adapt(location.getWorld());
        WorldGuardPlatform platform = worldGuardHook.getWorldGuard().getPlatform();

        com.sk89q.worldguard.protection.managers.RegionManager regionManager =
                platform.getRegionContainer().get(world);

        if (regionManager == null){
            return true;
        }

        BlockVector3 vector3 = new BlockVector3(
                location.getBlockX(),
                location.getBlockY(),
                location.getBlockZ()
        );
        ApplicableRegionSet regions = regionManager.getApplicableRegions(vector3);


        LocalPlayer localPlayer = worldGuardHook.hookPlugin().wrapPlayer(victim);
        return regions.testState(localPlayer, FluffyWGFlags.ALLOW_COMBAT_TAG);
    }

    @Override
    public boolean shouldRenderRegionWalls(Player player, Location location) {
        if (location.getWorld() != player.getWorld()){
            return false;
        }
        RegionContainer regionContainer = worldGuardHook.getWorldGuard().getPlatform().getRegionContainer();
        World world = BukkitAdapter.adapt(location.getWorld());
        com.sk89q.worldguard.protection.managers.RegionManager regionManager = regionContainer.get(world);
        if (regionManager == null){
            return false;
        }

        ApplicableRegionSet regions = regionManager.getApplicableRegions(BukkitAdapter.asBlockVector(location));
        boolean render = regions.testState(null, FluffyWGFlags.ZONE_BARRIER_RENDER) &&
                !regions.testState(worldGuardHook.hookPlugin().wrapPlayer(player), FluffyWGFlags.ENTER_ZONE_IN_COMBAT);
        boolean combat = worldGuardHook.main().getCombatManager().hasTags(player);

        return render && combat;
    }

    @Override
    public Color getBarrierColor(Location location) {
        World world = BukkitAdapter.adapt(location.getWorld());
        RegionContainer container = worldGuardHook.getWorldGuard().getPlatform().getRegionContainer();
        com.sk89q.worldguard.protection.managers.RegionManager regionManager = container.get(world);
        if (regionManager == null){
            return Color.RED;
        }

        ApplicableRegionSet regions = regionManager.getApplicableRegions(BukkitAdapter.asBlockVector(location));
        if (regions.getRegions().isEmpty()){
            return Color.RED;
        }

        Color color = regions.queryValue(null, FluffyWGFlags.ZONE_BARRIER_COLOR);
        if (color == null) {
            return Color.RED;
        }

        return color;
    }

    @Override
    public Material getBarrierMaterial(Location location) {
        World world = BukkitAdapter.adapt(location.getWorld());
        RegionContainer container = worldGuardHook.getWorldGuard().getPlatform().getRegionContainer();
        com.sk89q.worldguard.protection.managers.RegionManager regionManager = container.get(world);
        if (regionManager == null){
            return Material.GLASS;
        }

        ApplicableRegionSet regions = regionManager.getApplicableRegions(BukkitAdapter.asBlockVector(location));
        if (regions.getRegions().isEmpty()){
            return Material.GLASS;
        }

        Material material = regions.queryValue(null, FluffyWGFlags.ZONE_BARRIER_MATERIAL);
        if (material == null) {
            return Material.GLASS;
        }

        return material;
    }

    @Override
    public void checkAndReplaceBarrier(@NotNull Player player) {
        org.bukkit.World world = player.getWorld();
        RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
        com.sk89q.worldguard.protection.managers.RegionManager
                regions = container.get(BukkitAdapter.adapt(world));
        if (regions == null) return;

        List<Location> locations = getSphereVisible(player, 15);
        Set<Location> actuallyUsed = new HashSet<>();

        for (Location location : locations) {
            ApplicableRegionSet regionSet = regions.getApplicableRegions(BukkitAdapter.asBlockVector(location));
            if (!regionSet.getRegions().isEmpty()) {
                for (ProtectedRegion region : regionSet) {
                    if (isRegionBorder(location, region, regions)) {
                        if (shouldRenderRegionWalls(player, location)){
                            actuallyUsed.add(location);
                            handleBlockPlacement(location, player);
                        }
                    }
                }
            }
        }

        clearOldLocations(player, actuallyUsed);
    }

    public boolean isRegionBorder(Location location, ProtectedRegion region, com.sk89q.worldguard.protection.managers.RegionManager regionManager) {
        // Create the current BlockVector3 for the location
        BlockVector3 currentVector = BlockVector3.at(location.getBlockX(), location.getBlockY(), location.getBlockZ());

        // Offsets to check all surrounding blocks on the same Y level
        int[][] offsets = {
                {-1, 0}, // West
                {1, 0},  // East
                {0, -1}, // South
                {0, 1},  // North
        };

        // Check each offset position to see if it lies outside the region
        for (int[] offset : offsets) {
            BlockVector3 offsetVector = currentVector.add(offset[0], 0, offset[1]);
            ApplicableRegionSet regions = regionManager.getApplicableRegions(offsetVector);

            // If the offset position is not part of the region, it's a border
            if (!regions.getRegions().contains(region)) {
                return true;
            }
        }

        // If all surrounding positions are part of the region, it's not a border
        return false;
    }

}
