package bet.astral.fluffy.commands.commands.debug;

import bet.astral.cloudplusplus.annotations.Cloud;
import bet.astral.fluffy.FluffyCommandRegisterer;
import bet.astral.fluffy.commands.FluffyCommand;
import bet.astral.guiman.clickable.Clickable;
import bet.astral.guiman.gui.InventoryGUI;
import bet.astral.guiman.gui.builders.InventoryGUIBuilder;
import bet.astral.guiman.utils.ChestRows;
import io.papermc.paper.datacomponent.DataComponentTypes;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.description.Description;
import org.incendo.cloud.paper.PaperCommandManager;
import org.incendo.cloud.permission.Permission;

@Cloud
public class GUISlotsCommand extends FluffyCommand {
    public GUISlotsCommand(FluffyCommandRegisterer registerer, PaperCommandManager.Bootstrapped<CommandSender> commandManager) {
        super(registerer, commandManager);
        command("flf-gui-slots", Description.EMPTY,
                b -> b.permission(Permission.of("fluffy.debug.slots"))
                        //.optional(EnumParser.enumComponent(InventoryType.class).name("type").defaultValue(DefaultValue.constant(InventoryType.CHEST)))
                        .senderType(Player.class)
                        .handler(this::handle)
                        )
                .register();
        /*
        command("flf-chest-slots", Description.EMPTY,
                b -> b.permission(Permission.of("fluffy.debug.slots"))
                        .senderType(Player.class)
                        .handler(this::handle))
                .register();
         */
    }

    private void handle(@NonNull CommandContext<? extends Player> handler){
        Player player = handler.sender();
        InventoryType type = InventoryType.CHEST;

        InventoryGUIBuilder builder = type == InventoryType.CHEST ?
                InventoryGUI.builder(ChestRows.SIX) :
                InventoryGUI.builder(type);
        builder.title(Component.text("Gui Slots"));
        builder.messenger(messenger);

        int slots = type.getDefaultSize();
        if (type == InventoryType.CHEST) {
            slots = ChestRows.SIX.getSlots();
        }

        for (int i = 0; i < slots; i++) {
            int finalI = i;
            Material material = i > 0
                    ? i % 2 == 0 ? Material.DIAMOND_SWORD : Material.STONE_SWORD
                    : Material.GRAY_STAINED_GLASS_PANE;
            ItemStack itemStack = new ItemStack(material);
            itemStack.setAmount(i > 0 ? i : 1);
            itemStack.setData(DataComponentTypes.MAX_STACK_SIZE, i > 0 ? i : 1);
            itemStack.setData(DataComponentTypes.ITEM_NAME, Component.text("Slot ").color(NamedTextColor.GRAY).append(Component.text(i).color(NamedTextColor.YELLOW)));

            builder.clickable(i,
                    Clickable.general(itemStack, context->player.sendMessage("You've clicked slot: "+ finalI)).hideItemFlags());
        }

        builder.build().open(player);
    }
}
