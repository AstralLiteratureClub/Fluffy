package bet.astral.fluffy.commands.commands;

import bet.astral.cloudplusplus.annotations.Cloud;
import bet.astral.fluffy.FluffyCommandRegisterer;
import bet.astral.fluffy.commands.FluffyCommand;
import bet.astral.fluffy.messenger.Translations;
import bet.astral.fluffy.statistic.Account;
import bet.astral.fluffy.statistic.Statistics;
import bet.astral.messenger.v2.placeholder.Placeholder;
import bet.astral.messenger.v2.placeholder.collection.PlaceholderList;
import bet.astral.messenger.v2.translation.TranslationKey;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.incendo.cloud.bukkit.parser.OfflinePlayerParser;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.paper.PaperCommandManager;
import org.incendo.cloud.permission.Permission;

import java.util.Arrays;

@Cloud
public class StatisticsCommand extends FluffyCommand {
    public StatisticsCommand(FluffyCommandRegisterer registerer, PaperCommandManager.Bootstrapped<CommandSender> commandManager) {
        super(registerer, commandManager);
        command("statistics", Translations.COMMAND_STATISTICS_DESCRIPTION,
                b -> b.permission(Permission.of("fluffy.plugin-hooks"))
                        .senderType(Player.class)
                        .optional(OfflinePlayerParser.offlinePlayerComponent().name("who").description(description(Translations.COMMAND_STATISTICS_WHO_DESCRIPTION)))
                        .handler(this::handle), "stats").register();
    }

    private void handle(@NonNull CommandContext<Player> handler){
        Player sender = handler.sender();
        OfflinePlayer who = (OfflinePlayer) handler.optional("who").orElse(sender);

        PlaceholderList placeholders = new PlaceholderList();
        if (fluffy().getStatisticManager().get(who.getUniqueId()) == null){
            sender.sendMessage(Component.text("Loading...", NamedTextColor.RED));
        }
        fluffy().getStatisticManager().load(who).thenRun(()->{
            TranslationKey translationKey = Translations.COMMAND_STATISTICS_SELF;
            if (!who.getUniqueId().equals(sender.getUniqueId())) {
                translationKey = Translations.COMMAND_STATISTICS_OTHER;
            }

            Account account = fluffy().getStatisticManager().get(who.getUniqueId());
            placeholders.add(Placeholder.of("player", who.getName()));
            placeholders.add(Placeholder.of("kills", account.getStatistic(Statistics.KILLS_GLOBAL)+account.getStatistic(Statistics.KILLS_TOTEM)));
            placeholders.add(Placeholder.of("deaths", account.getStatistic(Statistics.DEATHS_GLOBAL)+account.getStatistic(Statistics.DEATHS_TOTEM)));
            placeholders.add(Placeholder.of("killstreak", account.getStatistic(Statistics.STREAK_KILLS)+account.getStatistic(Statistics.STREAK_KILLS_TOTEM)));
            placeholders.add(Placeholder.of("deathstreak", account.getStatistic(Statistics.STREAK_DEATHS)+account.getStatistic(Statistics.STREAK_DEATHS_TOTEM)));
            placeholders.addAll(Arrays.stream(Statistics.values()).map(statistic -> Placeholder.of(statistic.getName(), account.getStatistic(statistic))).toList());

            messenger.disablePrefixForNextParse().message(sender, translationKey, placeholders);
        });
    }
}
