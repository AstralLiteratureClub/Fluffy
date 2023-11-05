package me.antritus.astral.fluffycombat.hitdetection;

import me.antritus.astral.fluffycombat.FluffyCombat;
import me.antritus.astral.fluffycombat.api.events.EntityDamageEntityByTNTEvent;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.type.Bed;
import org.bukkit.block.data.type.RespawnAnchor;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.TNTPrimeEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.projectiles.BlockProjectileSource;
import org.bukkit.projectiles.ProjectileSource;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.TimeUnit;

public class TNTDetection implements Listener {
	private final Map<UUID, UUID> crystalOwners = new HashMap<>();
	private final Map<Location, UUID> anchorOwners = new HashMap<>();
	private final Map<Location, UUID> bedOwners = new HashMap<>();
	private final Map<Location, Entity> primers = new HashMap<>();
	private final Map<Location, Block> blockPrimers = new HashMap<>();
	private final FluffyCombat fluffy;
	private final CrystalDetection crystalDetection;

	public TNTDetection(FluffyCombat fluffy, CrystalDetection crystalDetection) {
		this.fluffy = fluffy;
		this.crystalDetection = crystalDetection;
	}

	/**
	 * Returns null when an entity is a block or a non entity OR entity was not saved
	 *
	 * @param tntPrimed tnt
	 * @return type, else null
	 */
	@Nullable
	public static EntityType type(TNTPrimed tntPrimed) {
		List<MetadataValue> listValues = tntPrimed.getMetadata("fluffy_tnt_primer");
		if (listValues.isEmpty()) {
			return null;
		}
		Object obj = listValues.get(0).value();
		if (obj == null) {
			return null;
		}
		if (obj instanceof Block) {
			return null;
		}
		return ((Entity) obj).getType();
	}

	/**
	 * Returns the primer for given tnt. Maybe null
	 *
	 * @param tntPrimed tnt
	 * @return primer
	 */
	@Nullable
	private static Object getPrimer(TNTPrimed tntPrimed) {
		List<MetadataValue> listValues = tntPrimed.getMetadata("fluffy_tnt_primer");
		if (listValues.isEmpty()) {
			return null;
		}
		return listValues.get(0).value();
	}

	public static Block getBlockPrimer(TNTPrimed tntPrimed) {
		Object obj = getPrimer(tntPrimed);
		if (!(obj instanceof Block)) {
			return null;
		}
		return (Block) obj;
	}

	public static Entity getEntityPrimer(TNTPrimed tntPrimed) {
		Object obj = getPrimer(tntPrimed);
		if (!(obj instanceof Entity)) {
			return null;
		}
		return (Entity) obj;
	}


