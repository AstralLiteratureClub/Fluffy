package bet.astral.fluffy.hooks.worldguard;

import bet.astral.fluffy.manager.RegionManager;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.world.World;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.internal.platform.WorldGuardPlatform;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class WGRegionManager extends RegionManager {
    private final WorldGuardHook worldGuardHook;

    public WGRegionManager(WorldGuardHook worldGuardHook) {
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
}
