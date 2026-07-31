package bet.astral.fluffy.cosmetics;

import bet.astral.fluffy.FluffyCombat;
import bet.astral.fluffy.messenger.FluffyMessenger;
import net.kyori.adventure.text.Component;
import org.bukkit.Color;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Display;
import org.bukkit.entity.Entity;

public class TextEffect extends AbstractTextEffect {
    private final Component component;
    public TextEffect(FluffyCombat plugin, String name, String text, boolean seeThrough, boolean shadowed, Display.Billboard billboard, Color background, Animation animation) {
        super(plugin, name, seeThrough, shadowed, billboard, background, animation);
        this.component = Component.text(text);
    }
    public TextEffect(FluffyCombat plugin, String name, Component component, boolean seeThrough, boolean shadowed, Display.Billboard billboard, Color background, Animation animation) {
        super(plugin, name, seeThrough, shadowed, billboard, background, animation);
        this.component = component;
    }

    @Override
    public Component getDisplay(OfflinePlayer player, Entity entity, EffectData effectData) {
        return component;
    }

    @Override
    public void loadTranslations(FluffyMessenger messenger) {

    }
}
