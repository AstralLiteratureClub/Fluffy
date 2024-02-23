package bet.astral.fluffy.listeners.hitdetection;

import bet.astral.fluffy.FluffyCombat;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.CauldronLevelChangeEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;


public class LavaCauldronDetection implements Listener {
	private final FluffyCombat fluffy;

	public LavaCauldronDetection(@NotNull FluffyCombat fluffyCombat) {
		this.fluffy = fluffyCombat;
	}

	@EventHandler
	private void onCauldronFill(CauldronLevelChangeEvent event) {
		if (event.getReason() == CauldronLevelChangeEvent.ChangeReason.BUCKET_FILL) {
			Entity entity = event.getEntity();
			if (entity == null) {
				return;
			}
			if (!(entity instanceof Player player)) {
				return;
			}
			ItemStack itemStack = player.getInventory().getItemInMainHand();
			if (itemStack.getType().isAir()) {
				itemStack = player.getInventory().getItemInOffHand();
			}
			if (itemStack.getType().isAir()) {
				player.sendMessage("Filled without an item in hand!");
				return;
			}
			if (itemStack.getType() != Material.LAVA_BUCKET) {
				player.sendMessage("Not lava!");
				return;
			}
			Block block = event.getBlock();
			FluffyCombat.setBlockOwner(player, block);
		} else if (event.getReason() == CauldronLevelChangeEvent.ChangeReason.BUCKET_EMPTY){
			Block block = event.getBlock();
			FluffyCombat.clearBlockData(block.getChunk(), block.getLocation());
		}
	}

	@EventHandler
	private void onBlockBreak(BlockBreakEvent event ){
		if (event.getBlock().getType()==Material.LAVA_CAULDRON){
			Block block = event.getBlock();
			FluffyCombat.clearBlockData(block.getChunk(), block.getLocation());
		}
	}
}




















