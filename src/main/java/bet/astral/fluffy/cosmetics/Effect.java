package bet.astral.fluffy.cosmetics;

import bet.astral.fluffy.FluffyCombat;
import bet.astral.fluffy.messenger.FluffyMessenger;
import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Entity;

public abstract class Effect {
    @Getter
    private final FluffyCombat plugin;
    @Getter
    private final String name;

    protected Effect(FluffyCombat plugin, String name) {
        this.plugin = plugin;
        this.name = name;
    }

    /**
     * Runs the effect at the given location
     * @param player player who owns it
     * @param entity entity that was the cause for it
     * @param location location to display/execute it at
     * @param effectData extra effect data
     */
    public abstract void run(OfflinePlayer player, Entity entity, Location location, EffectData effectData);

    public abstract void loadTranslations(FluffyMessenger messenger);
}
