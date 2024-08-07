package bet.astral.fluffy.listeners.combat.mobility;

import bet.astral.fluffy.FluffyCombat;
import bet.astral.fluffy.manager.CombatManager;
import bet.astral.fluffy.messenger.Translations;
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
	private void onPlayerRiptide(PlayerRiptideEvent e) {
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
			fluffy.getMessenger().message(player, Translations.COMBAT_CANNOT_USE_TRIDENT_RIPTIDE);
		}
	}
}
