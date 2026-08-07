package bet.astral.fluffy.menu.dialogs;

import io.papermc.paper.dialog.Dialog;
import io.papermc.paper.registry.data.dialog.ActionButton;
import io.papermc.paper.registry.data.dialog.DialogBase;
import io.papermc.paper.registry.data.dialog.action.DialogAction;
import io.papermc.paper.registry.data.dialog.body.DialogBody;
import io.papermc.paper.registry.data.dialog.type.DialogType;
import lombok.Getter;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickCallback;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

import java.util.List;
import java.util.Random;
import java.util.function.Consumer;
import java.util.function.Function;

public class MoreDialogs implements Listener {
    private static final Random random = new Random();

    public static void dialog(Player player, Component dialogName, boolean deleteAfterClose, List<MultiActionListComponent> actions) {
        List<ActionButton> actionButtons = actions.stream()
                .map(comp -> ActionButton.builder(comp.getTitle().apply(null))
                        .action(DialogAction.customClick((view, audience) -> {
                            if (audience instanceof Player p) {
                                comp.getAction().accept(player);
                                dialog(player, dialogName, deleteAfterClose, actions);
                            }
                        }, ClickCallback.Options.builder().uses(1).build()))
                        .build())
                .toList();

        Dialog dialog = Dialog.create(builder -> builder.empty()
                .base(DialogBase.builder(dialogName)
                        .canCloseWithEscape(true)
                        .body(List.of(
                                DialogBody.plainMessage(dialogName)
                        ))
                        .build()
                )
                .type(
                        DialogType.multiAction(actionButtons)
                                .exitAction(
                                        ActionButton.builder(Component.text("Exit / Close"))
                                                .action(DialogAction.customClick((view, audience) -> {
                                                    if (audience instanceof Player p) {
                                                        player.sendMessage("You closed the dialog using the custom inline action.");
                                                        if (deleteAfterClose) {
                                                            // Handle cleanup logic if needed
                                                        }
                                                    }
                                                }, ClickCallback.Options.builder().uses(1).build()))
                                                .build()
                                )
                                .build()
                ));
        player.showDialog(dialog);
    }

    public static MultiActionListComponent multiActionListComponent(Key dialogName, String actionName, Function<Void, Component> title, Consumer<Player> action) {
        return new MultiActionListComponent(Key.key(dialogName.namespace(), dialogName.value()+"/"+actionName), title, action);
    }

    @Getter
    public static class MultiActionListComponent {
        private final Key key;
        private final Function<Void, Component> title;
        private final Consumer<Player> action;

        private MultiActionListComponent(Key key, Function<Void, Component> title, Consumer<Player> action) {
            this.key = key;
            this.title = title;
            this.action = action;
        }
    }

}
