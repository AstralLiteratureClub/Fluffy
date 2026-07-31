package bet.astral.fluffy.cosmetics;

import bet.astral.fluffy.FluffyCombat;
import bet.astral.fluffy.messenger.FluffyMessenger;
import bet.astral.messenger.v2.translation.TranslationKey;
import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class ConfigurableEffect extends Effect implements NamedEffect {
    private final List<Effect> effects;
    private final String name;
    private final TranslationKey formattedName;
    @Getter
    private final TranslationKey description;
    @Getter
    private final ItemStack displayItem;

    public ConfigurableEffect(@NotNull List<Effect> effects, FluffyCombat fluffyCombat, String name, TranslationKey formattedName, TranslationKey description, ItemStack displayItem) {
        super(fluffyCombat, name);
        this.effects = effects;
        this.name = name;
        this.formattedName = formattedName;
        this.description = description;
        this.displayItem = displayItem;
    }

    @Override
    public void run(OfflinePlayer player, Entity entity, Location location, EffectData effectData) {
        for (Effect effect : effects) {
            effect.run(player, entity, location, effectData);
        }
    }

    @Override
    public void loadTranslations(FluffyMessenger messenger) {
        List<TranslationKey> keys = new ArrayList<>();
        keys.add(formattedName);
        keys.add(description);
        messenger.loadTranslations(keys);
        for (Effect effect : effects) {
            effect.loadTranslations(messenger);
        }
    }

    @Override
    public TranslationKey getFormattedName() {
        return formattedName;
    }

}
