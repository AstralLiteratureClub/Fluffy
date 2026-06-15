package bet.astral.fluffy.hooks.gsit;

import bet.astral.fluffy.FluffyCombat;
import bet.astral.fluffy.hooks.Hook;
import bet.astral.fluffy.hooks.HookState;
import dev.geco.gsit.GSitMain;
import dev.geco.gsit.api.GSitAPI;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class GSitHook implements Hook, Listener {
    private final FluffyCombat plugin;
    private final GSitMain gSitMain;
    private final Class<GSitMain> gSitMainClass;
    private final HookState hookState;

    public GSitHook(@NotNull FluffyCombat fluffyCombat, @Nullable GSitMain papi, @Nullable Class<?> clazz, @NotNull HookState state) {
        this.plugin = fluffyCombat;
        this.hookState = state;
        this.gSitMain = papi;
        this.gSitMainClass = (Class<GSitMain>) clazz;
    }

    @Override
    public FluffyCombat main() {
        return plugin;
    }

    @Override
    public GSitMain hookPlugin() {
        return gSitMain;
    }

    @Override
    public Class<?> hookPluginClass() {
        return gSitMainClass;
    }

    @Override
    public HookState state() {
        return hookState;
    }

    @Override
    public void onLoad() {

    }

    @Override
    public void onEnable() {

    }

    public void handleCombatUpdate(@NotNull OfflinePlayer player) {
        if (player.isOnline()) {
            GSitAPI.setPlayerCanUsePlayerSit(player.getUniqueId(), false);
        }
        if (!hasCombat(player)) {
            GSitAPI.setPlayerCanUsePlayerSit(player.getUniqueId(), true);
        }
    }

    @Override
    public void onCombatBegin(Player player, OfflinePlayer player2) {
        handleCombatUpdate(player);
        handleCombatUpdate(player2);

    }

    @Override
    public void onCombatEnd(Player player, OfflinePlayer player2) {
        handleCombatUpdate(player);
        handleCombatUpdate(player2);
    }

    @Override
    public void onCombatUpdate(Player player, OfflinePlayer player2) {
        handleCombatUpdate(player);
        handleCombatUpdate(player2);
    }

    @Override
    public void onCombatBegin(Player player, Block player2) {
        handleCombatUpdate(player);
    }

    @Override
    public void onCombatEnd(Player player, Block player2) {
        handleCombatUpdate(player);
    }

    @Override
    public void onCombatUpdate(Player player, Block player2) {
        handleCombatUpdate(player);
    }
}
