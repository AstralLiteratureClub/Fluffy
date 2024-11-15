package bet.astral.fluffy.hooks.worldguard;

import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.flags.Flag;
import com.sk89q.worldguard.protection.flags.IntegerFlag;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.flags.registry.FlagConflictException;
import com.sk89q.worldguard.protection.flags.registry.FlagRegistry;
import org.jetbrains.annotations.NotNull;

public class FluffyWGFlags {
    public static StateFlag ALLOW_COMBAT_TAG;
    public static StateFlag ENTER_ZONE_IN_COMBAT;
    public static IntegerFlag EXTEND_COMBAT_IF_ENTER;

    public static void register(@NotNull WorldGuardHook worldGuardHook) {
        WorldGuard worldGuard = worldGuardHook.getWorldGuard();
        FlagRegistry flags = worldGuard.getFlagRegistry();

        ALLOW_COMBAT_TAG = create(flags, new StateFlag("flf-allow-combat-tag", true));
        ENTER_ZONE_IN_COMBAT = create(flags, new StateFlag("flf-enter-region-in-combat", true));
        EXTEND_COMBAT_IF_ENTER = create(flags, new IntegerFlag("flf-extend-combat-on-enter"));
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
