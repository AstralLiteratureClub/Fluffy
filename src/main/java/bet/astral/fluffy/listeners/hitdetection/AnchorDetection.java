package bet.astral.fluffy.listeners.hitdetection;

import bet.astral.fluffy.api.CombatCause;
import bet.astral.fluffy.api.CombatTag;
import bet.astral.fluffy.FluffyCombat;
import bet.astral.fluffy.events.damage.CombatDamageUsingRespawnAnchorEvent;
import bet.astral.fluffy.listeners.combat.begin.BeginCombatListener;
import org.bukkit.Location;
import org.bukkit.Material;
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
import java.util.concurrent.TimeUnit;

public class AnchorDetection implements Listener {
	public final Map<Location, AnchorTag> detectionMap = new HashMap<>();
	private final FluffyCombat fluffyCombat;

	public AnchorDetection(FluffyCombat fluffyCombat) {
		this.fluffyCombat = fluffyCombat;
	}

	@EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
	private void onAnchorClick(PlayerInteractEvent event) {
		if (event.getAction() != Action.RIGHT_CLICK_BLOCK) {
			return;
		}
		World world = event.getPlayer().getWorld();
		World.Environment environment = world.getEnvironment();
		if (environment == World.Environment.NETHER) {
			return;
		}
		Block block = event.getClickedBlock();
		if (block == null) {
			return;
		}

		ItemStack itemStack = event.getPlayer().getInventory().getItemInMainHand();
		if (itemStack.getType() == Material.AIR) {
			itemStack = event.getPlayer().getInventory().getItemInOffHand();
		}
		if (block.getType() != Material.RESPAWN_ANCHOR) {
			return;
		}
		if (event.getPlayer().isSneaking()
				&& itemStack.getType() != Material.AIR) {
			return;
		}
		RespawnAnchor anchor = (RespawnAnchor) block.getBlockData();
		int charges = anchor.getCharges();
		Player player = event.getPlayer();
		Location location = block.getLocation().toBlockLocation();
		if (itemStack.getType() == Material.GLOWSTONE
				|| charges > 0) {
			AnchorTag anchorTag = new AnchorTag(location, player, itemStack, charges);
			detectionMap.put(location, anchorTag);
		}
		if (detectionMap.get(location) != null) {
			fluffyCombat.getServer().getAsyncScheduler().runDelayed(fluffyCombat,
					(x) -> {
						detectionMap.remove(location);
					},
					100, TimeUnit.MILLISECONDS);
		}
	}

	@EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
	private void onAnchorExplode(EntityDamageByBlockEvent event) {
		if (!(event.getEntity() instanceof Player victim)) {
			return;
		}
		BlockState blockState = event.getDamagerBlockState();
		if (blockState == null) {
			return;
		}
		if (blockState.getType() != Material.RESPAWN_ANCHOR) {
			return;
		}
		Location location = blockState.getLocation().toBlockLocation();
		location.setWorld(victim.getWorld());
		AnchorTag tag = detectionMap.get(location);
		if (tag == null) {
			return;
		}

		if (tag.owner==victim){
			return;
		}

		ItemStack itemStack = tag.itemStack;
		BeginCombatListener.handle(victim, tag.owner, CombatCause.RESPAWN_ANCHOR, itemStack);
		CombatTag combatTag = fluffyCombat.getCombatManager().getLatest(victim);
		if (combatTag == null){
			return;
		}
		CombatDamageUsingRespawnAnchorEvent damageEvent = new CombatDamageUsingRespawnAnchorEvent(
				fluffyCombat, combatTag, victim, tag.owner, blockState, event.getDamager(), tag.itemStack);
		combatTag.setDamageDealt(tag.owner, event.getFinalDamage());
		damageEvent.callEvent();

	}


	public FluffyCombat fluffyCombat() {
		return fluffyCombat;
	}

	public record AnchorTag(Location location, Player owner, ItemStack itemStack, int charges) {
		@Override
			public String toString() {
				return "AnchorTag[" +
						"location=" + location + ", " +
						"owner=" + owner + ", " +
						"itemStack=" + itemStack + ", " +
						"charges=" + charges + ']';
			}
		}


}