package bet.astral.fluffy.listeners.combat;

import bet.astral.shine.receiver.ShineReceiver;
import bet.astral.fluffy.FluffyCombat;
import bet.astral.fluffy.api.BlockCombatUser;
import bet.astral.fluffy.manager.BlockUserManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

public class BreakCombatTaggedBlockListener implements Listener {
	private final FluffyCombat fluffy;

	public BreakCombatTaggedBlockListener(FluffyCombat fluffyCombat) {
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
			try {
				fluffy.getShine().removeGlowing(ShineReceiver.of(event.getBlock()), event.getPlayer());
			} catch (ReflectiveOperationException e) {
				throw new RuntimeException(e);
			}
		}
	}
}
