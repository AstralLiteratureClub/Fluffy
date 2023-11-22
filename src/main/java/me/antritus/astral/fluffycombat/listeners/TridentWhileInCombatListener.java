package me.antritus.astral.fluffycombat.listeners;

import me.antritus.astral.fluffycombat.FluffyCombat;
import me.antritus.astral.fluffycombat.manager.CombatManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerRiptideEvent;
import org.bukkit.util.Vector;

public class TridentWhileInCombatListener implements Listener {
	private final FluffyCombat fluffy;
	public TridentWhileInCombatListener(FluffyCombat fluffy){
		this.fluffy = fluffy;
	}

	@EventHandler
	public void onPlayerRiptide(PlayerRiptideEvent e) {
		CombatManager combatManager =  fluffy.getCombatManager();
		Player player = e.getPlayer();
		if (fluffy.getCombatConfig().isTridentBoostAllowed()){
			return;
		}
		if (combatManager.hasTags(e.getPlayer())) {
			Location location = e.getPlayer().getLocation();
			Vector vel = player.getVelocity();
			Bukkit.getScheduler().scheduleSyncDelayedTask(fluffy, () -> {
				player.setVelocity(vel);
				player.teleportAsync(location);
			}, 1L);
			if (fluffy.getCombatConfig().isTridentBoostMessage()){
				fluffy.getMessageManager().message(player, "trident.riptide");
			}
		}
	}
}
