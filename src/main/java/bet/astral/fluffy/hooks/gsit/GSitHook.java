package bet.astral.fluffy.hooks.gsit;

import bet.astral.fluffy.FluffyCombat;
import bet.astral.fluffy.hooks.Hook;
import bet.astral.fluffy.hooks.HookState;
import dev.geco.gsit.GSitMain;
import dev.geco.gsit.api.GSitAPI;
import dev.geco.gsit.api.event.PrePlayerCrawlEvent;
import dev.geco.gsit.api.event.PrePlayerPoseEvent;
import dev.geco.gsit.model.StopReason;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
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
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
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

    public void handleCombatUpdate(OfflinePlayer player) {
        if (player == null) {
            return;
        }
        if (player.isOnline() && hasCombat(player)) {
            Player oPlayer = player.getPlayer();

            GSitAPI.getInstance().getToggleService().setEntityCanUseSit(player.getUniqueId(), false);
            GSitAPI.getInstance().getToggleService().setPlayerCanUsePlayerSit(player.getUniqueId(), false);
            GSitAPI.getInstance().getToggleService().setPlayerCanUseCrawl(player.getUniqueId(), false);

            if (GSitAPI.getPoseByPlayer(oPlayer) != null) {
                GSitAPI.removePose(GSitAPI.getPoseByPlayer(oPlayer), StopReason.PLUGIN);
            }
            if (GSitAPI.isEntitySitting((Player) player)) {
                GSitAPI.stopPlayerSit((Player) player, StopReason.PLUGIN);
            }
            if (GSitAPI.isPlayerCrawling((Player) player)) {
                GSitAPI.stopCrawl(GSitAPI.getCrawlByPlayer((Player) player), StopReason.PLUGIN);
            }
            if (GSitAPI.getSeatByEntity((Player) player) != null){
                GSitAPI.removeSeat(GSitAPI.getSeatByEntity((Player) player), StopReason.PLUGIN);
            }
        }
        if (!hasCombat(player)) {
            GSitAPI.getInstance().getToggleService().setEntityCanUseSit(player.getUniqueId(), true);
            GSitAPI.getInstance().getToggleService().setPlayerCanUsePlayerSit(player.getUniqueId(), true);
            GSitAPI.getInstance().getToggleService().setPlayerCanUseCrawl(player.getUniqueId(), true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerPose(PrePlayerPoseEvent event) {
        if (hasCombat(event.getPlayer())) {
            handleCombatUpdate(event.getPlayer());
            if (hasCombat(event.getPlayer()))
                event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onEntityPoseChange(dev.geco.gsit.api.event.PreEntitySitEvent event) {
        if (event.getEntity() instanceof Player player) {
            handleCombatUpdate(player);
            if (hasCombat(player))
                event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerCrawl(PrePlayerCrawlEvent event) {
        Player player = event.getPlayer();
        handleCombatUpdate(player);
        if (hasCombat(player)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPrePlayerPose(PrePlayerPoseEvent event) {
        handleCombatUpdate(event.getPlayer());
        if (hasCombat(event.getPlayer())) {
            event.setCancelled(true);
        }
    }




}
