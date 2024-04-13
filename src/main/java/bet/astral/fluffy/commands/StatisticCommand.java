package bet.astral.fluffy.commands;

import bet.astral.fluffy.FluffyCombat;
import bet.astral.fluffy.messenger.MessageKey;
import bet.astral.fluffy.statistic.Account;
import bet.astral.messenger.placeholder.PlaceholderList;
import bet.astral.messenger.utils.PlaceholderUtils;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.incendo.cloud.bukkit.parser.OfflinePlayerParser;
import org.incendo.cloud.description.Description;
import org.incendo.cloud.paper.PaperCommandManager;

public class StatisticCommand {
	private final FluffyCombat fluffy;

	public StatisticCommand(FluffyCombat fluffy, PaperCommandManager<CommandSender> manager) {
		this.fluffy = fluffy;
		manager.command(
				manager.commandBuilder("statistics",
						"stats")
						.commandDescription(Description.of("Shows the statistics of a player."))
						.optional(
								OfflinePlayerParser.offlinePlayerComponent()
										.name("who")
										.description(Description.of("Player to see statistics of"))
						)
						.handler((context)->{
							CommandSender sender = context.sender();
							OfflinePlayer player = (OfflinePlayer) context.optional("who").orElse(sender instanceof Player p ? p : null);
							if (player == null){
								fluffy.getMessageManager()
										.message(sender, MessageKey.STATS_CONSOLE);
								return;
							}
							Account account = fluffy.getStatisticManager().get(player.getUniqueId());
							if (account==null){
								fluffy.getStatisticManager()
										.load(player)
										.thenAcceptAsync((VOID)->{
											Account acc = fluffy.getStatisticManager().get(player.getUniqueId());
											if (player instanceof Player p && p.equals(sender)){
												info(MessageKey.STATS_SELF, sender, acc, player);
											} else {
												info(MessageKey.STATS_OTHER, sender, acc, player);
											}
										});
								return;
							}
							if (player instanceof Player p && p.equals(sender)){
								info(MessageKey.STATS_SELF, sender, account, player);
							} else {
								info(MessageKey.STATS_OTHER, sender, account, player);
							}						})
		);
	}

	private static void info(String key, CommandSender sender, Account account, OfflinePlayer player){
		PlaceholderList placeholders = new PlaceholderList();
		placeholders.addAll(account.asPlaceholder("statistic"));
		placeholders.addAll(account.asPlaceholder("statistics"));
		placeholders.addAll(PlaceholderUtils.createPlaceholders("player", player));
		FluffyCombat.getPlugin(FluffyCombat.class).messenger()
				.message(sender,
						key,
						placeholders
						);
	}
}
