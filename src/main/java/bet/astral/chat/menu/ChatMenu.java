package bet.astral.chat.menu;

import bet.astral.messenger.v2.Messenger;
import bet.astral.messenger.v2.placeholder.collection.PlaceholderCollection;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.function.Function;

public class ChatMenu {
    private final ArrayList<MenuComponent> components;
    private final int valuesPerPage;
    private final Messenger messenger;
    private final boolean disablePrefixFromMessage;
    public final int valueLength;

    public ChatMenu(ArrayList<MenuComponent> components, int valuesPerPage, Messenger messenger, boolean disablePrefixFromMessage) {
        this.components = components;
        this.valuesPerPage = valuesPerPage;
        this.messenger = messenger;
        this.disablePrefixFromMessage = disablePrefixFromMessage;
        valueLength = components.size();
    }

    public void openView(@NotNull CommandSender sender, int page) {
        openView(sender, page, null);
    }

    public void openView(CommandSender sender, int page, Function<Player, PlaceholderCollection> placeholderGenerator) {
        ArrayList<MenuComponent> components = new ArrayList<>(
                this.components.stream()
                        .filter(
                                component ->
                                        component.getShouldDisplay().test(sender)).toList());

        ArrayList<MenuComponent> toDisplay = new ArrayList<>(
                components.subList(valuesPerPage * page, (valuesPerPage * page) + valuesPerPage));

        PlaceholderCollection placeholders = placeholderGenerator.apply(null);

        toDisplay.forEach(component -> {
            if (disablePrefixFromMessage) {
                messenger.disablePrefixForNextParse();
            }
            messenger.message(sender, component.getValue(), placeholders);
        });
    }
}
