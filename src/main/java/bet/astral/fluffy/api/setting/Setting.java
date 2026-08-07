package bet.astral.fluffy.api.setting;

import bet.astral.fluffy.api.actionbar.ActionBarMode;
import bet.astral.fluffy.api.actionbar.HealthDesign;
import bet.astral.fluffy.api.actionbar.TimerDesign;
import bet.astral.fluffy.messenger.Translations;
import bet.astral.messenger.v2.placeholder.collection.PlaceholderCollection;
import bet.astral.messenger.v2.placeholder.collection.PlaceholderList;
import bet.astral.messenger.v2.translation.TranslationKey;
import lombok.Getter;
import lombok.Setter;
import net.kyori.adventure.key.Key;

import java.util.UUID;

@Setter
@Getter
public abstract class Setting<T> implements Cloneable{
    public static BooleanSetting ACTION_BAR_ENABLED = new BooleanSetting(UUID.fromString("f9c971d3-003c-4078-99f7-288bb2a30176"), Translations.SETTING_ACTION_BAR_ENABLED, true);
    public static EnumSetting<ActionBarMode> ACTION_BAR_MODE = new EnumSetting<>(UUID.fromString("b2192556-33ce-47de-9d15-91b77ae782aa"), Translations.SETTING_ACTION_BAR_TYPE, ActionBarMode.TIMER);
    public static EnumSetting<HealthDesign> ACTION_BAR_HEALTH_DESIGN = new EnumSetting<>(UUID.fromString("b6ec3cf6-e169-408a-82eb-807dd4591369"), Translations.SETTING_ACTION_BAR_HEALTH_MODE, HealthDesign.PRECISE);
    public static EnumSetting<TimerDesign> ACTION_BAR_TIMER_DESIGN = new EnumSetting<>(UUID.fromString("ddf9990e-218e-4a96-ba8b-f507bfee53a1"), Translations.SETTING_ACTION_BAR_TIMER_MODE, TimerDesign.MINUTES_AND_SECONDS);
    public static SettingPage ACTION_BAR_PAGE = new SettingPage(Key.key("fluffy:action_bar"), Translations.SETTING_ACTION_BAR_PAGE, ACTION_BAR_ENABLED, ACTION_BAR_MODE, ACTION_BAR_HEALTH_DESIGN, ACTION_BAR_TIMER_DESIGN);

    private final UUID uniqueId;
    private final TranslationKey name;
    private T value;

    public Setting(UUID uniqueId, TranslationKey name, T value) {
        this.uniqueId = uniqueId;
        this.name = name;
        this.value = value;
    }

    public abstract void parse(Object v);

    public abstract Setting<T> clone();

    public abstract void settingClicked();

    public PlaceholderCollection getPlaceholders() {
        return new PlaceholderList();
    }
}
