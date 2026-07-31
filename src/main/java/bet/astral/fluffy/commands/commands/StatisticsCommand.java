package bet.astral.fluffy.commands.commands;

import bet.astral.cloudplusplus.annotations.Cloud;
import bet.astral.fluffy.FluffyCommandRegisterer;
import bet.astral.fluffy.commands.FluffyCommand;
import bet.astral.fluffy.menu.ChatMenu;
import bet.astral.fluffy.menu.ChatMenuBuilder;
import bet.astral.fluffy.menu.MenuComponent;
import bet.astral.fluffy.messenger.StatisticsLanguageSource;
import bet.astral.fluffy.messenger.Translations;
import bet.astral.fluffy.statistic.Account;
import bet.astral.fluffy.statistic.Statistics;
import bet.astral.messenger.v2.AbstractMessenger;
import bet.astral.messenger.v2.placeholder.Placeholder;
import bet.astral.messenger.v2.placeholder.collection.PlaceholderCollection;
import bet.astral.messenger.v2.placeholder.collection.PlaceholderList;
import bet.astral.messenger.v2.source.source.LanguageSource;
import bet.astral.messenger.v2.translation.TranslationKey;
import bet.astral.messenger.v2.translation.TranslationKeyRegistry;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.stream.JsonReader;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.incendo.cloud.bukkit.parser.OfflinePlayerParser;
import org.incendo.cloud.component.CommandComponent;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.paper.PaperCommandManager;
import org.incendo.cloud.parser.standard.IntegerParser;
import org.incendo.cloud.permission.Permission;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.function.Function;

@Cloud
public class StatisticsCommand extends FluffyCommand {
    private final ChatMenu menuSelf;
    private final ChatMenu menuOther;
    public StatisticsCommand(FluffyCommandRegisterer registerer, PaperCommandManager.Bootstrapped<CommandSender> commandManager) {
        super(registerer, commandManager);

        menuSelf = loadMessagesAndCreateMenu(registerer, false);
        menuOther = loadMessagesAndCreateMenu(registerer, true);

        command("statistics", Translations.COMMAND_STATISTICS_DESCRIPTION,
                b -> b.permission(Permission.of("fluffy.plugin-hooks"))
                        .senderType(Player.class)
                        .optional(OfflinePlayerParser.offlinePlayerComponent().name("who").description(description(Translations.COMMAND_STATISTICS_WHO_DESCRIPTION)))
                        .optional(CommandComponent.<CommandSender, Integer>builder().parser(IntegerParser.integerParser(1, menuSelf.valueLength)).name("page"))
                        .handler(this::handle), "stats").register();

    }
    public ChatMenu loadMessagesAndCreateMenu(@NotNull FluffyCommandRegisterer registerer, boolean otherPlayer) {
        File file = new File(
                registerer.getBootstrapContext().getDataDirectory().toFile(),
                "statistics.json");

        try {
            JsonReader reader = new JsonReader(new FileReader(file));
            JsonObject obj = new GsonBuilder().disableHtmlEscaping()
                    .create().fromJson(reader, JsonObject.class);

            JsonElement element = obj.get("pages-"+(otherPlayer ? "other" : "self"));
            List<Map.Entry<TranslationKey, Component>> pages = new LinkedList<>();

            if (element == null || element.isJsonNull()) {
                return new ChatMenu(new ArrayList<>(), 1, messenger, true);
            }

            if (element.isJsonArray()) {
                int i = 0;
                for (JsonElement jsonElement : element.getAsJsonArray()) {
                    String value = jsonElement.getAsString();
                    pages.add(Map.entry(TranslationKey.of("fluffy.statistics.page."+value+(otherPlayer ? "other" : "self")), MiniMessage.miniMessage().deserialize(value)));
                    i++;
                }
            } else if (element.isJsonPrimitive()){
                pages.add(Map.entry(TranslationKey.of("fluffy.statistics.page.0"), MiniMessage.miniMessage().deserialize(element.getAsString())));
            }

            TranslationKeyRegistry registry = TranslationKeyRegistry.create();
            ChatMenuBuilder builder = new ChatMenuBuilder();

            Map<TranslationKey, Component> translations = new HashMap<>();
            List<TranslationKey> keys = new LinkedList<>();
            ArrayList<MenuComponent> components = new ArrayList<>();
            for (Map.Entry<TranslationKey, Component> entry : pages) {
                MenuComponent component = new MenuComponent(entry.getKey(), (player)->true);
                keys.add(entry.getKey());
                components.add(component);

                translations.put(entry.getKey(), entry.getValue());
                registry.register(entry.getKey());
            }

            builder
                    .setComponents(components)
                    .setMessenger(registerer.getMessenger())
                    .setValuesPerPage(1)
                    .setDisablePrefixFromMessage(true)
                    ;



            AbstractMessenger messenger = (AbstractMessenger) registerer.getMessenger();
            messenger.loadTranslations(keys);


            LanguageSource source = new StatisticsLanguageSource(messenger, registry, Locale.US, translations);
            messenger.getLanguageTable(Locale.US).addAdditionalLanguageSource(source);
            messenger.loadTranslations(keys);

            reader.close();

            return builder.build();
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void handle(@NonNull CommandContext<Player> handler){
        Player sender = handler.sender();
        OfflinePlayer who = (OfflinePlayer) handler.optional("who").orElse(sender);
        int page = (int) handler.optional("page").orElse(1);

        PlaceholderList placeholders = new PlaceholderList();
        if (fluffy().getStatisticManager().get(who.getUniqueId()) == null){
            sender.sendMessage(Component.text("Loading...", NamedTextColor.RED));
        }
        fluffy().getStatisticManager().load(who).thenRun(()->{
            ChatMenu menu = menuSelf;
            TranslationKey translationKey = Translations.COMMAND_STATISTICS_SELF;
            if (!who.getUniqueId().equals(sender.getUniqueId())) {
                menu = menuOther;
            }

            placeholders.add(Placeholder.of("player", who.getName()));

            Function<Player, PlaceholderCollection> generator = (player) -> {
                Account account = fluffy().getStatisticManager().get(who.getUniqueId());
                placeholders.add(Placeholder.of("kills", account.getStatistic(Statistics.KILLS_GLOBAL)+account.getStatistic(Statistics.KILLS_TOTEM)));
                placeholders.add(Placeholder.of("deaths", account.getStatistic(Statistics.DEATHS_GLOBAL)+account.getStatistic(Statistics.DEATHS_TOTEM)));
                placeholders.add(Placeholder.of("killstreak", account.getStatistic(Statistics.STREAK_KILLS)+account.getStatistic(Statistics.STREAK_KILLS_TOTEM)));
                placeholders.add(Placeholder.of("deathstreak", account.getStatistic(Statistics.STREAK_DEATHS)+account.getStatistic(Statistics.STREAK_DEATHS_TOTEM)));
                placeholders.addAll(Arrays.stream(Statistics.values()).map(statistic -> Placeholder.of(statistic.getName(), account.getStatistic(statistic))).toList());
                return placeholders;
            };

            menu.openView(sender, page, generator);
        });
    }
}
