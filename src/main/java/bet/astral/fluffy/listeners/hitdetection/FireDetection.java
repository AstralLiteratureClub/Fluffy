package bet.astral.fluffy.listeners.hitdetection;

import com.destroystokyo.paper.event.block.BlockDestroyEvent;
import lombok.Getter;
import bet.astral.fluffy.FluffyCombat;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.BlockSpreadEvent;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

@Getter
public class FireDetection implements Listener {
	public static Material[] SOUL_FIRE_BLOCKS = {Material.SOUL_SOIL, Material.SOUL_SAND};
	private final FluffyCombat fluffy;
	public FireDetection(FluffyCombat fluffyCombat){
		this.fluffy = fluffyCombat;
	}
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	private void onFirePlace(BlockPlaceEvent event) {
		Block block = event.getBlock();
		if (DetectionHelper.isAny(block.getType(), DetectionHelper.FIRE_BLOCKS)) {
			Player player = event.getPlayer();
			DetectionHelper.setBlockOwner(player, block);
		}
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	private void onFireExtinguishBDE(BlockDestroyEvent event){
		Block block = event.getBlock();
		if (DetectionHelper.isAny(block.getType(), DetectionHelper.FIRE_BLOCKS)) {
			if (DetectionHelper.isAny(event.getNewState().getMaterial(), DetectionHelper.FIRE_BLOCKS)){
				return;
			}
			DetectionHelper.clearBlockData(block.getChunk(), block.getLocation());
		}
	}


	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	private void onFireSpreadEvent(BlockSpreadEvent event){
		Block block = event.getBlock();
		if (DetectionHelper.isAny(block.getType(), DetectionHelper.FIRE_BLOCKS)) {
			if (DetectionHelper.isAny(event.getSource().getType(), DetectionHelper.FIRE_BLOCKS)) {
				@Nullable
				UUID owner = DetectionHelper.getBlockOwner(event.getSource());
				if (owner == null){
					return;
				}
				OfflinePlayer player = Bukkit.getOfflinePlayer(owner);
				DetectionHelper.setBlockOwner(player, block);
			}
		}
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	private void onFireIgnite(BlockIgniteEvent event){
		if (event.getCause()== BlockIgniteEvent.IgniteCause.LAVA){
			if (event.getIgnitingBlock() == null){
				return;
			}
			@Nullable
			UUID owner = DetectionHelper.getBlockOwner(event.getIgnitingBlock());
			if (owner == null){
				return;
			}
			OfflinePlayer player = Bukkit.getOfflinePlayer(owner);
			Material fireMaterial = Material.FIRE;
			Material down = event.getIgnitingBlock().getRelative(BlockFace.DOWN).getType();
			if (DetectionHelper.isAny(down, SOUL_FIRE_BLOCKS)) {
				fireMaterial = Material.SOUL_FIRE;
			}
			DetectionHelper.setBlockOwner(player, event.getIgnitingBlock(), fireMaterial);
		}
	}
}