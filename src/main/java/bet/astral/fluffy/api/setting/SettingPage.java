package bet.astral.fluffy.api.setting;

import bet.astral.messenger.v2.translation.TranslationKey;
import lombok.Getter;
import net.kyori.adventure.key.Key;

import java.util.ArrayList;
import java.util.Collections;

@Getter
public class SettingPage {
    private final Key key;
    private final TranslationKey title;
    private final ArrayList<Setting<?>> settings;

    public SettingPage(Key key, TranslationKey title, ArrayList<Setting<?>> settings) {
        this.key = key;
        this.title = title;
        this.settings = settings;
    }
    public SettingPage(Key key, TranslationKey title, Setting<?>... settings) {
        this.key = key;
        this.title = title;
        this.settings = new ArrayList<>();
        Collections.addAll(this.settings, settings);
    }
}
