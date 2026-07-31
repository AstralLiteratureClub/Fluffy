package bet.astral.fluffy.cosmetics.menu;

import bet.astral.fluffy.FluffyCombat;
import bet.astral.fluffy.cosmetics.ConfigurableEffect;
import bet.astral.fluffy.cosmetics.manager.EffectManager;
import bet.astral.guiman.background.Background;
import bet.astral.guiman.clickable.Clickable;
import bet.astral.guiman.gui.InventoryGUI;
import bet.astral.guiman.gui.builders.InventoryGUIBuilder;
import bet.astral.guiman.utils.ChestRows;
import bet.astral.messenger.v2.component.ComponentType;
import bet.astral.messenger.v2.info.MessageInfoBuilder;
import bet.astral.messenger.v2.placeholder.collection.PlaceholderList;
import bet.astral.messenger.v2.translation.TranslationKey;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

public class EffectMenu {
    private final FluffyCombat fluffy;
    private final EffectManager effectManager;
    private final TranslationKey effectChosenTranslation;
    private final TranslationKey menuTitleTranslation;
    private final TranslationKey menuCloseTranslation;
    private final TranslationKey menuBackTranslation;


    public EffectMenu(@NotNull FluffyCombat combat, EffectManager effectManager, TranslationKey effectChosenTranslation, TranslationKey menuTitleTranslation, TranslationKey menuCloseTranslation, TranslationKey menuBackTranslation) {
        this.fluffy = combat;
        this.effectManager = effectManager;
        this.effectChosenTranslation = effectChosenTranslation;
        this.menuTitleTranslation = menuTitleTranslation;
        this.menuCloseTranslation = menuCloseTranslation;
        this.menuBackTranslation = menuBackTranslation;
    }

    public void open(@NotNull Player player) {
        InventoryGUIBuilder builder = InventoryGUI.builder(ChestRows.SIX)
                .background(Background.border(ChestRows.SIX, Clickable.noTooltip(Material.BLACK_STAINED_GLASS_PANE), Clickable.noTooltip(Material.GRAY_STAINED_GLASS_PANE)))
                .messenger(fluffy.getMessenger())
                .title(menuTitleTranslation)
                .clickable(49, Clickable.general(
                        ItemStack.of(Material.BARRIER),
                        context->context.getWho().getScheduler()
                                .run(fluffy, task->context.getWho().closeInventory(), null))
                        .title(menuCloseTranslation))
                ;

        int slot = 10;
        int[] skip = {17, 18, 26, 27, 35, 36};
        int newPage = 44;
        for (ConfigurableEffect effect : this.effectManager.getRegisteredEffects()) {
            if (slot >= newPage) {
                return;
            }
            if (effect == null){
                continue;
            }
            builder.clickable(slot, Clickable.general(effect.getDisplayItem(), context->{
                effectManager.getPlayerEffects().put(player.getUniqueId(), effect);
                player.sendMessage(effect.getName());
                PlaceholderList placeholders = new PlaceholderList();
                Component name = fluffy.getMessenger().disablePrefixForNextParse().parseComponent(new MessageInfoBuilder(effect.getFormattedName()).build(), ComponentType.CHAT);
                Component description = fluffy.getMessenger().disablePrefixForNextParse().parseComponent(new MessageInfoBuilder(effect.getFormattedName()).build(), ComponentType.CHAT);
                placeholders.add("effect", name != null ? name : Component.text(effect.getFormattedName().getKey()));
                placeholders.add("description", description != null ? description : Component.text(effect.getDescription().getKey()));
                fluffy.getMessenger().message(player, effectChosenTranslation, placeholders);

                        player.sendMessage(effectManager.getPlayerEffects().get(player.getUniqueId()).getName());
            })
                    .title(effect.getFormattedName())
                    .description(effect.getDescription())
                    .hideItemFlags());
            slot++;
            int finalSlot = slot;
            while (Arrays.stream(skip).anyMatch(value -> finalSlot == value)){
                slot++;
            }
        }

        builder.build().open(player);
    }
}
