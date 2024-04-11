package bet.astral.fluffy.listeners.combat.end;

import bet.astral.fluffy.FluffyCombat;
import bet.astral.fluffy.api.BlockCombatUser;
import bet.astral.fluffy.api.CombatTag;
import bet.astral.fluffy.api.CombatUser;
import bet.astral.fluffy.configs.CombatConfig;
import bet.astral.fluffy.events.CombatTagEndEvent;
import bet.astral.fluffy.manager.CombatManager;
import fr.skytasul.glowingentities.GlowingBlocks;
import fr.skytasul.glowingentities.GlowingEntities;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

import java.util.List;

public class CombatEndListener implements Listener {
	private final FluffyCombat fluffy;
	public CombatEndListener(FluffyCombat fluffy){
		this.fluffy = fluffy;
	}

	public FluffyCombat fluffy() {
		return fluffy;
	}


	@EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
	private void onDeath(PlayerDeathEvent event){
		CombatManager combatManager = fluffy.getCombatManager();
		Player player = event.getEntity();
		if (combatManager.hasTags(player)){
			List<CombatTag> tags = combatManager.getTags(player);
				tags.forEach(tag->{
					// Set the tag ticks to -1 as it's instantly removed from the player
					tag.setAttackerTicksLeft(-1);
					tag.setVictimTicksLeft(-1);
					if (tag.getAttacker().getUniqueId().equals(player.getUniqueId())){
						tag.setDeadAttacker(true);
					} else {
						tag.setDeadVictim(true);
					}
			});
		}
	}

	@EventHandler
	private void onCombatEnd(CombatTagEndEvent event){
		CombatTag tag = event.getCombatTag();
		CombatUser victim = tag.getVictim();
		CombatUser attacker = tag.getAttacker();
		if (attacker instanceof BlockCombatUser blockAttacker){
			if (victim.getPlayer() instanceof Player player){
				GlowingBlocks glowingBlocks = fluffy.getGlowingBlocks();
				try {
					glowingBlocks.unsetGlowing(blockAttacker.getLocation(), player);
				} catch (ReflectiveOperationException e) {
					throw new RuntimeException(e);
				}
			}
			return;
		}
		CombatConfig config = fluffy.getCombatConfig();
		// Wasting resources on the glowing removal if it's not even used
		// Might be changed in runtime by the operators,
		// but the configuration should be tested in a private server and not in their public server.
		if (config.isCombatGlow()) {
			OfflinePlayer attackerOP = attacker.getPlayer();
			OfflinePlayer victimOP = victim.getPlayer();
			if (victimOP instanceof Player victimP && attackerOP instanceof Player attackerP) {
				GlowingEntities glowingEntities = fluffy.getGlowingEntities();
				try {
					glowingEntities.unsetGlowing(attackerP, victimP);
					glowingEntities.unsetGlowing(victimP, attackerP);
				} catch (ReflectiveOperationException e) {
					throw new RuntimeException(e);
				}
			}
		}
	}
}