package bet.astral.fluffy.listeners;

import bet.astral.fluffy.FluffyCombat;
import lombok.Getter;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;

import java.util.UUID;

public class LiquidOwnerListener implements Listener {
	@Getter
	private final FluffyCombat fluffy;

	public LiquidOwnerListener(FluffyCombat fluffy) {
		this.fluffy = fluffy;
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	private void onLiquidMove(BlockFromToEvent event) {
		//if (event.getBlock().getType() == Material.WATER || event.getBlock().getType() == Material.LAVA){
		if (event.getBlock().getType()==Material.LAVA){
			UUID owner = FluffyCombat.getBlockOwner(event.getBlock());
			if (owner != null)
				FluffyCombat.setBlockOwner(Bukkit.getOfflinePlayer(owner), event.getToBlock(), event.getBlock().getType());
		}
	}


	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	private void onLiquidPlace(PlayerBucketEmptyEvent event) {
		Block block = event.getBlock().getLocation().getBlock();
		if (event.getBucket() == Material.LAVA_BUCKET) {
			FluffyCombat.setBlockOwner(event.getPlayer(), block, Material.LAVA);
		}
	}

	private Material bucketMapper(Material material){
		switch (material){
			case WATER_BUCKET -> {
				return Material.WATER;
			}
			case LAVA_BUCKET -> {
				return Material.LAVA;
			}
		}
		return null;
	}
}
