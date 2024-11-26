package bet.astral.fluffy.hooks.worldguard;

import bet.astral.fluffy.manager.RegionManager;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.flags.EnumFlag;
import com.sk89q.worldguard.protection.flags.Flag;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.flags.registry.FlagConflictException;
import com.sk89q.worldguard.protection.flags.registry.FlagRegistry;
import org.jetbrains.annotations.NotNull;

public class FluffyWGFlags {
    public static StateFlag ALLOW_COMBAT_TAG;
    public static StateFlag ENTER_ZONE_IN_COMBAT;
    public static StateFlag EXTEND_COMBAT_IF_ENTER;
    public static StateFlag ZONE_BARRIER_RENDER;
    public static EnumFlag<RegionManager.Color> ZONE_BARRIER_COLOR;
    public static EnumFlag<RegionManager.Material> ZONE_BARRIER_MATERIAL;

    public static void register(@NotNull WorldGuardHook worldGuardHook) {
        WorldGuard worldGuard = worldGuardHook.getWorldGuard();
        FlagRegistry flags = worldGuard.getFlagRegistry();

        ALLOW_COMBAT_TAG = create(flags, new StateFlag("flf-allow-combat-tag", true));
        ENTER_ZONE_IN_COMBAT = create(flags, new StateFlag("flf-enter-region-in-combat", true));
        EXTEND_COMBAT_IF_ENTER = create(flags, new StateFlag("flf-extend-combat-on-enter", true));
        ZONE_BARRIER_RENDER = create(flags, new StateFlag("flf-zone-barrier", true));
        ZONE_BARRIER_COLOR = create(flags, new EnumFlag<>("flf-zone-barrier-color", RegionManager.Color.class));
        ZONE_BARRIER_MATERIAL = create(flags, new EnumFlag<>("flf-zone-barrier-material", RegionManager.Material.class));
    }

    private static <T extends Flag<?>> T create(FlagRegistry flagRegistry, T flag){
        try {
            flagRegistry.register(flag);
            return flag;
        } catch (FlagConflictException e){
            Flag<?> flagFound = flagRegistry.get(flag.getName());
            if (flag.getClass().isInstance(flagFound)){
                //noinspection unchecked
                return (T) flagFound;
            }
        }
        return null;
    }
}
