package bet.astral.fluffy.cosmetics;

import bet.astral.messenger.v2.translation.TranslationKey;

public interface NamedEffect {
    String getName();
    TranslationKey getFormattedName();
}
