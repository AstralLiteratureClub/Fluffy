package bet.astral.fluffy.hooks.worldguard;

import bet.astral.fluffy.FluffyCombat;
import bet.astral.fluffy.hooks.Hook;
import bet.astral.fluffy.hooks.HookState;
import bet.astral.fluffy.hooks.worldguard.messenger.WGReceiverConverter;
import bet.astral.fluffy.hooks.worldguard.session.FluffyWGHandlers;
import bet.astral.fluffy.manager.RegionManager;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class WorldGuardHook implements Hook {
    private final WorldGuardPlugin worldGuardPlugin;
    @Getter
    private final WorldGuard worldGuard;
    private final Class<?> hookClass;
    private final FluffyCombat fluffy;
    private final HookState hookState;

    public WorldGuardHook(@NotNull FluffyCombat fluffyCombat, @Nullable WorldGuardPlugin hook, @Nullable Class<?> clazz, @NotNull HookState state) {
        this.fluffy = fluffyCombat;
        this.hookState = state;
        this.worldGuardPlugin = hook;
        this.worldGuard = WorldGuard.getInstance();
        this.hookClass = clazz;
        onLoad();
    }

    public void onLoad(){
        if (worldGuard != null) {
            FluffyWGFlags.register(this);

            RegionManager regionManager = new WGRegionManager(this);
            fluffy.setRegionManager(regionManager);
        }
    }

    public void onEnable(){
        worldGuard.getPlatform().getSessionManager()
                .registerHandler(FluffyWGHandlers.COMBAT_ENTRY_HANDLER_FACTORY, null);

        fluffy.getMessenger().registerReceiverConverter(new WGReceiverConverter());
    }


    @Override
    public FluffyCombat main() {
        return fluffy;
    }

    @Override
    public WorldGuardPlugin hookPlugin() {
        return worldGuardPlugin;
    }

    @Override
    public Class<?> hookPluginClass() {
        return hookClass;
    }

    @Override
    public HookState state() {
        return hookState;
    }
}