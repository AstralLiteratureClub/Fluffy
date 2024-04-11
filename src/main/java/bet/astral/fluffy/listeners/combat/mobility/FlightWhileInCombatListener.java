package bet.astral.fluffy.listeners.combat.mobility;

import bet.astral.fluffy.FluffyCombat;
import bet.astral.fluffy.api.CombatUser;
import bet.astral.fluffy.events.player.PlayerCombatFullEndEvent;
import bet.astral.fluffy.configs.CombatConfig;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerToggleFlightEvent;
import org.bukkit.scheduler.BukkitTask;

public class FlightWhileInCombatListener implements Listener {
	private final FluffyCombat fluffy;

	public FlightWhileInCombatListener(FluffyCombat fluffy) {
		this.fluffy = fluffy;
	}

	@EventHandler
	private void onCombatEnd(PlayerCombatFullEndEvent event){
		OfflinePlayer player = event.player();
		if (player instanceof Player oPlayer){
			CombatUser user = fluffy.getUserManager().getUser(oPlayer);
			BukkitTask task = user.getTaskFlightTimer();
			if (task != null && !task.isCancelled()){
				task.cancel();
				user.setFlightTimer(fluffy.getCombatConfig().getFlightTicks());
				user.setTaskFlightTimer(null);
			}
		}
	}

	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	private void onFlightToggle(PlayerToggleFlightEvent event){
		if (fluffy.getCombatConfig().getFlightMode()== CombatConfig.FlightMode.ALLOW){
			return;
		}
		Player player = event.getPlayer();
		if (fluffy.getCombatManager().hasTags(player)){
			boolean toggleOn = player.isFlying();
			if (!toggleOn) {
				CombatUser user = fluffy.getUserManager().getUser(player);
				if (user.getTaskFlightTimer() != null && !user.getTaskFlightTimer().isCancelled()){
					user.getTaskFlightTimer().cancel();
					user.setTaskFlightTimer(null);
				}
			} else {
				if (fluffy.getCombatConfig().getFlightMode() == CombatConfig.FlightMode.DENY) {
					event.setCancelled(true);
					return;
				}

			}
			CombatUser user = fluffy.getUserManager().getUser(player);
			if (user.getTaskFlightTimer() != null && !user.getTaskFlightTimer().isCancelled()){
				user.getTaskFlightTimer().cancel();
				user.setTaskFlightTimer(null);
			}
			user.setTaskFlightTimer(fluffy.getServer().getScheduler().runTaskTimer(fluffy,
					()->{
						if (!player.isOnline()){
							user.getTaskFlightTimer().cancel();
							user.setTaskFlightTimer(null);
							user.setFlightTimer(fluffy.getCombatConfig().getFlightTicks());
							return;
						}
						user.setFlightTimer(fluffy.getCombatConfig().getFlightTicks()-1);
						if (user.getFlightTimer()<=0){
							user.getTaskFlightTimer().cancel();
							user.setTaskFlightTimer(null);
							user.getPlayer().getPlayer().setAllowFlight(false);
						}
					}, 1, 1));
		}
	}
}
