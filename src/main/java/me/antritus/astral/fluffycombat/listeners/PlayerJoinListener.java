package me.antritus.astral.fluffycombat.listeners;

import me.antritus.astral.fluffycombat.FluffyCombat;
import me.antritus.astral.fluffycombat.api.CombatUser;
import me.antritus.astral.fluffycombat.configs.CombatConfig;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

public class PlayerJoinListener implements Listener {
	private final FluffyCombat fluffy;

	public PlayerJoinListener(FluffyCombat combat) {
		this.fluffy = combat;
	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void onJoin(@NotNull PlayerJoinEvent event){
		fluffy.getUserManager().onJoin(event.getPlayer());
		CombatUser user = fluffy.getUserManager().getUser(event.getPlayer());
		CombatConfig config = fluffy.getCombatConfig();
		int rejoinTicks = config.getCombatLogRejoinTicks();
		if (user != null){
			if (user.getTaskRejoinTimer() != null){
				user.getTaskRejoinTimer().cancel();
				user.setTaskRejoinTimer(null);
			}
			user.setRejoinTimer(rejoinTicks);
			user.setTaskRejoinTimer(new BukkitRunnable() {
				@Override
				public void run() {
					int ticks = user.getRejoinTimer();
					if (ticks>=0){
						ticks--;
						user.setRejoinTimer(ticks);
					} else {
						user.setRejoinTimer(-1);
						user.setTaskRejoinTimer(null);
						cancel();
					}
				}
			}.runTaskTimerAsynchronously(fluffy, 5, 1));
		}
	}
}
