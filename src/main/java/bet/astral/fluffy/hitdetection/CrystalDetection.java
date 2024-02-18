package bet.astral.fluffy.hitdetection;

import bet.astral.fluffy.FluffyCombat;
import bet.astral.fluffy.api.events.EntityDamageEntityByEnderCrystalEvent;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.projectiles.BlockProjectileSource;

import java.util.HashMap;
import java.util.Map;

public class CrystalDetection implements Listener {

	public final Map<EnderCrystal, CrystalTag> detectionMap = new HashMap<>();
	private final FluffyCombat fluffy;

	public CrystalDetection(FluffyCombat fluffy) {
		this.fluffy = fluffy;
	}


	@EventHandler(priority = EventPriority.NORMAL)
	private void onPlayerAttackCrystal(EntityDamageByEntityEvent event){
		if (!(event.getEntity() instanceof EnderCrystal enderCrystal)){
			return;
		}
		if (event.getEntity() instanceof Player player){
			ItemStack itemStack = player.getInventory().getItemInMainHand();
			CrystalTag crystalTag = new CrystalTag(enderCrystal, player, itemStack);
			detectionMap.put(enderCrystal, crystalTag);
		} else {
			Entity entity = event.getDamager();
			ItemStack itemStack = null;
			if (entity instanceof LivingEntity){
				EntityEquipment entityEquipment = ((LivingEntity) entity).getEquipment();
				if (entityEquipment != null){
					itemStack = entityEquipment.getItemInMainHand();
				}
			}
			CrystalTag crystalTag = new CrystalTag(enderCrystal, entity, itemStack);
			detectionMap.put(enderCrystal, crystalTag);
		}
		fluffy.getServer().getScheduler().runTaskLaterAsynchronously(fluffy, () -> {
			detectionMap.remove(enderCrystal);
		}, 3);
	}

	@EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
	private void onCrystalAttack(final EntityDamageByEntityEvent event){
		if (!(event.getDamager() instanceof EnderCrystal crystal)){
			return;
		}
		CrystalTag tag = detectionMap.get(crystal);
		if (tag == null) {
			return;
		}
		detectionMap.remove(crystal);
		Entity entity = tag.entity;
		if (entity instanceof Projectile projectile){
			if (projectile.getShooter() instanceof BlockProjectileSource){
				return;
			}
			entity = (Entity) projectile.getShooter();
			if (entity == null){
				return;
			}
		}
		EntityDamageEntityByEnderCrystalEvent enderCrystalEvent = new EntityDamageEntityByEnderCrystalEvent(
				event.getEntity(),
				entity,
				crystal,
				event.getFinalDamage(), tag.itemStack
		);
		enderCrystalEvent.callEvent();
		if (enderCrystalEvent.isCancelled()){
			event.setCancelled(true);
		}
	}

	public FluffyCombat fluffy() {
		return fluffy;
	}

	public record CrystalTag(EnderCrystal crystal, Entity entity, ItemStack itemStack) {
	}
}