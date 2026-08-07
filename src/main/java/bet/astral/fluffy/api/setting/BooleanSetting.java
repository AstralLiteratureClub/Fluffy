package bet.astral.fluffy.api.setting;

import bet.astral.messenger.v2.translation.TranslationKey;

import java.util.UUID;

public class BooleanSetting extends Setting<Boolean> {
    public BooleanSetting(UUID uniqueId, TranslationKey name, Boolean value) {
        super(uniqueId, name, value);
    }

    @Override
    public void parse(Object v) {
        this.setValue((boolean) v);
    }

    @Override
    public Setting<Boolean> clone() {
        return new BooleanSetting(getUniqueId(), getName(), getValue());
    }

    @Override
    public void settingClicked() {
        setValue(!getValue());
    }
}
