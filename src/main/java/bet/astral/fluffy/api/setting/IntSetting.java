package bet.astral.fluffy.api.setting;

import bet.astral.messenger.v2.translation.TranslationKey;
import lombok.Getter;

import java.util.UUID;

@Getter
public class IntSetting extends Setting<Integer> {
    private final Integer maxValue;
    private final Integer minValue;
    public IntSetting(UUID uniqueId, TranslationKey name, Integer maxValue, Integer minValue, Integer value) {
        super(uniqueId, name, value);
        this.maxValue = maxValue;
        this.minValue = minValue;
    }

    @Override
    public void parse(Object v) {
        this.setValue((int) v);
    }

    @Override
    public Setting<Integer> clone() {
        return new IntSetting(getUniqueId(), getName(), maxValue, minValue, getValue());
    }

    @Override
    public void settingClicked() {
        int value = getValue();
        value++;
        if (value>maxValue) {
            value = minValue;
        } else if (value<minValue) {
            value = minValue;
        }
        this.setValue(value);
    }

    @Override
    public void setValue(Integer value) {
        if (value>maxValue) {
            value = minValue;
        } else if (value<minValue) {
            value = minValue;
        }
        super.setValue(value);
    }
}
