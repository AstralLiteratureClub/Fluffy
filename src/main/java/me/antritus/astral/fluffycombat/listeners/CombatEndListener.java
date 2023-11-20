package me.antritus.astral.fluffycombat.listeners;

import fr.skytasul.glowingentities.GlowingBlocks;
import fr.skytasul.glowingentities.GlowingEntities;
import me.antritus.astral.fluffycombat.FluffyCombat;
import me.antritus.astral.fluffycombat.api.BlockCombatUser;
import me.antritus.astral.fluffycombat.api.CombatTag;
import me.antritus.astral.fluffycombat.api.CombatUser;
import me.antritus.astral.fluffycombat.api.events.CombatEndEvent;
import me.antritus.astral.fluffycombat.configs.CombatConfig;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class CombatEndListener implements Listener {
	private final FluffyCombat fluffy;

	public CombatEndListener(FluffyCombat fluffy) {
		this.fluffy = fluffy;
	}

	@EventHandler
	public void onCombatEnd(CombatEndEvent event){
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
