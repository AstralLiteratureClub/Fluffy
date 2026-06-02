package bet.astral.chat.menu;

import bet.astral.messenger.v2.translation.TranslationKey;
import lombok.Getter;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.function.Predicate;

/**
 * Chat text menu component value.
 */
@Getter
public class MenuComponent implements TranslationKey {
    /**
     * Text value of this component
     */
    private final TranslationKey value;
    /**
     * The predicate to display this to the command sender (console/player)
     */
    private final Predicate<CommandSender> shouldDisplay;

    /**
     * Creates a new menu component with no predicate
     * @param value text translation
     * @return new menu component
     */
    public static MenuComponent value(TranslationKey value) {
        return new MenuComponent(value, null);
    }

    /**
     * Creates a new menu component
     * @param value text translation to display
     * @param shouldDisplay predicate to check if sender has permission to see it.
     */
    public MenuComponent(TranslationKey value, Predicate<CommandSender> shouldDisplay) {
        this.value = value;
        this.shouldDisplay = shouldDisplay;
    }

    @Override
    public @NotNull String getKey() {
        return this.value.getKey();
    }
}
