package me.antritus.astral.fluffycombat.hitdetection;

import me.antritus.astral.fluffycombat.FluffyCombat;
import me.antritus.astral.fluffycombat.api.events.EntityDamageEntityByBedEvent;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.data.type.Bed;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByBlockEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class BedDetection implements Listener {
	private final FluffyCombat fluffy;
	public final Map<Location, UUID> detectionMap = new HashMap<>();
	private final Map<Location, ItemStack> itemsMap = new HashMap<>();
	protected final Map<Location, Location> partMap = new HashMap<>();

	public BedDetection(FluffyCombat fluffy) {
		this.fluffy = fluffy;
	}

	public static Block getOppositePart(Block block){
		Bed bedData = (Bed) block.getBlockData();
		Block face;
		if (bedData.getPart() == Bed.Part.HEAD) {
			face = block.getRelative(bedData.getFacing().getOppositeFace());
		} else {
			face = block.getRelative(bedData.getFacing());
		}
		if (!(face.getBlockData() instanceof Bed)) return null;
		return face;
	}


	@EventHandler
	private void onInteract(PlayerInteractEvent event) {
		if (event.getAction() != Action.RIGHT_CLICK_BLOCK) {
			return;
		}
		Player player = event.getPlayer();
		World world = player.getWorld();
		World.Environment environment = world.getEnvironment();
		if (environment != World.Environment.NETHER
				&& environment != World.Environment.THE_END) {
			return;
		}
		Block block = event.getClickedBlock();
		if (block == null) {
			return;
		}
		if (!(block.getBlockData() instanceof Bed bed)){
			return;
		}
		ItemStack itemStack = player.getInventory().getItemInMainHand();
		if (itemStack.getType().isAir()) {
			itemStack = player.getInventory().getItemInOffHand();
		}
		if (player.isSneaking() && !itemStack.getType().isAir()) {
			return;
		}

		Bed.Part part = bed.getPart();
		Block headBed = part == Bed.Part.HEAD ? block : getOppositePart(block);
		Block footBed = part == Bed.Part.FOOT ? block : getOppositePart(block);
		Location[] locations = {
				headBed != null ? headBed.getLocation().toBlockLocation() : null,
				footBed != null ? footBed.getLocation().toBlockLocation() : null
		};
		if (locations[0] != null && locations[1] != null) {
			detectionMap.put(locations[0], player.getUniqueId());
			partMap.put(locations[0], locations[1]);
			itemsMap.put(locations[0], itemStack);
		} else if (locations[0] != null){
			detectionMap.put(locations[0], player.getUniqueId());
			itemsMap.put(locations[0], itemStack);
		} else if (locations[1] != null){
			detectionMap.put(locations[1], player.getUniqueId());
			itemsMap.put(locations[1], itemStack);
		} else {
			return;
		}
		fluffy.getServer().getAsyncScheduler().runDelayed(fluffy,
				(x) -> {
					detectionMap.remove(locations[0]);
					detectionMap.remove(locations[1]);

					itemsMap.remove(locations[0]);
					itemsMap.remove(locations[1]);

					partMap.remove(locations[0]);
					partMap.remove(locations[1]);
				},
				2,
				TimeUnit.SECONDS
		);
	}

	@EventHandler
	private void onBedExplode(EntityDamageByBlockEvent event){
		if (!(event.getEntity() instanceof Player victim)){
			return;
		}
		if (!(event.getDamagerBlockState() instanceof org.bukkit.block.Bed bed)){
			return;
		}
		Location location = bed.getLocation().toBlockLocation();
		location.setWorld(victim.getWorld());
		UUID owner = detectionMap.get(location);
		if (owner == null){
			location = partMap.get(location);
			if (location == null){
				return;
			}
			owner = detectionMap.get(location);
		}
		if (owner == null){
			return;
		}
		OfflinePlayer attacker = fluffy.getServer().getOfflinePlayer(owner);
		if (!attacker.hasPlayedBefore() && !attacker.isOnline()){
			return;
		}
		ItemStack itemStack = itemsMap.get(location.toBlockLocation());

		EntityDamageEntityByBedEvent newDamageEvent =
				new EntityDamageEntityByBedEvent(victim,
						attacker,
						event.getDamager(), bed, itemStack);
		newDamageEvent.callEvent();
		if (newDamageEvent.isCancelled()){
			event.setCancelled(true);
		}
	}
}