	/**
	 * Sources all possible primers of all primers who primed given tnt.
	 *
	 * @param tntPrimed primer
	 * @return offline player, player or entity;
	 */
	@Nullable
	public static Object getStartingEntityPrimer(TNTPrimed tntPrimed) {
		FluffyCombat fluffy = FluffyCombat.getPlugin(FluffyCombat.class);
		TNTDetection tntDetection = fluffy.getTntDetection();
		Object obj = getPrimer(tntPrimed);
		if (obj instanceof Block block) {
			BlockData blockData = block.getBlockData();
			Location location = block.getLocation();
			Location blockLocation = location.toBlockLocation();
			if (blockData instanceof RespawnAnchor respawnAnchor) {
				UUID anchorOwner = tntDetection.anchorOwners.get(blockLocation);
				return fluffy.getServer().getOfflinePlayer(anchorOwner);
			} else if (block instanceof Bed bed){
				UUID bedOwner = tntDetection.bedOwners.get(blockLocation);
				return fluffy.getServer().getOfflinePlayer(bedOwner);
			}
			// TODO add bed detection
		} else if (obj instanceof Entity entity) {
			if (entity instanceof LivingEntity livingEntity) {
				return livingEntity;
			} else if (entity instanceof Projectile projectile) {
				ProjectileSource source = projectile.getShooter();
				if (source instanceof BlockProjectileSource blockProjectileSource) {
					return null;
				}
				return source;
			} else if (entity instanceof TNTPrimed tnt) {
				return getEntityPrimer(tnt);
			} else if (entity instanceof EnderCrystal enderCrystal) {
				CrystalDetection crystalDetect = fluffy.getCrystalDetection();
				Entity detected = crystalDetect.detectionMap.get(enderCrystal);
				if (detected == null) {
					Map<UUID, UUID> owners = fluffy.getTntDetection().crystalOwners;
					UUID owner = owners.get(entity.getUniqueId());
					Entity ownerEntity = fluffy.getServer().getPlayer(owner);
					if (ownerEntity == null) {
						ownerEntity = fluffy.getServer().getEntity(owner);
						if (ownerEntity == null) {
							OfflinePlayer player = fluffy.getServer().getOfflinePlayer(owner);
							if (!player.hasPlayedBefore() && !player.isOnline()) {
								return null;
							}
							return player;
						}
					}
					return null;
				}
				if (detected instanceof Projectile projectile) {
					if (projectile.getShooter() instanceof BlockProjectileSource blockProjectileSource) {
						return null;
					}
					return projectile.getShooter();
				}
				return detected;
			}
		}
		return null;
	}


	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	private void onTNTPrime(TNTPrimeEvent event){
		if (event.getCause()== TNTPrimeEvent.PrimeCause.EXPLOSION){
			if (event.getPrimingBlock() != null){
				Block block = event.getPrimingBlock();
				BlockData blockData = block.getBlockData();
				Location location = block.getLocation();
				Location blockLocation = location.toBlockLocation();
				blockPrimers.put(event.getBlock().getLocation().toBlockLocation(), block);
				if (blockData instanceof RespawnAnchor respawnAnchor){
					blockPrimers.put(blockLocation, block);
					AnchorDetection anchorDetection = fluffy.getAnchorDetection();
					UUID owner = anchorDetection.attackers.get(blockLocation);
					anchorOwners.put(blockLocation, owner);
					fluffy.getServer().getAsyncScheduler().runDelayed(fluffy,
							scheduledTask -> {
								anchorOwners.remove(blockLocation);
							}, 5, TimeUnit.SECONDS);
				} if (blockData instanceof Bed bed){
					blockPrimers.put(blockLocation, block);
					BedDetection bedDetection = fluffy.getBedDetection();
					UUID owner = bedDetection.detectionMap.get(blockLocation);
					bedOwners.put(blockLocation, owner);
					fluffy.getServer().getAsyncScheduler().runDelayed(fluffy,
							scheduledTask -> {
								bedOwners.remove(blockLocation);
							}, 5, TimeUnit.SECONDS);
				}
			}
			Entity entity = event.getPrimingEntity();
			if (entity == null){
				return;
			}
			if (entity instanceof EnderCrystal crystal) {
				Entity owner = crystalDetection.detectionMap.get(crystal);
				crystalOwners.put(entity.getUniqueId(), owner.getUniqueId());
				primers.put(event.getBlock().getLocation().toBlockLocation(), crystal);
				UUID id = entity.getUniqueId();
				fluffy.getServer().getAsyncScheduler().runDelayed(fluffy,
						scheduledTask -> {
							crystalOwners.remove(id);
						}, 5, TimeUnit.SECONDS);
			} else if (entity instanceof TNTPrimed tnt){
				primers.put(event.getBlock().getLocation().toBlockLocation(), tnt);
			} else if (entity instanceof Projectile projectile){
				primers.put(event.getBlock().getLocation().toBlockLocation(), (Entity) projectile.getShooter());
			}
		} else if (event.getCause() == TNTPrimeEvent.PrimeCause.PROJECTILE){
			primers.put(event.getBlock().getLocation().toBlockLocation(), event.getPrimingEntity());
		} else if (event.getCause() == TNTPrimeEvent.PrimeCause.PLAYER) {
			primers.put(event.getBlock().getLocation().toBlockLocation(), event.getPrimingEntity());
		} else if (event.getCause() == TNTPrimeEvent.PrimeCause.BLOCK_BREAK){
			primers.put(event.getBlock().getLocation().toBlockLocation(), event.getPrimingEntity());
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	private void onTNTSPawn(EntitySpawnEvent event){
		if (event.getEntity() instanceof TNTPrimed tnt){
			Location location = tnt.getLocation().toBlockLocation();
			Entity entity = primers.get(location);
			if (entity == null){
				Block block = blockPrimers.get(location);
				if (block == null){
					return;
				}
				blockPrimers.remove(location);
				tnt.setMetadata("fluffy_tnt_primer", new FixedMetadataValue(fluffy, block));
				return;
			}
			primers.remove(location);
			tnt.setMetadata("fluffy_tnt_primer", new FixedMetadataValue(fluffy, entity));
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	private void onTNTDamage(EntityDamageByEntityEvent event) {
		if (event.getDamager() instanceof TNTPrimed tnt){
			if (tnt.hasMetadata("fluffy_tnt_primer")){
				Object value = getStartingEntityPrimer(tnt);
				tnt.removeMetadata("fluffy_tnt_primer", fluffy);
				if (value == null){
					return;
				}
				if (value instanceof Entity entity){
					EntityDamageEntityByTNTEvent tntEvent = new EntityDamageEntityByTNTEvent(event.getEntity(), entity, tnt);
					tntEvent.callEvent();
					if (tntEvent.isCancelled()){
						event.setCancelled(true);
					}
				}
			}
		}
	}
}
