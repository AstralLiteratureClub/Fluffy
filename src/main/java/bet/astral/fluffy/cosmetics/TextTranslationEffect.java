package bet.astral.fluffy.cosmetics;

import bet.astral.fluffy.FluffyCombat;
import bet.astral.fluffy.messenger.FluffyMessenger;
import bet.astral.messenger.v2.component.ComponentType;
import bet.astral.messenger.v2.info.MessageInfoBuilder;
import bet.astral.messenger.v2.placeholder.collection.PlaceholderCollection;
import bet.astral.messenger.v2.placeholder.collection.PlaceholderList;
import bet.astral.messenger.v2.translation.TranslationKey;
import net.kyori.adventure.text.Component;
import org.bukkit.Color;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Display;
import org.bukkit.entity.Entity;

import java.util.List;

public class TextTranslationEffect extends AbstractTextEffect {
    private final TranslationKey key;
    public TextTranslationEffect(FluffyCombat plugin, String name, TranslationKey key, boolean seeThrough, boolean shadowed, Display.Billboard billboard, Color background, Animation animation) {
        super(plugin, name, seeThrough, shadowed, billboard, background, animation);
        this.key = key;
    }

    @Override
    public Component getDisplay(OfflinePlayer player, Entity entity, EffectData effectData) {
        PlaceholderCollection placeholderCollection = effectData instanceof TranslationTextEffectData data ? data.getPlaceholderCollection() : new PlaceholderList();
        return getPlugin().getMessenger().disablePrefixForNextParse().parseComponent(new MessageInfoBuilder(key).withReceiver(player).withPlaceholders(placeholderCollection).build(), ComponentType.CHAT);
    }

    @Override
    public void loadTranslations(FluffyMessenger messenger) {
        messenger.loadTranslations(List.of(key));
    }
}
