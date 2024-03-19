package bet.astral.fluffy.listeners;

import fr.skytasul.glowingentities.GlowingBlocks;
import fr.skytasul.glowingentities.GlowingEntities;
import bet.astral.fluffy.FluffyCombat;
import bet.astral.fluffy.api.BlockCombatUser;
import bet.astral.fluffy.api.CombatTag;
import bet.astral.fluffy.api.CombatUser;
import bet.astral.fluffy.events.CombatEndEvent;
import bet.astral.fluffy.configs.CombatConfig;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class PlayerGlowDisableListener implements Listener {
	private final FluffyCombat fluffy;

	public PlayerGlowDisableListener(FluffyCombat fluffy) {
		this.fluffy = fluffy;
	}

	@EventHandler
	private void onCombatEnd(CombatEndEvent event){
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
