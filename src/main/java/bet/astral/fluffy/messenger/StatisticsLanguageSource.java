package bet.astral.fluffy.messenger;

import bet.astral.messenger.v2.Messenger;
import bet.astral.messenger.v2.component.ComponentBase;
import bet.astral.messenger.v2.component.ComponentBaseBuilder;
import bet.astral.messenger.v2.component.ComponentPart;
import bet.astral.messenger.v2.component.ComponentType;
import bet.astral.messenger.v2.source.source.LanguageSource;
import bet.astral.messenger.v2.translation.TranslationKey;
import bet.astral.messenger.v2.translation.TranslationKeyRegistry;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class StatisticsLanguageSource implements LanguageSource {
    private final Messenger messenger;
    private final TranslationKeyRegistry registry;
    private final Locale locale;
    @Getter
    private final Map<TranslationKey, Component> messages;

    public StatisticsLanguageSource(Messenger messenger, TranslationKeyRegistry registry, Locale locale, Map<TranslationKey, Component> translations) {
        this.messenger = messenger;
        this.registry = registry;
        this.locale = locale;
        this.messages = translations;
    }

    @Override
    public @NotNull Messenger getMessenger() {
        return messenger;
    }

    @Override
    public @NotNull TranslationKeyRegistry getTranslationKeyRegistry() {
        return registry;
    }

    @Override
    public @NotNull Locale getLocale() {
        return locale;
    }

    @Override
    public @NotNull CompletableFuture<@Nullable ComponentBase> loadComponent(@NotNull TranslationKey translationKey) {
        return CompletableFuture.supplyAsync(() -> {
            if (messages.containsKey(translationKey)) {
                ComponentBaseBuilder builder = new ComponentBaseBuilder(translationKey);
                builder.setComponentPart(ComponentType.CHAT, ComponentPart.of(messages.get(translationKey)));
                return builder.build();
            }
            return null;
        });
    }

    @Override
    public @NotNull CompletableFuture<@NotNull Map<@NotNull TranslationKey, @Nullable ComponentBase>> loadAllComponents(@NotNull TranslationKey... translationKeys) {
        return CompletableFuture.supplyAsync(()->{
            Map<TranslationKey, ComponentBase> components = new HashMap<>();
            for (TranslationKey translationKey : translationKeys) {
                if (messages.containsKey(translationKey)) {
                    ComponentBaseBuilder builder = new ComponentBaseBuilder(translationKey);
                    builder.setComponentPart(ComponentType.CHAT, ComponentPart.of(messages.get(translationKey)));
                    components.put(translationKey, builder.build());
                }
            }
            return components;
        });
    }

    @Override
    public @NotNull CompletableFuture<@NotNull Map<@NotNull TranslationKey, @Nullable ComponentBase>> loadAllComponents(@NotNull Collection<? extends TranslationKey> translationKeys) {
        return loadAllComponents(translationKeys.stream().toArray(TranslationKey[]::new));
    }
}
