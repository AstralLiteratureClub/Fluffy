package me.antritus.astral.fluffycombat.hitdetection;

import me.antritus.astral.fluffycombat.FluffyCombat;
import me.antritus.astral.fluffycombat.api.events.EntityDamageEntityByEnderCrystalEvent;
import org.bukkit.entity.EnderCrystal;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.projectiles.BlockProjectileSource;

import java.util.HashMap;
import java.util.Map;

public class CrystalDetection implements Listener {
	public final Map<EnderCrystal, Entity> detectionMap = new HashMap<>();
	public final Map<EnderCrystal, ItemStack> itemMap = new HashMap<>();
	private final FluffyCombat fluffy;

	public CrystalDetection(FluffyCombat fluffy) {
		this.fluffy = fluffy;
	}


	@EventHandler(priority = EventPriority.NORMAL)
	private void onPlayerAttackCrystal(EntityDamageByEntityEvent event){
		if (!(event.getEntity() instanceof EnderCrystal enderCrystal)){
			return;
		}
		detectionMap.put(enderCrystal, event.getDamager());
		if (event.getEntity() instanceof LivingEntity entity){
			if (entity.getEquipment() != null) {
				ItemStack itemStack = entity.getEquipment().getItemInMainHand();
				itemMap.put(enderCrystal, itemStack);
			}
		}
		fluffy.getServer().getScheduler().runTaskLaterAsynchronously(fluffy, new Runnable() {
			@Override
			public void run() {
				detectionMap.remove(enderCrystal);
				itemMap.remove(enderCrystal);
			}
		}, 3);
	}

	@EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
	private void onCrystalAttack(final EntityDamageByEntityEvent event){
		if (!(event.getDamager() instanceof EnderCrystal crystal)){
			return;
		}
		Entity entity = detectionMap.get(crystal);
		detectionMap.remove(crystal);
		if (entity == null) {
			return;
		}
		if (entity instanceof Projectile projectile){
			if (projectile.getShooter() instanceof BlockProjectileSource blockProjectileSource){
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
				event.getFinalDamage(), itemMap.get(crystal)
		);
		enderCrystalEvent.callEvent();
		if (enderCrystalEvent.isCancelled()){
			event.setCancelled(true);
		}
	}

	public FluffyCombat fluffy() {
		return fluffy;
	}
}
