package bet.astral.fluffy.listeners.cosmetics;

import bet.astral.fluffy.FluffyCombat;
import bet.astral.fluffy.cosmetics.Effect;
import bet.astral.fluffy.cosmetics.TranslationTextEffectData;
import bet.astral.fluffy.cosmetics.manager.EffectManager;
import bet.astral.fluffy.events.damage.death.CombatDeathEvent;
import bet.astral.fluffy.manager.RegionManager;
import bet.astral.fluffy.messenger.Placeholders;
import bet.astral.messenger.v2.placeholder.collection.PlaceholderList;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class DeathEffectListener implements Listener {
    private final FluffyCombat fluffy;
    public DeathEffectListener(FluffyCombat fluffy) {
        this.fluffy = fluffy;
    }

    @EventHandler(ignoreCancelled = true)
    public void onEntityDeath(CombatDeathEvent event) {
        RegionManager regionManager = fluffy.getRegionManager();
        if (!regionManager.canDisplayDeathEffects(event.getVictim().getLocation())){
            return;
        }

        Bukkit.broadcastMessage("!");

        EffectManager effectManager = fluffy.getDeathEffectManager();

        Effect effect = effectManager.getPlayerEffects().get(event.getDamager().getUniqueId());
        if (effect == null) {
            Bukkit.broadcastMessage("Null;");
            Bukkit.broadcastMessage(event.getDamager().getName());
        }
        if (effect != null) {
            Bukkit.broadcastMessage("?");
            PlaceholderList placeholders = new PlaceholderList();
            placeholders.add("total-damage", event.getAttackerDamageDealt());
            placeholders.addAll(Placeholders.combatPlaceholders(event.getVictim(), event.getDamager(), event.getCombatCause(), event.getItemStack()));
            TranslationTextEffectData effectData = new TranslationTextEffectData(placeholders);

            effect.run(event.getDamager(), event.getVictim(), event.getVictim().getLocation().add(0, 1, 0), effectData);
        }
    }
}
