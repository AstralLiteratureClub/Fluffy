package me.antritus.astral.fluffycombat.death_messages;

import me.antritus.astral.fluffycombat.FluffyCombat;
import me.antritus.astral.fluffycombat.hitdetection.CrystalDetection;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class VoidListener implements Listener {
	private final FluffyCombat fluffy;
	private final Map<Player, Boolean> cancelRemoval = new HashMap<>();
	private final Map<Entity, Player> lastToDamage = new HashMap<>();
	private BukkitTask task;
	public void onEnable(){
		task = new BukkitRunnable() {
			@Override
			public void run() {
				for (Player player : fluffy.getServer().getOnlinePlayers()){
					if (((Entity) player).isOnGround()){
						if (cancelRemoval.containsKey(player) && !cancelRemoval.get(player)){
							lastToDamage.remove(player);
						} else if (!cancelRemoval.containsKey(player)){
							lastToDamage.remove(player);
						}
					}
				}
			}
		}.runTaskTimer(fluffy, 20, 1);

	}

	public void onDisable() {
		task.cancel();
	}

	public VoidListener(FluffyCombat fluffy) {
		this.fluffy = fluffy;
	}

	@EventHandler
	public void onDeath(PlayerDeathEvent event){
		if (event.getEntity().getLastDamageCause() == null || event.getEntity().getLastDamageCause().getCause() == EntityDamageEvent.DamageCause.VOID){
			return;
		}
		EntityDamageEvent damageEvent = event.getEntity().getLastDamageCause();
		if (damageEvent instanceof EntityDamageByEntityEvent entityDamageEvent){
			Entity entity = entityDamageEvent.getDamager();
			if (entity instanceof EnderCrystal crystal){
				CrystalDetection detection = fluffy.getCrystalDetection();
				CrystalDetection.CrystalTag tag = detection.detectionMap.get(crystal);
				Entity owner = tag.entity();
				OfflinePlayer player = null;
				while (!(owner instanceof Player)){
					if (owner instanceof Projectile projectile){
						owner = (Entity) projectile.getShooter();
					} else if (owner instanceof EnderCrystal enderCrystal) {
						CrystalDetection.CrystalTag crystalTag = detection.detectionMap.get(enderCrystal);
						owner = crystalTag.entity();
					} else if (owner instanceof TNTPrimed tntPrimed){
						UUID ownerId = null;
						owner = fluffy.getTntDetection().tntOwners.get(tntPrimed);
						if (owner == null){
							ownerId = fluffy.getTntDetection().fireOwners.get(tntPrimed);
						} if (owner == null && ownerId == null){
							ownerId = fluffy.getTntDetection().bedOwners.get(tntPrimed);
						} if (owner == null && ownerId == null){
							ownerId = fluffy.getTntDetection().anchorOwners.get(tntPrimed);
						}
						if (owner == null && ownerId == null){
							return;
						}
						if (ownerId != null){
							player = Bukkit.getOfflinePlayer(ownerId);
						}
					} else {
						break;
					}
				}
				player = owner != null ? (OfflinePlayer) owner : player;
			}
		}
	}


	@EventHandler
	public void onKill(PlayerDeathEvent e) {
		Entity victim = e.getEntity();

		// Fell into void
		if(e.getDeathMessage().contains("fell out of the world")) {
			if(lastToDamage.containsKey(victim)) {
				victim.sendMessage("victim");
				Player killer = lastToDamage.get(victim);
				killer.sendMessage("killer");
				lastToDamage.remove(victim);
			}
		}
		// Hit the ground
		else if(e.getDeathMessage().contains("fell from") || e.getDeathMessage().contains("fell off some")) {
			if(lastToDamage.containsKey(victim)) {
				victim.sendMessage("victim");
				Player killer = lastToDamage.get(victim);
				killer.sendMessage("killer");
				lastToDamage.remove(victim);
			}
		}
		// Fell into void, KD is tracked?
		else if(e.getDeathMessage().contains("live in the same world")) {
			if(lastToDamage.containsKey(victim)) {
				victim.sendMessage("victim");
				Player killer = lastToDamage.get(victim);
				killer.sendMessage("killer");
				lastToDamage.remove(victim);
			}
		}
	}

	@EventHandler
	public void onMove(PlayerMoveEvent e) {
		if(lastToDamage.containsKey(e.getPlayer())) {
			if(((Entity) e.getPlayer()).isOnGround()) {
				Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(fluffy, () -> {
					if(!(e.getPlayer().isDead())) {
						lastToDamage.remove(e.getPlayer());
					}
				}, 2);
			}
		}
	}
}
