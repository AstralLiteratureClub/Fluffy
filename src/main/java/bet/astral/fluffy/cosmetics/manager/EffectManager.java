package bet.astral.fluffy.cosmetics.manager;

import bet.astral.fluffy.FluffyCombat;
import bet.astral.fluffy.cosmetics.ConfigurableEffect;
import bet.astral.fluffy.cosmetics.Effect;
import bet.astral.fluffy.cosmetics.menu.EffectMenu;
import bet.astral.messenger.v2.translation.TranslationKey;
import lombok.Getter;

import java.util.*;

@Getter
public class EffectManager {
    private final FluffyCombat fluffy;
    private final Set<ConfigurableEffect> registeredEffects = new HashSet<>();
    private final Map<UUID, Effect> playerEffects = new HashMap<>();
    private final EffectMenu menu;
    private final String fileName;

    public EffectManager(FluffyCombat fluffy, String fileName, TranslationKey effectChosenTranslation, TranslationKey menuTitleTranslation, TranslationKey menuCloseTranslation, TranslationKey menuBackTranslation) {
        this.fluffy = fluffy;
        this.menu = new EffectMenu(fluffy, this, effectChosenTranslation, menuTitleTranslation, menuCloseTranslation, menuBackTranslation);
        this.fileName = fileName;
    }

    public void registerEffect(ConfigurableEffect effect) {
        registeredEffects.add(effect);
    }

    public void clearRegistry() {
        registeredEffects.clear();
    }

    public void loadTranslations() {
        getRegisteredEffects().forEach(
                effect -> {
                    effect.loadTranslations(getFluffy().getMessenger());
                }
        );
    }
}
