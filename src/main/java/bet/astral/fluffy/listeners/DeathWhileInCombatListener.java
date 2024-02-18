package bet.astral.fluffy.listeners;

import bet.astral.fluffy.FluffyCombat;
import bet.astral.fluffy.api.CombatTag;
import bet.astral.fluffy.manager.CombatManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

import java.util.List;

public class DeathWhileInCombatListener implements Listener {
	private final FluffyCombat fluffy;
	public DeathWhileInCombatListener(FluffyCombat fluffy){
		this.fluffy = fluffy;
	}

	public FluffyCombat fluffy() {
		return fluffy;
	}


	@EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
	public void onDeath(PlayerDeathEvent event){
		CombatManager combatManager = fluffy.getCombatManager();
		Player player = event.getEntity();
		if (combatManager.hasTags(player)){
			List<CombatTag> tags = combatManager.getTags(player);
				tags.forEach(tag->{
					if (tag.getAttacker().getUniqueId().equals(player.getUniqueId())){
						tag.setDeadAttacker(true);
					} else {
						tag.setDeadVictim(true);
					}
			});
		}
	}
}