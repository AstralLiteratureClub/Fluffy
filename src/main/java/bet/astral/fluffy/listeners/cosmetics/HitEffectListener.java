package bet.astral.fluffy.listeners.cosmetics;

import bet.astral.fluffy.FluffyCombat;
import bet.astral.fluffy.cosmetics.Effect;
import bet.astral.fluffy.cosmetics.TranslationTextEffectData;
import bet.astral.fluffy.manager.RegionManager;
import bet.astral.fluffy.messenger.Placeholders;
import bet.astral.messenger.v2.placeholder.collection.PlaceholderCollection;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;

public class HitEffectListener implements Listener {
    private final List<Effect> effectDataList = new ArrayList<>();
    private final Random random = new Random();

    private final FluffyCombat fluffy;

    public HitEffectListener(FluffyCombat fluffy) {
        this.fluffy = fluffy;
    }

    public Effect randomEffect() {
        return effectDataList.get(random.nextInt(effectDataList.size()));
    }
    @EventHandler
    public void onPlayerHit(EntityDamageByEntityEvent event) {
        Player player = null;
        if (event.getDamager() instanceof Projectile projectile) {
            if (projectile.getShooter() instanceof Player) {
                player = (Player) projectile.getShooter();
            }
        } else if (event.getDamager() instanceof Player p) {
            player = p;
        }
        if (player != null) {

            // Check if hit effects can be displayed
            RegionManager regionManager = fluffy.getRegionManager();
            if (!regionManager.canDisplayHitEffects(event.getEntity().getLocation())) {
                return;
            }

            
            Entity victim = event.getEntity();

            double damage = event.getFinalDamage();
            String damageText = String.format(Locale.US, "%.2f", damage);

            // Get the hit location based of the attacker's direction to the victim
            Location hitLocation = getHitLocation(player, victim);

            // Translations
            PlaceholderCollection placeholderCollection = PlaceholderCollection.list(
                    Placeholders.playerPlaceholders("player", player)
            );
            placeholderCollection.add("damage", damageText);
            // Translation data
            Effect effect = fluffy.getHitEffectManager().getPlayerEffects().get(player.getUniqueId());
            if (effect != null) {
                effect.run(player, victim, hitLocation, new TranslationTextEffectData(placeholderCollection));
            }
        }

        if (event.getEntity() instanceof LivingEntity entity) {
            entity.setNoDamageTicks(0);
        }
    }

    private Location getHitLocation(Player attacker, Entity victim) {
        // Ray origin: attacker's eye position
        Vector rayOrigin = attacker.getEyeLocation().toVector();
        Vector rayDir    = attacker.getEyeLocation().getDirection().normalize();

        // Build the victim's AABB from their bounding box
        BoundingBox bb = victim.getBoundingBox();

        Vector min = new Vector(bb.getMinX(), bb.getMinY(), bb.getMinZ());
        Vector max = new Vector(bb.getMaxX(), bb.getMaxY(), bb.getMaxZ());

        // Slab-method ray/AABB intersection (returns the entry distance 't')
        Double t = rayAABBIntersect(rayOrigin, rayDir, min, max);

        if (t != null) {
            // Hit point = origin + direction * t
            Vector hitPoint = rayOrigin.clone().add(rayDir.clone().multiply(t));
            return hitPoint.toLocation(victim.getWorld());
        }

        // Fallback: victim center at mid-body height if ray somehow misses
        return victim.getLocation().add(0, victim.getHeight() / 2.0, 0);
    }

    /**
     * Slab-method ray vs AABB intersection.
     * Returns the entry distance 't' along the ray, or null if no intersection.
     */
    private Double rayAABBIntersect(Vector origin, Vector dir, Vector min, Vector max) {
        double tMin = Double.NEGATIVE_INFINITY;
        double tMax = Double.POSITIVE_INFINITY;

        double[] o = {origin.getX(), origin.getY(), origin.getZ()};
        double[] d = {dir.getX(),    dir.getY(),    dir.getZ()};
        double[] bMin = {min.getX(), min.getY(), min.getZ()};
        double[] bMax = {max.getX(), max.getY(), max.getZ()};

        for (int i = 0; i < 3; i++) {
            if (Math.abs(d[i]) < 1e-8) {
                // Ray is parallel to slab — origin must be inside slab
                if (o[i] < bMin[i] || o[i] > bMax[i]) return null;
            } else {
                double t1 = (bMin[i] - o[i]) / d[i];
                double t2 = (bMax[i] - o[i]) / d[i];

                if (t1 > t2) { double tmp = t1; t1 = t2; t2 = tmp; }

                tMin = Math.max(tMin, t1);
                tMax = Math.min(tMax, t2);

                if (tMin > tMax) return null; // Miss
            }
        }

        // tMin < 0 means the ray origin is inside the box — use tMax (exit point) instead
        return tMin >= 0 ? tMin : (tMax >= 0 ? tMax : null);
    }
}