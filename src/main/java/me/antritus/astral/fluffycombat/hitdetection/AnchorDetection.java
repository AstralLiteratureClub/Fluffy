package me.antritus.astral.fluffycombat.hitdetection;

import me.antritus.astral.fluffycombat.FluffyCombat;
import me.antritus.astral.fluffycombat.api.events.EntityDamageEntityByRespawnAnchorEvent;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.type.RespawnAnchor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByBlockEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class AnchorDetection implements Listener {
	public final Map<Location, UUID> attackers = new HashMap<>();
	public final Map<Location, ItemStack> itemsMap = new HashMap<>();
	private final FluffyCombat fluffyCombat;

	public AnchorDetection(FluffyCombat fluffyCombat) {
		this.fluffyCombat = fluffyCombat;
	}

	@EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
	private void onAnchorClick(PlayerInteractEvent event){
		if (event.getAction()!= Action.RIGHT_CLICK_BLOCK){
			return;
		}
		World world = event.getPlayer().getWorld();
		World.Environment environment = world.getEnvironment();
		if (environment== World.Environment.NETHER){
			return;
		}
		Block block = event.getClickedBlock();
		if (block == null){
			return;
		}

		ItemStack itemStack = event.getPlayer().getInventory().getItemInMainHand();
		if (itemStack.getType() == Material.AIR){
			itemStack = event.getPlayer().getInventory().getItemInOffHand();
		}
		if (block.getType()!=Material.RESPAWN_ANCHOR){
			return;
		}
		if (event.getPlayer().isSneaking()
				&& itemStack.getType() != Material.AIR){
			return;
		}
		RespawnAnchor anchor = (RespawnAnchor) block.getBlockData();
		int charges = anchor.getCharges();
		Player player = event.getPlayer();
		Location location = block.getLocation().toBlockLocation();
		if (itemStack.getType()==Material.GLOWSTONE){
			if (charges==anchor.getMaximumCharges()) {
				attackers.put(location, player.getUniqueId());
				itemsMap.put(location, itemStack);
			}
		} else {
			if (charges>0){
				attackers.put(location, player.getUniqueId());
				itemsMap.put(location, itemStack);
			}
		}
		if (attackers.get(location) != null) {
			fluffyCombat.getServer().getScheduler().runTaskLaterAsynchronously(fluffyCombat,
					(x) -> {
						attackers.remove(location);
						itemsMap.remove(location);
					},
					40
			);
		}
	}

	@EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
	private void onAnchorExplode(EntityDamageByBlockEvent event){
		if (!(event.getEntity() instanceof Player victim)){
			return;
		}
		BlockState blockState = event.getDamagerBlockState();
		if (blockState == null){
			return;
		}
		if (blockState.getType()!=Material.RESPAWN_ANCHOR){
			return;
		}
		Location location = blockState.getLocation().toBlockLocation();
		location.setWorld(victim.getWorld());
		UUID ownerId = attackers.get(location);
		if (ownerId == null){
			return;
		}
		OfflinePlayer attacker = fluffyCombat.getServer().getOfflinePlayer(ownerId);
		if (!attacker.hasPlayedBefore() && !attacker.isOnline()){
			return;
		}

		ItemStack itemStack = itemsMap.get(location);


		EntityDamageEntityByRespawnAnchorEvent newDamageEvent =
				new EntityDamageEntityByRespawnAnchorEvent(victim,
						attacker,
						event.getDamager(), blockState, itemStack);
		newDamageEvent.callEvent();
		if (newDamageEvent.isCancelled()){
			event.setCancelled(true);
		}
	}


	public FluffyCombat fluffyCombat() {
		return fluffyCombat;
	}

}