package me.antritus.astral.fluffycombat.hitdetection;

import lombok.Getter;
import me.antritus.astral.fluffycombat.FluffyCombat;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class LavaDetection implements Listener {
	@Getter
	private final FluffyCombat fluffy;

	public LavaDetection(FluffyCombat fluffy) {
		this.fluffy = fluffy;
	}

	@EventHandler
	private void onLavaFlow(BlockFromToEvent event){
		Block source = event.getBlock();
		if (source.getType() != Material.LAVA){
			return;
		}
		@Nullable
		UUID owner = FluffyCombat.getBlockOwner(source);
		if (owner == null){
			return;
		}
		OfflinePlayer player = Bukkit.getOfflinePlayer(owner);
		Block to = event.getBlock();
		FluffyCombat.setBlockOwner(player, to);
	}

	@EventHandler
	private void onLavaPlace(BlockPlaceEvent event){
		Block source = event.getBlock();
		if (source.getType() != Material.LAVA){
			return;
		}
		FluffyCombat.setBlockOwner(event.getPlayer(), source);
	}

	@EventHandler
	private void onLavaBreak(BlockBreakEvent event){
		if (event.getBlock().getType()==Material.LAVA){
			FluffyCombat.clearBlockData(event.getBlock().getChunk(), event.getPlayer().getLocation());
		}
	}
}
