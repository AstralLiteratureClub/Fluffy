package bet.astral.fluffy.listeners.hitdetection;

import bet.astral.fluffy.utils.Compatibility;
import bet.astral.fluffy.api.CombatCause;
import bet.astral.fluffy.api.CombatTag;
import bet.astral.fluffy.events.damage.CombatDamageUsingTNTEvent;
import bet.astral.fluffy.listeners.combat.begin.BeginCombatListener;
import bet.astral.fluffy.FluffyCombat;
import com.destroystokyo.paper.event.entity.EntityRemoveFromWorldEvent;
import org.bukkit.Location;
import org.bukkit.Material;
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
	/**
	 * Used to detect TNT lit by CRYSTALS
	 */
	protected final Map<UUID, UUID> crystalOwners = new HashMap<>();
	/**
	 * Used to detect TNT lit by TNT
	 */
	public final Map<TNTPrimed, TNTPrimed> tntOwners = new HashMap<>();
	/**
	 * Used to detect TNT lit by ANCHORS
	 */
	public final Map<TNTPrimed, UUID> anchorOwners = new HashMap<>();
	/**
	 * Used to detect TNT lit by BEDS
	 */
	public final Map<TNTPrimed, UUID> bedOwners = new HashMap<>();
	/**
	 * Used to detect TNT lit by FIRE
	 */
	public final Map<TNTPrimed, UUID> fireOwners = new HashMap<>();
	/**
	 * Used internally for detecting TNT primers
	 */
	protected final Map<Location, Entity> primers = new HashMap<>();
	/**
	 * Used internally for detecting TNT primers
	 */
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
			if (blockData instanceof RespawnAnchor  && Compatibility.RESPAWN_ANCHOR.isCompatible()) {
				UUID anchorOwner = tntDetection.anchorOwners.get(tntPrimed);
				return fluffy.getServer().getOfflinePlayer(anchorOwner);
			} else if (block instanceof Bed && Compatibility.BED.isCompatible()){
				UUID bedOwner = tntDetection.bedOwners.get(tntPrimed);
				return fluffy.getServer().getOfflinePlayer(bedOwner);
			} else if (block.getType() == Material.FIRE || block.getType() == Material.SOUL_FIRE){
				UUID owner = tntDetection.fireOwners.get(tntPrimed);
				return fluffy.getServer().getOfflinePlayer(owner);
			}
		} else if (obj instanceof Entity entity) {
			if (entity instanceof LivingEntity livingEntity) {
				return livingEntity;
			} else if (entity instanceof Projectile projectile) {
				ProjectileSource source = projectile.getShooter();
				if (source instanceof BlockProjectileSource) {
					return null;
				}
				return source;
			} else if (entity instanceof TNTPrimed tnt) {
				return tntDetection.tntOwners.get(tnt);
			} else if (entity instanceof EnderCrystal enderCrystal && Compatibility.ENDER_CRYSTAL.isCompatible()) {
				CrystalDetection crystalDetect = fluffy.getCrystalDetection();
				CrystalDetection.CrystalTag detected = crystalDetect.detectionMap.get(enderCrystal);
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
				if (detected.entity() instanceof Projectile projectile) {
					if (projectile.getShooter() instanceof BlockProjectileSource) {
						return null;
					}
					return projectile.getShooter();
				}
				return detected.entity();
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
				if (Compatibility.RESPAWN_ANCHOR.isCompatible() && blockData instanceof RespawnAnchor){
					blockPrimers.put(blockLocation, block);
				} if (Compatibility.BED.isCompatible() && blockData instanceof Bed){
					blockPrimers.put(blockLocation, block);
				}
			}
			Entity entity = event.getPrimingEntity();
			if (entity == null){
				return;
			}
			if (entity instanceof EnderCrystal crystal && Compatibility.ENDER_CRYSTAL.isCompatible() ) {
				primers.put(event.getBlock().getLocation().toBlockLocation(), crystal);
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
		} else if (event.getCause() == TNTPrimeEvent.PrimeCause.FIRE){
			Block block = event.getPrimingBlock();
			if (block == null){
				return;
			}
			@Nullable
			UUID owner = FluffyCombat.getBlockOwner(block);
			if (owner == null){
				return;
			}
			Location location = block.getLocation().toBlockLocation();
			blockPrimers.put(location, block);
			fluffy.getServer().getAsyncScheduler().runDelayed(fluffy,
					scheduledTask -> {
						crystalOwners.remove(owner);
					}, 5, TimeUnit.SECONDS);
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	private void onTNTSPawn(EntitySpawnEvent event){
		if (event.getEntity() instanceof TNTPrimed tnt){
			Location location = tnt.getLocation().toBlockLocation();
			tnt.setMetadata("fluffy_location", new FixedMetadataValue(fluffy, location));
			Entity entity = primers.get(location);
			if (entity == null){
				Block block = blockPrimers.get(location);
				if (block == null){
					return;
				}
				Location mid = block.getLocation().toBlockLocation();
				if (Compatibility.RESPAWN_ANCHOR.isCompatible()){
					if (block.getBlockData() instanceof RespawnAnchor) {
						AnchorDetection anchorDetection = fluffy.getAnchorDetection();
						AnchorDetection.AnchorTag owner = anchorDetection.detectionMap.get(mid);
						anchorOwners.put(tnt, owner.owner().getUniqueId());
						fluffy.getServer().getAsyncScheduler().runDelayed(fluffy,
								scheduledTask -> {
									anchorOwners.remove(tnt);
								}, 5, TimeUnit.SECONDS);
					}
				} else if (Compatibility.BED.isCompatible()){
					if (block.getBlockData() instanceof Bed) {
						blockPrimers.put(mid, block);
						BedDetection bedDetection = fluffy.getBedDetection();
						BedDetection.BedTag owner = bedDetection.detectionMap.get(mid);
						bedOwners.put(tnt, owner.owner().getUniqueId());
						fluffy.getServer().getAsyncScheduler().runDelayed(fluffy,
								scheduledTask -> {
									bedOwners.remove(tnt);
								}, 5, TimeUnit.SECONDS);
					}
				} else if (block.getBlockData().getMaterial()== Material.FIRE ||
						block.getBlockData().getMaterial()== Material.SOUL_FIRE){
					@Nullable UUID owner = FluffyCombat.getBlockOwner(block);
					if (owner == null){
						return;
					}
					fireOwners.put(tnt, owner);
					fluffy.getServer().getAsyncScheduler().runDelayed(fluffy, scheduledTask -> {
						fireOwners.remove(tnt);
					}, 5, TimeUnit.SECONDS);
				}
				blockPrimers.remove(location);
				tnt.setMetadata("fluffy_tnt_primer", new FixedMetadataValue(fluffy, block));
				return;
			}
			if (entity instanceof TNTPrimed tntPrimed) {
				tntOwners.put(tnt, tntPrimed);
				fluffy.getServer().getAsyncScheduler().runDelayed(fluffy, (x)->{
					tntOwners.remove(tnt);
				}, 5, TimeUnit.SECONDS);
			} else if (entity instanceof EnderCrystal crystal){
				CrystalDetection.CrystalTag owner = crystalDetection.detectionMap.get(crystal);
				crystalOwners.put(entity.getUniqueId(), owner.entity().getUniqueId());
				UUID id = entity.getUniqueId();
				fluffy.getServer().getAsyncScheduler().runDelayed(fluffy,
						scheduledTask -> {
							crystalOwners.remove(id);
						}, 5, TimeUnit.SECONDS);
			}
			primers.remove(location);
			tnt.setMetadata("fluffy_tnt_primer", new FixedMetadataValue(fluffy, entity));
		}
	}

	@EventHandler
	private void onEntityRemove(EntityRemoveFromWorldEvent event){
		if (event.getEntity() instanceof TNTPrimed tnt){
			tnt.removeMetadata("fluffy_tnt_primer", fluffy);
			MetadataValue value = tnt.getMetadata("fluffy_location").stream().filter(data->data.getOwningPlugin() instanceof FluffyCombat).findAny().orElse(null);
			if (value != null) {
				Location location = (Location) value.value();
				blockPrimers.remove(location);
				primers.remove(location);
				tnt.removeMetadata("fluffy_location", fluffy);
			}
		}
	}

	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
	private void onTNTDamage(EntityDamageByEntityEvent event) {
		if (!(event.getEntity() instanceof Player victim)){
			return;
		}
		if (event.getDamager() instanceof TNTPrimed tnt){
			if (tnt.hasMetadata("fluffy_tnt_primer")){
				Object value = getStartingEntityPrimer(tnt);
				tnt.removeMetadata("fluffy_tnt_primer", fluffy);
				if (value == null){
					return;
				}
				if (value instanceof OfflinePlayer attacker){
					BeginCombatListener.handle(victim, attacker, CombatCause.TNT, null);
					CombatTag combatTag = fluffy.getCombatManager().getLatest(victim);
					if (combatTag == null){
						return;
					}					CombatDamageUsingTNTEvent damageEvent = new CombatDamageUsingTNTEvent(
							fluffy, combatTag, victim, attacker, null, tnt);
					combatTag.setDamageDealt(attacker, event.getFinalDamage());
					damageEvent.callEvent();
				}
			}
		}
	}
}
