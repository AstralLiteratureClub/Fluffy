package bet.astral.fluffy.cosmetics;

import bet.astral.messenger.v2.placeholder.collection.PlaceholderCollection;
import lombok.Getter;

@Getter
public class TranslationTextEffectData extends EffectData {
    public final PlaceholderCollection placeholderCollection;

    public TranslationTextEffectData(PlaceholderCollection placeholderCollection) {
        this.placeholderCollection = placeholderCollection;
    }
}
