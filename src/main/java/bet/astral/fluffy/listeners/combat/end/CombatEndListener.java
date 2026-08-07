package bet.astral.fluffy.listeners.combat.end;

import bet.astral.aura.api.Aura;
import bet.astral.fluffy.FluffyCombat;
import bet.astral.fluffy.api.BlockCombatUser;
import bet.astral.fluffy.api.CombatTag;
import bet.astral.fluffy.api.CombatUser;
import bet.astral.fluffy.configs.CombatConfig;
import bet.astral.fluffy.events.CombatTagEndEvent;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public class CombatEndListener implements Listener {
	private final FluffyCombat fluffy;
	public CombatEndListener(FluffyCombat fluffy){
		this.fluffy = fluffy;
	}

	public FluffyCombat fluffy() {
		return fluffy;
	}
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	private void onCombatEnd(CombatTagEndEvent event){
		CombatTag tag = event.getCombatTag();
		CombatUser victim = tag.getVictim();
		CombatUser attacker = tag.getAttacker();
		if (attacker instanceof BlockCombatUser blockAttacker){
			if (victim.getPlayer() instanceof Player player) {
				/*
				try {
					fluffy.getShine().removeGlowing(ShineReceiver.of(blockAttacker.getBlock()), player);
				} catch (ReflectiveOperationException e) {
					throw new RuntimeException(e);
				}

				 */
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
				Aura.get().unsetGlowing(attackerP, victimP);
				Aura.get().unsetGlowing(victimP, attackerP);
			}
		}
	}
}