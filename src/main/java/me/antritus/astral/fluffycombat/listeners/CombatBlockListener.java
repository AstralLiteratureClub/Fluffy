package me.antritus.astral.fluffycombat.listeners;

import fr.skytasul.glowingentities.GlowingBlocks;
import me.antritus.astral.fluffycombat.FluffyCombat;
import me.antritus.astral.fluffycombat.api.BlockCombatUser;
import me.antritus.astral.fluffycombat.manager.BlockUserManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

public class CombatBlockListener implements Listener {
	private final FluffyCombat fluffy;

	public CombatBlockListener(FluffyCombat fluffyCombat) {
		this.fluffy = fluffyCombat;
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onBlockBreak(BlockBreakEvent event){
		BlockUserManager blockUserManager = fluffy.getBlockUserManager();
		BlockCombatUser blockCombatUser = blockUserManager.getUser(event.getBlock().getLocation());
		if (blockCombatUser == null){
			return;
		}
		blockCombatUser.setAlive(event.isCancelled());
		if (fluffy.getCombatConfig().isCombatGlow()) {
			GlowingBlocks glowingBlocks = fluffy.getGlowingBlocks();
			try {
				glowingBlocks.unsetGlowing(event.getBlock(), event.getPlayer());
			} catch (ReflectiveOperationException e) {
				throw new RuntimeException(e);
			}
		}
	}
}
