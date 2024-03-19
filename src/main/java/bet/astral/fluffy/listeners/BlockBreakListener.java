package bet.astral.fluffy.listeners;

import fr.skytasul.glowingentities.GlowingBlocks;
import bet.astral.fluffy.FluffyCombat;
import bet.astral.fluffy.api.BlockCombatUser;
import bet.astral.fluffy.manager.BlockUserManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

public class BlockBreakListener implements Listener {
	private final FluffyCombat fluffy;

	public BlockBreakListener(FluffyCombat fluffyCombat) {
		this.fluffy = fluffyCombat;
	}

	public void onBlockExplodeEvent(){

	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	private void onBlockBreak(BlockBreakEvent event){
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
