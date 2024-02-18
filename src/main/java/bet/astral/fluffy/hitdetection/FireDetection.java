package bet.astral.fluffy.hitdetection;

import com.destroystokyo.paper.event.block.BlockDestroyEvent;
import lombok.Getter;
import bet.astral.fluffy.FluffyCombat;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.BlockSpreadEvent;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

@Getter
public class FireDetection implements Listener {
	private final FluffyCombat fluffy;
	public FireDetection(FluffyCombat fluffyCombat){
		this.fluffy = fluffyCombat;
	}
	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	private void onFirePlace(BlockPlaceEvent event) {
		Block block = event.getBlock();
		if (block.getType() != Material.FIRE || block.getType() != Material.SOUL_FIRE) {
			return;
		}
		System.out.println("Placed fire!");
		Player player = event.getPlayer();
		FluffyCombat.setBlockOwner(player, block);
	}
	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	private void onFireExtinguishBDE(BlockDestroyEvent event){
		Block block = event.getBlock();
		if (block.getType() != Material.FIRE || block.getType() != Material.SOUL_FIRE) {
			return;
		}
		if (event.getNewState().getMaterial() != Material.FIRE || event.getNewState().getMaterial() != Material.SOUL_FIRE){
			FluffyCombat.clearBlockData(block.getChunk(), block.getLocation());
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	private void onFireExtinguishBRE(BlockBreakEvent event){
		Block block = event.getBlock();
		if (block.getType() != Material.FIRE || block.getType() != Material.SOUL_FIRE) {
			return;
		}
		FluffyCombat.clearBlockData(block.getChunk(), block.getLocation());
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	private void onFireSpreadEvent(BlockSpreadEvent event){
		Block block = event.getBlock();
		if (block.getType() != Material.FIRE || block.getType() != Material.SOUL_FIRE) {
			return;
		}
		if (event.getSource().getType() != Material.FIRE || event.getSource().getType() != Material.SOUL_FIRE) {
			return;
		}
		@Nullable
		UUID owner = FluffyCombat.getBlockOwner(event.getSource());
		if (owner == null){
			return;
		}
		OfflinePlayer player = Bukkit.getOfflinePlayer(owner);
		FluffyCombat.setBlockOwner(player, block);
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	private void onFireIgnite(BlockIgniteEvent event){
		if (event.getCause()== BlockIgniteEvent.IgniteCause.LAVA){
			if (event.getIgnitingBlock() == null){
				return;
			}
			@Nullable
			UUID owner = FluffyCombat.getBlockOwner(event.getIgnitingBlock());
			if (owner == null){
				return;
			}
			OfflinePlayer player = Bukkit.getOfflinePlayer(owner);
			FluffyCombat.setBlockOwner(player, event.getIgnitingBlock());
		}
	}
}