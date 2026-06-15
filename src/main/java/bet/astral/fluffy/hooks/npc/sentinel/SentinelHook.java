package bet.astral.fluffy.hooks.npc.sentinel;

import bet.astral.fluffy.FluffyCombat;
import bet.astral.fluffy.hooks.Hook;
import bet.astral.fluffy.hooks.HookState;
import lombok.Getter;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mcmonkey.sentinel.SentinelIntegration;
import org.mcmonkey.sentinel.SentinelPlugin;

public class SentinelHook extends SentinelIntegration implements Hook {
    private final SentinelPlugin sentinel;
    @Getter
    private final Class<?> hookClass;
    private final FluffyCombat fluffy;
    private final HookState hookState;

    public SentinelHook(@NotNull FluffyCombat fluffyCombat, @Nullable SentinelPlugin hook, @Nullable Class<?> clazz, @NotNull HookState state) {
        this.fluffy = fluffyCombat;
        this.hookState = state;
        this.sentinel = hook;
        this.hookClass = clazz;
    }

    @Override
    public void onLoad() {
    }

    @Override
    public void onEnable() {
        main().setNpcManager(new SentinelNPCManager(this));
    }

    @Override
    public void onCombatBegin(Player player, OfflinePlayer player2) {

    }

    @Override
    public void onCombatEnd(Player player, OfflinePlayer player2) {

    }

    @Override
    public void onCombatUpdate(Player player, OfflinePlayer player2) {

    }

    @Override
    public void onCombatBegin(Player player, Block player2) {

    }

    @Override
    public void onCombatEnd(Player player, Block player2) {

    }

    @Override
    public void onCombatUpdate(Player player, Block player2) {

    }


    @Override
    public FluffyCombat main() {
        return fluffy;
    }

    @Override
    public SentinelPlugin hookPlugin() {
        return sentinel;
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