package bet.astral.fluffy.listeners.region;

import bet.astral.fluffy.FluffyCombat;
import bet.astral.fluffy.events.player.PlayerCombatFullEndEvent;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.Set;

public class RegionWallListener implements Listener {
    private final FluffyCombat fluffy;

    public RegionWallListener(FluffyCombat fluffy) {
        this.fluffy = fluffy;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        Player player = e.getPlayer();
        player.getScheduler().runAtFixedRate(fluffy, t->{
            if (fluffy.getCombatManager().hasTags(player)) {
                fluffy.getRegionManager().checkAndReplaceBarrier(player);
            }
        }, null, 5, 10);
    }

    @EventHandler
    public void onEnd(PlayerCombatFullEndEvent event) {
        OfflinePlayer oPlayer = event.player();
        if (oPlayer instanceof Player player){
            fluffy.getRegionManager().clearOldLocations(player, Set.of());
        }
    }
}
