package bet.astral.fluffy.api.setting;

import bet.astral.fluffy.api.actionbar.Designable;
import bet.astral.messenger.v2.placeholder.collection.PlaceholderCollection;
import bet.astral.messenger.v2.placeholder.collection.PlaceholderList;
import bet.astral.messenger.v2.translation.TranslationKey;

import java.util.Arrays;
import java.util.UUID;

public class EnumSetting<E extends Enum<?>> extends Setting<E> {
    public EnumSetting(UUID uniqueId, TranslationKey name, E value) {
        super(uniqueId, name, value);
    }

    @Override
    public void parse(Object v) {
        String value = v.toString();
        Enum<?> match = Arrays.stream(this.getValue().getClass().getEnumConstants())
                .filter(e -> e.name().equals(value))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No enum constant named: " + value));
        this.setValue((E) match);
    }

    @Override
    public Setting<E> clone() {
        return new EnumSetting<>(getUniqueId(), getName(), getValue());
    }

    @Override
    public void settingClicked() {
        E[] constants = (E[]) getValue().getClass().getEnumConstants();
        int index = getValue().ordinal();
        index++;
        if (index >= constants.length) {
            index = 0;
        }
        setValue(constants[index]);
    }

    @Override
    public PlaceholderCollection getPlaceholders() {
        if (getValue() instanceof Designable designable) {
            PlaceholderList placeholders = new PlaceholderList();
            placeholders.add("design", designable.design());
            return placeholders;
        }
        return super.getPlaceholders();
    }
}