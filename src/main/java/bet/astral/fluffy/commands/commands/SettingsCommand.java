package bet.astral.fluffy.commands.commands;

import bet.astral.aura.api.color.VanillaGlowColor;
import bet.astral.cloudplusplus.annotations.Cloud;
import bet.astral.fluffy.FluffyCommandRegisterer;
import bet.astral.fluffy.api.CombatUser;
import bet.astral.fluffy.api.setting.Setting;
import bet.astral.fluffy.api.setting.SettingPage;
import bet.astral.fluffy.commands.FluffyCommand;
import bet.astral.fluffy.menu.dialogs.MoreDialogs;
import bet.astral.messenger.v2.component.ComponentType;
import bet.astral.messenger.v2.info.MessageInfoBuilder;
import bet.astral.messenger.v2.placeholder.collection.PlaceholderCollection;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.paper.PaperCommandManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

@Cloud
public class SettingsCommand extends FluffyCommand {
    private static final VanillaGlowColor[] COLORS = Stream.concat(
            Stream.of((VanillaGlowColor) null),
            Arrays.stream(VanillaGlowColor.values())
    ).toArray(VanillaGlowColor[]::new);

    public SettingsCommand(FluffyCommandRegisterer registerer, PaperCommandManager.Bootstrapped<CommandSender> commandManager) {
        super(registerer, commandManager);
        /*
        command("settings", Translations.COMMAND_TAG_DESCRIPTION, b -> b.senderType(Player.class)
                .permission("fluffy.settings")
                .handler(this::handle)).register();
        command("settings_2", Translations.COMMAND_TAG_DESCRIPTION, b -> b.senderType(Player.class)
                .permission("fluffy.settings")
                .handler(context->{
                    openPage(context.sender(), Setting.ACTION_BAR_PAGE);
                })).register();

         */
    }

    public void openPage(Player player, SettingPage page) {
        Key key = page.getKey();
        ArrayList<MoreDialogs.MultiActionListComponent> components = new ArrayList<>();
        for (Setting<?> setting : page.getSettings()) {
            components.add(MoreDialogs.multiActionListComponent(
                    key,
                    setting.getName().getKey().replace(".", "/"),
                    v -> {
                        PlaceholderCollection placeholders = setting.getPlaceholders();
                        Component title = messenger.parseComponent(new MessageInfoBuilder(setting.getName()).withPlaceholders(placeholders).build(), ComponentType.CHAT);
                        return title;
                    },
                    action -> {
                        setting.settingClicked();
                    }));
        }
        MoreDialogs.dialog(player, Component.text("Manage action bar"), true, components);
    }

    private VanillaGlowColor getNextColor(VanillaGlowColor current) {
        for (int i = 0; i < COLORS.length; i++) {
            if (Objects.equals(COLORS[i], current)) {
                return COLORS[(i + 1) % COLORS.length];
            }
        }
        return COLORS[1];
    }

    private static Component getColorComponent(VanillaGlowColor colorKey) {
        if (colorKey == null) {
            return Component.text("No Color Selected", NamedTextColor.GRAY);
        }
        String name = colorKey.getName().toLowerCase();
        Matcher matcher = Pattern.compile("(^|_)([a-z])").matcher(name);
        StringBuffer sb = new StringBuffer();

        while (matcher.find()) {
            matcher.appendReplacement(sb, matcher.group(1) + matcher.group(2).toUpperCase());
        }
        matcher.appendTail(sb);

        String finalName = sb.toString().replace("_", " ");
        return Component.text(finalName, colorKey.asNamedTextColor());
    }

    private static NamedTextColor getToggleColor(boolean state) {
        return state ? NamedTextColor.GREEN : NamedTextColor.RED;
    }

    private static Component getToggleMessage(boolean state, String end) {
        Component component = Component.text(state ? "Enabled " : "Disabled ", getToggleColor(state));
        return Component.text(end + ": ", NamedTextColor.WHITE).append(component);
    }

    public void handle(CommandContext<Player> context) {
        Key dialogName = Key.key("fluffy:settings");
        CombatUser user = fluffy().getUserManager().getUser(context.sender());
        MoreDialogs.dialog(context.sender(), Component.text("Manage Glow Settings"), true, List.of(
                MoreDialogs.multiActionListComponent(dialogName, "latest-tagged-toggle", t -> getToggleMessage(user.isShowGlowingLatest(), "Latest Tag Glow"), p -> {
                    user.setShowGlowingLatest(!user.isShowGlowingLatest());
                }),
                MoreDialogs.multiActionListComponent(dialogName, "latest-tagged-glow", t -> getColorComponent(user.getLatestGlowColor().orElse(null)), p -> {
                    VanillaGlowColor color = user.getLatestGlowColor().orElse(null);
                    user.setLatestGlowColor(getNextColor(color));
                }),
                MoreDialogs.multiActionListComponent(dialogName, "combat-tag-toggle", t -> getToggleMessage(user.isShowGlowingTagged(), "Latest Tag Glow"), p -> {
                    user.setShowGlowingTagged(!user.isShowGlowingTagged());
                }),
                MoreDialogs.multiActionListComponent(dialogName, "combat-tag-glow", t -> getColorComponent(user.getTaggedGlowColor().orElse(null)), p -> {
                    VanillaGlowColor color = user.getTaggedGlowColor().orElse(null);
                    user.setTaggedGlowColor(getNextColor(color));
                }),
                MoreDialogs.multiActionListComponent(dialogName, "combat-logged-toggle", t -> getToggleMessage(user.isShowGlowingTagReLogged(), "Combat Logged Glow"), p -> {
                    user.setShowGlowingTagReLogged(!user.isShowGlowingTagReLogged());
                }),
                MoreDialogs.multiActionListComponent(dialogName, "combat-logged-glow", t -> getColorComponent(user.getRejoinedGlowColor().orElse(null)), p -> {
                    VanillaGlowColor color = user.getRejoinedGlowColor().orElse(null);
                    user.setRejoinedGlowColor(getNextColor(color));
                }),
                MoreDialogs.multiActionListComponent(dialogName, "disable-all-glows", t -> Component.text("Disable All Glowing Toggles"), p -> {
                    user.setShowGlowingLatest(false);
                    user.setShowGlowingTagged(false);
                    user.setShowGlowingTagReLogged(false);
                })
        ));
    }
}